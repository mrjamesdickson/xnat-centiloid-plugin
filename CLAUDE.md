# XNAT Centiloid Plugin - Development and Deployment Knowledge

## Overview
This project creates an XNAT datatype plugin for storing Centiloid PET analysis results and integrates with a containerized analysis pipeline.

## Components

### 1. XNAT Plugin
- **Location**: `/Users/james/projects/pet_centiloid_xnat_datatype/`
- **Built JAR**: `build/libs/xnat-centiloid-plugin-1.0.0.jar`
- **Schema**: `src/main/resources/schemas/centiloid/centiloid.xsd`
- **Datatype**: Extends `xnat:imageAssessorData` as `centiloid:centiloidAssessmentData`

### 2. Centiloid Container
- **Docker Hub**: `xnatworks/xnat_centiloid_container:v1.1.0`
- **Location**: `/Users/james/projects/centiloud_container_with_dicom_qc/`
- **XNAT Integration**: `app/xnat_upload.py` module

### 3. Test Data
- **Location**: `/Users/james/projects/data/`
- **DICOM Data**: `Elder_subject_florbetapir/` (PET amyloid imaging data)
- **Expected Output**: JSON/CSV results with Centiloid values

## Deployment Process

### XNAT Setup with Docker Compose
1. **Location**: `/Users/james/projects/xnat-docker-compose/`
2. **Configuration**:
   - XNAT Version: 1.8.9
   - Base Image: `tomcat:9-jdk8-openjdk-bullseye` (fixed Debian Buster EOL issue)
   - Max Heap: 2g (prevents container kills)
   - PostgreSQL: 13.11-alpine
3. **Plugin Installation**:
   ```bash
   # Copy plugin to mounted plugins directory
   cp xnat-centiloid-plugin-1.0.0.jar /xnat-docker-compose/xnat/plugins/
   # Restart XNAT to load plugin
   docker-compose restart xnat-web
   ```

### Database Schema Creation
When the plugin is loaded, XNAT automatically creates:
- `centiloid_centiloidAssessmentData` table
- `centiloid_centiloidAssessmentData_meta_data` table
- `centiloid_centiloidAssessmentData_history` table
- All necessary indexes and foreign key constraints

## Container Testing Commands

### Basic Container Execution
```bash
# Run container with test data
docker run --rm \
  -v /Users/james/projects/data/Elder_subject_florbetapir:/input \
  -v /tmp/output:/output \
  xnatworks/xnat_centiloid_container:v1.1.0 \
  python /app/pipeline.py --input /input --output /output

# Run with XNAT upload (requires running XNAT instance)
docker run --rm \
  -v /Users/james/projects/data/Elder_subject_florbetapir:/input \
  -v /tmp/output:/output \
  --network xnat-docker-compose_default \
  xnatworks/xnat_centiloid_container:v1.1.0 \
  python /app/pipeline.py \
    --input /input \
    --output /output \
    --xnat-host http://xnat-web:8080 \
    --xnat-user admin \
    --xnat-password admin \
    --project-id TEST_PROJECT \
    --session-id TEST_SESSION
```

## Common Issues and Solutions

### 1. Docker Compose Build Failures
- **Issue**: Debian Buster repositories EOL (404 errors)
- **Solution**: Update Dockerfile to use `bullseye` base image

### 2. XNAT Memory Issues
- **Issue**: Container exits with code 137 (killed)
- **Solution**: Reduce `XNAT_MAX_HEAP` from 4g to 2g

### 3. Plugin Database Errors
- **Issue**: Plugin causes XNAT startup failures
- **Solution**: Start XNAT clean first, then add plugin and restart

### 4. Container Network Access
- **Issue**: Container cannot reach XNAT from host
- **Solution**: Use Docker Compose network: `--network xnat-docker-compose_default`

