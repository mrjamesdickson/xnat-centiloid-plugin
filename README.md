# XNAT Centiloid Plugin

An XNAT datatype plugin for storing Centiloid PET analysis results from the centiloid container.

## Overview

This plugin provides an XNAT image assessor datatype specifically designed to store the output from the [centiloid_container_with_dicom_qc](../centiloid_container_with_dicom_qc) processing pipeline. It captures all quantitative metrics, processing parameters, and output files generated during Centiloid analysis.

## Features

- **Complete Data Capture**: Stores all input parameters, intermediate results, and quantitative metrics
- **File Management**: Tracks locations of output files (QC images, PDFs, DICOM parametric maps, etc.)
- **REST API**: Full REST API for creating, reading, updating, and deleting assessments
- **Web Interface**: XNAT web interface for viewing assessment details
- **Validation**: Built-in validation to ensure data integrity

## Data Model

The Centiloid assessment captures:

### Input Parameters
- DICOM directory or PET NIfTI file path
- Template space and mask files
- Tracer type (FBP, NAV4694, etc.)
- Analysis mode (amyloid, tau)
- Registration mode (rigid, affine)

### Quantitative Results
- Target region mean value
- Reference region mean value
- Global cortical SUVR
- Centiloid value and units

### Output Files
- QC overlay image
- QC PDF report
- DICOM parametric map
- Results JSON and CSV files

### Processing Metadata
- Processing status and date
- Container version
- Transform files and intermediate results

## Installation

1. Build the plugin:
   ```bash
   ./gradlew build
   ```

2. Copy the generated JAR to your XNAT plugins directory:
   ```bash
   cp build/libs/xnat-centiloid-plugin-1.0.0.jar /path/to/xnat/plugins/
   ```

3. Restart XNAT server

4. The new datatype will be available in the XNAT interface

## Usage

### Via REST API

Create a new Centiloid assessment from container results:

```bash
curl -X POST \
  "http://your-xnat/xapi/centiloid/projects/PROJECT_ID/sessions/SESSION_ID/assessments" \
  -H "Content-Type: application/json" \
  -d '{
    "tracer": "FBP",
    "mode": "amyloid",
    "reg_mode": "rigid",
    "target_mean": 6660.75,
    "reference_mean": 5060.20,
    "suvr_global_cortex_over_ref": 1.3163,
    "scaled_value": 63.15,
    "scaled_units": "Centiloid",
    "processing_status": "completed",
    "results_json_path": "/path/to/centiloid.json"
  }'
```

Get an existing assessment:

```bash
curl -X GET \
  "http://your-xnat/xapi/centiloid/projects/PROJECT_ID/sessions/SESSION_ID/assessments/ASSESSMENT_ID"
```

### Via Container Integration

When running the centiloid container, the results can be automatically stored in XNAT by calling the REST API from your container orchestration system:

```bash
# After container completes successfully
curl -X POST \
  "http://your-xnat/xapi/centiloid/projects/${PROJECT_ID}/sessions/${SESSION_ID}/assessments" \
  -H "Content-Type: application/json" \
  -d @/path/to/output/centiloid.json
```

### Programmatic Usage

```java
// Create assessment from container results
CentiloidAssessmentService service = // inject service
CentiloidAssessmentData assessment = service.createFromContainerResults(
    projectId, sessionId, "/path/to/results.json", user);

// Retrieve assessment
CentiloidAssessmentData assessment = service.getById(assessmentId, user);

// Access quantitative results
Float centiloidValue = assessment.getScaledValue();
Float suvr = assessment.getSuvrGlobalCortexOverRef();
String qcImagePath = assessment.getQcOverlayFile();
```

## Schema Definition

The plugin defines the `centiloid:centiloidAssessmentData` schema element, extending the standard XNAT `xnat:imageAssessorData` base type. See [centiloid.xsd](src/main/resources/schemas/centiloid/centiloid.xsd) for the complete schema definition.

## API Documentation

The REST API is documented using Swagger annotations. Once deployed, documentation will be available at:
`http://your-xnat/swagger-ui.html`

## File Structure

```
src/
├── main/
│   ├── java/
│   │   └── org/nrg/xnatx/plugins/centiloid/
│   │       ├── entities/           # Data model classes
│   │       ├── rest/              # REST API endpoints
│   │       ├── services/          # Business logic services
│   │       └── CentiloidPlugin.java
│   └── resources/
│       ├── META-INF/
│       │   └── xnat-plugin.properties
│       ├── schemas/centiloid/
│       │   ├── centiloid.xsd      # XML Schema Definition
│       │   └── display/           # Web display configuration
│       └── templates/screens/     # Web interface templates
├── build.gradle                  # Build configuration
└── README.md
```

## Development

### Building

```bash
./gradlew build
```

### Testing

```bash
./gradlew test
```

### IDE Setup

Import the project as a Gradle project in your IDE. Lombok plugin is recommended for annotation processing.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make changes and add tests
4. Submit a pull request

## License

Licensed under the Apache License, Version 2.0. See LICENSE file for details.

## Support

For issues and questions:
- Check existing GitHub issues
- Create a new issue with detailed description and steps to reproduce
- Include XNAT version and plugin version information