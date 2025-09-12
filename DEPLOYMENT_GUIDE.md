# XNAT Centiloid Plugin - Deployment Guide

## 🎉 Plugin Built Successfully!

The XNAT Centiloid Plugin has been compiled and packaged. The built JAR file is ready for deployment to your XNAT server.

## 📦 Build Output

**Main Plugin JAR:** `build/libs/xnat-centiloid-plugin-1.0.0.jar`

**Contents:**
- ✅ XML Schema Definition (`centiloid.xsd`)
- ✅ Display Configuration (`centiloid_centiloidAssessmentData_display.xml`)
- ✅ Web Interface Template (`centiloid_assessment_detail.vm`)  
- ✅ Plugin Metadata (`xnat-plugin.properties`)
- ✅ Complete Manifest with plugin information

**File Size:** ~5KB (lightweight schema-only deployment)

## 🚀 Installation Steps

### 1. Copy Plugin to XNAT

Copy the built JAR to your XNAT plugins directory:

```bash
# Copy plugin JAR to XNAT plugins directory
cp build/libs/xnat-centiloid-plugin-1.0.0.jar /path/to/xnat/plugins/

# Example paths:
# Ubuntu/Debian: /var/lib/tomcat9/webapps/xnat/WEB-INF/plugins/
# CentOS/RHEL: /opt/tomcat/webapps/xnat/WEB-INF/plugins/
# Docker: /usr/local/tomcat/webapps/ROOT/WEB-INF/plugins/
```

### 2. Set Permissions

Ensure proper permissions:

```bash
chown tomcat:tomcat /path/to/xnat/plugins/xnat-centiloid-plugin-1.0.0.jar
chmod 644 /path/to/xnat/plugins/xnat-centiloid-plugin-1.0.0.jar
```

### 3. Restart XNAT Server

Restart your XNAT server to load the plugin:

```bash
# Ubuntu/Debian
sudo systemctl restart tomcat9

# CentOS/RHEL  
sudo systemctl restart tomcat

# Docker
docker restart xnat-container
```

### 4. Verify Installation

After XNAT restarts, verify the plugin loaded successfully:

1. **Check XNAT Logs** for plugin loading messages:
   ```bash
   tail -f /path/to/tomcat/logs/catalina.out | grep -i centiloid
   ```

2. **Access XNAT Admin Interface**:
   - Log into XNAT as admin
   - Go to Administer → Plugin Settings
   - Look for "Centiloid Plugin" in the plugin list

3. **Check Schema Registration**:
   - Navigate to Administer → Data Types
   - Look for `centiloid:centiloidAssessmentData` in the list

## 🧪 Testing the Installation

### Method 1: REST API Test

Test the plugin REST endpoints:

```bash
# Test assessment creation endpoint
curl -X POST "http://your-xnat/xapi/centiloid/projects/TEST/sessions/TEST/assessments" \
  -H "Content-Type: application/json" \
  -u username:password \
  -d '{
    "tracer": "FBP",
    "mode": "amyloid", 
    "scaled_value": 63.15,
    "scaled_units": "Centiloid"
  }'
```

### Method 2: Python Test Script

Use the provided test script:

```bash
cd examples/
python test_plugin.py
```

Update the script configuration:
- `xnat_host`: Your XNAT server URL
- `username`: XNAT username
- `password`: XNAT password
- `project_id`: Test project ID
- `session_id`: Test session ID

## 🔄 Container Integration

Once the plugin is installed, update your container service configuration:

### 1. Update Container Image

Build the updated container with XNAT integration:

```bash
cd ../centiloid_container_with_dicom_qc
docker build -t xnatworks/xnat_centiloid_container:v1.1.0 .
```

### 2. Update Container Service

In XNAT Container Service admin interface:

1. Navigate to Administer → Plugin Settings → Container Service
2. Update the centiloid container configuration:
   - **Image**: `xnatworks/xnat_centiloid_container:v1.1.0`
   - **Command**: Updated command line with XNAT parameters (see `command.json`)

### 3. Test Complete Workflow

1. **Launch Container** from XNAT on a test PET scan
2. **Verify Processing** completes successfully  
3. **Check Assessment** is automatically created
4. **Review Results** in XNAT web interface

## 📊 What You'll See in XNAT

After successful processing with XNAT integration:

### New Assessment Type

- **Name**: Centiloid Assessment
- **Type**: `centiloid:centiloidAssessmentData`
- **Location**: Under session → Assessments tab

### Assessment Details

**Quantitative Results:**
- Target Region Mean: 6660.75
- Reference Region Mean: 5060.20  
- Global Cortical SUVR: 1.3163
- **Centiloid Value: 63.15 Centiloid**

**Processing Information:**
- Tracer: FBP
- Mode: amyloid
- Registration: rigid
- Processing Date/Time
- Container Version

**Output Files:**
- QC Overlay Image (PNG)
- QC Report (PDF)
- Results (JSON/CSV)
- DICOM Parametric Maps

## 🐛 Troubleshooting

### Plugin Not Loading

**Check XNAT Logs:**
```bash
grep -i "centiloid\|plugin" /path/to/tomcat/logs/catalina.out
```

**Common Issues:**
- JAR file permissions incorrect
- Plugin directory path wrong
- XNAT version compatibility

### Schema Not Registered

**Symptoms:** 
- Plugin loads but datatype not available
- REST API endpoints return 404

**Solutions:**
- Restart XNAT server completely
- Check plugin metadata in JAR manifest
- Verify schema files are included in JAR

### Assessment Creation Fails

**Check:**
- Project and session exist in XNAT
- User has sufficient permissions
- Plugin REST endpoints are accessible
- Container has network access to XNAT

## 📈 Usage Analytics

Monitor plugin usage through:

1. **XNAT Logs** - Track assessment creation
2. **Database Queries** - Query `xnat_centiloidassessmentdata` table
3. **REST API** - Monitor endpoint usage
4. **Container Logs** - Track upload success/failure rates

## 🔒 Security Considerations

- **Plugin JARs** should have restricted permissions (644)
- **XNAT credentials** in container should use service accounts
- **Network access** between container and XNAT should be secured
- **Data validation** - Plugin validates all input data

## 🚀 Next Steps

1. **Deploy to Production** - Install plugin on production XNAT
2. **Update Containers** - Deploy updated container images  
3. **Train Users** - Document new assessment features
4. **Monitor Usage** - Track adoption and performance
5. **Iterate** - Collect feedback for future improvements

## 📞 Support

For deployment issues:

1. **Check Logs** - XNAT and Tomcat logs provide detailed error info
2. **Verify Prerequisites** - Ensure all requirements are met
3. **Test Components** - Use provided test scripts
4. **Documentation** - Review README and integration guide

---

## ✅ Deployment Checklist

- [ ] Copy JAR to XNAT plugins directory
- [ ] Set proper file permissions  
- [ ] Restart XNAT server
- [ ] Verify plugin loads successfully
- [ ] Test REST API endpoints
- [ ] Update container service configuration
- [ ] Test complete container → XNAT workflow
- [ ] Train users on new features
- [ ] Document deployment for team

**Deployment Date:** ___________  
**Deployed By:** ___________  
**XNAT Version:** ___________  
**Plugin Version:** 1.0.0