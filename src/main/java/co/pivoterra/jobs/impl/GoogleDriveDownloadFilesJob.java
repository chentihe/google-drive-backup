package co.pivoterra.jobs.impl;

import co.pivoterra.pojos.GoogleMimeType;
import co.pivoterra.jobs.GoogleDriveBackupComposite;
import co.pivoterra.utils.GoogleConstants;
import co.pivoterra.utils.GoogleDriveUtils;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
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

public class GoogleDriveDownloadFilesJob implements GoogleDriveBackupComposite {
    private final Logger LOG = Logger.getLogger(GoogleDriveDownloadFilesJob.class);

    @Override
    public void backup(Drive service) throws IOException {
        LOG.info("Starting Backup Files");
        String pageToken = null;
        AtomicInteger downloadFiles = new AtomicInteger();
        final LocalTime startDownload = LocalTime.now();

        do {
            FileList result = GoogleDriveUtils.fetchFileList(service,
                    GoogleDriveUtils.getGoogleDriveConfig().getFields().get(GoogleConstants.FILES),
                    GoogleDriveUtils.getGoogleDriveConfig().getQuery().get(GoogleConstants.FILE),
                    GoogleDriveUtils.getGoogleDriveConfig().getLastBackupDate(), pageToken);
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
            // google mimetype needs to invoke service.files().export()
            final GoogleMimeType googleMimeType = GoogleDriveUtils.getGoogleDriveConfig().getGoogleMimeTypes().get(mimeType);
            if (Objects.nonNull(googleMimeType)) {
                fos = new FileOutputStream(path.toAbsolutePath() + googleMimeType.getFileExtension());
                service.files().export(file.getId(), googleMimeType.getExportFormat())
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