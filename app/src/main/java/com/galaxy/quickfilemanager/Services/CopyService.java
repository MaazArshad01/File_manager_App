/*
 * Copyright (C) 2014 Arpit Khurana <arpitkh96@gmail.com>, Vishal Nehra <vishalmeham2@gmail.com>
 *
 * This file is part of Amaze File Manager.
 *
 * Amaze File Manager is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.galaxy.quickfilemanager.Services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.galaxy.quickfilemanager.MainActivity;
import com.galaxy.quickfilemanager.Utils.DataPackage;
import com.galaxy.quickfilemanager.Utils.FileUtil;
import com.galaxy.quickfilemanager.Utils.Futils;
import com.galaxy.quickfilemanager.Utils.HFile;
import com.galaxy.quickfilemanager.ProgressListener;
import com.galaxy.quickfilemanager.R;
import com.galaxy.quickfilemanager.RegisterCallback;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jcifs.smb.SmbFile;

public class CopyService extends Service {

    private final IBinder mBinder = new LocalBinder();
    public HashMap<Integer, DataPackage> hash1 = new HashMap<Integer, DataPackage>();
    Futils futils = new Futils();
    HFile hFile = new HFile();

    // Binder given to clients
    HashMap<Integer, Boolean> hash = new HashMap<Integer, Boolean>();
    NotificationManager mNotifyManager;
    NotificationCompat.Builder mBuilder;

    Context c;
    boolean foreground = true;
    // ProgressListener progressListener;

    ProgressListener progressListener;

    //bind with processviewer
    RegisterCallback registerCallback = new RegisterCallback.Stub() {
        @Override
        public void registerCallBack(ProgressListener p) throws RemoteException {
            progressListener = p;
        }

        @Override
        public List<DataPackage> getCurrent() throws RemoteException {
            List<DataPackage> dataPackages = new ArrayList<>();
            for (int i : hash1.keySet()) {
                dataPackages.add(hash1.get(i));
            }
            return dataPackages;
        }
    };

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */

    private BroadcastReceiver receiver1 = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            hash.put(intent.getIntExtra("id", 1), false);
            mBuilder.setContentText("Stopping.. Please Wait...");
            mBuilder.setOngoing(false);
            int id1 = Integer.parseInt("456" + intent.getIntExtra("id", 1));
            mNotifyManager.notify(id1, mBuilder.build());
        }
    };

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate() {
        c = getApplicationContext();
        registerReceiver(receiver1, new IntentFilter("copycancel"));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Bundle b = new Bundle();

        mNotifyManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra("openprocesses", true);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        mBuilder.setContentIntent(pendingIntent);

        mBuilder.setContentTitle(getResources().getString(R.string.copying))
                .setSmallIcon(R.drawable.ic_copy);
        mBuilder.setContentText("Waiting...");

        Toast.makeText(c, "Start copying", Toast.LENGTH_SHORT).show();

        if (foreground) {
            startForeground(Integer.parseInt("456" + startId), mBuilder.build());
        }

       /* String name = intent.getStringExtra("name");
        if ((zpath != null && zpath.length() != 0)) {
            if (zpath.endsWith("/")) name = zpath + new File(name).getName();
            else name = zpath + "/" + new File(name).getName();
        }
        File c = new File(name);
        if (!c.exists()) {
            try {
                c.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/

        String FILE2 = intent.getStringExtra("COPY_DIRECTORY");
        ArrayList<String> files = intent.getStringArrayListExtra("FILE_PATHS");
        int mode = intent.getIntExtra("MODE", 0);
        boolean movefile = intent.getBooleanExtra("move", false);

        b.putInt("id", startId);
        b.putBoolean("move", movefile);
        b.putString("FILE2", FILE2);
        b.putStringArrayList("files", files);
        b.putInt("MODE", mode);
        hash.put(startId, true);

        DataPackage intent1 = new DataPackage();
        intent1.setName(FileUtil.getFileNameFromPath(files.get(0)));
        intent1.setTotal(0);
        intent1.setDone(0);
        intent1.setId(startId);
        intent1.setP1(0);
        intent1.setP2(0);
        intent1.setMove(movefile);
        intent1.setCompleted(false);
        hash1.put(startId, intent1);

        hFile.setMode(mode);
        new Doback().execute(b);
        Log.d("Service number", String.valueOf(startId));
        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

   /* public void setProgressListener(ProgressListener progressListener) {
        this.progressListener = progressListener;
    }*/

    private void publishResults(String a, int p1, int p2, int id, long total, long done, boolean b, boolean move, String target_filepath) {
        if (hash.get(id)) {
            //notification
            mBuilder.setProgress(100, p1, false);
            mBuilder.setOngoing(true);
            int title = R.string.copying;
            if (move) title = R.string.moving;
            mBuilder.setContentTitle(futils.getString(c, title));
            mBuilder.setContentText(new File(a).getName() + " " + futils.readableFileSize(done) + "/" + futils.readableFileSize(total));
            int id1 = Integer.parseInt("456" + id);
            mNotifyManager.notify(id1, mBuilder.build());
            if (p1 == 100 || total == 0) {
                mBuilder.setContentTitle("Copy completed");
                if (move)
                    mBuilder.setContentTitle("Move Completed");
                mBuilder.setContentText("");
                mBuilder.setProgress(0, 0, false);
                mBuilder.setOngoing(false);
                mBuilder.setAutoCancel(true);
                mNotifyManager.notify(id1, mBuilder.build());
                publishCompletedResult(id1);
            }
            //for processviewer
            DataPackage intent = new DataPackage();
            intent.setName(a);
            intent.setTotal(total);
            intent.setDone(done);
            intent.setId(id);
            intent.setP1(p1);
            intent.setP2(p2);
            intent.setMove(move);
            intent.setTarget_filepath(target_filepath);
            intent.setCompleted(b);
            hash1.put(id, intent);
            try {
                if (progressListener != null) {
                    progressListener.onUpdate(intent);
                    if (b) progressListener.refresh();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else publishCompletedResult(Integer.parseInt("456" + id));
    }

    public void publishCompletedResult(int id1) {
        try {
            mNotifyManager.cancel(id1);
           /* Intent intent = new Intent("CopyCompleted");
            intent.putExtra("copy_success", false);
            sendBroadcast(intent);*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
       // return mBinder;
        return registerCallback.asBinder();
    }

    @Override
    public void onDestroy() {
        this.unregisterReceiver(receiver1);
    }

    void generateNotification(ArrayList<HFile> failedOps, boolean move) {

        mNotifyManager.cancelAll();

        Intent intent = new Intent("CopyCompleted");
        intent.putExtra("copy_success", failedOps.size() == 0 ? true : false);
        String msg;

        if (failedOps.size() == 0) {
            msg = "%s successfully".replace("%s", move ? "Moved" : "Copied");
        } else {
            msg = "Some files weren't %s successfully".replace("%s", move ? "Moved" : "Copied");
        }
       // intent.putExtra("copy_msg", msg);

        Toast.makeText(c, msg, Toast.LENGTH_SHORT).show();
        sendBroadcast(intent);

       /* if (failedOps.size() == 0) return;
        mNotifyManager.cancelAll();
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(c);
        mBuilder.setContentTitle("Operation Unsuccessful");
        mBuilder.setContentText("Some files weren't %s successfully".replace("%s", move ? "moved" : "copied"));
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("failedOps", failedOps);
        intent.putExtra("move", move);
        PendingIntent pIntent = PendingIntent.getActivity(this, 101, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pIntent);
        mBuilder.setSmallIcon(R.drawable.ic_copy);
        mNotifyManager.notify(741, mBuilder.build());
        intent = new Intent("general_communications");
        intent.putExtra("failedOps", failedOps);
        intent.putExtra("move", move);
        sendBroadcast(intent);*/
    }

    /*public interface ProgressListener {
        void onUpdate(DataPackage dataPackage);

        void refresh();
    }*/

    public class LocalBinder extends Binder {
        public CopyService getService() {
            // Return this instance of LocalService so clients can call public methods
            return CopyService.this;
        }
    }

    public class Doback extends AsyncTask<Bundle, Void, Integer> {
        ArrayList<String> files;
        long totalBytes = 0L;
        String name;
        private boolean move;
        private Copy copy;
        private String FILE2;

        public Doback() {
        }

        protected Integer doInBackground(Bundle... p1) {
            FILE2 = p1[0].getString("FILE2");
            int id = p1[0].getInt("id");
            files = p1[0].getStringArrayList("files");
            move = p1[0].getBoolean("move");
            copy = new Copy();
            copy.execute(id, files, FILE2, move, p1[0].getInt("MODE"));

            // TODO: Implement this method
            return id;
        }

        @Override
        public void onPostExecute(Integer b) {
            publishResults("", 0, 0, b, 0, 0, true, move, FILE2);
            generateNotification(copy.failedFOps, move);

            Intent intent = new Intent("loadlist");
            sendBroadcast(intent);
            hash.put(b, false);
            boolean stop = true;
            for (int a : hash.keySet()) {
                if (hash.get(a)) stop = false;
            }
            if (!stop)
                stopSelf(b);
            else stopSelf();

        }

        class Copy {

            long totalBytes = 0L, copiedBytes = 0L;
            boolean calculatingTotalSize = false;
            ArrayList<HFile> failedFOps;
            ArrayList<String> toDelete;
            boolean copy_successful;
            long time = System.nanoTime() / 500000000;
            AsyncTask asyncTask;

            public Copy() {
                copy_successful = true;
                failedFOps = new ArrayList<>();
                toDelete = new ArrayList<>();
            }

            long getTotalBytes(final ArrayList<String> files, final int mode) {
                calculatingTotalSize = true;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        long totalBytes = 0l;
                        try {
                            for (int i = 0; i < files.size(); i++) {
                                String path = (files.get(i));
                                HFile f1 = new HFile();
                                f1.setMode(mode);
                                f1.setPath(files.get(i));
                                if (f1.isDirectory(files.get(i))) {
                                    totalBytes = totalBytes + f1.folderSize();
                                } else {
                                    totalBytes = totalBytes + f1.length();
                                }
                            }
                        } catch (Exception e) {
                        }
                        Copy.this.totalBytes = totalBytes;
                        calculatingTotalSize = false;
                    }
                }).run();

                return totalBytes;
            }

            public void execute(int id, final ArrayList<String> files, final String FILE2, final boolean move, int mode) {
                if (futils.checkFolder((FILE2), c) == 1) {
                    getTotalBytes(files, mode);

                    for (int i = 0; i < files.size(); i++) {

                        HFile f1 = new HFile();
                        f1.setMode(mode);
                        f1.setPath(files.get(i));

                        // Log.e("Copy", "basefile\t" + f1.getPath());
                        try {

                            if (hash.get(id)) {
                                HFile hFile = new HFile();
                                hFile.setMode(FILE2.startsWith("smb://") ? HFile.SMB_MODE : HFile.LOCAL_MODE);
                                hFile.setPath(FILE2);

                                copyFiles(f1, hFile, id, move);
                                //copyToDirectory(files.get(i), FILE2, id, move, mode);

                                if (move) {
                                    Intent intent1 = new Intent(c, DeleteService.class);
                                    ArrayList<String> deletedFile = new ArrayList<String>();
                                    deletedFile.clear();
                                    deletedFile.add(f1.getAbsolutePath());
                                    intent1.putStringArrayListExtra("FILE_PATHS", deletedFile);
                                    intent1.putExtra("MODE", f1.getMode());
                                    intent1.putExtra("ServiceType", "Hide");
                                    c.startService(intent1);
                                }

                            } else {
                                break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e("Copy", "Got exception checkout");

                            failedFOps.add(f1);
                            // for (int j = i + 1; j < files.size(); j++) failedFOps.add(files.get(j));
                            break;
                        }

                    }
                }
            }

            private void copyFiles(final HFile sourceFile, final HFile targetFile, final int id, final boolean move) throws IOException {
                // Log.e("Copy", sourceFile.getPath());

                try {

                    if (!hash.get(id)) return;

                    HFile old_file = new HFile();
                    old_file.setMode(sourceFile.getMode());

                    if (sourceFile.getMode() == HFile.SMB_MODE) {
                        if (sourceFile.isDirectory())
                            old_file.setPath(sourceFile.getPath() + "/");
                        else
                            old_file.setPath(sourceFile.getPath());

                    } else {
                        old_file.setPath(sourceFile.getPath());
                    }

                    HFile temp_dir = new HFile();
                    temp_dir.setMode(targetFile.getMode());
                    temp_dir.setPath(targetFile.getPath());

                    /*File old_file = new File(old); // source file
                    File temp_dir = new File(newDir); //targetfile*/

                    //  if (old_file.isFile() && temp_dir.isDirectory() && temp_dir.canWrite()) {
                    if (old_file.isFile() && temp_dir.isDirectory()) {
                        if (!hash.get(id)) return;

                        String file_name = sourceFile.getPath().substring(sourceFile.getPath().lastIndexOf("/"), sourceFile.getPath().length());

                        HFile cp_file = new HFile();
                        cp_file.setMode(targetFile.getMode());
                        cp_file.setPath(targetFile.getPath() + file_name);

                        try {

                            long size = old_file.length(); //new File(old_file.getPath()).length();

                            InputStream in = old_file.getInputStream();
                            OutputStream out = cp_file.getOutputStream(c);

                            // copy(in, out, size, id, new File(old_file.getPath()).getName(), move);
                            copy(in, out, size, id, old_file, move, targetFile.getPath());

                        } catch (FileNotFoundException e) {
                            Log.e("FileNotFoundException", e.getMessage());

                        } catch (IOException e) {
                            Log.e("IOException", e.getMessage());
                        }

                        //  } else if (old_file.isDirectory() && temp_dir.isDirectory() && temp_dir.canWrite()) {
                    } else if (old_file.isDirectory() && temp_dir.isDirectory()) {

                        String files[] = old_file.getListFiles();
                        String dir = targetFile.getPath() + sourceFile.getPath().substring(sourceFile.getPath().lastIndexOf("/"), sourceFile.getPath().length()); //target
                        int len = files.length;

                        HFile tFile = new HFile();
                        tFile.setMode(targetFile.getMode());
                        tFile.setPath(dir);

                        if (!tFile.exists())
                            tFile.mkdir(c);

                        for (int i = 0; i < len; i++) {
                            HFile sFile = new HFile();
                            sFile.setMode(sourceFile.getMode());
                            sFile.setPath(sourceFile.getPath() + "/" + files[i]);

                            copyFiles(sFile, tFile, id, move);
                        }

                        //copyToDirectory(old + "/" + files[i], dir, id, move, mode);

                    } else if (!temp_dir.canWrite())
                        return;

                } catch (Exception e) {
                    Log.e("Copy", "streams null");
                    failedFOps.add(sourceFile);
                    copy_successful = false;
                }
               /* } else if (!targetFile.canWrite()) {
                    Log.e("Copy", "streams null");
                    failedFOps.add(sourceFile);
                    copy_successful = false;
                }*/
            }

            public void copyToDirectory(String old, String newDir, int id, boolean move, int mode) {

                if (mode == 1)
                    copyToDirectorySMB(old, newDir, id, move, mode);
                else
                    copyToDirectoryNormal(old, newDir, id, move, mode);

            }

            private void copyToDirectoryNormal(String old, String newDir, int id, boolean move, int mode) {
                File old_file = new File(old); // source file
                File temp_dir = new File(newDir); //targetfile

                if (old_file.isFile() && temp_dir.isDirectory() && temp_dir.canWrite()) {
                    if (!hash.get(id)) return;

                    String file_name = old.substring(old.lastIndexOf("/"), old.length());
                    File cp_file = new File(newDir + file_name);

                    try {

                        long size = new File(old_file.getPath()).length();
                        FileInputStream in = new FileInputStream(old_file);
                        FileOutputStream out = new FileOutputStream(cp_file);

                        //copy(in, out, size, id, new File(old_file.getPath()).getName(), move);
                        HFile hFile = new HFile();
                        hFile.setMode(mode);
                        hFile.setPath(old_file.getPath());
                        copy(in, out, size, id, hFile, move, newDir);

                    } catch (FileNotFoundException e) {
                        Log.e("FileNotFoundException", e.getMessage());


                    } catch (IOException e) {
                        Log.e("IOException", e.getMessage());
                    }

                } else if (old_file.isDirectory() && temp_dir.isDirectory() && temp_dir.canWrite()) {
                    String files[] = old_file.list();
                    String dir = newDir + old.substring(old.lastIndexOf("/"), old.length()); //target
                    int len = files.length;

                    if (!new File(dir).mkdir())
                        return;

                    for (int i = 0; i < len; i++)
                        copyToDirectory(old + "/" + files[i], dir, id, move, mode);

                } else if (!temp_dir.canWrite())
                    return;


            }

            private void copyToDirectorySMB(String old, String newDir, int id, boolean move, int mode) {
                try {
                    SmbFile old_file = new SmbFile(old);
                    if (old_file.isDirectory())
                        old_file = new SmbFile(old + "/");

                    SmbFile temp_dir = new SmbFile(newDir);


                    if (old_file.isFile() && temp_dir.isDirectory()) {
                        String file_name = old.substring(old.lastIndexOf("/"), old.length());
                        SmbFile cp_file = new SmbFile(newDir + file_name);

                        try {

                            long size = new SmbFile(old_file.getPath()).length();

                            InputStream in = old_file.getInputStream();
                            OutputStream out = cp_file.getOutputStream();

                            //copy(in, out, size, id, new SmbFile(old_file.getPath()).getName(), move);
                            HFile hFile = new HFile();
                            hFile.setMode(mode);
                            hFile.setPath(old_file.getPath());
                            copy(in, out, size, id, hFile, move, newDir);

                        } catch (FileNotFoundException e) {
                            Log.e("FileNotFoundException", e.getMessage());


                        } catch (IOException e) {
                            Log.e("IOException", e.getMessage());

                        }

                    } else if (old_file.isDirectory() && temp_dir.isDirectory() && temp_dir.canWrite()) {
                        String files[] = old_file.list();
                        String dir = newDir + old.substring(old.lastIndexOf("/"), old.length());
                        int len = files.length;

                        if (!new SmbFile(dir).exists()) {
                            new SmbFile(dir).mkdir();
                            if (!new SmbFile(dir).exists())
                                return;
                        }

                        for (int i = 0; i < len; i++)
                            copyToDirectory(old + "/" + files[i], dir, id, move, mode);

                    } else if (!temp_dir.canWrite())
                        return;

                } catch (Exception e) {

                }

            }

            void calculateProgress(final String name, final long fileBytes, final int id, final long
                    size, final boolean move, final String target_filepath) {
                if (asyncTask != null && asyncTask.getStatus() == Status.RUNNING)
                    asyncTask.cancel(true);
                asyncTask = new AsyncTask<Void, Void, Void>() {
                    int p1, p2;

                    @Override
                    protected Void doInBackground(Void... voids) {
                        p1 = (int) ((copiedBytes / (float) totalBytes) * 100);
                        p2 = (int) ((fileBytes / (float) size) * 100);
                        if (calculatingTotalSize) p1 = 0;
                        return null;
                    }

                    @Override
                    public void onPostExecute(Void v) {
                        publishResults(name, p1, p2, id, totalBytes, copiedBytes, false, move, target_filepath);
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }

            void copy(InputStream stream, OutputStream outputStream, long size, int id, HFile name, boolean move, String target_filepath) throws IOException {
                long fileBytes = 0l;
                BufferedInputStream in = new BufferedInputStream(stream);
                BufferedOutputStream out = new BufferedOutputStream(outputStream);
                byte[] buffer = new byte[1024 * 60];
                int length;
                // copy the file content in bytes
                while ((length = in.read(buffer)) > 0) {
                    boolean b = hash.get(id);
                    if (b) {
                        out.write(buffer, 0, length);
                        copiedBytes += length;
                        fileBytes += length;
                        long time1 = System.nanoTime() / 500000000;
                        if (((int) time1) > ((int) (time))) {
                            calculateProgress(name.getName(), fileBytes, id, size, move, target_filepath);
                            time = System.nanoTime() / 500000000;
                        }
                    } else {
                        break;
                    }
                }

               /* if (move) {
                    Intent intent1 = new Intent(c, DeleteService.class);

                    ArrayList<String> deletedFile = new ArrayList<String>();
                    deletedFile.clear();
                    deletedFile.add(name.getAbsolutePath());

                    intent1.putStringArrayListExtra("FILE_PATHS", deletedFile);
                    intent1.putExtra("MODE", name.getMode());
                    intent1.putExtra("ServiceType", "Hide");
                    c.startService(intent1);
                }*/
                in.close();
                out.close();
                stream.close();
                outputStream.close();
            }
        }
    }
}
