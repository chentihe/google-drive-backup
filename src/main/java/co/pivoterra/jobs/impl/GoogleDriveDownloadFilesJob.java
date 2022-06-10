package co.pivoterra.jobs.impl;

import co.pivoterra.jobs.GoogleDriveBackupComposite;
import co.pivoterra.pojos.GoogleDriveConfig;
import co.pivoterra.pojos.GoogleMimeType;
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
    private final GoogleDriveConfig config;

    public GoogleDriveDownloadFilesJob(GoogleDriveConfig config) {
        this.config = config;
    }

    @Override
    public void backup(Drive service) {
        LOG.info("Starting Backup Files");
        String pageToken = null;
        final AtomicInteger downloadFiles = new AtomicInteger();
        final LocalTime startDownload = LocalTime.now();

        do {
            final FileList result = GoogleDriveUtils.fetchFileList(service,
                    config.getFields().get(GoogleConstants.FILES),
                    config.getQuery().get(GoogleConstants.FILE),
                    config.getLastBackupDate(), pageToken);
            if (Objects.nonNull(result)) {
                final List<File> files = result.getFiles();

                if (CollectionUtils.isNotEmpty(files)) {
                    files.parallelStream().forEach(file -> {
                        downloadFile(service, file, file.getMimeType());
                        downloadFiles.getAndIncrement();
                    });
                }
                pageToken = result.getNextPageToken();
            }
        } while (pageToken != null);

        final LocalTime finishDownload = LocalTime.now();
        LOG.info(String.format("Total Backup %d Files \nTime Duration: %s",
                downloadFiles.get(),
                GoogleDriveUtils.durationFormat(Duration.between(startDownload, finishDownload))));
    }

    private void downloadFile(Drive service, File file, String mimeType) {
        FileOutputStream fos = null;
        final Path path = GoogleDriveUtils.getFilePath(service, file);

        if (Objects.nonNull(path)) {
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
            } catch (IOException e) {
                LOG.warn(String.format("[downloadFile] Something went wrong while downloading file: %s", file.getName()));
            } finally {
                try {
                    fos.close();
                } catch (IOException e) {
                    LOG.warn(String.format("[downloadFile] Something went wrong while closing the FileOutputStream of %s.", file.getName()));
                }
            }
        }
    }
}