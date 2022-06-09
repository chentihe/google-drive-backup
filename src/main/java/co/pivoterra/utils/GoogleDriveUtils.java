package co.pivoterra.utils;

import co.pivoterra.pojos.GoogleDriveConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public class GoogleDriveUtils {
    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));
    private static final java.io.File googleDriveConfigFile = new java.io.File(GoogleConstants.GOOGLE_DRIVE_CONFIG_YML);

    public final static FileList fetchFileList(Drive service, String fields, String query, String backupDate, String pageToken) throws IOException {
        return service.files().list()
                .setFields(fields)
                .setQ(String.format(query, backupDate))
                .setSupportsAllDrives(true)
                .setPageToken(pageToken)
                .execute();
    }

    public final static File fetchFile(Drive service, String fileId, String fields) throws IOException {
        return service.files().get(fileId).setFields(fields).execute();
    }

    public final static void updateBackupDateTime() throws IOException {
        getGoogleDriveConfig().setLastBackupDate(ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT));
        mapper.writeValue(googleDriveConfigFile, getGoogleDriveConfig());
    }

    public final static Path getFilePath(Drive service, File file) throws IOException {
        if (Objects.isNull(file.getParents())) {
            return Path.of(getGoogleDriveConfig().getRootFolderPath(), file.getName().replace(java.io.File.separator, "_"));
        }
        List<String> parentIds = file.getParents();
        final StringBuffer sb = new StringBuffer().append(java.io.File.separator).append(file.getName().replace(java.io.File.separator, "_"));

        do {
            String parentId = parentIds.stream().findFirst().get();
            File parentFolder = fetchFile(service, parentId, getGoogleDriveConfig().getFields().get(GoogleConstants.FILE));
            sb.insert(0, parentFolder.getName()).insert(0, java.io.File.separator);
            parentIds = parentFolder.getParents();
        } while (Objects.nonNull(parentIds));
        return Path.of(getGoogleDriveConfig().getRootFolderPath(), sb.toString());
    }

    public final static GoogleDriveConfig getGoogleDriveConfig() throws IOException {
        return mapper.readValue(googleDriveConfigFile, GoogleDriveConfig.class);
    }
}
