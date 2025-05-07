package net.kaw.dev.sheerlook.files;

import androidx.documentfile.provider.DocumentFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FileSearch {
    public static List<DocumentFile> search(DocumentFile dir) {
        List<DocumentFile> files = new ArrayList<>();

        if (!dir.isDirectory()) {
            return files;
        }

        for (DocumentFile file : Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory()) {
                files.addAll(search(file));
            } else if (file.isFile()) {
                files.add(file);
            }
        }

        return files;
    }
}
