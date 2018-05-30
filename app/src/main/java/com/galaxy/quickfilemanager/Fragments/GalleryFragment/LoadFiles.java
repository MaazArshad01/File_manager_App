package com.galaxy.quickfilemanager.Fragments.GalleryFragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.galaxy.quickfilemanager.Fragments.GalleryFragment.Adapter.ImagesListAdapter;
import com.galaxy.quickfilemanager.MainActivity;
import com.galaxy.quickfilemanager.Model.MediaFileListModel;
import com.galaxy.quickfilemanager.Utils.AppController;
import com.galaxy.quickfilemanager.Utils.BitmapUtil;
import com.galaxy.quickfilemanager.Utils.Futils;
import com.galaxy.quickfilemanager.Utils.Utils;
import com.galaxy.quickfilemanager.R;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by Umiya Mataji on 1/25/2017.
 */

public class LoadFiles {

    private static final int MSG_LOAD = 1;
    private static final int MSG_LOADED = 2;
    private static final int MSG_DESTROY = 3;
    private final Futils futils;
    private final BitmapUtil bitmapUtil;
    private final MainActivity mainActivity;
    Context mContext;
    LoadCompletedListener loadCompletedListener;
    private ArrayList<MediaFileListModel> imageListModelsArray;
    private String mParamFRAGMENT_TYPE_PARAM;
    private RecyclerView recyclerView;
    private LinearLayout noMediaLayout;
    private ImagesListAdapter imagesListAdapter;

