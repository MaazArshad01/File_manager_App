package com.galaxy.quickfilemanager.Utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.os.StatFs;
import android.webkit.MimeTypeMap;

import com.galaxy.quickfilemanager.Model.MediaFileListModel;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Umiya Mataji on 2/15/2017.
 */

public class StorageDetails {

    private final Futils futils;
    public Utils utils;
    Context context;
    HashMap<String, Long> storageSpaces;
    long imageSpace, audioSpace, videoSpace, documentSpace, applicationSpace, otherSpace;
    private LoadCompletedListener loadCompletedListener;
    private LoadCompletedListener internalLoadCompletedListener;
    private LoadCompletedListener externalLoadCompletedListener;
    private String storageType = "";

    public StorageDetails(Context context) {
        this.context = context;
        utils = new Utils(context);
        futils = new Futils();
        storageSpaces = new HashMap<>();
    }

    public boolean isSDCardMounted() {
        String path = utils.StoragePath("ExternalStorage");
        return path.equals("") ? false : true;
    }

    public String getAvailableExternalMemorySize() {
        if (android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED)) {

            long total, aval;
            int kb = 1024;
            File path = new File(utils.StoragePath("ExternalStorage"));
            StatFs fs = new StatFs(path.getPath());

            total = fs.getBlockCount() * (fs.getBlockSize() / kb);
            aval = fs.getAvailableBlocks() * (fs.getBlockSize() / kb);

            return String.format(" %.2f GB / " +
                            "%.2f GB",
                    (double) aval / (kb * kb), (double) total / (kb * kb));

        } else {
            return "0";
        }
    }

    public String ReadableSpace(long size) {
        int kb = 1024;

        return String.format(" %.2f GB",
                (double) size / (kb * kb));
    }

    public String getAvailableInternalMemorySize() {

        long total, aval;
        int kb = 1024;
        // File path = Environment.getDataDirectory();
        File path = new File(utils.StoragePath("InternalStorage"));
        StatFs fs = new StatFs(path.getPath());

        total = fs.getBlockCount() * (fs.getBlockSize() / kb);
        aval = fs.getAvailableBlocks() * (fs.getBlockSize() / kb);

        return String.format(" %.2f GB / " +
                        "%.2f GB",
                (double) aval / (kb * kb), (double) total / (kb * kb));
    }

    public long getInternalSpace(String space) {
        long spaces;
        int kb = 1024;
        // File path = Environment.getDataDirectory();
        File path = new File(utils.StoragePath("InternalStorage"));
        StatFs fs = new StatFs(path.getPath());

        if (space.equalsIgnoreCase("free")) {
            spaces = fs.getAvailableBlocks() * (fs.getBlockSize() / kb);
        } else {
            spaces = fs.getBlockCount() * (fs.getBlockSize() / kb);
        }

        return spaces * 1024;
    }

    public long getExternalSpace(String space) {
        long spaces;
        int kb = 1024;

        File path = new File(utils.StoragePath("ExternalStorage"));
        StatFs fs = new StatFs(path.getPath());

        if (space.equalsIgnoreCase("free"))
            spaces = fs.getAvailableBlocks() * (fs.getBlockSize() / kb);
        else
            spaces = fs.getBlockCount() * (fs.getBlockSize() / kb);

        return spaces * 1024;
    }

    /*public void setLoadCompletedListener(LoadCompletedListener loadCompletedListener) {
        this.loadCompletedListener = loadCompletedListener;
    }*/

    private String getMimeType(File selected) {
        Uri selectedUri = Uri.fromFile(selected);
        String fileExtension
                = MimeTypeMap.getFileExtensionFromUrl(selectedUri.toString());
        String mimeType
                = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
        return mimeType;
    }

    private String getFileExtension(File selected) {
        Uri selectedUri = Uri.fromFile(selected);
        String fileExtension
                = MimeTypeMap.getFileExtensionFromUrl(selectedUri.toString());
        return fileExtension;
    }

    public synchronized void getStorageSpaceList(String path, LoadCompletedListener listener) {
        storageType = path;
        if (path.equalsIgnoreCase("ExternalStorage")) {
            this.externalLoadCompletedListener = listener;
        } else {
            this.internalLoadCompletedListener = listener;
        }

        new getStorageSpaces().execute(path);
    }

    public interface LoadCompletedListener {
        void onLoadstart();

        void onLoadCompleted(HashMap<String, Long> storageSpaces);
    }

    class getStorageSpaces extends AsyncTask<String, Void, Void> {
        String pathType;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            imageSpace = 0;
            audioSpace = 0;
            videoSpace = 0;
            documentSpace = 0;
            applicationSpace = 0;
            otherSpace = 0;
            storageSpaces.clear();

            if (storageType.equalsIgnoreCase("ExternalStorage"))
                externalLoadCompletedListener.onLoadstart();
            else
                internalLoadCompletedListener.onLoadstart();
        }

        public ArrayList<MediaFileListModel> getAllFile(File dir) {
            File listFile[] = dir.listFiles();
            if (listFile != null && listFile.length > 0) {
                for (int i = 0; i < listFile.length; i++) {

                    if (listFile[i].isDirectory()) {
                        getAllFile(listFile[i]);
                    } else {

                        //String flsType = getMimeType(listFile[i]);
                        //String flsExt = getFileExtension(listFile[i]);
                        String fileName = listFile[i].getName();

                        if (fileName.endsWith(".pdf") || fileName.endsWith(".json")
                                || fileName.endsWith(".xls") || fileName.endsWith(".xlsx") || fileName.endsWith(".xml")
                                || fileName.endsWith(".ppt") || fileName.endsWith(".pptx")
                                || fileName.endsWith(".txt") || fileName.endsWith(".html")
                                || fileName.endsWith(".doc") || fileName.endsWith(".docx")) { // Documents Files

                            AddingFiles(listFile[i], "documents");

                        } else if (fileName.endsWith(".m4v")
                                || fileName.endsWith(".wmv") || fileName.endsWith(".3gp")
                                || fileName.endsWith(".mp4") || fileName.endsWith(".avi")) { // Video Files

                            AddingFiles(listFile[i], "video");

                        } else if (fileName.endsWith("mp3") ||
                                fileName.endsWith("wma") ||
                                fileName.endsWith("ogg") ||
                                fileName.endsWith("m4a") ||
                                fileName.endsWith("m4p")) { // Audio Files

                            AddingFiles(listFile[i], "audio");

                        } else if (fileName.endsWith("png") ||
                                fileName.endsWith("jpg") ||
                                fileName.endsWith("jpeg") ||
                                fileName.endsWith("gif") ||
                                fileName.endsWith("tiff")) { // Image Files

                            AddingFiles(listFile[i], "image");

                        } else if (fileName.endsWith("apk")) { // Application Files
                            AddingFiles(listFile[i], "app");

                        } else { // Others Files
                            AddingFiles(listFile[i], "others");
                        }
                    }
                }
            }

            return null;//imageListModelsArray;
        }

        public void AddingFiles(File listFile, String fileType) {

            try {
                File file = new File(listFile.getPath());
                long length = file.length();

                if (fileType.equalsIgnoreCase("image")) {
                    imageSpace += length;
                } else if (fileType.equalsIgnoreCase("audio")) {
                    audioSpace += length;
                } else if (fileType.equalsIgnoreCase("video")) {
                    videoSpace += length;
                } else if (fileType.equalsIgnoreCase("documents")) {
                    documentSpace += length;
                } else if (fileType.equalsIgnoreCase("app")) {
                    applicationSpace += length;
                } else {
                    otherSpace += length;
                }

            } catch (Exception e) {

            }

        }

        public void getInstalledAppSize() {
            applicationSpace = 0;

            final PackageManager pm = context.getPackageManager();

            List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

            for (ApplicationInfo ai : packages) {
                if ((ai.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                    try {

                        PackageInfo p = pm.getPackageInfo(ai.packageName, PackageManager.GET_META_DATA);
                        /*String appName = p.applicationInfo.loadLabel(
                                mContext.getPackageManager()).toString();
                        String pname = p.packageName;
                        Drawable icon = p.applicationInfo.loadIcon(mContext
                                .getPackageManager());*/

                        String appName = (String) ai.loadLabel(pm);
                        String pName = ai.packageName;
                        Drawable appIcon = ai.loadIcon(pm);
                        String apkPath = ai.publicSourceDir;


                        Method getPackageSizeInfo = pm.getClass().getMethod(
                                "getPackageSizeInfo", String.class, IPackageStatsObserver.class);
                        getPackageSizeInfo.invoke(pm, pName,
                                new IPackageStatsObserver.Stub() {

                                    @Override
                                    public void onGetStatsCompleted(PackageStats pStats, boolean succeeded)
                                            throws RemoteException {
                                        // mediaFileListModel.setFileSize(futils.readableFileSize(pStats.codeSize));
                                        applicationSpace += pStats.codeSize;
                                    }
                                });

                    } catch (Exception e) {
                        e.getMessage();
                    }
                }
            }
        }

        @Override
        protected Void doInBackground(String... params) {
            pathType = params[0];

            File dir;
            if (pathType.equalsIgnoreCase("InternalStorage"))
                dir = new File(utils.StoragePath("InternalStorage"));
            else
                dir = new File(utils.StoragePath("ExternalStorage"));

            getAllFile(dir);
            //getInstalledAppSize();

            return null;
        }

        @Override
        protected void onPostExecute(Void list) {
            super.onPostExecute(list);

            storageSpaces.clear();

            /*long totalInternalSpace = getExternalSpace("total");
            long freeInternalSpace = getExternalSpace("free");

            Log.d("Spaces", "Image :- " + imageSpace +
                    "\n audio :- " + audioSpace +
                    "\n video :- " + videoSpace +
                    "\n document :- " + documentSpace +
                    "\n application :- " + applicationSpace +
                    "\n other :- " + otherSpace +
                    "\n\n Total :- " + (totalInternalSpace) +
                    "\n\n free :- " + (freeInternalSpace)
            );*/

            storageSpaces.put("imageSpace", imageSpace);
            storageSpaces.put("audioSpace", audioSpace);
            storageSpaces.put("videoSpace", videoSpace);
            storageSpaces.put("documentSpace", documentSpace);
            storageSpaces.put("applicationSpace", applicationSpace);

            long totalUsedSpace = imageSpace + audioSpace + videoSpace + documentSpace + applicationSpace;

            if (pathType.equalsIgnoreCase("InternalStorage")) {

                long totalInternalSpace = getInternalSpace("total");
                long freeInternalSpace = getInternalSpace("free");

                long usedSpace = (totalInternalSpace - freeInternalSpace);
                long otherspace = usedSpace - (totalUsedSpace);
                storageSpaces.put("otherSpace", otherspace);

                storageSpaces.put("totalSpace", totalInternalSpace);
                storageSpaces.put("freeSpace", freeInternalSpace);

            } else {

                long totalExternalSpace = getExternalSpace("total");
                long freeExternalSpace = getExternalSpace("free");

                long usedSpace = (totalExternalSpace - freeExternalSpace);
                long otherspace = usedSpace - (totalUsedSpace);
                storageSpaces.put("otherSpace", otherspace);

                storageSpaces.put("totalSpace", totalExternalSpace);
                storageSpaces.put("freeSpace", freeExternalSpace);
            }
            if (pathType.equalsIgnoreCase("InternalStorage")) {
                if (internalLoadCompletedListener != null)
                    internalLoadCompletedListener.onLoadCompleted(storageSpaces);
            } else {
                if (externalLoadCompletedListener != null)
                    externalLoadCompletedListener.onLoadCompleted(storageSpaces);
            }
        }
    }
}
