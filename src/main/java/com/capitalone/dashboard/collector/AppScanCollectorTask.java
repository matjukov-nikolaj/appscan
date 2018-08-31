package com.capitalone.dashboard.collector;

import com.capitalone.dashboard.model.*;
import com.capitalone.dashboard.repository.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class AppScanCollectorTask extends CollectorTask<AppScanCollector> {
    @SuppressWarnings({"PMD.UnusedPrivateField", "unused"})
    private static final Log LOG = LogFactory.getLog(AppScanCollectorTask.class);

    private final AppScanCollectorRepository appScanCollectorRepository;
    private final AppScanProjectRepository appScanProjectRepository;
    private final DefaultAppScanClient appScanClient;
    private final AppScanSettings appScanSettings;
    private final AppScanCollectorController collectorController;

    @Autowired
    public AppScanCollectorTask(TaskScheduler taskScheduler,
                                AppScanCollectorRepository appScanCollectorRepository,
                                AppScanProjectRepository appScanProjectRepository,
                                AppScanRepository appScanRepository,
                                DefaultAppScanClient appScanClient,
                                AppScanSettings appScanSettings) {
        super(taskScheduler, "AppScan");
        this.appScanCollectorRepository = appScanCollectorRepository;
        this.appScanProjectRepository = appScanProjectRepository;
        this.appScanClient = appScanClient;
        this.appScanSettings = appScanSettings;

        this.collectorController = new AppScanCollectorController(appScanProjectRepository, appScanRepository, appScanClient);
    }

    @Override
    public AppScanCollector getCollector() {
        return AppScanCollector.prototype(appScanSettings.getServer());
    }

    @Override
    public BaseCollectorRepository<AppScanCollector> getCollectorRepository() {
        return appScanCollectorRepository;
    }

    @Override

    public String getCron() {
        return appScanSettings.getCron();
    }

    @Override
    public void collect(AppScanCollector collector) {
        if (collector.getAppScanServer().isEmpty()) {
            return;
        }
        Set<ObjectId> udId = new HashSet<>();
        udId.add(collector.getId());
        List<AppScanProject> existingProjects = appScanProjectRepository.findByCollectorIdIn(udId);
        String instanceUrl = collector.getAppScanServer();
        appScanClient.parseDocument(instanceUrl);
        AppScanProject project = appScanClient.getProject();
        logBanner("Fetched project: " + project.getProjectName() + ":" + project.getProjectDate());
        if (this.collectorController.isNewProject(project, existingProjects)) {
            this.collectorController.addNewProject(project, collector, existingProjects);
        }
    }

}
