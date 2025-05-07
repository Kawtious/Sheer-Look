package net.kaw.dev.sheerlook.ai;

import androidx.documentfile.provider.DocumentFile;

public class MatchResult {
    private final DocumentFile file;
    private final double matchPercentage;
    private final String description;

    public MatchResult(DocumentFile file, double matchPercentage, String description) {
        this.file = file;
        this.matchPercentage = matchPercentage;
        this.description = description;
    }

    public DocumentFile getFile() {
        return file;
    }

    public double getMatchPercentage() {
        return matchPercentage;
    }

    public String getDescription() {
        return description;
    }
}
