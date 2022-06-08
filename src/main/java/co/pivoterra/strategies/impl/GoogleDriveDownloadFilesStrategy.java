package co.pivoterra.strategies.impl;

import co.pivoterra.pojos.GoogleDrive;
import co.pivoterra.strategies.GoogleDriveBackupStrategy;
import co.pivoterra.utils.GoogleConstants;
import co.pivoterra.utils.GoogleDriveUtils;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import javafx.util.Pair;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class GoogleDriveDownloadFilesStrategy implements GoogleDriveBackupStrategy {
    private final Logger LOG = Logger.getLogger(GoogleDriveDownloadFilesStrategy.class);
    private final GoogleDriveUtils googleDriveUtils = new GoogleDriveUtils();

    private final GoogleDrive googleDrive;

    public GoogleDriveDownloadFilesStrategy(GoogleDrive googleDrive) {
        this.googleDrive = googleDrive;
    }

    @Override
    public void execute(Drive service) throws IOException {
        String pageToken = null;
        AtomicInteger downloadFiles = new AtomicInteger();
        final LocalTime startDownload = LocalTime.now();

        do {
            FileList result = GoogleDriveUtils.fetchFileList(service, GoogleConstants.FILES_FIELDS,
                    GoogleConstants.GET_FILE_QUERY, googleDrive.getLastBackupDate(), pageToken);
            List<File> files = result.getFiles();

            if (CollectionUtils.isNotEmpty(files)) {
                files.parallelStream().forEach(file -> {
                    try {
                        downloadFile(service, file, file.getMimeType());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    downloadFiles.getAndIncrement();
                });
            }
            pageToken = result.getNextPageToken();
        } while (pageToken != null);

        final LocalTime finishDownload = LocalTime.now();
        LOG.info(String.format("Total Backup %d Files", downloadFiles.get()));
        LOG.info(String.format("Time Duration: ", Duration.between(startDownload, finishDownload)));
    }

    private void downloadFile(Drive service, File file, String mimeType) throws IOException {
        FileOutputStream fos = null;
        Path path = GoogleDriveUtils.getFilePath(service, file);

        try {
            if (Objects.nonNull(googleDriveUtils.getGoogleMimeTypes().get(mimeType))) {
                final Pair<String, String> googleMimeType = googleDriveUtils.getGoogleMimeTypes().get(mimeType);
                fos = new FileOutputStream(path.toAbsolutePath() + googleMimeType.getValue());
                service.files().export(file.getId(), googleMimeType.getKey())
                        .executeMediaAndDownloadTo(fos);
            } else {
                fos = new FileOutputStream(path.toAbsolutePath().toString());
                service.files().get(file.getId())
                        .executeMediaAndDownloadTo(fos);
            }
        } finally {
            fos.close();
        }
    }
}