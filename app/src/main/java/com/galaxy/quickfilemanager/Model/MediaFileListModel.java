package com.galaxy.quickfilemanager.Model;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by Satish on 29-12-2015.
 */
public class MediaFileListModel implements Parcelable {

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

    private String apkType;

    private Cursor audioCursor;
    private long audioLink;

    public MediaFileListModel() {
    }


    protected MediaFileListModel(Parcel in) {
        isDirectory = in.readByte() != 0;
        isHidden = in.readByte() != 0;
        fileListCount = in.readInt();
        fileName = in.readString();
        filePath = in.readString();
        fileSize = in.readString();
        fileCreatedTime = in.readString();
        mediaBitmap = in.readParcelable(Bitmap.class.getClassLoader());
        fileType = in.readString();
        filePermission = in.readString();
        appPackageName = in.readString();
        appVersionCode = in.readString();
        appVersionName = in.readString();
        apkType = in.readString();
    }

    public static final Creator<MediaFileListModel> CREATOR = new Creator<MediaFileListModel>() {
        @Override
        public MediaFileListModel createFromParcel(Parcel in) {
            return new MediaFileListModel(in);
        }

        @Override
        public MediaFileListModel[] newArray(int size) {
            return new MediaFileListModel[size];
        }
    };

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

    public String getApkType() {
        return apkType;
    }

    public void setApkType(String apkType) {
        this.apkType = apkType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeByte((byte) (isDirectory ? 1 : 0));
        parcel.writeByte((byte) (isHidden ? 1 : 0));
        parcel.writeInt(fileListCount);
        parcel.writeString(fileName);
        parcel.writeString(filePath);
        parcel.writeString(fileSize);
        parcel.writeString(fileCreatedTime);
        parcel.writeParcelable(mediaBitmap, i);
        parcel.writeString(fileType);
        parcel.writeString(filePermission);
        parcel.writeString(appPackageName);
        parcel.writeString(appVersionCode);
        parcel.writeString(appVersionName);
        parcel.writeString(apkType);
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof MediaFileListModel)) {
            return false;
        }
        if (this == obj || (this.fileName.equals(((MediaFileListModel) obj).fileName) && this.filePath.equals(((MediaFileListModel) obj).filePath))) {
            return true;
        }
        return false;
    }

    public Cursor getAudioCursor() {
        return audioCursor;
    }

    public void setAudioCursor(Cursor audioCursor) {
        this.audioCursor = audioCursor;
    }

    public long getAudioLink() {
        return audioLink;
    }

    public void setAudioLink(long audioLink) {
        this.audioLink = audioLink;
    }
}