package co.pivoterra.jobs.impl;

import co.pivoterra.jobs.GoogleDriveBackupComposite;
import co.pivoterra.pojos.GoogleDriveConfig;
import co.pivoterra.utils.GoogleConstants;
import co.pivoterra.utils.GoogleDriveUtils;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class GoogleDriveCreateFoldersJob implements GoogleDriveBackupComposite {
    private final Logger LOG = Logger.getLogger(GoogleDriveCreateFoldersJob.class);
    private final GoogleDriveConfig config;

    public GoogleDriveCreateFoldersJob(GoogleDriveConfig config) {
        this.config = config;
    }

    @Override
    public void backup(Drive service) {
        LOG.info("Starting Backup Folders");
        String pageToken = null;
        final AtomicInteger createdFolders = new AtomicInteger();
        final LocalTime startDownload = LocalTime.now();

        do {
            final FileList result = GoogleDriveUtils.fetchFileList(service,
                    config.getFields().get(GoogleConstants.FILES),
                    config.getQuery().get(GoogleConstants.FOLDER),
                    config.getLastBackupDate(), pageToken);
            if (Objects.nonNull(result)) {
                final List<File> files = result.getFiles();

                if (CollectionUtils.isNotEmpty(files)) {
                    files.parallelStream().forEach(file -> {
                        try {
                            createFolder(service, file);
                        } catch (IOException e) {
                            LOG.warn(String.format("Something went wrong while creating folder: %s", file.getName()));
                        }
                        createdFolders.getAndIncrement();
                    });
                }
                pageToken = result.getNextPageToken();
            }
        } while (pageToken != null);

        final LocalTime finishDownload = LocalTime.now();
        LOG.info(String.format("Total Backup %d Folders \nTime Duration: %s",
                createdFolders.get(),
                GoogleDriveUtils.durationFormat(Duration.between(startDownload, finishDownload))));
    }

    private void createFolder(Drive service, File file) throws IOException {
        final Path path = GoogleDriveUtils.getFilePath(service, file);
        if (Objects.nonNull(path)) {
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        }
    }
}
