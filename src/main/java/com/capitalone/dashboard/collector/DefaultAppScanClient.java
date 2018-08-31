package com.capitalone.dashboard.collector;

import com.capitalone.dashboard.model.AppScanProject;
import com.capitalone.dashboard.model.AppScan;
import codesecurity.collectors.collector.DefaultCodeSecurityClient;
import codesecurity.config.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.*;
import java.util.*;

@Component("DefaultAppScanClient")
public class DefaultAppScanClient extends DefaultCodeSecurityClient<AppScan, AppScanProject> {
    private static final String XML_REPORT = "xml-report";
    private static final String DATE_FORMAT = "M/dd/yyyy HH:mm:ss a";
    private static final String PROJECT_NAME = "name";
    private static final String ISSUES_COUNT = "issues-count";
    private static final String SCAN_DATE = "scan-date-and-time";

    private AppScan appScan;
    private AppScanProject project;
    private Map<String, Integer> metrics = new HashMap<>();
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

    public void setSettings(AppScanSettings settings) {
        this.settings = settings;
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
        parseProject(document);
        parseMetrics(document);
        setAppScanMetrics();
    }

    protected void initializationFields() {
        this.project = new AppScanProject();
        this.appScan = new AppScan();
        this.metrics.put(Constants.AppScan.INFORMATIONAL, 0);
        this.metrics.put(Constants.AppScan.LOW, 0);
        this.metrics.put(Constants.AppScan.MEDIUM, 0);
        this.metrics.put(Constants.AppScan.HIGH, 0);
        this.metrics.put(Constants.AppScan.TOTAL, 0);
    }

    private void setAppScanMetrics() {
        this.appScan.setName(project.getProjectName());
        this.appScan.setMetrics(this.metrics);
        this.appScan.setUrl(project.getInstanceUrl());
        this.appScan.setTimestamp(project.getProjectTimestamp());
    }

    private void parseProject(Document document) {
        NodeList xmlReportTag = document.getElementsByTagName(XML_REPORT);
        String name = getValueOfNodeAttribute(xmlReportTag.item(0), PROJECT_NAME);
        String scanDate = getScanDate(document);
        this.project.setProjectName(name);
        this.project.setProjectTimestamp(getTimeStamp(scanDate));
        this.project.setProjectDate(getProjectDate(scanDate));
    }

    private void parseMetrics(Document document) {
        NodeList nodesWithIssues = document.getElementsByTagName(ISSUES_COUNT);
        for (int i = 0; i < nodesWithIssues.getLength(); ++i) {
            Node node = nodesWithIssues.item(i);
            NamedNodeMap attributes = node.getAttributes();
            parseNodeAttributes(attributes);
        }
    }

    private void parseNodeAttributes(NamedNodeMap attributes) {
        for (int j = 0; j < attributes.getLength(); ++j) {
            Node attribute = attributes.item(j);
            String name = attribute.getNodeName();
            int value = Integer.parseInt(attribute.getNodeValue());
            updateScanRiskLevel(name, value);
        }
    }

    private void updateScanRiskLevel(String name, int value) {
        switch (name) {
            case Constants.AppScan.HIGH:
                incrementMetric(Constants.AppScan.HIGH, value);
                break;
            case Constants.AppScan.MEDIUM:
                incrementMetric(Constants.AppScan.MEDIUM, value);
                break;
            case Constants.AppScan.LOW:
                incrementMetric(Constants.AppScan.LOW, value);
                break;
            case Constants.AppScan.INFORMATIONAL:
                incrementMetric(Constants.AppScan.INFORMATIONAL, value);
                break;
            case Constants.AppScan.TOTAL:
                incrementMetric(Constants.AppScan.TOTAL, value);
                break;
            default:
                break;
        }
    }

    private void incrementMetric(String name, int value) {
        this.metrics.put(name, this.metrics.get(name) + value);
    }

    public String getScanDate(Document document) {
        NodeList tagsWithDate = document.getElementsByTagName(SCAN_DATE);
        Node nodeWithDate = tagsWithDate.item(0);
        return nodeWithDate.getFirstChild().getNodeValue();
    }

    private String getValueOfNodeAttribute(Node tag, String itemName) {
        return tag.getAttributes().getNamedItem(itemName).getNodeValue();
    }
}
