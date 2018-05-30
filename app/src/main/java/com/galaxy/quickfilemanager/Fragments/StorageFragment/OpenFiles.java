package com.galaxy.quickfilemanager.Fragments.StorageFragment;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import com.galaxy.quickfilemanager.FileOperation.EventHandler;
import com.galaxy.quickfilemanager.FileOperation.FileManager;

import java.io.File;

/**
 * Created by Umiya Mataji on 3/24/2017.
 */

public class OpenFiles {

    private final Context mContext;
    private FileManager mFileMag;
    private EventHandler mHandler;
    private boolean zipOptionEnable = false;

    public OpenFiles(Context context) {
        mContext = context;
    }

    public void setHandler(FileManager fileManager, EventHandler eventHandler){
        this.mFileMag = fileManager;
        this.mHandler = eventHandler;
    }

    public void enableZipOptions(boolean enabled){
        this.zipOptionEnable = enabled;
    }

    public void open(String filePath){

        final File file = new File(filePath);
        final String item = file.getPath();
        
        String item_ext = null;
        try {
            item_ext = item.substring(item.lastIndexOf("."), item.length());
        } catch (IndexOutOfBoundsException e) {
            item_ext = "";
        }


        if (item_ext.equalsIgnoreCase(".mp3") ||
                item_ext.equalsIgnoreCase(".m4a") ||
                item_ext.equalsIgnoreCase(".ogg")) {

            openFileIntent(file, "audio/*");

        }  else if (item_ext.equalsIgnoreCase(".jpeg") ||
                item_ext.equalsIgnoreCase(".jpg") ||
                item_ext.equalsIgnoreCase(".png") ||
                item_ext.equalsIgnoreCase(".gif") ||
                item_ext.equalsIgnoreCase(".tiff")) {

            openFileIntent(file, "image/*");

        } else if (item_ext.equalsIgnoreCase(".m4v") ||
                item_ext.equalsIgnoreCase(".3gp") ||
                item_ext.equalsIgnoreCase(".wmv") ||
                item_ext.equalsIgnoreCase(".mp4") ||
                item_ext.equalsIgnoreCase(".ogg") ||
                item_ext.equalsIgnoreCase(".wav")) {

            openFileIntent(file, "video/*");

        }  else if (item_ext.equalsIgnoreCase(".zip")) {

            if(zipOptionEnable) {

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                AlertDialog alert;

                CharSequence[] option = {"View", "Extract here"};

                builder.setTitle("Zip");
                builder.setItems(option, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:

                                openFileIntent(file, "application/zip");
                                break;
                            case 1:
                                String dir = mFileMag.getCurrentDir();
                                mHandler.unZipFile(item, dir + "/");
                                break;
                        }
                    }
                });

                alert = builder.create();
                alert.show();

            }else{
                openFileIntent(file, "application/zip");
            }

        } else if (item_ext.equalsIgnoreCase(".pdf")) {
            openFileIntent(file, "application/pdf");
        }  else if (item_ext.equalsIgnoreCase(".apk")) {
            openFileIntent(file, "application/vnd.android.package-archive");
        } else if (item_ext.equalsIgnoreCase(".html")) {
            openFileIntent(file, "text/html");
        }  else if (item_ext.equalsIgnoreCase(".txt")) {
            openFileIntent(file, "text/*");
        }else {
            openFileIntent(file, "text/plain");
        }
    }


    public void openFileIntent(File file, String mimeType){
        try {
            Intent i = new Intent();
            i.setAction(Intent.ACTION_VIEW);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Uri uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);
            Uri uri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                uri = FileProvider.getUriForFile(mContext, mContext.getApplicationContext().getPackageName() + ".provider", file);
            } else {
                uri = Uri.fromFile(file);
            }

            i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            i.setDataAndType(uri, mimeType);

            try {
                mContext.startActivity(i);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(mContext, "Sorry, couldn't find a viewer for this kind of file",
                        Toast.LENGTH_SHORT).show();
            }

            // mContext.startActivity(i);

        }catch (Exception e){}

    }
}
