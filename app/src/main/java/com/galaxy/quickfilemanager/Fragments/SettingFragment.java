package com.galaxy.quickfilemanager.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.galaxy.quickfilemanager.Utils.Constats;
import com.galaxy.quickfilemanager.Utils.PreferencesUtils;
import com.galaxy.quickfilemanager.R;


public class SettingFragment extends Fragment {

    private boolean hidden_state = false;
    private boolean thumbnail_state = false;
    private int sort_state;
    private Context mContext;
    private LinearLayout show_hidden_file_layout, show_image_preview_layout, sorting_layout;
    private CheckBox hidden_bx, thumbnail_bx;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, null);
        setHasOptionsMenu(true);

        mContext = getActivity();

        init(view);

        hidden_state = getMenu(Constats.PREFS_HIDDEN_FILES, false);
        thumbnail_state = getMenu(Constats.PREFS_IMAGE_THUMBNAIL, false);
        sort_state = getMenu(Constats.PREFS_SORT_FILES, 3);

        hidden_bx.setChecked(hidden_state);
        thumbnail_bx.setChecked(thumbnail_state);

        return view;
    }

    public void init(View view) {
        show_hidden_file_layout = (LinearLayout) view.findViewById(R.id.show_hidden_file_layout);
        show_image_preview_layout = (LinearLayout) view.findViewById(R.id.show_image_preview_layout);
        sorting_layout = (LinearLayout) view.findViewById(R.id.sorting_layout);

        hidden_bx = (CheckBox) view.findViewById(R.id.setting_hidden_box);
        thumbnail_bx = (CheckBox) view.findViewById(R.id.setting_thumbnail_box);

        onClicked();
    }

    public void onClicked() {

        hidden_bx.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                hidden_state = isChecked;
                saveMenu(Constats.PREFS_HIDDEN_FILES, hidden_state);
            }
        });

        show_hidden_file_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(hidden_state)
                    hidden_bx.setChecked(false);
                else
                    hidden_bx.setChecked(true);
            }
        });



        thumbnail_bx.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                thumbnail_state = isChecked;
                saveMenu(Constats.PREFS_IMAGE_THUMBNAIL, thumbnail_state);
            }
        });

        show_image_preview_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(thumbnail_state)
                    thumbnail_bx.setChecked(false);
                else
                    thumbnail_bx.setChecked(true);
            }
        });

        sorting_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                CharSequence[] options = {"None", "Alphabetical", "Type", "Size"};

                builder.setTitle("Sort by...");
                builder.setIcon(R.drawable.filter);
                builder.setSingleChoiceItems(options, sort_state, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int index) {
                        switch (index) {
                            case 0:
                                sort_state = 0;
                                saveMenu(Constats.PREFS_SORT_FILES, sort_state);
                                break;

                            case 1:
                                sort_state = 1;
                                saveMenu(Constats.PREFS_SORT_FILES, sort_state);
                                break;

                            case 2:
                                sort_state = 2;
                                saveMenu(Constats.PREFS_SORT_FILES, sort_state);
                                break;

                            case 3:
                                sort_state = 3;
                                saveMenu(Constats.PREFS_SORT_FILES, sort_state);
                                break;
                        }
                    }
                });

                builder.create().show();
            }
        });

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }


    private void saveMenu(String SettingName, boolean value) {
        PreferencesUtils.saveToPreference(mContext, SettingName, value);
    }

    private boolean getMenu(String SettingName, boolean type) {
        boolean value = (boolean) PreferencesUtils.getValueFromPreference(mContext, Boolean.class, SettingName, false);
        return value;
    }

    private void saveMenu(String SettingName, Integer value) {
        PreferencesUtils.saveToPreference(mContext, SettingName, value);
    }

    private Integer getMenu(String SettingName, int type) {
        Integer value = (Integer) PreferencesUtils.getValueFromPreference(mContext, Integer.class, SettingName, 3);
        return value;
    }
}
