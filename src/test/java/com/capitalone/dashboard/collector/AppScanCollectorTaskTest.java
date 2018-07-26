package com.capitalone.dashboard.collector;

import com.capitalone.dashboard.model.*;
import com.capitalone.dashboard.repository.*;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.scheduling.TaskScheduler;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.util.ArrayList;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AppScanCollectorTaskTest{

    private AppScanCollectorTask task;

    private TaskScheduler mockScheduler;
    private AppScanCollectorRepository mockCollectorRepository;
    private AppScanProjectRepository mockProjectRepository;
    private AppScanRepository mockRepository;
    private ComponentRepository mockComponentRepository;
    private DefaultAppScanClient client;

    private static final String SERVER = "appscan-tests/test.xml";
    private static final String CRON = "0 0/1 * * * *";
    private static final String NAME = "test";

    @Before
    public void setup() {
        mockScheduler = mock(TaskScheduler.class);
        mockCollectorRepository = mock(AppScanCollectorRepository.class);
        mockProjectRepository = mock(AppScanProjectRepository.class);
        mockRepository = mock(AppScanRepository.class);
        mockComponentRepository = mock(ComponentRepository.class);

        AppScanSettings settings = new AppScanSettings();
        settings.setCron(CRON);
        settings.setServer(getUrlToTestFile(SERVER));
        settings.setUsername("");
        settings.setPassword("");

        client = new DefaultAppScanClient(settings);
        this.task = new AppScanCollectorTask(mockScheduler, mockCollectorRepository, mockProjectRepository,
                mockRepository, client, settings, mockComponentRepository);
    }

    @Test
    public void getCollectorReturnsAppScanCollector() {
        final AppScanCollector collector = task.getCollector();

        assertThat(collector).isNotNull().isInstanceOf(AppScanCollector.class);
        assertThat(collector.isEnabled()).isTrue();
        assertThat(collector.isOnline()).isTrue();
        assertThat(collector.getAppScanServer()).contains(getUrlToTestFile(SERVER));
        assertThat(collector.getCollectorType()).isEqualTo(CollectorType.AppScan);
        assertThat(collector.getName()).isEqualTo("AppScan");
        assertThat(collector.getAllFields().get("instanceUrl")).isEqualTo("");
        assertThat(collector.getAllFields().get("projectName")).isEqualTo("");
        assertThat(collector.getAllFields().get("projectTimestamp")).isEqualTo(null);
        assertThat(collector.getUniqueFields().get("instanceUrl")).isEqualTo("");
        assertThat(collector.getUniqueFields().get("projectName")).isEqualTo("");
    }

    @Test
    public void getCollectorRepositoryReturnsTheRepository() {
        assertThat(task.getCollectorRepository()).isNotNull().isSameAs(mockCollectorRepository);
    }

    @Test
    public void getCron() {
        assertThat(task.getCron()).isNotNull().isSameAs(CRON);
    }


    @Test
    public void collectEmpty() {
        when(mockComponentRepository.findAll()).thenReturn(components());
        task.collect(new AppScanCollector());
        verifyZeroInteractions(mockRepository);
    }

    @Test
    public void collectWithServer() {
        when(mockComponentRepository.findAll()).thenReturn(components());
        AppScanCollector collector = collectorWithServer();
        task.collect(collector);
        AppScanProject project = client.getProject();
        StringBuilder name = new StringBuilder(project.getProjectName());
        name.replace(4, name.length(), "");
        assertEquals(name.toString(), NAME);
        verify(mockProjectRepository).save(project);
        verify(mockProjectRepository).findAppScanProject(collector.getId(), project.getProjectName(), project.getProjectTimestamp());
    }

    private ArrayList<com.capitalone.dashboard.model.Component> components() {
        ArrayList<com.capitalone.dashboard.model.Component> cArray = new ArrayList<>();
        com.capitalone.dashboard.model.Component c = new Component();
        c.setId(new ObjectId());
        c.setName("COMPONENT1");
        c.setOwner("JOHN");
        cArray.add(c);
        return cArray;
    }

    private String getUrlToTestFile(String server) {
        ClassLoader classLoader = getClass().getClassLoader();
        return classLoader.getResource(server).toString();
    }

    private AppScanCollector collectorWithServer() {
        return AppScanCollector.prototype(getUrlToTestFile(SERVER));
    }
}
