package co.pivoterra.jobs.impl;

import co.pivoterra.jobs.GoogleDriveBackupComposite;
import co.pivoterra.utils.GoogleDriveUtils;
import com.google.api.services.drive.Drive;

import java.io.IOException;

public class DefaultGoogleDriveBackupComposite implements GoogleDriveBackupComposite {
    private final GoogleDriveBackupComposite[] backupTypes;

    public DefaultGoogleDriveBackupComposite(GoogleDriveBackupComposite... backupTypes) {
        this.backupTypes = backupTypes;
    }

    @Override
    public void backup(Drive service) throws IOException {
        for (GoogleDriveBackupComposite backupType : backupTypes) {
            backupType.backup(service);
        }
        GoogleDriveUtils.updateBackupTime();
    }
}