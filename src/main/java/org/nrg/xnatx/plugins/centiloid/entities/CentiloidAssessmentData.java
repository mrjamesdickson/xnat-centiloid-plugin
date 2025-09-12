package org.nrg.xnatx.plugins.centiloid.entities;

import org.nrg.xdat.om.XnatImageassessordata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Date;

/**
 * Entity class for Centiloid Assessment data
 */
public class CentiloidAssessmentData extends XnatImageassessordata {

    public static final String SCHEMA_ELEMENT_NAME = "centiloid:centiloidAssessmentData";

    public CentiloidAssessmentData(ItemI item) {
        super(item);
    }

    public CentiloidAssessmentData(UserI user) {
        super(user);
    }

    public CentiloidAssessmentData() {
        super();
    }

    @Override
    public String getSchemaElementName() {
        return SCHEMA_ELEMENT_NAME;
    }

    // Input parameters
    public String getDicomDir() {
        return getStringProperty("dicom_dir");
    }

    public void setDicomDir(String dicomDir) {
        setProperty("dicom_dir", dicomDir);
    }

    public String getPetNifti() {
        return getStringProperty("pet_nifti");
    }

    public void setPetNifti(String petNifti) {
        setProperty("pet_nifti", petNifti);
    }

    public String getTemplate() {
        return getStringProperty("template");
    }

    public void setTemplate(String template) {
        setProperty("template", template);
    }

    public String getTargetMask() {
        return getStringProperty("target_mask");
    }

    public void setTargetMask(String targetMask) {
        setProperty("target_mask", targetMask);
    }

    public String getRefMask() {
        return getStringProperty("ref_mask");
    }

    public void setRefMask(String refMask) {
        setProperty("ref_mask", refMask);
    }

    public String getTracer() {
        return getStringProperty("tracer");
    }

    public void setTracer(String tracer) {
        setProperty("tracer", tracer);
    }

    public String getMode() {
        return getStringProperty("mode");
    }

    public void setMode(String mode) {
        setProperty("mode", mode);
    }

    public String getRegMode() {
        return getStringProperty("reg_mode");
    }

    public void setRegMode(String regMode) {
        setProperty("reg_mode", regMode);
    }

    // Processing results
    public String getConvertedPetNifti() {
        return getStringProperty("converted_pet_nifti");
    }

    public void setConvertedPetNifti(String convertedPetNifti) {
        setProperty("converted_pet_nifti", convertedPetNifti);
    }

    public String getRegisteredPetNifti() {
        return getStringProperty("registered_pet_nifti");
    }

    public void setRegisteredPetNifti(String registeredPetNifti) {
        setProperty("registered_pet_nifti", registeredPetNifti);
    }

    public String getTransform() {
        return getStringProperty("transform");
    }

    public void setTransform(String transform) {
        setProperty("transform", transform);
    }

    // Quantitative metrics
    public Float getTargetMean() {
        return getFloatProperty("target_mean");
    }

    public void setTargetMean(Float targetMean) {
        setProperty("target_mean", targetMean);
    }

    public Float getReferenceMean() {
        return getFloatProperty("reference_mean");
    }

    public void setReferenceMean(Float referenceMean) {
        setProperty("reference_mean", referenceMean);
    }

    public Float getSuvrGlobalCortexOverRef() {
        return getFloatProperty("suvr_global_cortex_over_ref");
    }

    public void setSuvrGlobalCortexOverRef(Float suvr) {
        setProperty("suvr_global_cortex_over_ref", suvr);
    }

    public Float getScaledValue() {
        return getFloatProperty("scaled_value");
    }

    public void setScaledValue(Float scaledValue) {
        setProperty("scaled_value", scaledValue);
    }

    public String getScaledUnits() {
        return getStringProperty("scaled_units");
    }

    public void setScaledUnits(String scaledUnits) {
        setProperty("scaled_units", scaledUnits);
    }

    // QC and output files
    public String getQcOverlayFile() {
        return getStringProperty("qc_overlay_file");
    }

    public void setQcOverlayFile(String qcOverlayFile) {
        setProperty("qc_overlay_file", qcOverlayFile);
    }

    public String getQcPdfFile() {
        return getStringProperty("qc_pdf_file");
    }

    public void setQcPdfFile(String qcPdfFile) {
        setProperty("qc_pdf_file", qcPdfFile);
    }

    public String getParametricMapFile() {
        return getStringProperty("parametric_map_file");
    }

    public void setParametricMapFile(String parametricMapFile) {
        setProperty("parametric_map_file", parametricMapFile);
    }

    public String getResultsJson() {
        return getStringProperty("results_json");
    }

    public void setResultsJson(String resultsJson) {
        setProperty("results_json", resultsJson);
    }

    public String getResultsCsv() {
        return getStringProperty("results_csv");
    }

    public void setResultsCsv(String resultsCsv) {
        setProperty("results_csv", resultsCsv);
    }

    // Processing status and metadata
    public String getProcessingStatus() {
        return getStringProperty("processing_status");
    }

    public void setProcessingStatus(String processingStatus) {
        setProperty("processing_status", processingStatus);
    }

    public Date getProcessingDate() {
        return getDateProperty("processing_date");
    }

    public void setProcessingDate(Date processingDate) {
        setProperty("processing_date", processingDate);
    }

    public String getContainerVersion() {
        return getStringProperty("container_version");
    }

    public void setContainerVersion(String containerVersion) {
        setProperty("container_version", containerVersion);
    }
}