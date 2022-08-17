package co.pivoterra.pojos;

import java.util.Map;

public class GoogleDriveConfig {
    private String lastBackupDate;
    private String currentBackupDate;
    private String rootFolderPath;
    private String rootFolderByYear;
    private String applicationName;
    private Map<String, GoogleMimeType> googleMimeTypes;
    private Map<String, String> fields;
    private Map<String, String> query;

    public String getLastBackupDate() {
        return lastBackupDate;
    }

    public void setLastBackupDate(String lastBackupDate) {
        this.lastBackupDate = lastBackupDate;
    }

    public String getCurrentBackupDate() {
        return currentBackupDate;
    }

    public void setCurrentBackupDate(String currentBackupDate) {
        this.currentBackupDate = currentBackupDate;
    }

    public String getRootFolderPath() {
        return rootFolderPath;
    }

    public void setRootFolderPath(String rootFolderPath) {
        this.rootFolderPath = rootFolderPath;
    }

    public String getRootFolderByYear() {
        return rootFolderByYear;
    }

    public void setRootFolderByYear(String rootFolderByYear) {
        this.rootFolderByYear = rootFolderByYear;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public Map<String, GoogleMimeType> getGoogleMimeTypes() {
        return googleMimeTypes;
    }

    public void setGoogleMimeTypes(Map<String, GoogleMimeType> googleMimeTypes) {
        this.googleMimeTypes = googleMimeTypes;
    }

    public Map<String, String> getFields() {
        return fields;
    }

    public void setFields(Map<String, String> fields) {
        this.fields = fields;
    }

    public Map<String, String> getQuery() {
        return query;
    }

    public void setQuery(Map<String, String> query) {
        this.query = query;
    }
}
