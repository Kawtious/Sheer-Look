package net.kaw.dev.sheerlook;

import android.content.Context;
import android.net.Uri;
import android.provider.DocumentsContract;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.Objects;

public class FileUtil {
    public static String getFullPathFromTreeUri(Context context, Uri uri) {
        String docId = DocumentsContract.getTreeDocumentId(uri);
        String[] split = docId.split(":");
        String type = split[0];
        String relativePath = split.length > 1 ? split[1] : "";

        if (!"primary".equalsIgnoreCase(type)) {
            return null;
        }

        return Objects.requireNonNull(context.getExternalFilesDir(null))
                .getAbsolutePath()
                .replaceAll("/Android/data/.+", "")
                + "/"
                + relativePath;
    }

    public static File uriToFile(Context context, Uri uri) {
        String path = FileUtil.getFullPathFromTreeUri(context, uri);

        if (path == null) {
            return null;
        }

        return new File(path);
    }

    public static String getMimeType(String filename) {
        String extension = FilenameUtils.getExtension(filename);

        switch (extension) {
            case "pdf":
                return "application/pdf";
            case "txt":
                return "text/plain";
        }

        return "*/*";
    }
}
