package org.nrg.xnatx.plugins.centiloid.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.nrg.action.ActionException;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xapi.rest.AbstractExperimentRestApi;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatImageassessordata;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.centiloid.entities.CentiloidAssessmentData;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Map;

@Api("Centiloid Assessment REST API")
@XapiRestController
@RequestMapping(value = "/centiloid")
@Slf4j
public class CentiloidAssessmentRestApi extends AbstractExperimentRestApi {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @ApiOperation(value = "Create Centiloid assessment from container results", 
                  notes = "Creates a new Centiloid assessment from container output files")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Assessment created successfully"),
        @ApiResponse(code = 400, message = "Invalid request or missing required fields"),
        @ApiResponse(code = 401, message = "Must be authenticated to access XNAT REST services"),
        @ApiResponse(code = 403, message = "Insufficient privileges to create assessments"),
        @ApiResponse(code = 404, message = "Session not found"),
        @ApiResponse(code = 500, message = "Unexpected error")
    })
    @XapiRequestMapping(value = "/projects/{projectId}/sessions/{sessionId}/assessments", 
                       method = RequestMethod.POST, 
                       restrictTo = AccessLevel.Edit)
    public ResponseEntity<Map<String, Object>> createCentiloidAssessment(
            @ApiParam(value = "Project ID", required = true) @PathVariable final String projectId,
            @ApiParam(value = "Session ID", required = true) @PathVariable final String sessionId,
            @ApiParam(value = "Centiloid results data", required = true) @RequestBody final Map<String, Object> body) {

        try {
            final UserI user = getSessionUser();
            
            // Validate session exists and user has access
            final XnatImageassessordata session = getSession(projectId, sessionId, user);
            if (session == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            // Create new Centiloid assessment
            CentiloidAssessmentData assessment = new CentiloidAssessmentData(user);
            assessment.setProject(projectId);
            assessment.setImageSessionId(sessionId);
            
            // Set processing date
            assessment.setProcessingDate(new Date());
            
            // Extract and set data from request body
            setAssessmentDataFromRequest(assessment, body);
            
            // Process JSON results file if provided
            String resultsJsonPath = (String) body.get("results_json_path");
            if (resultsJsonPath != null && Files.exists(Paths.get(resultsJsonPath))) {
                processJsonResults(assessment, resultsJsonPath);
            }
            
            // Save assessment
            assessment.save(user, false, false, null);
            
            log.info("Created Centiloid assessment {} for session {}/{}", 
                    assessment.getId(), projectId, sessionId);
            
            return new ResponseEntity<>(Map.of(
                "id", assessment.getId(),
                "label", assessment.getLabel(),
                "status", "created"
            ), HttpStatus.CREATED);
            
        } catch (Exception e) {
            log.error("Error creating Centiloid assessment", e);
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Get Centiloid assessment", 
                  notes = "Retrieves a Centiloid assessment by ID")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Assessment retrieved successfully"),
        @ApiResponse(code = 401, message = "Must be authenticated to access XNAT REST services"),
        @ApiResponse(code = 403, message = "Insufficient privileges to view assessment"),
        @ApiResponse(code = 404, message = "Assessment not found"),
        @ApiResponse(code = 500, message = "Unexpected error")
    })
    @XapiRequestMapping(value = "/projects/{projectId}/sessions/{sessionId}/assessments/{assessmentId}", 
                       method = RequestMethod.GET, 
                       restrictTo = AccessLevel.Read)
    public ResponseEntity<Map<String, Object>> getCentiloidAssessment(
            @ApiParam(value = "Project ID", required = true) @PathVariable final String projectId,
            @ApiParam(value = "Session ID", required = true) @PathVariable final String sessionId,
            @ApiParam(value = "Assessment ID", required = true) @PathVariable final String assessmentId) {

        try {
            final UserI user = getSessionUser();
            
            // Retrieve assessment
            CentiloidAssessmentData assessment = (CentiloidAssessmentData) XDAT.getSearchHelperService()
                    .getSearchHelper(user, CentiloidAssessmentData.SCHEMA_ELEMENT_NAME, false, true)
                    .retrieveItem(assessmentId, user);
                    
            if (assessment == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            // Build response data
            Map<String, Object> responseData = Map.of(
                "id", assessment.getId(),
                "label", assessment.getLabel(),
                "project", assessment.getProject(),
                "session_id", assessment.getImageSessionId(),
                "tracer", assessment.getTracer(),
                "mode", assessment.getMode(),
                "target_mean", assessment.getTargetMean(),
                "reference_mean", assessment.getReferenceMean(),
                "suvr", assessment.getSuvrGlobalCortexOverRef(),
                "centiloid_value", assessment.getScaledValue(),
                "centiloid_units", assessment.getScaledUnits(),
                "processing_status", assessment.getProcessingStatus(),
                "processing_date", assessment.getProcessingDate(),
                "container_version", assessment.getContainerVersion()
            );
            
            return new ResponseEntity<>(responseData, HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("Error retrieving Centiloid assessment", e);
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void setAssessmentDataFromRequest(CentiloidAssessmentData assessment, Map<String, Object> body) {
        // Set input parameters
        setIfPresent(body, "dicom_dir", assessment::setDicomDir);
        setIfPresent(body, "pet_nifti", assessment::setPetNifti);
        setIfPresent(body, "template", assessment::setTemplate);
        setIfPresent(body, "target_mask", assessment::setTargetMask);
        setIfPresent(body, "ref_mask", assessment::setRefMask);
        setIfPresent(body, "tracer", assessment::setTracer);
        setIfPresent(body, "mode", assessment::setMode);
        setIfPresent(body, "reg_mode", assessment::setRegMode);
        
        // Set processing results
        setIfPresent(body, "converted_pet_nifti", assessment::setConvertedPetNifti);
        setIfPresent(body, "registered_pet_nifti", assessment::setRegisteredPetNifti);
        setIfPresent(body, "transform", assessment::setTransform);
        
        // Set metrics
        setFloatIfPresent(body, "target_mean", assessment::setTargetMean);
        setFloatIfPresent(body, "reference_mean", assessment::setReferenceMean);
        setFloatIfPresent(body, "suvr_global_cortex_over_ref", assessment::setSuvrGlobalCortexOverRef);
        setFloatIfPresent(body, "scaled_value", assessment::setScaledValue);
        setIfPresent(body, "scaled_units", assessment::setScaledUnits);
        
        // Set output files
        setIfPresent(body, "qc_overlay_file", assessment::setQcOverlayFile);
        setIfPresent(body, "qc_pdf_file", assessment::setQcPdfFile);
        setIfPresent(body, "parametric_map_file", assessment::setParametricMapFile);
        setIfPresent(body, "results_json", assessment::setResultsJson);
        setIfPresent(body, "results_csv", assessment::setResultsCsv);
        
        // Set metadata
        setIfPresent(body, "processing_status", assessment::setProcessingStatus);
        setIfPresent(body, "container_version", assessment::setContainerVersion);
    }

    private void processJsonResults(CentiloidAssessmentData assessment, String jsonPath) {
        try {
            String jsonContent = Files.readString(Paths.get(jsonPath));
            Map<String, Object> results = objectMapper.readValue(jsonContent, Map.class);
            
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
            
        } catch (Exception e) {
            log.warn("Failed to process JSON results file: {}", e.getMessage());
        }
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