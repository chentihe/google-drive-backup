package co.pivoterra;

import co.pivoterra.jobs.impl.DefaultGoogleDriveBackupComposite;
import co.pivoterra.jobs.impl.GoogleDriveCreateFoldersJob;
import co.pivoterra.jobs.impl.GoogleDriveDownloadFilesJob;
import co.pivoterra.utils.GoogleDriveUtils;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.drive.Drive;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Objects;

public class DriveBackup {
    public static void main(String... args) throws IOException, GeneralSecurityException {
        if (Objects.nonNull(GoogleDriveUtils.getGoogleDriveConfig())) {
            // Build a new authorized API client service.
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            final Drive service = new Drive.Builder(HTTP_TRANSPORT, GoogleDriveUtils.JSON_FACTORY, GoogleDriveUtils.getCredentials())
                    .setApplicationName(GoogleDriveUtils.getGoogleDriveConfig().getApplicationName())
                    .build();

            new DefaultGoogleDriveBackupComposite(new GoogleDriveCreateFoldersJob(GoogleDriveUtils.getGoogleDriveConfig()),
                    new GoogleDriveDownloadFilesJob(GoogleDriveUtils.getGoogleDriveConfig())).backup(service);
        }
    }
}