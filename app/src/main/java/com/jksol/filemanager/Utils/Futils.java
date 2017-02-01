package com.jksol.filemanager.Utils;

import android.content.Context;
import android.os.Build;

import java.io.File;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

/**
 * Created by Umiya Mataji on 1/30/2017.
 */

public class Futils {

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



}
