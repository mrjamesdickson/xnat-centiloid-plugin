#!/usr/bin/env python3
"""
Example script showing how to integrate the centiloid container with XNAT
using the Centiloid plugin REST API.

This script demonstrates:
1. Running the centiloid container
2. Parsing the results 
3. Creating an XNAT assessment via REST API
4. Uploading output files to XNAT
"""

import json
import os
import requests
import subprocess
import sys
from pathlib import Path
from typing import Dict, Any

class XNATCentiloidIntegration:
    
    def __init__(self, xnat_host: str, username: str, password: str):
        self.xnat_host = xnat_host.rstrip('/')
        self.auth = (username, password)
        self.session = requests.Session()
        self.session.auth = self.auth
        
    def run_centiloid_container(self, 
                               dicom_dir: str,
                               template: str, 
                               target_mask: str,
                               ref_mask: str,
                               tracer: str,
                               mode: str,
                               output_dir: str,
                               reg_mode: str = "rigid") -> Dict[str, Any]:
        """
        Run the centiloid container and return results
        """
        
        # Ensure output directory exists
        Path(output_dir).mkdir(parents=True, exist_ok=True)
        
        # Build container command
        cmd = [
            "docker", "run", "--rm",
            "-v", f"{os.path.abspath(dicom_dir)}:/data/dicom",
            "-v", f"{os.path.abspath(template)}:/data/template.nii.gz",
            "-v", f"{os.path.abspath(target_mask)}:/data/target_mask.nii.gz", 
            "-v", f"{os.path.abspath(ref_mask)}:/data/ref_mask.nii.gz",
            "-v", f"{os.path.abspath(output_dir)}:/data/out",
            "xnatworks/xnat_centiloid_container",
            "--dicom-dir", "/data/dicom",
            "--template", "/data/template.nii.gz",
            "--target-mask", "/data/target_mask.nii.gz",
            "--ref-mask", "/data/ref_mask.nii.gz",
            "--tracer", tracer,
            "--mode", mode,
            "--reg-mode", reg_mode,
            "--out-dir", "/data/out"
        ]
        
        print(f"Running container: {' '.join(cmd)}")
        
        # Execute container
        try:
            result = subprocess.run(cmd, capture_output=True, text=True, check=True)
            print("Container completed successfully")
            print(f"STDOUT: {result.stdout}")
            if result.stderr:
                print(f"STDERR: {result.stderr}")
                
        except subprocess.CalledProcessError as e:
            print(f"Container failed with return code {e.returncode}")
            print(f"STDOUT: {e.stdout}")
            print(f"STDERR: {e.stderr}")
            raise
            
        # Load results
        results_file = Path(output_dir) / "centiloid.json"
        if not results_file.exists():
            raise FileNotFoundError(f"Results file not found: {results_file}")
            
        with open(results_file, 'r') as f:
            results = json.load(f)
            
        return results
        
    def create_xnat_assessment(self, 
                              project_id: str,
                              session_id: str, 
                              results: Dict[str, Any],
                              output_dir: str) -> str:
        """
        Create XNAT assessment from container results
        """
        
        # Prepare assessment data
        assessment_data = {
            "processing_status": "completed",
            "container_version": "1.0.0"  # Update with actual version
        }
        
        # Add input parameters
        if "inputs" in results:
            inputs = results["inputs"]
            assessment_data.update({
                "dicom_dir": inputs.get("dicom_dir"),
                "pet_nifti": inputs.get("pet_nifti"),
                "template": inputs.get("template"),
                "target_mask": inputs.get("target_mask"),
                "ref_mask": inputs.get("ref_mask"),
                "tracer": inputs.get("tracer"),
                "mode": inputs.get("mode"),
                "reg_mode": inputs.get("reg_mode")
            })
            
        # Add intermediate results
        if "intermediate" in results:
            intermediate = results["intermediate"]
            assessment_data.update({
                "converted_pet_nifti": intermediate.get("converted_pet_nifti"),
                "registered_pet_nifti": intermediate.get("registered_pet_nifti"),
                "transform": intermediate.get("transform")
            })
            
        # Add quantitative metrics
        if "metrics" in results:
            metrics = results["metrics"]
            assessment_data.update({
                "target_mean": metrics.get("target_mean"),
                "reference_mean": metrics.get("reference_mean"),
                "suvr_global_cortex_over_ref": metrics.get("suvr_global_cortex_over_ref"),
                "scaled_value": metrics.get("scaled_value"),
                "scaled_units": metrics.get("scaled_units")
            })
            
        # Add file paths (relative to output directory)
        output_path = Path(output_dir)
        
        # Check for output files and add their paths
        qc_overlay = output_path / "qc_overlay.png"
        if qc_overlay.exists():
            assessment_data["qc_overlay_file"] = str(qc_overlay)
            
        qc_pdf = output_path / "qc" / "QC_Report.pdf"
        if qc_pdf.exists():
            assessment_data["qc_pdf_file"] = str(qc_pdf)
            
        parametric_map = output_path / "parametric_map" / "SUVR_parametric_map.dcm"
        if parametric_map.exists():
            assessment_data["parametric_map_file"] = str(parametric_map)
            
        results_json = output_path / "centiloid.json"
        if results_json.exists():
            assessment_data["results_json"] = str(results_json)
            
        results_csv = output_path / "centiloid.csv"  
        if results_csv.exists():
            assessment_data["results_csv"] = str(results_csv)
        
        # Create assessment via REST API
        url = f"{self.xnat_host}/xapi/centiloid/projects/{project_id}/sessions/{session_id}/assessments"
        
        response = self.session.post(url, json=assessment_data)
        response.raise_for_status()
        
        result = response.json()
        assessment_id = result.get("id")
        
        print(f"Created XNAT assessment: {assessment_id}")
        return assessment_id
        
    def upload_files_to_xnat(self, 
                            project_id: str,
                            session_id: str,
                            assessment_id: str,
                            output_dir: str):
        """
        Upload output files to XNAT assessment
        """
        
        output_path = Path(output_dir)
        
        # Files to upload
        files_to_upload = [
            ("qc_overlay.png", "QC Overlay"),
            ("centiloid.json", "Results JSON"),
            ("centiloid.csv", "Results CSV"),
            ("qc/QC_Report.pdf", "QC Report"),
            ("parametric_map/SUVR_parametric_map.dcm", "Parametric Map")
        ]
        
        base_url = f"{self.xnat_host}/REST/projects/{project_id}/subjects/{session_id}/experiments/{assessment_id}"
        
        for file_path, description in files_to_upload:
            full_path = output_path / file_path
            if full_path.exists():
                try:
                    # Upload file
                    url = f"{base_url}/resources/files"
                    with open(full_path, 'rb') as f:
                        files = {'file': (full_path.name, f)}
                        response = self.session.post(url, files=files)
                        response.raise_for_status()
                        print(f"Uploaded {description}: {full_path.name}")
                        
                except Exception as e:
                    print(f"Failed to upload {description}: {e}")
            else:
                print(f"File not found: {full_path}")


