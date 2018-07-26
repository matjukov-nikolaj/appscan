package com.capitalone.dashboard.collector;

import com.capitalone.dashboard.model.AppScanProject;
import com.capitalone.dashboard.model.AppScan;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;
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
        StringBuilder name = new StringBuilder(project.getProjectName());
        name.replace(4, name.length(), "");
        assertEquals(name.toString(), NAME);
    }

    @Test
    public void canGetCurrentAppScanMetrics() {
        appScanClient.parseDocument(settings.getServer());
        AppScanProject project = appScanClient.getProject();
        AppScan appScan = appScanClient.getCurrentMetrics(project);
        Map<String, String> metrics = appScan.getMetrics();
        assertEquals("10", metrics.get(LOW));
        assertEquals("0", metrics.get(MEDIUM));
        assertEquals("0", metrics.get(HIGH));
        assertEquals("19", metrics.get(TOTAL));
        assertEquals("9", metrics.get(INFORMATIONAL));
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

