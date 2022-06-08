package co.pivoterra.utils;

public class GoogleConstants {
    public static final String FILES_FIELDS = "nextPageToken, files(id, name, mimeType, parents, modifiedTime, shortcutDetails)";
    public static final String FILE_FIELDS = "id, name, mimeType, parents, modifiedTime";
    public static final String GET_FOLDER_QUERY = "mimeType='application/vnd.google-apps.folder' and modifiedTime > '%s'";
    public static final String GET_FILE_QUERY = "mimeType!='application/vnd.google-apps.folder' and mimeType!='application/vnd.google-apps.shortcut' and mimeType='application/vnd.google-apps.map' and modifiedTime > '%s'";
    public static final String ROOT_FOLDER_PATH = "/home/tihe/Desktop/test";
    public static final String GOOGLE_DRIVE_YML = "src/main/sources/googledrive.yml";
}
