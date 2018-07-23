package com.capitalone.dashboard.collector;

import com.capitalone.dashboard.model.AppScanProject;
import com.capitalone.dashboard.model.AppScan;
import codesecurity.collector.DefaultCodeSecurityClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.*;

import java.util.*;

@Component("DefaultBlackDuckClient")
public class DefaultAppScanClient extends DefaultCodeSecurityClient<AppScan, AppScanProject> {
    private static final Log LOG = LogFactory.getLog(DefaultAppScanClient.class);

    private static final String INFORMATIONAL = "TotalInformationalIssues";
    private static final String LOW = "TotalLowSeverityIssues";
    private static final String MEDIUM = "TotalMediumSeverityIssues";
    private static final String HIGH = "TotalHighSeverityIssues";
    private static final String TOTAL = "Total";
    private static final String XML_REPORT = "XmlReport ";
    private static final String DATE_FORMAT = "EEE MMM dd HH:mm:ss zzz yyyy";

    //Mon Jul 23 15:18:46 MSK 2018
    private AppScan appScan;
    private AppScanProject project;
    private Map<String, String> metrics = new HashMap<>();
    private AppScanSettings settings;

    @Autowired
    public DefaultAppScanClient(AppScanSettings settings) {
        this.settings = settings;
    }

    @Override
    public AppScanProject getProject() { return this.project; }

    @Override
    public AppScan getCurrentMetrics(AppScanProject project) {
        return this.appScan;
    }

    protected void setInstanceUrlInProject(String instanceUrl) {
        this.project.setInstanceUrl(instanceUrl);
    }

    protected String getDateFormat() {
        return DATE_FORMAT;
    }

    protected String getUsernameFromSettings() {
        return this.settings.getUsername();
    }

    protected String getPasswordFromSettings() {
        return this.settings.getPassword();
    }

    protected void parseCodeSecurityDocument(Document document) {
        try {
        } catch (Exception e) {
            LOG.error(e);
        }
    }

    protected void initializationFields() {
        this.project = new AppScanProject();
        this.appScan = new AppScan();
        this.metrics.put(INFORMATIONAL, "");
        this.metrics.put(LOW, "");
        this.metrics.put(MEDIUM, "");
        this.metrics.put(HIGH, "");
        this.metrics.put(TOTAL, "");
    }

    private void setBlackDuckMetrics() {
        this.appScan.setName(project.getProjectName());
        this.appScan.setMetrics(this.metrics);
        this.appScan.setUrl(project.getInstanceUrl());
        this.appScan.setTimestamp(Long.parseLong(project.getProjectTimestamp(), 10));
    }

    public void setSettings(AppScanSettings settings) {
        this.settings = settings;
    }

}
