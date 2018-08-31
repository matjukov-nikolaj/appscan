package com.capitalone.dashboard.collector;

import com.capitalone.dashboard.model.AppScanProject;
import com.capitalone.dashboard.model.AppScan;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import codesecurity.config.Constants;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class DefaultAppScanClientTest {
    @Mock
    private AppScanSettings settings;
    private DefaultAppScanClient appScanClient;

    private static final String SERVER = "appscan-tests/test.xml";
    private static final String FAIL_SERVER = "appscan-tests/fail-test.xml";
    private static final String CRON = "0 0/1 * * * *";

    private static final String INFORMATIONAL = "TotalInformationalIssues";
    private static final String LOW = "TotalLowSeverityIssues";
    private static final String MEDIUM = "TotalMediumSeverityIssues";
    private static final String HIGH = "TotalHighSeverityIssues";
    private static final String TOTAL = "Total";
    private static final String NAME = "test";
    private static final String DATE = "2018-8-25";
    private static final Long TIMESTAMP = (Long) 1535185027000L;

    @Before
    public void init() {
        settings = new AppScanSettings();
        settings.setCron(CRON);
        settings.setServer(getUrlToTestFile(SERVER));
        settings.setPassword("");
        settings.setUsername("");

        appScanClient = new DefaultAppScanClient(settings);
    }

    @Test
    public void canGetProjects() {
        appScanClient.parseDocument(settings.getServer());
        AppScanProject project = appScanClient.getProject();
        assertEquals(project.getProjectName(), NAME);
        assertEquals(project.getProjectTimestamp(), TIMESTAMP);
        assertEquals(project.getProjectDate(), DATE);
    }

    @Test
    public void canGetCurrentAppScanMetrics() {
        appScanClient.parseDocument(settings.getServer());
        AppScanProject project = appScanClient.getProject();
        AppScan appScan = appScanClient.getCurrentMetrics(project);
        Map<String, Integer> metrics = appScan.getMetrics();
        assertThat(metrics.get(Constants.AppScan.LOW)).isEqualTo(1);
        assertThat(metrics.get(Constants.AppScan.MEDIUM)).isEqualTo(1);
        assertThat(metrics.get(Constants.AppScan.HIGH)).isEqualTo(1);
        assertThat(metrics.get(Constants.AppScan.INFORMATIONAL)).isEqualTo(1);
        assertThat(metrics.get(Constants.AppScan.TOTAL)).isEqualTo(4);
    }

    @Test
    public void throwNullPointerExceptionWhenCanNotGetAAppScanReport() {
        appScanClient.parseDocument(getUrlToTestFile(FAIL_SERVER));
    }

    private String getUrlToTestFile(String server) {
        ClassLoader classLoader = getClass().getClassLoader();
        return classLoader.getResource(server).toString();
    }
}

