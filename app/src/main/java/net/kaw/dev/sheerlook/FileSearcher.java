package net.kaw.dev.sheerlook;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileSearcher {
    public static List<FileMatchResult> search(File dir, String prompt) {
        List<FileMatchResult> results = new ArrayList<>();
        searchRecursive(dir, prompt, results);

        results.sort((a, b)
                -> Double.compare(b.getMatchPercentage(), a.getMatchPercentage()));
        return results;
    }

    private static void searchRecursive(File dir, String prompt, List<FileMatchResult> results) {
        if (dir == null || !dir.isDirectory()) {
            return;
        }

        File[] files = dir.listFiles();

        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                searchRecursive(file, prompt, results);
                continue;
            }

            String extension = FilenameUtils.getExtension(file.getName());

            switch (extension) {
                case "txt":
                case "pdf":
                    // TODO: Call AI here to get FileMatchResult
                    results.add(new FileMatchResult(file, 100));
            }
        }
    }
}
