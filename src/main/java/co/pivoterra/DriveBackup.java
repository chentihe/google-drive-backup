package co.pivoterra;

import co.pivoterra.jobs.impl.DefaultGoogleDriveBackupComposite;
import co.pivoterra.jobs.impl.GoogleDriveCreateFoldersJob;
import co.pivoterra.jobs.impl.GoogleDriveDownloadFilesJob;
import co.pivoterra.utils.GoogleDriveUtils;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class DriveBackup {
    /**
     * Application name.
     */
    private static final String APPLICATION_NAME = "gleaming-mason-352503";
    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    public static void main(String... args) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, GoogleDriveUtils.getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        new DefaultGoogleDriveBackupComposite(new GoogleDriveCreateFoldersJob(),
                new GoogleDriveDownloadFilesJob()).backup(service);
    }
}