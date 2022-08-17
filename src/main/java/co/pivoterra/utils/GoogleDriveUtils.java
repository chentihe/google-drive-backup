package co.pivoterra.utils;

import co.pivoterra.pojos.GoogleDriveConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class GoogleDriveUtils {
    /**
     * Mapper to map the yaml file into the pojo.
     */
    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));
    private static final java.io.File GOOGLE_DRIVE_CONFIG_FILE = new java.io.File(GoogleConstants.GOOGLE_DRIVE_CONFIG_YML);

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    /**
     * Global instance of the JSON factory.
     */
    public static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final Logger LOG = Logger.getLogger(GoogleDriveUtils.class);
    private static GoogleDriveConfig config;

    static {
        try {
            config = MAPPER.readValue(GOOGLE_DRIVE_CONFIG_FILE, GoogleDriveConfig.class);
        } catch (IOException e) {
            LOG.warn("[GoogleDriveConfig] Cannot read the yaml file, check if the file exist or not.");
        }
    }

    /**
     * Creates an authorized Credential object.
     *
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    public static HttpCredentialsAdapter getCredentials() throws IOException {
        GoogleCredentials credentials = GoogleCredentials.fromStream(GoogleDriveUtils.class.getResourceAsStream(CREDENTIALS_FILE_PATH))
                .createScoped(SCOPES);
        credentials.refreshIfExpired();
        return new HttpCredentialsAdapter(credentials);
    }

    /**
     * Fetch the file list by calling GET https://www.googleapis.com/drive/v3/files
     *
     * @param service    Google drive service
     * @param fields     Display fields of file while fetching the file from Google
     * @param query      Query parameters
     * @param backupDate Last backup date for update use
     * @param pageToken  Next page token
     * @return File list
     */
    public final static FileList fetchFileList(Drive service, String fields, String query, String backupDate, String pageToken) {
        try {
            return service.files().list()
                    .setFields(fields)
                    .setQ(String.format(query, backupDate))
                    .setSupportsAllDrives(true)
                    .setPageToken(pageToken)
                    .execute();
        } catch (IOException e) {
            LOG.warn("[fetchFileList] Cannot fetch the file list from google");
        }
        return null;
    }

    /**
     * Fetch the specific file by calling GET https://www.googleapis.com/drive/v3/files/fileId
     *
     * @param service Google drive service
     * @param fileId  File id
     * @param fields  Display fields of file while fetching the file from Google
     * @return Detail of the specific file
     */
    public final static File fetchFile(Drive service, String fileId, String fields) {
        try {
            return service.files().get(fileId).setFields(fields).execute();
        } catch (IOException e) {
            LOG.warn("[fetchFile] Cannot fetch the file from google.");
        }
        return null;
    }

    /**
     * Check the existence of root folder
     * ex. In 2022, all files should be stored into rootFolder/2022
     */
    public final static void checkRootFolder() {
        String year = getGoogleDriveConfig().getLastBackupDate().substring(0, 4);
        Path rootFolderConcatYear = Path.of(getGoogleDriveConfig().getRootFolderPath(), year);
        if (!rootFolderConcatYear.toAbsolutePath().toString()
                .equals(getGoogleDriveConfig().getRootFolderByYear())) {
            try {
                Files.createDirectories(rootFolderConcatYear);
                getGoogleDriveConfig().setRootFolderByYear(rootFolderConcatYear.toAbsolutePath().toString());
                MAPPER.writeValue(GOOGLE_DRIVE_CONFIG_FILE, getGoogleDriveConfig());
            } catch (IOException e) {
                LOG.warn(String.format("Something went wrong while creating root folder: %s", rootFolderConcatYear));
            }
        }
    }

    /**
     * Store the start backup time for temp,
     * will update when the backup process is done
     */
    public final static void updateCurrentBackupTime() {
        config.setCurrentBackupDate(ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT));
        try {
            MAPPER.writeValue(GOOGLE_DRIVE_CONFIG_FILE, getGoogleDriveConfig());
        } catch (IOException e) {
            LOG.warn("[updateBackupTime] Cannot write values into the yaml file.");
        }
    }

    /**
     * Update backup time so that it will only back up files whose
     * modified time is greater than last backup time
     */
    public final static void updateLastBackupTime() {
        config.setLastBackupDate(getGoogleDriveConfig().getCurrentBackupDate());
        try {
            MAPPER.writeValue(GOOGLE_DRIVE_CONFIG_FILE, getGoogleDriveConfig());
        } catch (IOException e) {
            LOG.warn("[updateBackupTime] Cannot write values into the yaml file.");
        }
    }

    /**
     * Get the Absolute File Path to download a file / create a folder
     * into the correct path
     *
     * @param service Google drive service
     * @param file    File is ready to download / folder is ready to create
     * @return Absolute file path for download / creation
     */
    public final static Path getFilePath(Drive service, File file) {
        if (Objects.isNull(file.getParents())) {
            return Path.of(getGoogleDriveConfig().getRootFolderByYear(), file.getName().replace(java.io.File.separator, "_"));
        }

        List<String> parentIds = file.getParents();
        final StringBuffer sb = new StringBuffer().append(java.io.File.separator)
                .append(file.getName().replace(java.io.File.separator, "_"));

        do {
            String parentId = parentIds.stream().findFirst().get();
            final File parentFolder = fetchFile(service, parentId, getGoogleDriveConfig().getFields().get(GoogleConstants.FILE));
            sb.insert(0, parentFolder.getName()).insert(0, java.io.File.separator);
            parentIds = parentFolder.getParents();
        } while (Objects.nonNull(parentIds));
        return Path.of(getGoogleDriveConfig().getRootFolderByYear(), sb.toString());
    }

    /**
     * Set configurations in yaml file for easy modification
     *
     * @return google drive config
     */
    public final static GoogleDriveConfig getGoogleDriveConfig() {
        return config;
    }

    /**
     * @param duration Duration between backup starting time and ending time
     * @return Formatted datetime string
     */
    public static String durationFormat(Duration duration) {
        long days = duration.toDays();
        duration = duration.minusDays(days);
        long hours = duration.toHours();
        duration = duration.minusHours(hours);
        long minutes = duration.toMinutes();
        duration = duration.minusMinutes(minutes);
        long seconds = duration.getSeconds();
        return (days == 0 ? "" : days + " days ") +
                (hours == 0 ? "" : hours + " hours ") +
                (minutes == 0 ? "" : minutes + " minutes ") +
                (seconds == 0 ? "" : seconds + " seconds");
    }
}
