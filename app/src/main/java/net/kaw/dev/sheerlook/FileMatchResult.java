package net.kaw.dev.sheerlook;

import java.io.File;

public class FileMatchResult {
    private File file;
    private double matchPercentage;
    private String description;

    public FileMatchResult(File file, double matchPercentage) {
        this.file = file;
        this.matchPercentage = matchPercentage;
    }

    public FileMatchResult(File file, double matchPercentage, String description) {
        this.file = file;
        this.matchPercentage = matchPercentage;
        this.description = description;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public double getMatchPercentage() {
        return matchPercentage;
    }

    public void setMatchPercentage(double matchPercentage) {
        this.matchPercentage = matchPercentage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