## File Structure
```
pet_centiloid_xnat_datatype/
├── CLAUDE.md                          # This documentation
├── build/libs/                        # Built plugin JAR
├── src/main/resources/schemas/         # XSD schema definitions
└── src/main/java/                     # Java entity classes

centiloid_container_with_dicom_qc/
├── app/xnat_upload.py                 # XNAT integration module
├── app/pipeline.py                    # Main processing script
└── Dockerfile                         # Container definition

xnat-docker-compose/
├── docker-compose.yml                 # XNAT deployment
├── .env                              # Environment configuration
└── xnat/plugins/                     # Plugin installation directory

data/
└── Elder_subject_florbetapir/        # Test DICOM data
```

## URLs and Access
- **XNAT Web**: http://localhost
- **Default Credentials**: admin/admin (if setup)
- **Plugin REST API**: `/data/projects/{project}/subjects/{subject}/experiments/{session}/assessors/{assessor}`

## Testing Workflow

### XNAT Setup ✓
1. Start XNAT: `cd xnat-docker-compose && docker-compose up -d`
2. Verify XNAT: `curl http://localhost` (returns 302)
3. Install plugin: Copy JAR to plugins/, restart XNAT
4. Plugin successfully creates database schema

### Container Testing ✓
```bash
# Build using proper build script
./build.sh v1.1.2

# Successful test run with embedded test data
docker run --rm \
  -v /Users/james/projects/centiloid_container_with_dicom_qc/test_data:/input:ro \
  -v /tmp/centiloid_test_output:/output \
  xnatworks/xnat_centiloid_container:v1.1.2 \
  --pet-nifti /input/pet.nii.gz \
  --template /maskdata/template_space.nii.gz \
  --target-mask /maskdata/centiloid_ctx_mask.nii.gz \
  --ref-mask /maskdata/whole_cerebellum_mask.nii.gz \
  --tracer FBP \
  --mode amyloid \
  --out-dir /output \
  --skip-xnat-upload
```

### Test Results ✓
- **SUVR**: 2.073 (global cortex over reference)
- **Centiloid value**: 212.18 CL
- **Target mean**: 1496.86
- **Reference mean**: 722.01
- **Processing**: Successful registration and quantification
- **DICOM outputs**: QC overlay and PDF report created

### XNAT Integration Status 🔧 DEBUGGING COMPLETED

#### Upload Issue Root Cause ✅
**Problem**: Container showed "XNAT upload module not available" error  
**Root Cause**: Import path bug in pipeline.py line 665  
**Original**: `from xnat_upload import upload_to_xnat`  
**Fixed**: `from app.xnat_upload import upload_to_xnat`  

#### Container Upload Fix ✅
- **Fixed import bug**: `from xnat_upload import upload_to_xnat` → `from app.xnat_upload import upload_to_xnat`
- **Fixed SimpleITK casting**: Added vector image handling for registration
- **Built with build.sh**: `xnatworks/xnat_centiloid_container:v1.1.2`  
- **Updated latest tag**: Points to v1.1.2 on Docker Hub
- **Build script**: Created `build.sh` for proper container versioning
- **Import test**: ✅ `from app.xnat_upload import upload_to_xnat` works
- **Processing test**: ✅ Container processes data correctly (212.18 CL)

#### XNAT Stability Issues 🔄
- XNAT keeps crashing with exit code 137 (memory killed)
- Reduced heap from 2g → 1500m to prevent crashes
- Long startup times (2-3 minutes) due to plugin schema creation
- Network connectivity confirmed via Docker Compose network

#### Next Steps for Upload Testing
1. Wait for XNAT to fully initialize (may take 3+ minutes)
2. Test fixed container (v1.1.1) with XNAT upload parameters  
3. Fix SimpleITK casting error that appeared in v1.1.1
4. Verify end-to-end assessment creation in XNAT database

#### Technical Findings
- **Container processing**: ✅ Works perfectly (212.18 CL results)
- **XNAT plugin**: ✅ Database schema created successfully  
- **Network**: ✅ Container can reach XNAT via Docker network
- **Upload module**: ✅ Import issue identified and fixed
- **Database storage**: ❓ Pending testing once XNAT stabilizes

## Completed Integration
✅ XNAT datatype plugin functional  
✅ Container processing pipeline working  
✅ Database schema automatically created  
✅ Embedded templates and masks working  
✅ Full Centiloid quantification pipeline  
✅ DICOM QC outputs generated