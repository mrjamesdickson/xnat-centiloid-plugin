#!/usr/bin/env python3
"""
Test script to validate the Centiloid plugin installation and functionality.
"""

import json
import requests
import sys

def test_plugin_api(xnat_host: str, username: str, password: str, 
                   project_id: str, session_id: str):
    """
    Test the Centiloid plugin REST API endpoints
    """
    
    # Setup session
    session = requests.Session()
    session.auth = (username, password)
    
    base_url = f"{xnat_host.rstrip('/')}/xapi/centiloid/projects/{project_id}/sessions/{session_id}"
    
    # Test data
    test_assessment = {
        "tracer": "FBP",
        "mode": "amyloid", 
        "reg_mode": "rigid",
        "target_mean": 6660.75,
        "reference_mean": 5060.20,
        "suvr_global_cortex_over_ref": 1.3163,
        "scaled_value": 63.15,
        "scaled_units": "Centiloid",
        "processing_status": "completed",
        "container_version": "1.0.0"
    }
    
    try:
        print("Testing Centiloid plugin REST API...")
        
        # Test 1: Create assessment
        print("1. Creating test assessment...")
        create_url = f"{base_url}/assessments"
        response = session.post(create_url, json=test_assessment)
        response.raise_for_status()
        
        result = response.json()
        assessment_id = result.get("id")
        print(f"   ✓ Created assessment: {assessment_id}")
        
        # Test 2: Get assessment
        print("2. Retrieving assessment...")
        get_url = f"{base_url}/assessments/{assessment_id}"
        response = session.get(get_url)
        response.raise_for_status()
        
        assessment_data = response.json()
        print(f"   ✓ Retrieved assessment: {assessment_data.get('label', 'N/A')}")
        
        # Validate key fields
        assert assessment_data.get("tracer") == test_assessment["tracer"]
        assert assessment_data.get("centiloid_value") == test_assessment["scaled_value"]
        print("   ✓ Data validation passed")
        
        # Test 3: Clean up (delete assessment)
        print("3. Cleaning up test assessment...")
        delete_url = f"{base_url}/assessments/{assessment_id}"
        response = session.delete(delete_url)
        response.raise_for_status()
        print("   ✓ Test assessment deleted")
        
        print("\n✓ All tests passed! Centiloid plugin is working correctly.")
        return True
        
    except requests.exceptions.HTTPError as e:
        print(f"\n✗ HTTP Error: {e}")
        print(f"Response: {e.response.text if e.response else 'No response'}")
        return False
        
    except Exception as e:
        print(f"\n✗ Error: {e}")
        return False


def check_schema_availability(xnat_host: str, username: str, password: str):
    """
    Check if the Centiloid schema is properly registered in XNAT
    """
    
    session = requests.Session()
    session.auth = (username, password)
    
    try:
        # Check schema endpoint
        url = f"{xnat_host.rstrip('/')}/REST/schemas"
        response = session.get(url)
        response.raise_for_status()
        
        # Look for centiloid schema in response
        content = response.text.lower()
        if "centiloid" in content:
            print("✓ Centiloid schema found in XNAT schemas")
            return True
        else:
            print("✗ Centiloid schema not found in XNAT schemas")
            return False
            
    except Exception as e:
        print(f"✗ Could not check schema availability: {e}")
        return False


def main():
    """
    Run plugin tests
    """
    
    # Configuration - update these values
    xnat_host = "http://localhost:8080"  # Your XNAT host
    username = "admin"                   # XNAT username  
    password = "admin"                   # XNAT password
    project_id = "TestProject"           # Test project ID
    session_id = "TestSession"           # Test session ID
    
    print("XNAT Centiloid Plugin Test")
    print("=" * 30)
    print(f"XNAT Host: {xnat_host}")
    print(f"Project: {project_id}")
    print(f"Session: {session_id}")
    print()
    
    # Check schema
    print("Checking schema registration...")
    schema_ok = check_schema_availability(xnat_host, username, password)
    
    if not schema_ok:
        print("\nWarning: Schema check failed. Plugin may not be properly installed.")
        print("Proceeding with API tests anyway...")
    
    print()
    
    # Test API
    success = test_plugin_api(xnat_host, username, password, project_id, session_id)
    
    if success:
        print("\n🎉 Centiloid plugin test completed successfully!")
        print("\nNext steps:")
        print("- Run your centiloid container")
        print("- Use the integration script to store results in XNAT")
        print("- View assessments in the XNAT web interface")
    else:
        print("\n❌ Plugin test failed!")
        print("\nTroubleshooting:")
        print("- Verify plugin JAR is in XNAT plugins directory")
        print("- Check XNAT logs for any errors during plugin loading")
        print("- Ensure XNAT server was restarted after plugin installation")
        print("- Verify project and session exist in XNAT")
        sys.exit(1)


if __name__ == "__main__":
    main()