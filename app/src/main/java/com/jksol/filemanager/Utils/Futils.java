package com.jksol.filemanager.Utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jksol.filemanager.R;

import java.io.File;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

/**
 * Created by Umiya Mataji on 1/30/2017.
 */

public class Futils {

    private AsyncTask<Void, Void, Long> asyncTask;
    private AsyncTask<Void, Void, Integer[]> countFileFolder_AsyncTask;

    public static long folderSize(File directory) {
        long length = 0;
        try {
            for (File file : directory.listFiles()) {

                if (file.isFile())
                    length += file.length();
                else
                    length += folderSize(file);
            }
        } catch (Exception e) {
        }
        return length;
    }

    public static long folderSize(SmbFile directory) {
        long length = 0;
        try {
            for (SmbFile file : directory.listFiles()) {

                if (file.isFile())
                    length += file.length();
                else
                    length += folderSize(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return length;
    }

    public String readableFileSize(long size) {
        if (size <= 0)
            return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.##").format(size
                / Math.pow(1024, digitGroups))
                + " " + units[digitGroups];
    }

    public ArrayList<File> toFileArray(ArrayList<String> a) {
        ArrayList<File> b = new ArrayList<>();
        for (int i = 0; i < a.size(); i++) {
            b.add(new File(a.get(i).toString()));
        }
        return b;
    }

    public int checkFolder(final String f, Context context) {
        if (f == null) return 0;
        if (f.startsWith("smb://")) return 1;

        File folder = new File(f);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && FileUtil.isOnExtSdCard(folder, context)) {
            if (!folder.exists() || !folder.isDirectory()) {
                return 0;
            }

            // On Android 5, trigger storage access framework.
            if (FileUtil.isWritableNormalOrSaf(folder, context)) {
                return 1;

            }
        } else if (Build.VERSION.SDK_INT == 19 && FileUtil.isOnExtSdCard(folder, context)) {
            // Assume that Kitkat workaround works
            return 1;
        } else if (FileUtil.isWritable(new File(folder, "DummyFile"))) {
            return 1;
        } else {
            return 0;
        }
        return 0;
    }

    public String getString(Context c, int a) {
        return c.getResources().getString(a);
    }

    public void FilePropertyDialog(Context mContext, ArrayList<HFile> hFile) {

        final Dialog properties_dialog = new Dialog(mContext);
        properties_dialog.setCancelable(false);
        properties_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        properties_dialog.setContentView(R.layout.property_dialog);
        properties_dialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        properties_dialog.getWindow().getAttributes().windowAnimations = R.style.confirmDeleteAnimation;
        properties_dialog.show();

        //Field Init
        LinearLayout file_property_layout = (LinearLayout) properties_dialog.findViewById(R.id.file_property_layout);
        file_property_layout.setVisibility(View.VISIBLE);

        LinearLayout file_bottom_btns = (LinearLayout) properties_dialog.findViewById(R.id.file_bottom_btns);
        LinearLayout multiplefile_bottom_btns = (LinearLayout) properties_dialog.findViewById(R.id.multiplefile_bottom_btns);
        LinearLayout file_permission_layout = (LinearLayout) properties_dialog.findViewById(R.id.file_permission_layout);

        TextView file_dialog_cancel = (TextView) properties_dialog.findViewById(R.id.file_dialog_cancel);
        file_dialog_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                properties_dialog.dismiss();
            }
        });

        TextView multplefile_dialog_cancel = (TextView) properties_dialog.findViewById(R.id.multplefile_dialog_cancel);
        multplefile_dialog_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                properties_dialog.dismiss();
            }
        });

        TextView file_type = (TextView) properties_dialog.findViewById(R.id.file_type);
        ImageView file_icon = (ImageView) properties_dialog.findViewById(R.id.file_icon);
        TextView file_name = (TextView) properties_dialog.findViewById(R.id.file_name);
        TextView file_path = (TextView) properties_dialog.findViewById(R.id.file_path);
        TextView total_file_contain = (TextView) properties_dialog.findViewById(R.id.total_file_contain);
        TextView file_size = (TextView) properties_dialog.findViewById(R.id.file_size);
        TextView file_modified = (TextView) properties_dialog.findViewById(R.id.file_modified);
        TextView file_permission = (TextView) properties_dialog.findViewById(R.id.file_permission);

        if (hFile.size() == 1) {
            file_bottom_btns.setVisibility(View.VISIBLE);

            HFile hFile1 = hFile.get(0);

            if (hFile1.isFile()) {
                file_icon.setImageResource(R.drawable.file_default);
                file_type.setText(getString(mContext, R.string.file));
            } else {
                file_icon.setImageResource(R.drawable.folder_default);
                file_type.setText(getString(mContext, R.string.folder));
            }

            file_name.setText(hFile1.getName());
            file_path.setText(hFile1.getPath());

            ArrayList<HFile> oneHFile = new ArrayList<HFile>();
            oneHFile.add(hFile1);

            countFileFolder(mContext, oneHFile, total_file_contain);
            FileFolderSize(mContext, oneHFile, file_size);

            try {
                Date lastModDate = new Date(hFile1.getLastModified());
                file_modified.setText(Utils.convertTimeFromUnixTimeStamp(lastModDate.toString()));

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (SmbException e) {
                e.printStackTrace();
            }

            try {

                file_permission_layout.setVisibility(View.VISIBLE);
                StringBuilder stringBuilder = new StringBuilder();

                if (hFile1.canRead())
                    stringBuilder.append("Readable");

                if (hFile1.canWrite())
                    stringBuilder.append(", Writable");

                file_permission.setText(stringBuilder.toString());

            } catch (Exception e) {
                file_permission_layout.setVisibility(View.GONE);
            }


        } else {
            multiplefile_bottom_btns.setVisibility(View.VISIBLE);

            file_name.setText(getString(mContext, R.string.multiplefiles));

            file_icon.setImageResource(R.drawable.multiplefiles);
            file_type.setText(getString(mContext, R.string.multiplefiles));

            LinearLayout type_layout = (LinearLayout) properties_dialog.findViewById(R.id.type_layout);
            type_layout.setVisibility(View.GONE);

            LinearLayout copy_path_layout = (LinearLayout) properties_dialog.findViewById(R.id.copy_path_layout);
            copy_path_layout.setVisibility(View.GONE);

            LinearLayout more_datail_layout = (LinearLayout) properties_dialog.findViewById(R.id.more_datail_layout);
            more_datail_layout.setVisibility(View.GONE);

            file_path.setText(hFile.get(0).getParent());

            countFileFolder(mContext, hFile, total_file_contain);
            FileFolderSize(mContext, hFile, file_size);

        }
    }

    public void countFileFolder(final Context mContext, final ArrayList<HFile> hFiles, final TextView total_file_contain) {
        if (countFileFolder_AsyncTask != null && countFileFolder_AsyncTask.getStatus() == AsyncTask.Status.RUNNING)
            countFileFolder_AsyncTask.cancel(true);
        countFileFolder_AsyncTask = new AsyncTask<Void, Void, Integer[]>() {
            int p1, p2;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                total_file_contain.setText("Calculating");
            }

            @Override
            protected Integer[] doInBackground(Void... voids) {
                int totalFiles = 0;
                int totalFolders = 0;

                for (HFile hFile : hFiles) {
                    int totalfilefolders[] = hFile.countInnerFolderFile(hFile.getPath());
                    totalFiles += totalfilefolders[0];
                    totalFolders += totalfilefolders[1];
                }

                Integer arr[] = {totalFiles, totalFolders};

                return arr;
            }

            @Override
            protected void onPostExecute(Integer[] totalfilefolders) {
                super.onPostExecute(totalfilefolders);
                if (totalfilefolders.length > 0) {
                    if (totalfilefolders[0] == 0 && totalfilefolders[1] == 0)
                        total_file_contain.setText("Empty");
                    else
                        total_file_contain.setText(totalfilefolders[1] + " " + getString(mContext, R.string.folder) + ", " + totalfilefolders[0] + " " + getString(mContext, R.string.file));
                } else
                    total_file_contain.setText("Empty");
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void FileFolderSize(final Context mContext, final ArrayList<HFile> hFiles, final TextView file_size) {
        if (asyncTask != null && asyncTask.getStatus() == AsyncTask.Status.RUNNING)
            asyncTask.cancel(true);
        asyncTask = new AsyncTask<Void, Void, Long>() {


            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                file_size.setText("Calculating");
            }

            @Override
            protected Long doInBackground(Void... voids) {
                long totalSize = 0;

                for (HFile hFile : hFiles) {
                    totalSize += hFile.totalFolderSize();
                }

                return totalSize;
            }

            @Override
            protected void onPostExecute(Long totalFileSize) {
                super.onPostExecute(totalFileSize);
                file_size.setText(readableFileSize(totalFileSize));
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


}
