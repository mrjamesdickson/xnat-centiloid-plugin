package org.nrg.xnatx.plugins.centiloid;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XnatDataModel;
import org.nrg.framework.annotations.XnatPlugin;
import org.nrg.xdat.om.base.BaseXnatExperimentdata;
import org.nrg.xnatx.plugins.centiloid.entities.CentiloidAssessmentData;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * XNAT plugin for Centiloid PET analysis results
 */
@XnatPlugin(value = "centiloid-plugin", 
           name = "Centiloid Plugin", 
           description = "XNAT datatype plugin for storing Centiloid PET analysis results",
           version = "1.0.0",
           dataModels = {@XnatDataModel(value = CentiloidAssessmentData.SCHEMA_ELEMENT_NAME,
                                      singular = "CentiloidAssessment",
                                      plural = "CentiloidAssessments")})
@ComponentScan({"org.nrg.xnatx.plugins.centiloid.rest", 
                "org.nrg.xnatx.plugins.centiloid.services"})
@Slf4j
public class CentiloidPlugin {

    public CentiloidPlugin() {
        log.info("Initializing Centiloid Plugin");
    }

    @Bean
    public String centiloidPluginName() {
        return "Centiloid Plugin";
    }
}