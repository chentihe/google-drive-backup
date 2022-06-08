package co.pivoterra.utils;

import co.pivoterra.pojos.GoogleDrive;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import javafx.util.Pair;

import java.io.IOException;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GoogleDriveUtils {
    private static final String GOOGLE_DRIVE_ID = "0AMAIQ3a4qcVsUk9PVA";
    // todo: find another solution to store googleMimeTypes
    private final Map<String, Pair<String, String>> googleMimeTypes = new HashMap<>();

    // google mimetype needs to invoke service.files().export()
    public GoogleDriveUtils() {
        googleMimeTypes.put("application/vnd.google-apps.document", new Pair<>("application/vnd.openxmlformats-officedocument.wordprocessingml.document", ".docx"));
        googleMimeTypes.put("application/vnd.google-apps.presentation", new Pair<>("application/vnd.openxmlformats-officedocument.presentationml.presentation", ".pptx"));
        googleMimeTypes.put("application/vnd.google-apps.spreadsheet", new Pair<>("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ".xlsx"));
    }

    public final static FileList fetchFileList(Drive service, String fields, String query, String backupDate, String pageToken) throws IOException {
        return service.files().list()
                .setFields(fields)
                .setQ(String.format(query, backupDate.toString()))
                .setSupportsAllDrives(true)
                .setPageToken(pageToken)
                .execute();
    }

    public final static File fetchFile(Drive service, String fileId, String fields) throws IOException {
        return service.files().get(fileId).setFields(fields).execute();
    }

    public final static void updateBackupDateTime(ObjectMapper mapper) throws IOException {
        final java.io.File file = new java.io.File(GoogleConstants.GOOGLE_DRIVE_YML);
        final GoogleDrive googleDrive = mapper.readValue(file, GoogleDrive.class);
        googleDrive.setLastBackupDate(ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT));
        mapper.writeValue(file, googleDrive);
    }
    public final static Path getFilePath(Drive service, File file) throws IOException {
        if (Objects.isNull(file.getParents())) {
            return Path.of(GoogleConstants.ROOT_FOLDER_PATH, file.getName().replace("/", "_"));
        }
        List<String> parentIds = file.getParents();
        StringBuffer sb = new StringBuffer().append("/").append(file.getName().replace("/", "_"));
        do {
            String parentId = parentIds.stream().findFirst().get();
            File parentFolder = fetchFile(service, parentId, GoogleConstants.FILE_FIELDS);
            sb.insert(0, parentFolder.getName()).insert(0, "/");
            parentIds = parentFolder.getParents();
        } while (Objects.nonNull(parentIds));
        return Path.of(GoogleConstants.ROOT_FOLDER_PATH, sb.toString());
    }

    public Map<String, Pair<String, String>> getGoogleMimeTypes() {
        return googleMimeTypes;
    }
}
