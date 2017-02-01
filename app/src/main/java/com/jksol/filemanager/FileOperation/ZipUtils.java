package com.jksol.filemanager.FileOperation;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

import com.jksol.filemanager.Utils.FileUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by Umiya Mataji on 1/10/2017.
 */

class ZipUtils extends Service {

    private final IBinder mBinder = new LocalBinder();
    HashMap<Integer, Boolean> hash = new HashMap<Integer, Boolean>();
    private Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Bundle b = new Bundle();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return mBinder;
    }

    public class Doback extends AsyncTask<Bundle, Void, Integer> {
        ArrayList<String> files;
        long totalBytes = 0L;
        String name;

        public Doback() {
        }

        protected Integer doInBackground(Bundle... p1) {
            /*int id = p1[0].getInt("id");
            ArrayList<BaseFile> a = (ArrayList<BaseFile>) p1[0].getSerializable("files");
            name = p1[0].getString("name");
            new zip().execute(id, utils.toFileArray(a), name);*/
            return 0;
        }

        @Override
        public void onPostExecute(Integer b) {
            // publishResults(b, name, 100, true, 0, totalBytes);
            hash.put(b, false);
            boolean stop = true;
            for (int a : hash.keySet()) {
                if (hash.get(a)) stop = false;
            }
            if (stop)
                stopSelf(b);
            Intent intent = new Intent("loadlist");
            sendBroadcast(intent);
        }

    }

    class zip {

        int count, lastpercent = 0;
        long size, totalBytes = 0;
        String fileName;
        ZipOutputStream zos;
        AsyncTask asyncTask;
        private int isCompressed = 0;

        public void execute(int id, ArrayList<File> a, String fileOut) {
            for (File f1 : a) {
                if (f1.isDirectory()) {
                    totalBytes = totalBytes + new FileManager().folderSize(f1);
                } else {
                    totalBytes = totalBytes + f1.length();
                }
            }
            OutputStream out = null;
            count = a.size();
            fileName = fileOut;
            File zipDirectory = new File(fileOut);

            try {
                out = FileUtil.getOutputStream(zipDirectory, mContext, totalBytes);
                zos = new ZipOutputStream(new BufferedOutputStream(out));
            } catch (Exception e) {
            }
            for (File file : a) {
                try {

                    compressFile(id, file, "");
                } catch (Exception e) {
                }
            }
            try {
                zos.flush();
                zos.close();

            } catch (Exception e) {
            }
        }

        void calculateProgress(final String name, final int id, final boolean completed, final long
                copiedbytes, final long totalbytes) {
            if (asyncTask != null && asyncTask.getStatus() == AsyncTask.Status.RUNNING)
                asyncTask.cancel(true);
            asyncTask = new AsyncTask<Void, Void, Void>() {
                int p1, p2;

                @Override
                protected Void doInBackground(Void... voids) {
                    if (isCancelled()) return null;
                    p1 = (int) ((copiedbytes / (float) totalbytes) * 100);
                    lastpercent = (int) copiedbytes;
                    if (isCancelled()) return null;
                    return null;
                }

                @Override
                public void onPostExecute(Void v) {
                    // publishResults(id, name, p1, completed, copiedbytes, totalbytes);
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        }

        private void compressFile(int id, File file, String path) throws IOException, NullPointerException {

            if (!file.isDirectory()) {
                byte[] buf = new byte[20480];
                int len;
                BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
                zos.putNextEntry(new ZipEntry(path + "/" + file.getName()));
                while ((len = in.read(buf)) > 0) {
                    if (hash.get(id)) {
                        zos.write(buf, 0, len);
                        size += len;
                        int p = (int) ((size / (float) totalBytes) * 100);
                        if (p != lastpercent || lastpercent == 0) {
                            calculateProgress(fileName, id, false, size, totalBytes);
                        }
                        lastpercent = p;
                    }
                }
                in.close();
                return;
            }
            if (file.list() == null) {
                return;
            }
            for (String fileName : file.list()) {

                File f = new File(file.getAbsolutePath() + File.separator
                        + fileName);
                compressFile(id, f, path + File.separator + file.getName());

            }
        }
    }

    public class LocalBinder extends Binder {
        public ZipUtils getService() {
            // Return this instance of LocalService so clients can call public methods
            return ZipUtils.this;
        }
    }
}