    private HandlerThread mWorkerThread;
    private Handler mWorkerHandler;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOADED:
                    processResult((LoadResult) msg.obj);
                    sendEmptyMessageDelayed(MSG_DESTROY, 3000);
                    break;
                case MSG_DESTROY:
                    shutdownWorker();
                    break;
            }
        }

        private void processResult(LoadResult result) {
            // Cache the new drawable
            final String filePath = (result.fso);

            result.mediaFileListModel.setMediaBitmap(result.result);

            if (result.result != null)
                result.mediaFileListModel.setMediaBitmap(result.result);
            else
                result.mediaFileListModel.setMediaBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.music));
            imagesListAdapter.notifyDataSetChanged();
        }
    };
    private View loaderLayout;
    private String apkType = "InstalledApps";
    private ScanObserver fileScanner;
    private AsyncTask<Void, Void, ArrayList<MediaFileListModel>> dataLoads;

    public LoadFiles(Context mContext) {
        futils = new Futils();
        bitmapUtil = new BitmapUtil(mContext);
        bitmapUtil.setImageQuality(10);
        this.mContext = mContext;
        imageListModelsArray = new ArrayList<>();
        mainActivity = (MainActivity) mContext;
    }

    public void setLoadCompletedListener(LoadCompletedListener loadCompletedListener) {
        this.loadCompletedListener = loadCompletedListener;
    }

    public void setImageParams(String mParamFRAGMENT_TYPE_PARAM, RecyclerView recyclerView, LinearLayout noMediaLayout, ImagesListAdapter imagesListAdapter) {
        this.mParamFRAGMENT_TYPE_PARAM = mParamFRAGMENT_TYPE_PARAM;
        this.recyclerView = recyclerView;
        this.noMediaLayout = noMediaLayout;
        this.imagesListAdapter = imagesListAdapter;
    }

    public void setLoaderLayout(View loaderLayout) {
        this.loaderLayout = loaderLayout;
    }

    public void setApkListType(String apkType) {
        this.apkType = apkType;
    }

    public void stopFileLoading() {
        if (dataLoads != null && dataLoads.getStatus() == AsyncTask.Status.RUNNING)
            dataLoads.cancel(true);
    }

    public void getFileList(final String FileType) {
        stopFileLoading();
        dataLoads = new AsyncTask<Void, Void, ArrayList<MediaFileListModel>>() {
            ProgressDialog progressDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                imageListModelsArray.clear();
                try {
                    if (FileType.equalsIgnoreCase("Gallery")) {
                        progressDialog = ProgressDialog.show(mContext, "",
                                "Loading Images", true);
                    } else if (FileType.equalsIgnoreCase("Audio")) {
                       /* progressDialog = ProgressDialog.show(mContext, "",
                                "Loading Audio", true);*/
                    } else if (FileType.equalsIgnoreCase("Documents")) {

                        if (loaderLayout != null) {
                            loaderLayout.setVisibility(View.VISIBLE);
                        } else {
                            progressDialog = ProgressDialog.show(mContext, "",
                                    "Loading Documents", true);
                        }
                    } else if (FileType.equalsIgnoreCase("Video")) {
                       /* progressDialog = ProgressDialog.show(mContext, "",
                                "Loading Video", true);*/
                    } else if (FileType.equalsIgnoreCase("Apk")) {
                        progressDialog = ProgressDialog.show(mContext, "",
                                "Loading Apk", true);
                    }
                } catch (Exception e) {
                }
            }

            @Override
            protected ArrayList<MediaFileListModel> doInBackground(Void... voids) {

                if (FileType.equalsIgnoreCase("Gallery")) {
                    getImagesList();
                } else if (FileType.equalsIgnoreCase("Audio")) {
                    getAudioList();
                } else if (FileType.equalsIgnoreCase("Documents")) {
                    getDocsList();
                } else if (FileType.equalsIgnoreCase("Video")) {
                    getVideoList();
                } else if (FileType.equalsIgnoreCase("Apk")) {
                    imageListModelsArray.clear();
                    if (apkType.equalsIgnoreCase("InstalledApps"))
                        getInstalledApkList();
                    else
                        getApkList();
                }

                return imageListModelsArray;
            }

            @Override
            protected void onPostExecute(ArrayList<MediaFileListModel> mediaFileListModels) {
                super.onPostExecute(mediaFileListModels);
                try {
                    if (mediaFileListModels != null) {
                        if (mediaFileListModels.size() == 0) {
                            if (noMediaLayout != null)
                                noMediaLayout.setVisibility(View.VISIBLE);
                            if (recyclerView != null)
                                recyclerView.setVisibility(View.GONE);
                        } else {
                            if (recyclerView != null)
                                recyclerView.setVisibility(View.VISIBLE);
                            if (noMediaLayout != null)
                                noMediaLayout.setVisibility(View.GONE);
                        }

                        if (loaderLayout != null)
                            loaderLayout.setVisibility(View.GONE);
                        else {
                            if (progressDialog != null)
                                progressDialog.dismiss();
                        }
                    } else {
                        if (loaderLayout != null)
                            loaderLayout.setVisibility(View.GONE);
                        else {
                            if (progressDialog != null)
                                progressDialog.dismiss();
                        }

                        if (noMediaLayout != null)
                            noMediaLayout.setVisibility(View.VISIBLE);
                        if (recyclerView != null)
                            recyclerView.setVisibility(View.GONE);
                    }

                } catch (Exception e) {
                }
                if (loadCompletedListener != null)
                    loadCompletedListener.onLoadCompleted(imageListModelsArray);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    boolean contains(String[] types, String path) {
        for (String string : types) {
            if (path.endsWith(string)) return true;
        }
        return false;
    }

    public ArrayList<MediaFileListModel> getInstalledApkList() {
        imageListModelsArray.clear();

        final PackageManager pm = mContext.getPackageManager();

        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo ai : packages) {
            if ((ai.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                try {
                    final MediaFileListModel mediaFileListModel = new MediaFileListModel();
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
                                    mediaFileListModel.setFileSize(futils.readableFileSize(pStats.codeSize));
                                    // Log.i(TAG, "codeSize: " + pStats.codeSize);
                                }
                            });

                    mediaFileListModel.setFileName(appName);
                    mediaFileListModel.setFilePath(apkPath);
                    mediaFileListModel.setAppPackageName(pName);
                    mediaFileListModel.setAppVersionCode(String.valueOf(p.versionCode));
                    mediaFileListModel.setAppVersionName(p.versionName);

                    mediaFileListModel.setApkType("InstalledApps");

                    Bitmap icon = ((BitmapDrawable) appIcon).getBitmap();
                    mediaFileListModel.setMediaBitmap(icon);

                    try {
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (fileScanner != null)
                                    fileScanner.fileFound(mediaFileListModel);
                            }
                        });
                    } catch (Exception e) {
                        Log.d("Error", e.getMessage());
                    }

                    imageListModelsArray.add(mediaFileListModel);
                } catch (Exception e) {
                    e.getMessage();
                }
            }
        }

        return imageListModelsArray;
    }

    public ArrayList<MediaFileListModel> getApkList() {
        imageListModelsArray.clear();

        String sortOrder = "LOWER(" + MediaStore.Files.FileColumns.DATE_MODIFIED + ") DESC"; // unordered
        final String[] projection = {MediaStore.Files.FileColumns.DATA};
        Cursor mCursor = AppController.getInstance().getApplicationContext().getContentResolver().query(MediaStore.Files
                        .getContentUri("external"), projection,
                null,
                null, sortOrder);

        if (mCursor != null) {
            String[] types = new String[]{".apk"};

            if (mCursor.moveToFirst()) {
                do {
                    String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
                    if (path != null && contains(types, path)) {

                        try {
                            Bitmap bitmap = new Utils().GetIcon(mContext, path);
                            if (bitmap != null) {
                                final MediaFileListModel mediaFileListModel = getFileDetail(mCursor, "Apk", path);
                                try {
                                    mainActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (fileScanner != null)
                                                fileScanner.fileFound(mediaFileListModel);
                                        }
                                    });
                                } catch (Exception e) {
                                    Log.d("Error", e.getMessage());
                                }
                                imageListModelsArray.add(mediaFileListModel);
                            }
                        } catch (Exception e) {
                        }


                    }
                } while (mCursor.moveToNext());
                mCursor.close();
            }
        }

        return imageListModelsArray;
    }

    public ArrayList<MediaFileListModel> getDocsList() {
        imageListModelsArray.clear();

        mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,  MediaStore.Files.getContentUri("external")));

        String sortOrder = "LOWER(" + MediaStore.Files.FileColumns.DATE_MODIFIED + ") DESC"; // unordered
        final String[] projection = {MediaStore.Files.FileColumns.DATA};
        Cursor mCursor = mContext.getContentResolver().query(
                            MediaStore.Files.getContentUri("external"),
                            projection,
                            null,
                            null, sortOrder);

        if (mCursor != null) {
            String[] types = new String[]{".pdf", ".xml", ".html", ".asm", ".text/x-asm", ".def", ".in", ".rc",
                    ".list", ".pl", ".prop", ".properties", ".rc", ".xls", ".xlsx", ".ppt", ".pptx",
                    ".doc", ".docx", ".msg", ".odt", ".pages", ".rtf", ".txt", ".wpd", ".wps"};

            if (mCursor.moveToFirst()) {
                do {
                    String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
                    if (path != null && contains(types, path)) {
                        final MediaFileListModel mediaFileListModel = getFileDetail(mCursor, "Documents", path);
                        try {
                            mainActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (fileScanner != null)
                                        fileScanner.fileFound(mediaFileListModel);
                                }
                            });
                        } catch (Exception e) {
                            Log.d("Error", e.getMessage());
                        }
                        imageListModelsArray.add(mediaFileListModel);
                    }
                } while (mCursor.moveToNext());
                mCursor.close();
            }
        }

        return imageListModelsArray;
    }

    public ArrayList<MediaFileListModel> getAudioList() {
        imageListModelsArray.clear();

        Cursor mCursor = mContext.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DATA},
                null,
                null,
                "LOWER(" + MediaStore.Audio.Media.TITLE + ") ASC");

        if (mCursor != null) {
            if (mCursor.moveToFirst()) {
                do {
                    String path = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                    final MediaFileListModel mediaFileListModel = getFileDetail(mCursor, "Audio", path);

                    try {
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (fileScanner != null)
                                    fileScanner.fileFound(mediaFileListModel);
                            }
                        });

                    } catch (Exception e) {
                        Log.d("Error", e.getMessage());
                    }

                    imageListModelsArray.add(mediaFileListModel);
                } while (mCursor.moveToNext());
                // mCursor.close();
            }
        }

        return imageListModelsArray;
    }

    public ArrayList<MediaFileListModel> getVideoList() {
        imageListModelsArray.clear();

        Cursor mCursor = AppController.getInstance().getApplicationContext().getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Video.Media.DISPLAY_NAME, MediaStore.Video.Media.DATA}, null, null,
                "LOWER(" + MediaStore.Video.Media.DATE_MODIFIED + ") DESC");

        if (mCursor != null) {
            if (mCursor.moveToFirst()) {
                do {
                    String path = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                    final MediaFileListModel mediaFileListModel = getFileDetail(mCursor, "Video", path);

                    try {
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (fileScanner != null)
                                    fileScanner.fileFound(mediaFileListModel);
                            }
                        });
                    } catch (Exception e) {
                        Log.d("Error", e.getMessage());
                    }

                    imageListModelsArray.add(mediaFileListModel);
                } while (mCursor.moveToNext());
                mCursor.close();
            }

            try {
                if (fileScanner != null)
                    fileScanner.searchFinished();
            } catch (Exception e) {
            }
        }

        return imageListModelsArray;
    }

    public void setObserver(LoadFiles.ScanObserver scanObserver) {
        this.fileScanner = scanObserver;
    }

    public ArrayList<MediaFileListModel> getImagesList() {
        imageListModelsArray.clear();
        @SuppressWarnings("deprecation")
        Cursor mCursor = null;

        if (mParamFRAGMENT_TYPE_PARAM.equalsIgnoreCase("All")) {
            mCursor = mContext.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // Uri
                    new String[]{MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATA}, // Projection
                    null,
                    null,
                    "LOWER(" + MediaStore.Images.Media.DATE_MODIFIED + ") DESC");

        } else if (mParamFRAGMENT_TYPE_PARAM.equalsIgnoreCase("Camera")) {

            String condition = MediaStore.Images.Media.DATA + " like '%/DCIM/%'";
            mCursor = mContext.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATA},
                    condition,
                    null,
                    "LOWER(" + MediaStore.Images.Media.DATE_MODIFIED + ") DESC");
        } else {
            /*mCursor = mContext.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATA}, null, null,
                    "LOWER(" + MediaStore.Images.Media.TITLE + ") DESC");*/
            String condition = MediaStore.Images.Media.DATA + " not like '%/DCIM/%'";
            mCursor = mContext.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATA},
                    condition,
                    null,
                    "LOWER(" + MediaStore.Images.Media.DATE_MODIFIED + ") DESC");
        }
        if (mCursor != null) {
            if (mCursor.moveToFirst()) {
                do {

                    String path = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                    final MediaFileListModel mediaFileListModel = getFileDetail(mCursor, "Gallery", path);
                    try {
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (fileScanner != null)
                                    fileScanner.fileFound(mediaFileListModel);
                            }
                        });
                    } catch (Exception e) {
                        Log.d("Error", e.getMessage());
                    }
                    imageListModelsArray.add(mediaFileListModel);
                } while (mCursor.moveToNext());
            }
            mCursor.close();
        }
        // loadCompletedListener.onLoadCompleted(imageListModelsArray);

        return imageListModelsArray;
    }

    public MediaFileListModel getFileDetail(Cursor mCursor, String fileType, final String Path) {
        File filePath = new File(Path);

        final MediaFileListModel mediaFileListModel = new MediaFileListModel();
        mediaFileListModel.setFileName(filePath.getName());
        mediaFileListModel.setFilePath(Path);
        try {

            long length = filePath.length();
            length = length / 1024;
            if (length >= 1024) {
                length = length / 1024;
                mediaFileListModel.setFileSize(length + " MB");
            } else {
                mediaFileListModel.setFileSize(length + " KB");
            }

            Date lastModDate = new Date(filePath.lastModified());
            mediaFileListModel.setFileCreatedTime(lastModDate.toString());
            mediaFileListModel.setFileCreatedTimeDatel(lastModDate);
        } catch (Exception e) {
            mediaFileListModel.setFileSize("unknown");
        }

        String ext = Path.toString();
        String sub_ext = ext.substring(ext.lastIndexOf(".") + 1);
        mediaFileListModel.setFileType(sub_ext);

        if (fileType.equalsIgnoreCase("Video")) {

            try {
                Bitmap videoThumb = bitmapUtil.CompressImage(R.drawable.movies);
                mediaFileListModel.setMediaBitmap(videoThumb);



            } catch (Exception e) {
            }

        }

        if (fileType.equalsIgnoreCase("Audio")) {
            try {

                Bitmap videoThumb = bitmapUtil.CompressImage(R.drawable.music);
                mediaFileListModel.setMediaBitmap(videoThumb);

                //mediaFileListModel.setAudioCursor(mCursor);
                int posColId = mCursor.getColumnIndex(MediaStore.Audio.Media._ID);
                long songId = mCursor.getLong(posColId);
                mediaFileListModel.setAudioLink(songId);


            } catch (Exception e) {

            }
        }

        if (fileType.equalsIgnoreCase("Apk")) {

            String APKFilePath = Path; //For example...

            try {
                PackageManager pm = mContext.getPackageManager();
                PackageInfo pi = pm.getPackageArchiveInfo(APKFilePath, 0);

                mediaFileListModel.setAppPackageName(pi.packageName);
                mediaFileListModel.setAppVersionCode(String.valueOf(pi.versionCode));
                mediaFileListModel.setAppVersionName(pi.versionName);
            } catch (Exception e) {
            }

            Bitmap apkIcon = null;

            if (Path.startsWith("smb://")) {
                apkIcon = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.appicon);
            } else {
                try {
                    Bitmap bitmap = new Utils().GetIcon(mContext, Path);
                    if (bitmap != null)
                        apkIcon = bitmap;
                    else
                        apkIcon = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.appicon);
                } catch (Exception e) {
                    apkIcon = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.appicon);
                }
            }

            mediaFileListModel.setApkType("AllAppFiles");
            mediaFileListModel.setMediaBitmap(apkIcon);

        }

        return mediaFileListModel;
    }

    private MediaFileListModel getApkDetail(final String Path) {

        File filePath = new File(Path);
        final MediaFileListModel mediaFileListModel = new MediaFileListModel();
        mediaFileListModel.setFileName(filePath.getName());
        mediaFileListModel.setFilePath(Path);
        try {

            long length = filePath.length();
            length = length / 1024;
            if (length >= 1024) {
                length = length / 1024;
                mediaFileListModel.setFileSize(length + " MB");
            } else {
                mediaFileListModel.setFileSize(length + " KB");
            }

            Date lastModDate = new Date(filePath.lastModified());
            mediaFileListModel.setFileCreatedTime(lastModDate.toString());
            mediaFileListModel.setFileCreatedTimeDatel(lastModDate);
        } catch (Exception e) {
            mediaFileListModel.setFileSize("unknown");
        }

        String ext = Path.toString();
        String sub_ext = ext.substring(ext.lastIndexOf(".") + 1);
        mediaFileListModel.setFileType(sub_ext);


        String APKFilePath = Path; //For example...

        try {
            PackageManager pm = mContext.getPackageManager();
            PackageInfo pi = pm.getPackageArchiveInfo(APKFilePath, 0);

            mediaFileListModel.setAppPackageName(pi.packageName);
            mediaFileListModel.setAppVersionCode(String.valueOf(pi.versionCode));
            mediaFileListModel.setAppVersionName(pi.versionName);


        } catch (Exception e) {
        }

        Bitmap apkIcon = null;

        if (Path.startsWith("smb://")) {
            apkIcon = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.appicon);
        } else {
            try {
                Bitmap bitmap = new Utils().GetIcon(mContext, Path);
                if (bitmap != null)
                    apkIcon = bitmap;
                else
                    apkIcon = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.appicon);
            } catch (Exception e) {
                apkIcon = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.appicon);
            }
        }

        mediaFileListModel.setMediaBitmap(apkIcon);

        return mediaFileListModel;
    }

    private void shutdownWorker() {
        if (mWorkerThread != null) {
            mWorkerThread.getLooper().quit();
            mWorkerHandler = null;
            mWorkerThread = null;
        }
    }

    public interface ScanObserver {
        void fileFound(MediaFileListModel mediaFileListModel);

        void searchFinished();
    }

    public interface LoadCompletedListener {
        void onLoadCompleted(ArrayList<MediaFileListModel> imageListModelsArray);
    }

    private static class LoadResult {
        String fso;
        Bitmap result;
        MediaFileListModel mediaFileListModel;
    }

    private class WorkerHandler extends Handler {
        public WorkerHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOAD:
                    LoadResult resultss = (LoadResult) msg.obj;

                    String fso = (String) resultss.fso;

                    Bitmap thumb = ThumbnailUtils.createVideoThumbnail(fso,
                            MediaStore.Images.Thumbnails.MINI_KIND);

                    Bitmap d = thumb;
                    if (d != null) {
                        LoadResult result = new LoadResult();
                        result.fso = fso;
                        result.result = d;
                        result.mediaFileListModel = resultss.mediaFileListModel;
                        mHandler.obtainMessage(MSG_LOADED, result).sendToTarget();
                    }
                    break;
            }
        }
    }

    class getTypedFile extends AsyncTask<String, Void, ArrayList<MediaFileListModel>> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            imageListModelsArray.clear();
            progressDialog = ProgressDialog.show(mContext, "",
                    "Loading", false);
        }

        public ArrayList<MediaFileListModel> getAllFile(File dir, String fileType) {
            File listFile[] = dir.listFiles();
            if (listFile != null && listFile.length > 0) {
                for (int i = 0; i < listFile.length; i++) {

                    if (listFile[i].isDirectory()) {
                        //  fileList.add(listFile[i]); // Enter Directory
                        getAllFile(listFile[i], fileType);
                    } else {

                        if (fileType.equalsIgnoreCase("Apk")) {

                            if (listFile[i].getName().endsWith(".apk")) {
                                AddingFiles(listFile[i], fileType);
                            }
                        }
                    }
                }
            }
            return imageListModelsArray;
        }

        public void AddingFiles(File listFile, String fileType) {
            MediaFileListModel mediaFileListModel = new MediaFileListModel();

            mediaFileListModel.setFileName(listFile.getName());
            mediaFileListModel.setFilePath(listFile.getPath());

            try {
                File file = new File(listFile.getPath());
                long length = file.length();
                length = length / 1024;
                if (length >= 1024) {
                    length = length / 1024;
                    mediaFileListModel.setFileSize(length + " MB");
                } else {
                    mediaFileListModel.setFileSize(length + " KB");
                }
                Date lastModDate = new Date(file.lastModified());
                mediaFileListModel.setFileCreatedTime(lastModDate.toString());
                mediaFileListModel.setFileCreatedTimeDatel(lastModDate);

            } catch (Exception e) {
                mediaFileListModel.setFileSize("unknown");
            }

            String ext = listFile.toString();
            String sub_ext = ext.substring(ext.lastIndexOf(".") + 1);
            mediaFileListModel.setFileType(sub_ext);


            if (fileType.equalsIgnoreCase("Documents")) {

            } else if (fileType.equalsIgnoreCase("Video")) {
                try {
                    Bitmap bMap = ThumbnailUtils.createVideoThumbnail(listFile.getPath(), MediaStore.Video.Thumbnails.MINI_KIND);
                    if (bMap != null)
                        mediaFileListModel.setMediaBitmap(bMap);
                    // else
                    // mediaFileListModel.setMediaBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.music));
                } catch (Exception e) {
                    //  mediaFileListModel.setMediaBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.music));
                }
            }

            imageListModelsArray.add(mediaFileListModel);
        }

        @Override
        protected ArrayList<MediaFileListModel> doInBackground(String... params) {
            File dir = new File(params[0]);
            String FileType = params[1];
            progressDialog.setMessage("Loading " + FileType);

            getAllFile(dir, FileType);

            Collections.sort(imageListModelsArray, new Comparator<MediaFileListModel>() {
                @Override
                public int compare(MediaFileListModel mediaFileListModel, MediaFileListModel t1) {
                    // For Asseccing sort swipe the mediaFileListModel, and t1 object
                    return t1.getFileCreatedTimeDatel().compareTo(mediaFileListModel.getFileCreatedTimeDatel());
                }
            });

            return imageListModelsArray;
        }

        @Override
        protected void onPostExecute(ArrayList<MediaFileListModel> list) {
            super.onPostExecute(list);

            if (list != null) {
                if (list.size() == 0) {
                    noMediaLayout.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    noMediaLayout.setVisibility(View.GONE);
                }
                progressDialog.dismiss();
            } else {
                progressDialog.dismiss();
                noMediaLayout.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }

        }
    }
}
