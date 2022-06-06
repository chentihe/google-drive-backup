package co.pivoterra.utils;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GoogleMimeTypeMapper {

    private final Map<String, String> googleMimeTypes = new HashMap<>();

    public GoogleMimeTypeMapper() {
        googleMimeTypes.put("application/vnd.google-apps.document", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        googleMimeTypes.put("application/vnd.google-apps.presentation", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        googleMimeTypes.put("application/vnd.google-apps.spreadsheet", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    public ByteArrayOutputStream createBaos(Drive service, File file, String mimeType) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (getGoogleMimeTypes().entrySet().stream().anyMatch(googleMimeType -> googleMimeType.getKey().equals(mimeType))) {
            service.files().export(file.getId(), googleMimeTypes.get(mimeType))
                    .executeMediaAndDownloadTo(baos);
        } else {
            service.files().get(file.getId())
                    .executeMediaAndDownloadTo(baos);
        }
        return baos;
    }

    protected Map<String, String> getGoogleMimeTypes() {
        return googleMimeTypes;
    }
}
