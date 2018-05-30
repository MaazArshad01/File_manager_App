package com.galaxy.quickfilemanager.Fragments.DialogDirectory;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import com.galaxy.quickfilemanager.FileOperation.EventHandler;
import com.galaxy.quickfilemanager.Fragments.GalleryFragment.Adapter.ImagesListAdapter;
import com.galaxy.quickfilemanager.R;

/**
 * Created by Umiya Mataji on 2/7/2017.
 */

public class CopyPasteDialog extends DialogFragment {

    public static DialogBackButtonPressListener dialogBackButtonPressListener;
    public static PasteLayoutListener pasteLayoutListener;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private Button dialog_cancel, dialog_paste;
    private EventHandler mHandler;
    private ImagesListAdapter mImageHandler;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog = new Dialog(getActivity(), getTheme()) {
            @Override
            public void onBackPressed() {
                // Toast.makeText(getActivity(), "Backed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public boolean onKeyDown(int keyCode, KeyEvent event) {
                if (dialogBackButtonPressListener != null)
                    dialogBackButtonPressListener.onDialogBackButtonPressed(keyCode, event);

                return super.onKeyDown(keyCode, event);
            }
        };

        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        return dialog;
    }

    public void setHandler(EventHandler mHandler) {
        this.mHandler = mHandler;
    }

    public void setHandler(ImagesListAdapter mImageHandler) {
        this.mImageHandler = mImageHandler;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.copy_paste_layout, container, false);

        tabLayout = (TabLayout) rootview.findViewById(R.id.tabLayout);
        viewPager = (ViewPager) rootview.findViewById(R.id.masterViewPager);
        CustomAdapter adapter = new CustomAdapter(getChildFragmentManager());
        adapter.addFragment("Internal Storage", new InternalStorageDialogTabFragment());
        adapter.addFragment("SD Card", new SDCardDialogTabFragment());

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        dialog_cancel = (Button) rootview.findViewById(R.id.dialog_cancel);
        dialog_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mHandler != null)
                    mHandler.mDelegate.killMultiSelect(true);

                if (mImageHandler != null)
                    mImageHandler.killMultiSelect(true);

                dismiss();
            }
        });

        dialog_paste = (Button) rootview.findViewById(R.id.dialog_paste);
        dialog_paste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //paste();

                if (mHandler != null) {
                    if (pasteLayoutListener != null)
                        pasteLayoutListener.onPasteButtonPressed(mHandler);
                } else if (mImageHandler != null) {
                    if (pasteLayoutListener != null)
                        pasteLayoutListener.onPasteButtonPressed(mImageHandler);
                }


                dismiss();
            }
        });

        return rootview;
    }

    public void paste() {

        if (mHandler != null) {
            boolean multi_select = mHandler.hasMultiSelectData();
            if (multi_select) {
                mHandler.copyFileMultiSelect(InternalStorageDialogTabFragment.mFileMag.getCurrentDir());
            }
        }

        if (mImageHandler != null) {
            boolean multi_select = mImageHandler.hasMultiSelectData();
            if (multi_select) {
                mImageHandler.copyFileMultiSelect(InternalStorageDialogTabFragment.mFileMag.getCurrentDir());
            }
        }


    }

    public interface DialogBackButtonPressListener {
        void onDialogBackButtonPressed(int keyCode, KeyEvent event);
    }

    public interface PasteLayoutListener {
        void onPasteButtonPressed(EventHandler mEventHandler);
        void onPasteButtonPressed(ImagesListAdapter mImageHandler);

        void onCancelButtonPressed();
    }
}
