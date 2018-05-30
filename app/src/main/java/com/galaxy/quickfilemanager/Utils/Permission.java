package com.galaxy.quickfilemanager.Utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by Akhil on 8/5/2016.
 */
public class Permission {

    public static final int REQUEST_CODE_READ_EXTERNAL_STORAGE_ASK_PERMISSIONS = 101;
    public static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE_ASK_PERMISSIONS = 102;
    public final Context mContext;

    public Permission(Context mContext) {
        this.mContext = mContext;
    }

    public boolean isExternalStorageWritePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (mContext.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("Cantact Persion", "Permission is granted");
                return true;
            } else {
                // selectedImagePath = "";
                Log.v("Contact Persion", "Permission is revoked");
                ActivityCompat.requestPermissions((AppCompatActivity) mContext, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_EXTERNAL_STORAGE_ASK_PERMISSIONS);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation

            return true;
        }
    }

    public boolean checkExternalStorageReadPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (mContext.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("Cantact Persion", "Permission is granted");
                return true;
            } else {
                // selectedImagePath = "";
                Log.v("Contact Persion", "Permission is revoked");
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation

            return true;
        }
    }

    public boolean isExternalStorageReadPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (mContext.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("Cantact Persion", "Permission is granted");
                return true;
            } else {
                // selectedImagePath = "";
                Log.v("Contact Persion", "Permission is revoked");
                ActivityCompat.requestPermissions((AppCompatActivity) mContext, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_READ_EXTERNAL_STORAGE_ASK_PERMISSIONS);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation

            return true;
        }
    }


    public boolean isExternalStorageReadPermissionGranted_Fragment(Fragment activity) {

        if (Build.VERSION.SDK_INT >= 23) {
            if (mContext.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                activity.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_READ_EXTERNAL_STORAGE_ASK_PERMISSIONS);
                return false;
            }
        } else { // Permission is automatically granted on sdk<23 upon installation

            return true;
        }
    }

}
