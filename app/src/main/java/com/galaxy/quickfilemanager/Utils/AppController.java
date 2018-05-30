package com.galaxy.quickfilemanager.Utils;

import android.app.Application;

import com.galaxy.quickfilemanager.Fragments.DialogDirectory.CopyPasteDialog;
import com.galaxy.quickfilemanager.Fragments.StorageFragment.StorageFragmentMain;
import com.galaxy.quickfilemanager.Interfaces.RecyclerViewContextmenuClick;
import com.galaxy.quickfilemanager.MainActivity;

/**
 * Created by satish on 4/2/16.
 */
public class AppController extends Application {
    private static AppController mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    public void setDialogBackButtonPressed(CopyPasteDialog.DialogBackButtonPressListener listener) {
        CopyPasteDialog.dialogBackButtonPressListener = listener;
    }

    public void setPasteLayoutPressed(CopyPasteDialog.PasteLayoutListener listener, String str) {
        CopyPasteDialog.pasteLayoutListener = listener;
    }

    public void setButtonBackPressed(MainActivity.ButtonBackPressListener listener) {
        MainActivity.buttonBackPressListener = listener;
    }

    public void setPasteLayoutPressed(StorageFragmentMain.PasteLayoutListener listener) {
        StorageFragmentMain.pasteLayoutListener = listener;
    }

    public void setRecyclerviewContextmenuClick(RecyclerViewContextmenuClick listener) {
        MainActivity.recyclerViewContextmenuClickListener = listener;
    }

}
