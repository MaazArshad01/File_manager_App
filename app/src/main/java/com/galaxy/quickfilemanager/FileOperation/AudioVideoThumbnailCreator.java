/*
    Open Manager, an open source file manager for the Android system
    Copyright (C) 2009, 2010, 2011  Joe Berria <nexesdevelopment@gmail.com>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.galaxy.quickfilemanager.FileOperation;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;

import com.galaxy.quickfilemanager.Model.MediaFileListModel;
import com.galaxy.quickfilemanager.R;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.List;

public class AudioVideoThumbnailCreator extends Thread {
    public static HashMap<String, Bitmap> mCacheMap = null;
    public static HashMap<String, Bitmap> mAudioCacheMap = null;
    private int mWidth;
    private int mHeight;
    private SoftReference<Bitmap> mThumb;
    public List<MediaFileListModel> mFiles;
    //private String mDir;
    private Handler mHandler;
    private boolean mStop = false;
    private String type = "";
    private Context mContext;

    public AudioVideoThumbnailCreator(int width, int height) {
        mHeight = height;
        mWidth = width;

        if (mCacheMap == null)
            mCacheMap = new HashMap<String, Bitmap>();

        if (mAudioCacheMap == null)
            mAudioCacheMap = new HashMap<String, Bitmap>();
    }

    public void setFileType(String type) {
        this.type = type;
    }

    public Bitmap isBitmapCached(String name) {
        if(type.equalsIgnoreCase("Video"))
            return mCacheMap.get(name);
        else
            return mAudioCacheMap.get(name);
    }

    public void setCancelThumbnails(boolean stop) {
        mStop = stop;
    }

    public void createNewThumbnail(List<MediaFileListModel> files, Handler handler, Context context) {
        this.mFiles = files;
        this.mHandler = handler;
        this.mContext = context;
    }

    @Override
    public void run() {
        int len = mFiles.size();

        for (int i = 0; i < len; i++) {
            if (mStop) {
                mStop = false;
                mFiles = null;
                return;
            }

            String name = "";
            long length = 0;
            String path = "";

            final File file = new File(mFiles.get(i).getFilePath());
            name = file.getName();
            length = file.length();
            path = file.getPath();


            if (type.equalsIgnoreCase("Video")) {

                long len_kb = length / 1024;

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.outWidth = mWidth;
                options.outHeight = mHeight;

                if (len_kb > 1000 && len_kb < 5000) {
                    options.inSampleSize = 32;
                    options.inPurgeable = true;

                    Bitmap bitmap = null;
                    try {
                        //bitmap = BitmapFactory.decodeFile(path, options);
                        bitmap = getVideoThumb(path, options);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    mThumb = new SoftReference<Bitmap>(bitmap);

                } else if (len_kb >= 5000) {
                    options.inSampleSize = 32;
                    options.inPurgeable = true;

                    Bitmap bitmap = null;
                    try {

                       // bitmap = BitmapFactory.decodeFile(path, options);
                        bitmap =  getVideoThumb(path, options);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    mThumb = new SoftReference<Bitmap>(bitmap);

                } else if (len_kb <= 1000) {
                    try {
                        options.inPurgeable = true;

                        Bitmap bitmap = null;
                        try {

                            // bitmap = BitmapFactory.decodeFile(path, options);
                            bitmap =  getVideoThumb(path, options);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        mThumb = new SoftReference<Bitmap>(Bitmap.createScaledBitmap(
                                bitmap,
                                mWidth,
                                mHeight,
                                false));


                    } catch (Exception e) {
                    }
                }
                try {
                    mCacheMap.put(path, mThumb.get());

                    final int finalI = i;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Message msg = mHandler.obtainMessage();
                            msg.obj = (int) finalI;
                         // msg.obj = (Bitmap) mThumb.get();
                            msg.sendToTarget();
                        }
                    });
                } catch (Exception e) {
                }

            }else if(type.equalsIgnoreCase("Audio")){

                long len_kb = length / 1024;

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.outWidth = mWidth;
                options.outHeight = mHeight;

                if (len_kb > 1000 && len_kb < 5000) {
                    options.inSampleSize = 32;
                    options.inPurgeable = true;

                    Bitmap bitmap = null;
                    try {
                        //bitmap = BitmapFactory.decodeFile(path, options);
                        bitmap = getAudioThumb(mFiles.get(i).getAudioLink(), options);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    mThumb = new SoftReference<Bitmap>(bitmap);

                } else if (len_kb >= 5000) {
                    options.inSampleSize = 32;
                    options.inPurgeable = true;

                    Bitmap bitmap = null;
                    try {

                        // bitmap = BitmapFactory.decodeFile(path, options);
                        bitmap =  getAudioThumb(mFiles.get(i).getAudioLink(), options);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    mThumb = new SoftReference<Bitmap>(bitmap);

                } else if (len_kb <= 1000) {
                    try {
                        options.inPurgeable = true;

                        Bitmap bitmap = null;
                        try {

                            // bitmap = BitmapFactory.decodeFile(path, options);
                            bitmap =  getAudioThumb(mFiles.get(i).getAudioLink(), options);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        mThumb = new SoftReference<Bitmap>(Bitmap.createScaledBitmap(
                                bitmap,
                                mWidth,
                                mHeight,
                                false));


                    } catch (Exception e) {
                    }
                }
                try {
                    mAudioCacheMap.put(path, mThumb.get());

                    final int finalI1 = i;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Message msg = mHandler.obtainMessage();
                            msg.obj = (int) finalI1;
                           // msg.obj = (Bitmap) mThumb.get();
                            msg.sendToTarget();
                        }
                    });
                } catch (Exception e) {
                }

            }
        }
    }

    private Bitmap getAudioThumb(long mCursor, BitmapFactory.Options options){

        /*int posColId = mCursor.getColumnIndex(MediaStore.Audio.Media._ID);
        long songId = mCursor.getLong(posColId);*/

        long songId = mCursor;
        Uri songUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId);
        String[] dataColumn = {MediaStore.Audio.Media.DATA};

        Cursor coverCursor = mContext.getContentResolver().query(songUri, dataColumn, null, null, null);
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
        else {
           // songCover = bitmapUtil.CompressImage(R.drawable.music);
            songCover = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.music, options);
        }

        return songCover;
    }

    private Bitmap getVideoThumb(String path, BitmapFactory.Options options){
        Bitmap bitmap = null;
        Bitmap bMap = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MICRO_KIND);
        if (bMap != null) {
            bitmap = bMap;
        }else
            bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.image, options);

        return bitmap;
    }

    private boolean isImageFile(String file) {
        String ext = file.substring(file.lastIndexOf(".") + 1);

        if (ext.equalsIgnoreCase("png") || ext.equalsIgnoreCase("jpg") ||
                ext.equalsIgnoreCase("jpeg") || ext.equalsIgnoreCase("gif") ||
                ext.equalsIgnoreCase("tiff") || ext.equalsIgnoreCase("tif"))
            return true;

        return false;
    }
}