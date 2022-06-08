package co.pivoterra.utils;

import co.pivoterra.strategies.GoogleDriveBackupStrategy;
import com.google.api.services.drive.Drive;

import java.io.IOException;

public class GoogleDriveBackup {
    private final Drive service;

    public GoogleDriveBackup(Drive service) {
        this.service = service;
    }

    public void backup(GoogleDriveBackupStrategy googleDriveBackupStrategy) throws IOException {
        googleDriveBackupStrategy.execute(service);
    }
}