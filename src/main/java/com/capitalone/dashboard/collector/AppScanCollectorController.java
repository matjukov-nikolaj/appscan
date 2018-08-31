package com.capitalone.dashboard.collector;

import codesecurity.collectors.collector.CodeSecurityCollectorController;
import com.capitalone.dashboard.model.AppScan;
import com.capitalone.dashboard.model.AppScanProject;
import com.capitalone.dashboard.model.AppScanCollector;
import com.capitalone.dashboard.repository.AppScanProjectRepository;
import com.capitalone.dashboard.repository.AppScanRepository;

public class AppScanCollectorController extends CodeSecurityCollectorController<AppScanCollector, AppScanProject> {

    private AppScanProjectRepository projectRepository;
    private AppScanRepository dataRepository;
    private DefaultAppScanClient client;

    public AppScanCollectorController(AppScanProjectRepository projectRepository,
                                      AppScanRepository dataRepository,
                                      DefaultAppScanClient client) {
        this.projectRepository = projectRepository;
        this.dataRepository = dataRepository;
        this.client = client;
    }

    @Override
    protected void saveProjectToProjectRepository(AppScanProject project) {
        projectRepository.save(project);
    }

    @Override
    protected AppScanProject getAMovedProject(AppScanProject lhs, AppScanProject rhs) {
        lhs.setProjectDate(rhs.getProjectDate());
        lhs.setProjectName(rhs.getProjectName());
        lhs.setInstanceUrl(rhs.getInstanceUrl());
        lhs.setProjectTimestamp(rhs.getProjectTimestamp());
        lhs.setDescription(rhs.getProjectName());
        return lhs;
    }

    @Override
    protected AppScanProject enabledProject(AppScanCollector collector, AppScanProject project) {
        if (collector.getId() == null) {
            return null;
        }
        return projectRepository.findAppScanProject(collector.getId(), project.getProjectName(), project.getProjectTimestamp());
    }

    @Override
    protected void refreshCollectorData(AppScanProject project) {
        AppScan appScan = client.getCurrentMetrics(project);
        if (appScan != null && isNewData(project, appScan)) {
            saveToDataRepository(appScan, project);
        }
    }

    @Override
    protected void refreshCollectorItemId(AppScanProject currentProject, AppScanProject project) {
        AppScan appScan = dataRepository.findByCollectorItemIdAndTimestamp(currentProject.getId(), project.getProjectTimestamp());
        saveToDataRepository(appScan, project);
    }

    @Override
    protected AppScanProject getNewProject() {
        return new AppScanProject();
    }

    @Override
    protected AppScanProject getCurrentProjectFromProjectRepository(AppScanCollector collector) {
        return projectRepository.findCurrentProjects(collector.getId(), true);
    }

    private void saveToDataRepository(AppScan appScan, AppScanProject project) {
        appScan.setCollectorItemId(project.getId());
        dataRepository.save(appScan);
    }

    private boolean isNewData(AppScanProject project, AppScan appScan) {
        return dataRepository.findByCollectorItemIdAndTimestamp(
                project.getId(), appScan.getTimestamp()) == null;
    }
}
