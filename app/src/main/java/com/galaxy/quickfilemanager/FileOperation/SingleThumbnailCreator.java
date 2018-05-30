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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;

import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

public class SingleThumbnailCreator extends Thread {
    private static HashMap<String, Bitmap> mCacheMap = null;
    private int mWidth;
    private int mHeight;
    private SoftReference<Bitmap> mThumb;

    private Handler mHandler;
    private boolean mStop = false;
    private String filename;
    private ArrayList<String> mFiles;

    public SingleThumbnailCreator(int width, int height) {
        mHeight = height;
        mWidth = width;

        if (mCacheMap == null)
            mCacheMap = new HashMap<String, Bitmap>();
    }

    public Bitmap isBitmapCached(String name) {
        return mCacheMap.get(name);
    }

    public void setCancelThumbnails(boolean stop) {
        mStop = stop;
    }

    public void createNewThumbnail(String filename, ArrayList<String> mFiles, Handler handler) {
        this.filename = filename;
        this.mFiles = mFiles;
        this.mHandler = handler;
    }

    @Override
    public void run() {
        int len = mFiles.size();

        for (int i = 0; i < len; i++) {

            if (mStop) {
                mStop = false;
                filename = "";
                return;
            }

            String fullpath = mFiles.get(i);

            String name = "";
            long length = 0;
            String path = "";
            SmbFile smbFile = null;

            if (fullpath.startsWith("smb://")) {
                try {
                    smbFile = new SmbFile(fullpath);
                    name = smbFile.getName();
                    length = smbFile.length();
                    path = smbFile.getPath();
                } catch (Exception e) {
                }
            } else {
                final File file = new File(fullpath);
                name = file.getName();
                length = file.length();
                path = file.getPath();
            }


            if (isImageFile(name)) {

                long len_kb = length / 1024;

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.outWidth = mWidth;
                options.outHeight = mHeight;

                if (len_kb > 1000 && len_kb < 5000) {
                    options.inSampleSize = 32;
                    options.inPurgeable = true;

                    Bitmap bitmap = null;
                    try {

                        if (fullpath.startsWith("smb://"))
                            bitmap = BitmapFactory.decodeStream(new SmbFileInputStream(smbFile), null, options);
                        else
                            bitmap = BitmapFactory.decodeFile(path, options);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    mThumb = new SoftReference<Bitmap>(bitmap);

                } else if (len_kb >= 5000) {
                    options.inSampleSize = 32;
                    options.inPurgeable = true;

                    Bitmap bitmap = null;
                    try {

                        if (fullpath.startsWith("smb://"))
                            bitmap = BitmapFactory.decodeStream(new SmbFileInputStream(smbFile), null, options);
                        else
                            bitmap = BitmapFactory.decodeFile(path, options);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    mThumb = new SoftReference<Bitmap>(bitmap);

                } else if (len_kb <= 1000) {
                    try {
                        options.inPurgeable = true;

                        Bitmap bitmap = null;
                        try {

                            if (fullpath.startsWith("smb://"))
                                bitmap = BitmapFactory.decodeStream(new SmbFileInputStream(smbFile), null, options);
                            else
                                bitmap = BitmapFactory.decodeFile(path, options);
                        } catch (IOException e) {
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

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Message msg = mHandler.obtainMessage();
                            msg.obj = (Bitmap) mThumb.get();
                            msg.sendToTarget();
                        }
                    });
                } catch (Exception e) {
                }
            }
        }
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