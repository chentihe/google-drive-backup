package co.pivoterra.pojos;

import java.util.List;
import java.util.Map;

public class GoogleDrive {
    private String lastBackupDate;
    private String rootFolderPath;
    private List<GoogleMimeType> googleMimeTypes;
    private Map<String, String> fields;
    private Map<String, String> query;

    public String getLastBackupDate() {
        return lastBackupDate;
    }

    public void setLastBackupDate(String lastBackupDate) {
        this.lastBackupDate = lastBackupDate;
    }

    public String getRootFolderPath() {
        return rootFolderPath;
    }

    public void setRootFolderPath(String rootFolderPath) {
        this.rootFolderPath = rootFolderPath;
    }

    public List<GoogleMimeType> getGoogleMimeTypes() {
        return googleMimeTypes;
    }

    public void setGoogleMimeTypes(List<GoogleMimeType> googleMimeTypes) {
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
