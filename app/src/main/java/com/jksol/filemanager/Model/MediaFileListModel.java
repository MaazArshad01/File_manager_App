package com.jksol.filemanager.Model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by Satish on 29-12-2015.
 */
public class MediaFileListModel{

    private boolean isDirectory;
    private boolean isHidden;
    private int fileListCount;

    private String fileName, filePath,fileSize,fileCreatedTime;
    private Date fileCreatedTimeDate;
    private Bitmap mediaBitmap;
    private String fileType;
    private String filePermission;

    private String appPackageName;
    private String appVersionCode, appVersionName;

    public MediaFileListModel() {
    }


    public String getFileCreatedTime() {
        return fileCreatedTime;
    }

    public void setFileCreatedTime(String fileCreatedTime) {
        this.fileCreatedTime = fileCreatedTime;
    }

    public Date getFileCreatedTimeDatel() {
        return fileCreatedTimeDate;
    }

    public void setFileCreatedTimeDatel(Date fileCreatedTimeDate) {
        this.fileCreatedTimeDate = fileCreatedTimeDate;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
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

    public Bitmap getMediaBitmap() {
        return mediaBitmap;
    }

    public void setMediaBitmap(Bitmap mediaBitmap) {
        this.mediaBitmap = mediaBitmap;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getAppPackageName() {
        return appPackageName;
    }

    public void setAppPackageName(String appPackageName) {
        this.appPackageName = appPackageName;
    }

    public String getAppVersionCode() {
        return appVersionCode;
    }

    public void setAppVersionCode(String appVersionCode) {
        this.appVersionCode = appVersionCode;
    }

    public String getAppVersionName() {
        return appVersionName;
    }

    public void setAppVersionName(String appVersionName) {
        this.appVersionName = appVersionName;
    }

    public String getFilePermission() {
        return filePermission;
    }

    public void setFilePermission(String filePermission) {
        this.filePermission = filePermission;
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

    public int isFileListCount() {
        return fileListCount;
    }

    public void setFileListCount(int fileListCount) {
        this.fileListCount = fileListCount;
    }

    public Date getFileCreatedTimeDate() {
        return fileCreatedTimeDate;
    }

    public void setFileCreatedTimeDate(Date fileCreatedTimeDate) {
        this.fileCreatedTimeDate = fileCreatedTimeDate;
    }
}