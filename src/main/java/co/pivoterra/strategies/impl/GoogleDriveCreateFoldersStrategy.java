package co.pivoterra.strategies.impl;

import co.pivoterra.strategies.GoogleDriveBackupStrategy;
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
import java.util.concurrent.atomic.AtomicInteger;

public class GoogleDriveCreateFoldersStrategy implements GoogleDriveBackupStrategy {
    private final Logger LOG = Logger.getLogger(GoogleDriveCreateFoldersStrategy.class);

    @Override
    public void execute(Drive service)  throws IOException {
        String pageToken = null;
        AtomicInteger createdFolders = new AtomicInteger();
        final LocalTime startDownload = LocalTime.now();

        do {
            FileList result = GoogleDriveUtils.fetchFileList(service,
                    GoogleDriveUtils.getGoogleDriveConfig().getFields().get(GoogleConstants.FILES),
                    GoogleDriveUtils.getGoogleDriveConfig().getQuery().get(GoogleConstants.FOLDER),
                    GoogleDriveUtils.getGoogleDriveConfig().getLastBackupDate(), pageToken);
            List<File> files = result.getFiles();

            if (CollectionUtils.isNotEmpty(files)) {
                files.parallelStream().forEach(file -> {
                    try {
                        createFolder(service, file);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    createdFolders.getAndIncrement();
                });
            }
            pageToken = result.getNextPageToken();
        } while (pageToken != null);

        final LocalTime finishDownload = LocalTime.now();
        LOG.info(String.format("Total Backup %d Folders", createdFolders.get()));
        LOG.info(String.format("Time Duration: %d seconds", Duration.between(startDownload, finishDownload).getSeconds()));
    }

    private void createFolder(Drive service, File file) throws IOException {
        Path path = GoogleDriveUtils.getFilePath(service, file);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }
}
