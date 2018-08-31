package com.capitalone.dashboard.model;

import codesecurity.collectors.model.CodeSecurityProject;

public class AppScanProject extends CodeSecurityProject {

    public static final String PROJECT_TIMESTAMP = "projectTimestamp";

    public Long getProjectTimestamp() { return (Long) getOptions().get(PROJECT_TIMESTAMP); }

    public void setProjectTimestamp(long timestamp) { getOptions().put(PROJECT_TIMESTAMP, timestamp); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AppScanProject that = (AppScanProject) o;
        return getInstanceUrl().equals(that.getInstanceUrl())
                && getProjectName().equals(that.getProjectName())
                && getProjectDate().equals(that.getProjectDate())
                && getProjectTimestamp().equals(that.getProjectTimestamp());
    }

    @Override
    public int hashCode() {
        int result = getInstanceUrl().hashCode();
        result = 31 * result + Long.toString(getProjectTimestamp()).hashCode();
        return result;
    }

}
