# XNAT Centiloid Plugin v1.0.0 - Release Notes

**Release Date:** September 11, 2025  
**JAR File:** `xnat-centiloid-plugin-1.0.0.jar` (4.8KB)  
**Git Tag:** `v1.0.0`

## 🎉 Initial Release

This is the first release of the XNAT Centiloid Plugin, providing complete integration between the Centiloid PET analysis container and XNAT's data management system.

## ✨ Features

### Core Datatype
- **XML Schema:** `centiloid:centiloidAssessmentData` extending `xnat:imageAssessorData`
- **Complete Data Model:** Captures all input parameters, quantitative metrics, and output files
- **Structured Storage:** Queryable data format for research and clinical workflows

### Quantitative Metrics Storage
- Target region mean values
- Reference region mean values  
- Global cortical SUVR calculations
- Centiloid values with units
- Processing parameters and metadata

### REST API
- **Create Assessments:** `POST /xapi/centiloid/projects/{project}/sessions/{session}/assessments`
- **Retrieve Assessments:** `GET /xapi/centiloid/projects/{project}/sessions/{session}/assessments/{id}`
- **JSON Processing:** Automatic parsing of centiloid container output
- **Error Handling:** Robust validation and error reporting

### Web Interface
- **Assessment Detail View:** Clean display of quantitative results
- **File Downloads:** Direct links to QC images, PDFs, and reports
- **Processing Information:** Complete audit trail of analysis parameters
- **Responsive Design:** Works across different screen sizes

### Container Integration
- **Automatic Creation:** Assessments created by centiloid container post-processing
- **File Upload:** QC images, PDF reports, DICOM parametric maps uploaded to XNAT
- **Status Tracking:** Processing status and completion timestamps
- **Version Control:** Container version tracking for reproducibility

## 📦 Installation

### Prerequisites
- XNAT server (tested with XNAT 1.9.2)
- Java 8+ runtime environment
- Centiloid container integration (optional)

### Deployment Steps
1. Copy `xnat-centiloid-plugin-1.0.0.jar` to XNAT plugins directory
2. Restart XNAT server
3. Verify plugin loads in XNAT admin interface
4. Test REST API endpoints (optional)

### Quick Verification
```bash
# Check plugin loaded
curl -X GET "http://your-xnat/xapi/version" -u username:password

# Test assessment creation
curl -X POST "http://your-xnat/xapi/centiloid/projects/TEST/sessions/TEST/assessments" \
  -H "Content-Type: application/json" \
  -u username:password \
  -d '{"tracer": "FBP", "scaled_value": 63.15, "scaled_units": "Centiloid"}'
```

## 🔧 Configuration

### Container Service Integration
Update your XNAT Container Service configuration:

```json
{
  "image": "xnatworks/xnat_centiloid_container:v1.1.0",
  "command-line": "... --xnat-host #XNAT_BASE_URL# --xnat-user #USER_ID# ..."
}
```

### Display Configuration
The plugin includes pre-configured display templates:
- Listing view with key metrics
- Detailed view with complete parameter set
- File download links for all output types

## 🧪 Testing

### Included Test Scripts
- `examples/test_plugin.py` - Plugin installation validation
- `examples/integrate_with_container.py` - Container integration example

### Manual Testing
1. Create test project and session in XNAT
2. Run centiloid container with XNAT parameters
3. Verify assessment appears in XNAT web interface
4. Check all quantitative values are correct
5. Test file downloads and QC review

## 📊 Data Model

### Assessment Fields
```xml
<!-- Core quantitative results -->
<target_mean>6660.75</target_mean>
<reference_mean>5060.20</reference_mean>
<suvr_global_cortex_over_ref>1.3163</suvr_global_cortex_over_ref>
<scaled_value>63.15</scaled_value>
<scaled_units>Centiloid</scaled_units>

<!-- Processing parameters -->
<tracer>FBP</tracer>
<mode>amyloid</mode>
<reg_mode>rigid</reg_mode>

<!-- File references -->
<qc_overlay_file>/path/to/qc_overlay.png</qc_overlay_file>
<results_json>/path/to/centiloid.json</results_json>
```

## 🔄 Container Workflow

1. **PET Processing:** Centiloid container analyzes DICOM data
2. **Results Generation:** Quantitative metrics and QC files created
3. **XNAT Upload:** Container calls plugin REST API
4. **Assessment Creation:** Structured assessment stored in XNAT
5. **File Upload:** All output files attached to assessment
6. **Web Display:** Results viewable in XNAT interface

## 📈 Benefits

### For Researchers
- **Structured Data:** Queryable database of Centiloid results
- **Quality Control:** Integrated QC review workflow
- **Data Export:** Easy access to quantitative metrics
- **Provenance Tracking:** Complete analysis audit trail

### For Clinicians  
- **Standardized Reporting:** Consistent Centiloid value presentation
- **Visual QC:** Integrated overlay images for review
- **Historical Tracking:** Longitudinal assessment comparison
- **PACS Integration:** DICOM output for clinical workflows

### For IT Administrators
- **Automated Workflow:** Minimal manual intervention required
- **Scalable Processing:** Container-based analysis pipeline
- **Data Integrity:** Structured validation and error handling
- **Version Control:** Plugin and container version tracking

## 🐛 Known Issues

### Limitations
- Plugin requires XNAT restart for installation
- Java source compilation requires XNAT development dependencies
- Web interface requires modern browser for optimal display

### Workarounds
- Resource-only JAR deployment for immediate use
- Manual assessment creation via REST API if web UI issues occur
- Container can operate independently if XNAT upload fails

## 📞 Support

### Documentation
- `README.md` - Complete usage guide
- `DEPLOYMENT_GUIDE.md` - Installation instructions  
- `examples/` - Integration examples and test scripts

### Troubleshooting
1. Check XNAT server logs for plugin loading messages
2. Verify schema registration in XNAT admin interface
3. Test REST API endpoints with curl or Postman
4. Validate container network access to XNAT

## 🚀 Future Enhancements

### Planned Features
- Multi-session batch processing
- Enhanced QC metrics and validation
- Custom template and mask support
- Advanced reporting and visualization
- Integration with other PET analysis pipelines

### Community Contributions
- Pull requests welcome for feature enhancements
- Bug reports and feedback appreciated
- Documentation improvements encouraged

---

**Plugin Version:** 1.0.0  
**XNAT Compatibility:** 1.9.2+  
**Container Integration:** xnatworks/xnat_centiloid_container:v1.1.0  
**License:** Apache 2.0  
**Generated with:** Claude Code