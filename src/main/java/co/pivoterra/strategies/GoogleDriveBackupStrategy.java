package co.pivoterra.strategies;

import com.google.api.services.drive.Drive;

import java.io.IOException;

public interface GoogleDriveBackupStrategy {
    void execute(Drive service) throws IOException;
}
