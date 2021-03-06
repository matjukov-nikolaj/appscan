package com.capitalone.dashboard.repository;

import com.capitalone.dashboard.model.AppScanProject;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface AppScanProjectRepository extends BaseCollectorItemRepository<AppScanProject> {

    @Query(value="{ 'collectorId' : ?0, options.projectName : ?1, options.projectTimestamp : ?2}}")
    AppScanProject findAppScanProject(ObjectId collectorId, String projectName, long timestamp);

    @Query(value="{ 'collectorId' : ?0, options.current : ?1}")
    AppScanProject findCurrentProjects(ObjectId collectorId, Boolean current);

    @Query(value="{ 'collectorId' : ?0, options.instanceUrl : ?1, enabled: true}")
    List<AppScanProject> findEnabledProjects(ObjectId collectorId, String instanceUrl);
}
