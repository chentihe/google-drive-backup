package co.pivoterra.utils;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import javafx.util.Pair;
import org.apache.commons.collections4.CollectionUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoogleMimeTypeHandler {

    public static final String GOOGLE_FOLDER = "application/vnd.google-apps.folder";
    private static final String GOOGLE_DRIVE_ID = "0AMAIQ3a4qcVsUk9PVA";
    private static final java.io.File ROOT_FOLDER_PATH = new java.io.File("/home/tihe/Desktop/test");
    // todo: find another solution to store googleMimeTypes
    private final Map<String, Pair<String, String>> googleMimeTypes = new HashMap<>();

    // google mimetype needs to invoke service.files().export()
    public GoogleMimeTypeHandler() {
        googleMimeTypes.put("application/vnd.google-apps.document", new Pair<>("application/vnd.openxmlformats-officedocument.wordprocessingml.document", ".docx"));
        googleMimeTypes.put("application/vnd.google-apps.presentation", new Pair<>("application/vnd.openxmlformats-officedocument.presentationml.presentation", ".pptx"));
        googleMimeTypes.put("application/vnd.google-apps.spreadsheet", new Pair<>("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ".xlsx"));
    }


    public List<String> createFirstLayerFolders(Drive service) throws IOException {
        List<String> folderIds = new ArrayList<>();

        String pageToken = null;
        do {
            FileList result = service.files().list()
                    .setFields("nextPageToken, files(id, name, parents)")
                    .setQ(String.format("mimeType = 'application/vnd.google-apps.folder' and '%s' in parents", GOOGLE_DRIVE_ID))
                    .setSupportsAllDrives(true)
                    .setPageToken(pageToken)
                    .execute();
            List<File> files = result.getFiles();
            if (CollectionUtils.isNotEmpty(files)) {
                for (File file : files) {
                    createFolder(file.getName());
                    folderIds.add(file.getId());
                }
            }
            pageToken = result.getNextPageToken();
        } while (pageToken != null);
        return folderIds;
    }

    public void createFoldersOrDownloadFiles(Drive service, List<String> folderIds) throws IOException {
        List<String> newFolderIds = new ArrayList<>();
        String pageToken = null;
        for (String folderId : folderIds) {
            do {
                FileList result = service.files().list()
                        .setFields("nextPageToken, files(id, name, mimeType, parents)")
                        .setQ(String.format("'%s' in parents", folderId))
                        .setSupportsAllDrives(true)
                        .setPageToken(pageToken)
                        .execute();
                List<File> files = result.getFiles();
                if (CollectionUtils.isNotEmpty(files)) {
                    for (File file : files) {
                        if (GOOGLE_FOLDER.equals(file.getMimeType())) {
                            createFolder(String.format("%s/%s", getParentFolder(service, folderId), file.getName()));
                            newFolderIds.add(file.getId());
                        } else {
                            downloadFile(service, file, file.getMimeType(), getParentFolder(service, folderId));
                        }
                    }
                }
                pageToken = result.getNextPageToken();
            } while (pageToken != null);
        }
        createFoldersOrDownloadFiles(service, newFolderIds);
    }

    private void downloadFile(Drive service, File file, String mimeType, String parentFolder) throws IOException {
        // todo: file path needs to move to properties or yaml
        FileOutputStream fos = null;
        Path path = Path.of(findFolder(ROOT_FOLDER_PATH, parentFolder), file.getName());
        try {
            if (googleMimeTypes.entrySet().stream().anyMatch(googleMimeType -> googleMimeType.getKey().equals(mimeType))) {
                fos = new FileOutputStream(path.toAbsolutePath() + googleMimeTypes.get(mimeType).getValue());
                service.files().export(file.getId(), googleMimeTypes.get(mimeType).getKey())
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

    private void createFolder(String folder) throws IOException {
        Path path = Path.of(ROOT_FOLDER_PATH.getAbsolutePath(), folder);
        if (!Files.exists(path)) {
            Files.createDirectory(path);
        }
    }

    private String findFolder(java.io.File root, String folder) {
        if (root.getName().equals(folder)) {
            return root.getAbsolutePath();
        }
        java.io.File[] files = root.listFiles();
        if (files != null) {
            for (java.io.File file : files) {
                if (file.isDirectory()) {
                    String dir = findFolder(file, folder);
                    if (dir != null) {
                        return dir;
                    }
                }
            }
        }
        return null;
    }

    private String getParentFolder(Drive service, String folderId) throws IOException {
        return service.files().get(folderId).execute().getName();
    }
}
