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

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.galaxy.quickfilemanager.Model.MediaFileListModel;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import jcifs.smb.SmbFile;

public class FilesLoaderThread extends Thread {
    private static HashMap<String, MediaFileListModel> mCacheMap = null;

    private final int KB = 1024;
    private final int MG = KB * KB;
    private final int GB = MG * KB;

    private ArrayList<String> mFiles;
    private String mDir;
    private Handler mHandler;
    private boolean mStop = false;
    private SoftReference<MediaFileListModel> cachedFiles;

    public FilesLoaderThread() {

        if (mCacheMap == null)
            mCacheMap = new HashMap<String, MediaFileListModel>();
    }

    public MediaFileListModel isFileCached(String name) {
        return mCacheMap.get(name);
    }

    public void setCancelFile(boolean stop) {
        mStop = stop;
    }

    public void createNewFile(ArrayList<String> files, String dir, Handler handler) {
        this.mFiles = files;
        this.mDir = dir;
        this.mHandler = handler;
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

            try {

                SmbFile file = new SmbFile(mDir + mFiles.get(i));

                if (file.exists()) {
                    MediaFileListModel mediaFileListModel = new MediaFileListModel();
                    mediaFileListModel.setDirectory(file.isDirectory());
                    mediaFileListModel.setFileName(file.getName());

                    double size = file.length();
                    String display_size = "";
                    if (size > GB)
                        display_size = String.format("%.2f Gb ", (double) size / GB);
                    else if (size < GB && size > MG)
                        display_size = String.format("%.2f Mb ", (double) size / MG);
                    else if (size < MG && size > KB)
                        display_size = String.format("%.2f Kb ", (double) size / KB);
                    else
                        display_size = String.format("%.2f bytes ", (double) size);

                    mediaFileListModel.setFileSize(display_size);
                    mediaFileListModel.setFilePermission(getFilePermissions(file));

                    String[] list = null;
                    if (file.isDirectory()) {
                        try {
                            SmbFile file1 = new SmbFile(mDir + mFiles.get(i) + "/");
                            list = file1.list();
                        }catch (Exception e){

                            Log.e("Smb file read error",mDir + mFiles.get(i) + "/" +  " Error" + e.getMessage() );
                        }
                    }


                    if (list != null)
                        mediaFileListModel.setFileListCount(list.length);
                    else
                        mediaFileListModel.setFileListCount(0);

                    mediaFileListModel.setFileCreatedTimeDatel(new Date(file.lastModified()));

                    cachedFiles = new SoftReference<MediaFileListModel>(mediaFileListModel);
                    mCacheMap.put(file.getPath(), cachedFiles.get());

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Message msg = mHandler.obtainMessage();
                            msg.obj = (MediaFileListModel) cachedFiles.get();
                            msg.sendToTarget();
                        }
                    });
                }


            } catch (Exception e) {
            }
        }
    }

    public String getFilePermissions(SmbFile file) {
        String per = "-";
        try {
            if (file.isDirectory())
                per += "d";
            if (file.canRead())
                per += "r";
            if (file.canWrite())
                per += "w";
        } catch (Exception e) {
        }
        return per;
    }

}