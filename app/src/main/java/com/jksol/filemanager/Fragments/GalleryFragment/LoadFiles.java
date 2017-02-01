package com.jksol.filemanager.Fragments.GalleryFragment;

import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
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
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import com.jksol.filemanager.Fragments.GalleryFragment.Adapter.ImagesListAdapter;
import com.jksol.filemanager.Model.MediaFileListModel;
import com.jksol.filemanager.R;
import com.jksol.filemanager.Utils.AppController;
import com.jksol.filemanager.Utils.Futils;
import com.jksol.filemanager.Utils.Utils;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
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

    public LoadFiles(Context mContext) {
        futils = new Futils();
        this.mContext = mContext;
        imageListModelsArray = new ArrayList<>();
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

    public void getFileList(final String FileType) {

        new AsyncTask<Void, Void, ArrayList<MediaFileListModel>>() {
            ProgressDialog progressDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                imageListModelsArray.clear();

                if (FileType.equalsIgnoreCase("Gallery")) {
                    progressDialog = ProgressDialog.show(mContext, "",
                            "Loading Images", true);
                } else if (FileType.equalsIgnoreCase("Audio")) {
                    progressDialog = ProgressDialog.show(mContext, "",
                            "Loading Audio", true);
                } else if (FileType.equalsIgnoreCase("Documents")) {

                    if (loaderLayout != null) {
                        loaderLayout.setVisibility(View.VISIBLE);
                    } else {
                        progressDialog = ProgressDialog.show(mContext, "",
                                "Loading Documents", true);
                    }
                } else if (FileType.equalsIgnoreCase("Video")) {
                    progressDialog = ProgressDialog.show(mContext, "",
                            "Loading Video", true);
                } else if (FileType.equalsIgnoreCase("Apk")) {
                    progressDialog = ProgressDialog.show(mContext, "",
                            "Loading Apk", true);
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
                    getApkList();
                }

                return imageListModelsArray;
            }

            @Override
            protected void onPostExecute(ArrayList<MediaFileListModel> mediaFileListModels) {
                super.onPostExecute(mediaFileListModels);


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
                    else
                        progressDialog.dismiss();
                } else {
                    if (loaderLayout != null)
                        loaderLayout.setVisibility(View.GONE);
                    else
                        progressDialog.dismiss();

                    if (noMediaLayout != null)
                        noMediaLayout.setVisibility(View.VISIBLE);
                    if (recyclerView != null)
                        recyclerView.setVisibility(View.GONE);
                }

                loadCompletedListener.onLoadCompleted(imageListModelsArray);
            }
        }.execute();

    }

    boolean contains(String[] types, String path) {
        for (String string : types) {
            if (path.endsWith(string)) return true;
        }
        return false;
    }

    public ArrayList<MediaFileListModel> getApkList() {
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

                    Bitmap icon = ((BitmapDrawable) appIcon).getBitmap();
                    mediaFileListModel.setMediaBitmap(icon);

                    imageListModelsArray.add(mediaFileListModel);
                } catch (Exception e) {
                    e.getMessage();
                }
            }
        }

        return imageListModelsArray;
    }

    public ArrayList<MediaFileListModel> getDocsList() {
        imageListModelsArray.clear();

        String sortOrder = "LOWER(" + MediaStore.Files.FileColumns.DATE_MODIFIED + ") DESC"; // unordered
        final String[] projection = {MediaStore.Files.FileColumns.DATA};
        Cursor mCursor = AppController.getInstance().getApplicationContext().getContentResolver().query(MediaStore.Files
                        .getContentUri("external"), projection,
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
                        MediaFileListModel mediaFileListModel = getFileDetail(mCursor, "Documents", path);
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

        Cursor mCursor = AppController.getInstance().getApplicationContext().getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DATA},
                null,
                null,
                "LOWER(" + MediaStore.Audio.Media.TITLE + ") ASC");

        if (mCursor != null) {
            if (mCursor.moveToFirst()) {
                do {
                    String path = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                    MediaFileListModel mediaFileListModel = getFileDetail(mCursor, "Audio", path);
                    imageListModelsArray.add(mediaFileListModel);
                } while (mCursor.moveToNext());
                mCursor.close();
            }
        }

        return imageListModelsArray;
    }

    public ArrayList<MediaFileListModel> getVideoList() {
        imageListModelsArray.clear();

        Cursor mCursor = AppController.getInstance().getApplicationContext().getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Video.Media.DISPLAY_NAME, MediaStore.Video.Media.DATA}, null, null,
                "LOWER(" + MediaStore.Video.Media.TITLE + ") ASC");

        if (mCursor != null) {
            if (mCursor.moveToFirst()) {
                do {
                    String path = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                    MediaFileListModel mediaFileListModel = getFileDetail(mCursor, "Video", path);
                    imageListModelsArray.add(mediaFileListModel);
                } while (mCursor.moveToNext());
                mCursor.close();
            }
        }

        return imageListModelsArray;
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
                    MediaFileListModel mediaFileListModel = getFileDetail(mCursor, "Gallery", path);
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
            mediaFileListModel.setMediaBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.movies));
            new Thread(new Runnable() {
                @Override
                public void run() {

                    mHandler.removeMessages(MSG_DESTROY);
                    if (mWorkerThread == null || mWorkerHandler == null) {
                        mWorkerThread = new HandlerThread("IconHolderLoader");
                        mWorkerThread.start();
                        mWorkerHandler = new WorkerHandler(mWorkerThread.getLooper());
                    }

                    LoadResult result = new LoadResult();
                    result.fso = Path;
                    result.mediaFileListModel = mediaFileListModel;

                    Message msg = mWorkerHandler.obtainMessage(MSG_LOAD, result);
                    msg.sendToTarget();
                }
            }).start();

             /* Bitmap bMap = ThumbnailUtils.createVideoThumbnail(mediaFileListModel.getFilePath(), MediaStore.Video.Thumbnails.MINI_KIND);
                if (bMap != null)
                    mediaFileListModel.setMediaBitmap(bMap);
                else
                    mediaFileListModel.setMediaBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.music));*/
        }


        if (fileType.equalsIgnoreCase("Audio")) {
            try {
                int posColId = mCursor.getColumnIndex(MediaStore.Audio.Media._ID);
                long songId = mCursor.getLong(posColId);
                Uri songUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId);
                String[] dataColumn = {MediaStore.Audio.Media.DATA};
                Cursor coverCursor = AppController.getInstance().getApplicationContext().getContentResolver().query(songUri, dataColumn, null, null, null);
                coverCursor.moveToFirst();
                int dataIndex = coverCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                String filePath1 = coverCursor.getString(dataIndex);
                coverCursor.close();
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(filePath1);
                byte[] coverBytes = retriever.getEmbeddedPicture();
                Bitmap songCover;
                if (coverBytes != null) //se l'array di byte non è vuoto, crea una bitmap
                    songCover = BitmapFactory.decodeByteArray(coverBytes, 0, coverBytes.length);
                else
                    songCover = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.music);
                mediaFileListModel.setMediaBitmap(songCover);

            } catch (Exception e) {
                mediaFileListModel.setMediaBitmap(null);

            }
        }

        return mediaFileListModel;
    }

    private void shutdownWorker() {
        if (mWorkerThread != null) {
            mWorkerThread.getLooper().quit();
            mWorkerHandler = null;
            mWorkerThread = null;
        }
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
}