def main():
    """
    Example usage
    """
    
    # Configuration
    xnat_host = "https://your-xnat-host"
    username = "your-username"
    password = "your-password"
    
    project_id = "YOUR_PROJECT"
    session_id = "YOUR_SESSION"
    
    # Input files
    dicom_dir = "/path/to/pet/dicom"
    template = "/path/to/template.nii.gz"
    target_mask = "/path/to/target_mask.nii.gz"
    ref_mask = "/path/to/ref_mask.nii.gz"
    output_dir = "/path/to/output"
    
    # Processing parameters
    tracer = "FBP"
    mode = "amyloid"
    reg_mode = "rigid"
    
    # Initialize integration
    integration = XNATCentiloidIntegration(xnat_host, username, password)
    
    try:
        # Run container
        print("Running centiloid container...")
        results = integration.run_centiloid_container(
            dicom_dir=dicom_dir,
            template=template,
            target_mask=target_mask,
            ref_mask=ref_mask,
            tracer=tracer,
            mode=mode,
            output_dir=output_dir,
            reg_mode=reg_mode
        )
        
        print(f"Container results: {json.dumps(results, indent=2)}")
        
        # Create XNAT assessment
        print("Creating XNAT assessment...")
        assessment_id = integration.create_xnat_assessment(
            project_id=project_id,
            session_id=session_id,
            results=results,
            output_dir=output_dir
        )
        
        # Upload files
        print("Uploading files to XNAT...")
        integration.upload_files_to_xnat(
            project_id=project_id,
            session_id=session_id, 
            assessment_id=assessment_id,
            output_dir=output_dir
        )
        
        print(f"Successfully integrated centiloid results into XNAT assessment {assessment_id}")
        
    except Exception as e:
        print(f"Integration failed: {e}")
        sys.exit(1)


if __name__ == "__main__":
    main()