package org.nrg.xnatx.plugins.centiloid.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatImageassessordata;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.centiloid.entities.CentiloidAssessmentData;
import org.nrg.xnatx.plugins.centiloid.services.CentiloidAssessmentService;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class DefaultCentiloidAssessmentService implements CentiloidAssessmentService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public CentiloidAssessmentData createFromContainerResults(String projectId, String sessionId, 
                                                             String resultsJsonPath, UserI user) throws Exception {
        
        log.info("Creating Centiloid assessment from container results: {}", resultsJsonPath);
        
        // Read and parse JSON results
        String jsonContent = Files.readString(Paths.get(resultsJsonPath));
        Map<String, Object> results = objectMapper.readValue(jsonContent, Map.class);
        
        // Create assessment
        CentiloidAssessmentData assessment = new CentiloidAssessmentData(user);
        assessment.setProject(projectId);
        assessment.setImageSessionId(sessionId);
        assessment.setProcessingDate(new Date());
        assessment.setProcessingStatus("completed");
        
        // Set data from JSON
        populateFromJsonResults(assessment, results);
        
        // Set results file path
        assessment.setResultsJson(resultsJsonPath);
        
        // Validate assessment
        if (!validate(assessment)) {
            throw new IllegalArgumentException("Assessment validation failed - missing required fields");
        }
        
        // Save assessment
        assessment.save(user, false, false, null);
        
        log.info("Created Centiloid assessment {} for session {}/{}", 
                assessment.getId(), projectId, sessionId);
        
        return assessment;
    }

    @Override
    public CentiloidAssessmentData create(String projectId, String sessionId, 
                                         Map<String, Object> data, UserI user) throws Exception {
        
        log.info("Creating Centiloid assessment for session {}/{}", projectId, sessionId);
        
        CentiloidAssessmentData assessment = new CentiloidAssessmentData(user);
        assessment.setProject(projectId);
        assessment.setImageSessionId(sessionId);
        assessment.setProcessingDate(new Date());
        
        // Populate from provided data
        populateFromMap(assessment, data);
        
        // Validate assessment
        if (!validate(assessment)) {
            throw new IllegalArgumentException("Assessment validation failed - missing required fields");
        }
        
        // Save assessment
        assessment.save(user, false, false, null);
        
        log.info("Created Centiloid assessment {} for session {}/{}", 
                assessment.getId(), projectId, sessionId);
        
        return assessment;
    }

    @Override
    public CentiloidAssessmentData getById(String assessmentId, UserI user) throws Exception {
        return (CentiloidAssessmentData) XDAT.getSearchHelperService()
                .getSearchHelper(user, CentiloidAssessmentData.SCHEMA_ELEMENT_NAME, false, true)
                .retrieveItem(assessmentId, user);
    }

    @Override
    public List<CentiloidAssessmentData> getBySession(String projectId, String sessionId, UserI user) throws Exception {
        // Implementation would require XDAT search for assessments by session
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public CentiloidAssessmentData update(String assessmentId, Map<String, Object> data, UserI user) throws Exception {
        CentiloidAssessmentData assessment = getById(assessmentId, user);
        if (assessment == null) {
            throw new IllegalArgumentException("Assessment not found: " + assessmentId);
        }
        
        // Update fields
        populateFromMap(assessment, data);
        
        // Validate assessment
        if (!validate(assessment)) {
            throw new IllegalArgumentException("Assessment validation failed - missing required fields");
        }
        
        // Save updated assessment
        assessment.save(user, false, false, null);
        
        log.info("Updated Centiloid assessment {}", assessmentId);
        
        return assessment;
    }

    @Override
    public void delete(String assessmentId, UserI user) throws Exception {
        CentiloidAssessmentData assessment = getById(assessmentId, user);
        if (assessment == null) {
            throw new IllegalArgumentException("Assessment not found: " + assessmentId);
        }
        
        assessment.delete(user, null);
        log.info("Deleted Centiloid assessment {}", assessmentId);
    }

    @Override
    public boolean validate(CentiloidAssessmentData assessment) {
        // Check required fields for a valid Centiloid assessment
        return assessment.getTracer() != null && 
               assessment.getMode() != null && 
               assessment.getSuvrGlobalCortexOverRef() != null &&
               assessment.getScaledValue() != null;
    }

    private void populateFromJsonResults(CentiloidAssessmentData assessment, Map<String, Object> results) {
        // Extract inputs
        Map<String, Object> inputs = (Map<String, Object>) results.get("inputs");
        if (inputs != null) {
            setIfPresent(inputs, "dicom_dir", assessment::setDicomDir);
            setIfPresent(inputs, "pet_nifti", assessment::setPetNifti);
            setIfPresent(inputs, "template", assessment::setTemplate);
            setIfPresent(inputs, "target_mask", assessment::setTargetMask);
            setIfPresent(inputs, "ref_mask", assessment::setRefMask);
            setIfPresent(inputs, "tracer", assessment::setTracer);
            setIfPresent(inputs, "mode", assessment::setMode);
            setIfPresent(inputs, "reg_mode", assessment::setRegMode);
        }
        
        // Extract intermediate results
        Map<String, Object> intermediate = (Map<String, Object>) results.get("intermediate");
        if (intermediate != null) {
            setIfPresent(intermediate, "converted_pet_nifti", assessment::setConvertedPetNifti);
            setIfPresent(intermediate, "registered_pet_nifti", assessment::setRegisteredPetNifti);
            setIfPresent(intermediate, "transform", assessment::setTransform);
        }
        
        // Extract metrics
        Map<String, Object> metrics = (Map<String, Object>) results.get("metrics");
        if (metrics != null) {
            setFloatIfPresent(metrics, "target_mean", assessment::setTargetMean);
            setFloatIfPresent(metrics, "reference_mean", assessment::setReferenceMean);
            setFloatIfPresent(metrics, "suvr_global_cortex_over_ref", assessment::setSuvrGlobalCortexOverRef);
            setFloatIfPresent(metrics, "scaled_value", assessment::setScaledValue);
            setIfPresent(metrics, "scaled_units", assessment::setScaledUnits);
        }
    }

    private void populateFromMap(CentiloidAssessmentData assessment, Map<String, Object> data) {
        // Set input parameters
        setIfPresent(data, "dicom_dir", assessment::setDicomDir);
        setIfPresent(data, "pet_nifti", assessment::setPetNifti);
        setIfPresent(data, "template", assessment::setTemplate);
        setIfPresent(data, "target_mask", assessment::setTargetMask);
        setIfPresent(data, "ref_mask", assessment::setRefMask);
        setIfPresent(data, "tracer", assessment::setTracer);
        setIfPresent(data, "mode", assessment::setMode);
        setIfPresent(data, "reg_mode", assessment::setRegMode);
        
        // Set processing results
        setIfPresent(data, "converted_pet_nifti", assessment::setConvertedPetNifti);
        setIfPresent(data, "registered_pet_nifti", assessment::setRegisteredPetNifti);
        setIfPresent(data, "transform", assessment::setTransform);
        
        // Set metrics
        setFloatIfPresent(data, "target_mean", assessment::setTargetMean);
        setFloatIfPresent(data, "reference_mean", assessment::setReferenceMean);
        setFloatIfPresent(data, "suvr_global_cortex_over_ref", assessment::setSuvrGlobalCortexOverRef);
        setFloatIfPresent(data, "scaled_value", assessment::setScaledValue);
        setIfPresent(data, "scaled_units", assessment::setScaledUnits);
        
        // Set output files
        setIfPresent(data, "qc_overlay_file", assessment::setQcOverlayFile);
        setIfPresent(data, "qc_pdf_file", assessment::setQcPdfFile);
        setIfPresent(data, "parametric_map_file", assessment::setParametricMapFile);
        setIfPresent(data, "results_json", assessment::setResultsJson);
        setIfPresent(data, "results_csv", assessment::setResultsCsv);
        
        // Set metadata
        setIfPresent(data, "processing_status", assessment::setProcessingStatus);
        setIfPresent(data, "container_version", assessment::setContainerVersion);
    }

    private void setIfPresent(Map<String, Object> map, String key, java.util.function.Consumer<String> setter) {
        if (map.containsKey(key) && map.get(key) != null) {
            setter.accept(map.get(key).toString());
        }
    }

    private void setFloatIfPresent(Map<String, Object> map, String key, java.util.function.Consumer<Float> setter) {
        if (map.containsKey(key) && map.get(key) != null) {
            try {
                setter.accept(Float.parseFloat(map.get(key).toString()));
            } catch (NumberFormatException e) {
                log.warn("Failed to parse float value for key {}: {}", key, map.get(key));
            }
        }
    }
}