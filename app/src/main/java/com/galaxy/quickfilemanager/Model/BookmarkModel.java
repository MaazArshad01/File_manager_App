package com.galaxy.quickfilemanager.Model;

import java.util.Date;

/**
 * Created by Satish on 29-12-2015.
 */
public class BookmarkModel {

    private boolean isDirectory;
    private boolean isHidden;

    private String fileName, filePath, fileSize, fileCreatedTime;
    private String fileType;
    private Date fileCreatedTimeDate;

    public BookmarkModel() {
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean directory) {
        isDirectory = directory;
    }

    public boolean isHidden() {
        return isHidden;
    }

    public void setHidden(boolean hidden) {
        isHidden = hidden;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileCreatedTime() {
        return fileCreatedTime;
    }

    public void setFileCreatedTime(String fileCreatedTime) {
        this.fileCreatedTime = fileCreatedTime;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Date getFileCreatedTimeDate() {
        return fileCreatedTimeDate;
    }

    public void setFileCreatedTimeDate(Date fileCreatedTimeDate) {
        this.fileCreatedTimeDate = fileCreatedTimeDate;
    }
}