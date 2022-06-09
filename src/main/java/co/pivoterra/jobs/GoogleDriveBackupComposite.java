package co.pivoterra.jobs;

import com.google.api.services.drive.Drive;

import java.io.IOException;

public interface GoogleDriveBackupComposite {
    /**
     * backup logic of file and folder
     *
     * @param service Google Drive Service
     * @throws IOException
     */
    void backup(Drive service) throws IOException;
}
