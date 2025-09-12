package org.nrg.xnatx.plugins.centiloid.services;

import org.nrg.xnatx.plugins.centiloid.entities.CentiloidAssessmentData;
import org.nrg.xft.security.UserI;

import java.util.List;
import java.util.Map;

/**
 * Service interface for Centiloid Assessment operations
 */
public interface CentiloidAssessmentService {

    /**
     * Create a new Centiloid assessment from container results
     * 
     * @param projectId Project ID
     * @param sessionId Session ID  
     * @param resultsJsonPath Path to container results JSON file
     * @param user User performing the operation
     * @return Created assessment
     * @throws Exception if creation fails
     */
    CentiloidAssessmentData createFromContainerResults(String projectId, String sessionId, 
                                                      String resultsJsonPath, UserI user) throws Exception;

    /**
     * Create a new Centiloid assessment from provided data
     * 
     * @param projectId Project ID
     * @param sessionId Session ID
     * @param data Assessment data
     * @param user User performing the operation
     * @return Created assessment
     * @throws Exception if creation fails
     */
    CentiloidAssessmentData create(String projectId, String sessionId, 
                                  Map<String, Object> data, UserI user) throws Exception;

    /**
     * Get a Centiloid assessment by ID
     * 
     * @param assessmentId Assessment ID
     * @param user User performing the operation
     * @return Assessment or null if not found
     * @throws Exception if retrieval fails
     */
    CentiloidAssessmentData getById(String assessmentId, UserI user) throws Exception;

    /**
     * Get all Centiloid assessments for a session
     * 
     * @param projectId Project ID
     * @param sessionId Session ID
     * @param user User performing the operation
     * @return List of assessments
     * @throws Exception if retrieval fails
     */
    List<CentiloidAssessmentData> getBySession(String projectId, String sessionId, UserI user) throws Exception;

    /**
     * Update an existing Centiloid assessment
     * 
     * @param assessmentId Assessment ID
     * @param data Updated data
     * @param user User performing the operation
     * @return Updated assessment
     * @throws Exception if update fails
     */
    CentiloidAssessmentData update(String assessmentId, Map<String, Object> data, UserI user) throws Exception;

    /**
     * Delete a Centiloid assessment
     * 
     * @param assessmentId Assessment ID
     * @param user User performing the operation
     * @throws Exception if deletion fails
     */
    void delete(String assessmentId, UserI user) throws Exception;

    /**
     * Validate that a Centiloid assessment has required fields
     * 
     * @param assessment Assessment to validate
     * @return True if valid, false otherwise
     */
    boolean validate(CentiloidAssessmentData assessment);
}