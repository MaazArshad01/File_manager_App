package com.jksol.filemanager.Utils;

import android.app.Application;
import android.text.TextUtils;

import com.jksol.filemanager.Interfaces.FragmentChange;
import com.jksol.filemanager.MainActivity;
import com.jksol.filemanager.Interfaces.RecyclerViewContextmenuClick;

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

    public void setButtonBackPressed(MainActivity.ButtonBackPressListener listener) {
        MainActivity.buttonBackPressListener = listener;
    }

    public void setRecyclerviewContextmenuClick(RecyclerViewContextmenuClick listener) {
        MainActivity.recyclerViewContextmenuClickListener = listener;
    }

}
