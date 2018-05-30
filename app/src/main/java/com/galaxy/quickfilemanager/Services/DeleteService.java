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
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.galaxy.quickfilemanager.Utils.AppController;
import com.galaxy.quickfilemanager.Utils.DataPackage;
import com.galaxy.quickfilemanager.Utils.FileUtil;
import com.galaxy.quickfilemanager.Utils.Futils;
import com.galaxy.quickfilemanager.Utils.HFile;
import com.galaxy.quickfilemanager.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class DeleteService extends Service {

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
    ProgressListener progressListener;
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
            int id1 = Integer.parseInt("678" + intent.getIntExtra("id", 1));
            mNotifyManager.notify(id1, mBuilder.build());
        }
    };

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate() {
        c = getApplicationContext();
        registerReceiver(receiver1, new IntentFilter("deletecancel"));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Bundle b = new Bundle();

        mNotifyManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);

       /* Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra("openprocesses", true);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        mBuilder.setContentIntent(pendingIntent);*/

        mBuilder.setContentTitle(getResources().getString(R.string.deleting))
                .setSmallIcon(R.drawable.delete);
        mBuilder.setContentText("Pending..");

        String ServiceType = intent.getStringExtra("ServiceType");

        if (ServiceType.equalsIgnoreCase("show")) {

            Toast.makeText(c, "Start deleting", Toast.LENGTH_SHORT).show();

            if (foreground) {
                startForeground(Integer.parseInt("678" + startId), mBuilder.build());
            }
        }

        ArrayList<String> files = intent.getStringArrayListExtra("FILE_PATHS");
        int mode = intent.getIntExtra("MODE", 0);

        b.putInt("id", startId);
        b.putStringArrayList("files", files);
        b.putInt("MODE", mode);
        b.putString("ServiceType", ServiceType);
        hash.put(startId, true);

        DataPackage intent1 = new DataPackage();
        intent1.setName(FileUtil.getFileNameFromPath(files.get(0)));
        intent1.setTotal(0);
        intent1.setDone(0);
        intent1.setId(startId);
        intent1.setP1(0);
        intent1.setCompleted(false);
        intent1.setServiceType(ServiceType);

        hash1.put(startId, intent1);

        hFile.setMode(mode);
        new Doback().execute(b);
        Log.d("Service number", String.valueOf(startId));
        // If we get killed, after returning from here, restart

        if (ServiceType.equalsIgnoreCase("show"))
            return START_STICKY;
        else
            return START_NOT_STICKY;

    }

    public void setProgressListener(ProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    private void publishResults(String a, int id, int total, int done, boolean completed, String serviceType) {
        if (hash.get(id)) {
            //notification
            mBuilder.setProgress(total, done, false);
            mBuilder.setOngoing(true);
            int title = R.string.deleting;

            mBuilder.setContentTitle(futils.getString(c, title));
            mBuilder.setContentText(new File(a).getName() + " " + done + "/" + total);
            int id1 = Integer.parseInt("678" + id);

            if (serviceType.equalsIgnoreCase("show"))
                mNotifyManager.notify(id1, mBuilder.build());

            if (total == done || total == 0) {
                mBuilder.setContentTitle("Deleting completed");

                mBuilder.setContentText("");
                mBuilder.setProgress(0, 0, false);
                mBuilder.setOngoing(false);
                mBuilder.setAutoCancel(true);

                if (serviceType.equalsIgnoreCase("show"))
                    mNotifyManager.notify(id1, mBuilder.build());

                publishCompletedResult(id1);
            }

            //for processviewer
            DataPackage intent = new DataPackage();
            intent.setName(a);
            intent.setTotal(total);
            intent.setDone(done);
            intent.setId(id);
            intent.setP1(done);

            intent.setCompleted(completed);
            hash1.put(id, intent);
            try {
                if (progressListener != null) {
                    progressListener.onUpdate(intent);
                    if (completed) progressListener.refresh();

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else publishCompletedResult(Integer.parseInt("678" + id));
    }

    public void publishCompletedResult(int id1) {
        try {
            mNotifyManager.cancel(id1);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return mBinder;
    }

    @Override
    public void onDestroy() {
        this.unregisterReceiver(receiver1);
    }

    void generateNotification(ArrayList<HFile> failedOps, boolean move, String serviceType) {

        mNotifyManager.cancelAll();


        Intent intent1 = new Intent("DeleteCompleted");
        intent1.putExtra("started", false);
        intent1.putExtra("completed", true);

        String msg;

        if (failedOps.size() == 0) {
            msg = "Deleted successfully";
        } else {
            msg = "Some files weren't deleted successfully";
        }
        //intent1.putExtra("delete_msg", msg);

        Toast.makeText(c, msg, Toast.LENGTH_SHORT).show();

        if (serviceType.equalsIgnoreCase("show"))
            sendBroadcast(intent1);

        /*if (failedOps.size() == 0) return;
        mNotifyManager.cancelAll();
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(c);
        mBuilder.setContentTitle("Operation Unsuccessful");
        mBuilder.setContentText("Some files weren't %s successfully".replace("%s", move ? "moved" : "copied"));
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("failedOps", failedOps);
        intent.putExtra("move", move);
        PendingIntent pIntent = PendingIntent.getActivity(this, 102, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pIntent);
        mBuilder.setSmallIcon(R.drawable.ic_copy);
        mNotifyManager.notify(742, mBuilder.build());
        intent = new Intent("general_communications");
        intent.putExtra("failedOps", failedOps);
        intent.putExtra("move", move);
        sendBroadcast(intent);*/
    }

    public interface ProgressListener {
        void onUpdate(DataPackage dataPackage);

        void refresh();
    }

    public class LocalBinder extends Binder {
        public DeleteService getService() {
            // Return this instance of LocalService so clients can call public methods
            return DeleteService.this;
        }
    }

    public class Doback extends AsyncTask<Bundle, Void, Integer> {
        ArrayList<String> files;
        private Delete delete;
        private String serviceType;

        public Doback() {
        }

        protected Integer doInBackground(Bundle... p1) {

            int id = p1[0].getInt("id");
            files = p1[0].getStringArrayList("files");
            serviceType = p1[0].getString("ServiceType");

            if (serviceType.equalsIgnoreCase("show")) {
                Intent intent = new Intent("DeleteCompleted");
                intent.putExtra("started", true);
                intent.putExtra("completed", false);
                sendBroadcast(intent);
            }

            delete = new Delete();
            delete.execute(id, files, p1[0].getInt("MODE"), serviceType);

            // TODO: Implement this method
            return id;
        }

        @Override
        public void onPostExecute(Integer b) {
            publishResults("", b, 0, 0, true, serviceType);
            generateNotification(delete.failedFOps, true, serviceType);

            hash.put(b, false);
            boolean stop = true;
            for (int a : hash.keySet()) {
                if (hash.get(a)) stop = false;
            }
            if (!stop)
                stopSelf(b);
            else stopSelf();

        }

        class Delete {

            ArrayList<HFile> failedFOps;
            ArrayList<String> toDelete;
            boolean delete_successful;
            boolean calculatingTotalSize = false;
            int totalFiles = 0;
            int totalDeletedFiles = 0;
            int totalFolders = 0;
            AsyncTask asyncTask;

            public Delete() {
                delete_successful = true;
                failedFOps = new ArrayList<>();
                toDelete = new ArrayList<>();
            }

            long getTotalFiles(final ArrayList<String> files, final int mode) {
                calculatingTotalSize = true;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int totalFiles = 0;
                        int totalFolder = 0;
                        try {
                            for (int i = 0; i < files.size(); i++) {

                                HFile f1 = new HFile();
                                f1.setMode(mode);
                                f1.setPath(files.get(i));
                                int[] count = f1.countInnerFolderFile(files.get(i));

                                if (count.length > 0)
                                    totalFiles += count[0];

                                if (count.length > 1)
                                    totalFolder += count[1];
                            }
                        } catch (Exception e) {
                        }

                        Delete.this.totalFiles = totalFiles;
                        Delete.this.totalFolders = totalFolder;
                        calculatingTotalSize = false;
                    }
                }).run();
                Log.d("Total files and Folders", "Folders :- " + totalFolders + " Files :- " + totalFiles);
                return totalFiles;
            }

            public void execute(int id, final ArrayList<String> files, int mode, String serviceType) {
                try {
                    getTotalFiles(files, mode);
                } catch (Exception e) {
                }
                for (int i = 0; i < files.size(); i++) {
                    HFile f1 = new HFile();
                    f1.setMode(mode);
                    f1.setPath(files.get(i));
                    try {

                        if (hash.get(id)) {
                            deleteTarget(f1, id, serviceType);
                            // copyFiles((f1), hFile, id, move);
                        } else {
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("Delete", "Got exception checkout :- " + e.getMessage());

                        failedFOps.add(f1);
                        // for (int j = i + 1; j < files.size(); j++) failedFOps.add(files.get(j));
                        break;
                    }
                }
            }

            public void deleteTarget(HFile hfile, int id, String serviceType) {
                HFile target = hfile;
                if (!hash.get(id)) return;

                // if (target.exists() && target.isFile() && target.canWrite()) {
                if (target.exists() && target.isFile()) {

                    if (target.getMode() != HFile.SMB_MODE)
                        deleteFiles(hfile);

                    target.delete(c);

                    //} else if (target.exists() && target.isDirectory() && target.canRead()) {
                } else if (target.exists() && target.isDirectory()) {
                    String[] file_list = target.list();

                    if (file_list != null && file_list.length == 0) {
                        target.delete(c);

                    } else if (file_list != null && file_list.length > 0) {

                        for (int i = 0; i < file_list.length; i++) {
                            HFile temp_f = new HFile();
                            temp_f.setMode(target.getMode());
                            temp_f.setPath(target.getAbsolutePath() + "/" + file_list[i]);
                            // File temp_f = new File(target.getAbsolutePath() + "/" + file_list[i]);

                            if (temp_f.isDirectory())
                                deleteTarget(temp_f, id, serviceType);
                            else if (temp_f.isFile()) {
                                totalDeletedFiles += 1;

                                if (target.getMode() != HFile.SMB_MODE)
                                    deleteFiles(temp_f);

                                temp_f.delete(c);
                                calculateProgress(temp_f.getName(), totalDeletedFiles, id, serviceType);
                            }
                        }
                    }

                    if (target.exists())
                        if (target.delete(c)) {
                            // totalDeletedFiles += 1;
                            calculateProgress(target.getName(), totalDeletedFiles, id, serviceType);
                        }
                }

            }

            void calculateProgress(final String name, final int currentDeletedFile, final int id, String serviceType) {
                publishResults(name, id, totalFiles, currentDeletedFile, false, serviceType);
            }

            public void deleteFiles(HFile hfile) {
                HFile file = hfile;

                Context context = AppController.getInstance().getApplicationContext();
                ContentResolver contentResolver = context.getContentResolver();

                String canonicalPath;
                try {
                    canonicalPath = file.getCanonicalPath();
                } catch (Exception e) {
                    canonicalPath = file.getAbsolutePath();
                }
                final Uri uri = MediaStore.Files.getContentUri("external");
                final int result = contentResolver.delete(uri,
                        MediaStore.Files.FileColumns.DATA + "=?", new String[]{canonicalPath});
                if (result == 0) {
                    final String absolutePath = file.getAbsolutePath();
                    if (!absolutePath.equals(canonicalPath)) {
                        contentResolver.delete(uri,
                                MediaStore.Files.FileColumns.DATA + "=?", new String[]{absolutePath});
                    }
                }
            }

        }
    }
}
