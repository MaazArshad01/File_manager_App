/*
    Open Manager, an open source file manager for the Android system
    Copyright (C) 2009, 2010, 2011  Joe Berria <nexesdevelopment@gmail.com>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.galaxy.quickfilemanager.FileOperation;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.galaxy.quickfilemanager.ApplicationBackup;
import com.galaxy.quickfilemanager.Fragments.BookmarkFragment.BookmarkUtils;
import com.galaxy.quickfilemanager.Fragments.DialogDirectory.CopyPasteDialog;
import com.galaxy.quickfilemanager.Fragments.DialogDirectory.InternalStorageDialogTabFragment;
import com.galaxy.quickfilemanager.Fragments.DialogDirectory.SDCardDialogTabFragment;
import com.galaxy.quickfilemanager.Fragments.StorageFragment.InternalStorageTabFragment;
import com.galaxy.quickfilemanager.Fragments.StorageFragment.SDCardTabFragment;
import com.galaxy.quickfilemanager.Fragments.StorageFragment.StorageFragmentMain;
import com.galaxy.quickfilemanager.HelpManager;
import com.galaxy.quickfilemanager.MainActivity;
import com.galaxy.quickfilemanager.Model.MediaFileListModel;
import com.galaxy.quickfilemanager.ProcessManager;
import com.galaxy.quickfilemanager.Services.CopyService;
import com.galaxy.quickfilemanager.Services.DeleteService;
import com.galaxy.quickfilemanager.Utils.AppController;
import com.galaxy.quickfilemanager.Utils.FileUtil;
import com.galaxy.quickfilemanager.Utils.Futils;
import com.galaxy.quickfilemanager.Utils.HFile;
import com.galaxy.quickfilemanager.Utils.MainActivityHelper;
import com.galaxy.quickfilemanager.Utils.Utils;
import com.galaxy.quickfilemanager.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class sits between the Main activity and the FileManager class.
 * To keep the FileManager class modular, this class exists to handle
 * UI events and communicate that information to the FileManger class
 * <p>
 * This class is responsible for the buttons onClick method. If one needs
 * to change the functionality of the buttons found from the Main activity
 * or add button logic, this is the class that will need to be edited.
 * <p>
 * This class is responsible for handling the information that is displayed
 * from the list view (the files and folder) with a a nested class TableRow.
 * The TableRow class is responsible for displaying which icon is shown for each
 * entry. For example a folder will display the folder icon, a Word doc will
 * display a word icon and so on. If more icons are to be added, the TableRow
 * class must be updated to display those changes.
 *
 * @author Joe Berria
 */
public class EventHandler implements OnClickListener {
    /*
     * Unique types to control which file operation gets
     * performed in the background
     */
    private static final int SEARCH_TYPE = 0x00;
    private static final int COPY_TYPE = 0x01;
    private static final int UNZIP_TYPE = 0x02;
    private static final int UNZIPTO_TYPE = 0x03;
    private static final int ZIP_TYPE = 0x04;
    private static final int DELETE_TYPE = 0x05;
    private static final int MANAGE_DIALOG = 0x06;
    private static final int RENAME_TYPE = 0x07;
    public final FileManager mFileMang;
    private final Context mContext;
    public ArrayList<String> mDataSource;
    public RecyclerViewTableRow mDelegate;
    //the list used to feed info into the array adapter and when multi-select is on
    LinearLayout hidden_rename, hidden_add_favourite, hidden_zip, hidden_share, hidden_copy, hidden_move, hidden_delete, hidden_detail;
    BookmarkUtils bookmarkUtils;

    /* public ArrayList<String> StorageFragmentMain.mMultiSelectData;
     public boolean StorageFragmentMain.multi_select_flag = false;
     public boolean StorageFragmentMain.delete_after_copy = false;*/
    private MainActivity mainActivity;
    private MainActivityHelper mainActivityHelper;
    private ArrayList<String> filteredUserList = null;
    private ThumbnailCreator mThumbnail;
    private boolean thumbnail_flag = true;
    private int mColor = Color.BLACK;
    private TextView mPathLabel;
    private TextView mInfoLabel;
    private LinearLayout hidden_buttons;
    private LinearLayout hidden_paste_layout;
    private FilesLoaderThread mFileLoaderThread;
    private LinearLayout empty_layout;
    private boolean isDownloadFolder = false, isLanConnetion = false;
    private AudioVideoThumbnailCreator mAudioVideoThumbnail;

    /**
     * Creates an EventHandler object. This object is used to communicate
     * most work from the Main activity to the FileManager class.
     *
     * @param context The context of the main activity e.g  Main
     * @param manager The FileManager object that was instantiated from Main
     */
    public EventHandler(Context context, final FileManager manager) {
        mContext = context;
        mFileMang = manager;


        mDataSource = new ArrayList<String>(mFileMang.setHomeDir
                (Environment.getExternalStorageDirectory().getPath()));
      /*  mDataSource = new ArrayList<String>(mFileMang.setHomeDir
                ("/storage/sdcard1"));*/
        filteredUserList = new ArrayList<String>();
        filteredUserList.addAll(mDataSource);
        bookmarkUtils = new BookmarkUtils(mContext);
    }

    /**
     * This constructor is called if the user has changed the screen orientation
     * and does not want the directory to be reset to home.
     *
     * @param context  The context of the main activity e.g  Main
     * @param manager  The FileManager object that was instantiated from Main
     * @param location The first directory to display to the user
     */
    public EventHandler(Context context, final FileManager manager, String location) {
        mContext = context;
        mFileMang = manager;

        mDataSource = new ArrayList<String>(mFileMang.getNextDir(location, true));
        filteredUserList = new ArrayList<String>();
        filteredUserList.addAll(mDataSource);
        bookmarkUtils = new BookmarkUtils(mContext);
    }

    public void setFragmentContext(InternalStorageTabFragment frg) {

        mainActivity = ((MainActivity) frg.getActivity());
        mainActivityHelper = new MainActivityHelper(mainActivity);
    }

    public void setFragmentContext(SDCardTabFragment frg) {

        mainActivity = ((MainActivity) frg.getActivity());
        mainActivityHelper = new MainActivityHelper(mainActivity);
    }

    public void setFragmentContext(InternalStorageDialogTabFragment frg) {

        mainActivity = ((MainActivity) frg.getActivity());
        mainActivityHelper = new MainActivityHelper(mainActivity);
    }

    public void setFragmentContext(SDCardDialogTabFragment frg) {

        mainActivity = ((MainActivity) frg.getActivity());
        mainActivityHelper = new MainActivityHelper(mainActivity);
    }

    public void setUpdateFileOperationLayout(LinearLayout hidden_buttons, LinearLayout hidden_paste_layout) {
        this.hidden_buttons = hidden_buttons;
        this.hidden_paste_layout = hidden_paste_layout;
    }

    public void setUpdateFileOperationViews(LinearLayout hidden_rename, LinearLayout hidden_add_favourite, LinearLayout hidden_zip,
                                            LinearLayout hidden_share, LinearLayout hidden_copy, LinearLayout hidden_move,
                                            LinearLayout hidden_delete, LinearLayout hidden_detail) {
        this.hidden_rename = hidden_rename;
        this.hidden_add_favourite = hidden_add_favourite;
        this.hidden_zip = hidden_zip;
        this.hidden_share = hidden_share;
        this.hidden_copy = hidden_copy;
        this.hidden_move = hidden_move;
        this.hidden_delete = hidden_delete;
        this.hidden_detail = hidden_detail;
    }

    /**
     * This method is called from the Main activity and this has the same
     * reference to the same object so when changes are made here or there
     * they will display in the same way.
     *
     * @param adapter The TableRow object
     */
    public void setListAdapter(RecyclerViewTableRow adapter) {
        mDelegate = adapter;
    }

    /**
     * This method is called from the Main activity and is passed
     * the TextView that should be updated as the directory changes
     * so the user knows which folder they are in.
     *
     * @param
     * @param
     * @param
     * @param isDownloadFolder
     * @param isLanConnetion
     */
    public void setStorageType(boolean isDownloadFolder, boolean isLanConnetion) {
        this.isDownloadFolder = isDownloadFolder;
        this.isLanConnetion = isLanConnetion;
    }

    /**
     * This method is called from the Main activity and is passed
     * the TextView that should be updated as the directory changes
     * so the user knows which folder they are in.
     *
     * @param path         The label to update as the directory changes
     * @param label        the label to update information
     * @param empty_layout
     */
    public void setUpdateLabels(TextView path, TextView label, LinearLayout empty_layout) {
        mPathLabel = path;
        mInfoLabel = label;
        this.empty_layout = empty_layout;

        mPathLabel.setText(mFileMang.getCurrentDir());
    }

    /**
     * @param color
     */
    public void setTextColor(int color) {
        mColor = color;
    }

    /**
     * Set this true and thumbnails will be used as the icon for image files. False will
     * show a default image.
     *
     * @param show
     */
    public void setShowThumbnails(boolean show) {
        thumbnail_flag = show;
    }

    /**
     * If you want to move a file (cut/paste) and not just copy/paste use this method to
     * tell the file manager to delete the old reference of the file.
     *
     * @param delete true if you want to move a file, false to copy the file
     */
    public void setDeleteAfterCopy(boolean delete) {
        StorageFragmentMain.delete_after_copy = delete;
    }

    /**
     * Indicates whether the user wants to select
     * multiple files or folders at a time.
     * <br><br>
     * false by default
     *
     * @return true if the user has turned on multi selection
     */
    public boolean isMultiSelected() {
        return StorageFragmentMain.multi_select_flag;
    }

    /**
     * Use this method to determine if the user has selected multiple files/folders
     *
     * @return returns true if the user is holding multiple objects (multi-select)
     */
    public boolean hasMultiSelectData() {
        return (StorageFragmentMain.mMultiSelectData != null && StorageFragmentMain.mMultiSelectData.size() > 0);
    }

    /**
     * Will search for a file then display all files with the
     * search parameter in its name
     *
     * @param name the name to search for
     */
    public void searchForFile(String name) {
        new BackgroundWork(SEARCH_TYPE).execute(name);
    }

    /**
     * Will delete the file name that is passed on a background
     * thread.
     *
     * @param name
     */
    public void deleteFile(String name) {
        new BackgroundWork(DELETE_TYPE).execute(name);
    }

    /**
     * Will copy a file or folder to another location.
     *
     * @param oldLocation from location
     * @param newLocation to location
     */
    public void copyFile(String oldLocation, String newLocation) {
        String[] data = {oldLocation, newLocation};

        new BackgroundWork(COPY_TYPE).execute(data);
    }

    /**
     * @param newLocation
     */
    public void copyFileMultiSelect(String newLocation) {
       /* String[] data;
        int index = 1;

        if (StorageFragmentMain.mMultiSelectData.size() > 0) {
            data = new String[StorageFragmentMain.mMultiSelectData.size() + 1];
            data[0] = newLocation;

            for (String s : StorageFragmentMain.mMultiSelectData)
                data[index++] = s;

            new BackgroundWork(COPY_TYPE).execute(data);
        }*/

        if (StorageFragmentMain.mMultiSelectData.size() > 0) {

            File file = new File(newLocation);
            int mode = mainActivityHelper.checkFolder(file, mContext);
            if (mode == 2) {
                Toast.makeText(mContext, "Please give a permission for file operation", Toast.LENGTH_SHORT).show();
                //  mainActivityHelper.guideDialogForLEXA(file.getPath());
            } else if (mode == 1 || mode == 0) {
                Intent intent1 = new Intent(mContext, CopyService.class);
                intent1.putExtra("FILE_PATHS", (StorageFragmentMain.mMultiSelectData));
                intent1.putExtra("COPY_DIRECTORY", newLocation);

                String path = StorageFragmentMain.mMultiSelectData.get(0);

                intent1.putExtra("MODE", path.startsWith("smb://") ? HFile.SMB_MODE : HFile.LOCAL_MODE);
                intent1.putExtra("move", StorageFragmentMain.delete_after_copy);
                mContext.startService(intent1);
            }

            try {
                mDelegate.killMultiSelect(true);
                updateDirectory(mFileMang.getNextDir(mFileMang.getCurrentDir(), true));
                StorageFragmentMain.delete_after_copy = false;
            } catch (Exception e) {
            }

        }
    }

    /**
     * @param newName
     */
    public void renameFileMultiSelect(String newName) {
        final String[] data;
        int index = 1;

        if (StorageFragmentMain.mMultiSelectData.size() > 0) {

            data = new String[StorageFragmentMain.mMultiSelectData.size() + 1];
            data[0] = newName;

            for (String s : StorageFragmentMain.mMultiSelectData) {
                data[index++] = s;
            }


            if (StorageFragmentMain.mMultiSelectData != null && !StorageFragmentMain.mMultiSelectData.isEmpty()) {

                if (StorageFragmentMain.mMultiSelectData.size() <= 1) {

                    int mode = mFileMang.isSmb() ? HFile.SMB_MODE : HFile.LOCAL_MODE;
                    String ext = "";

                    final HFile hFile0 = new HFile();
                    hFile0.setMode(mode);
                    hFile0.setPath(data[1]);

                    final HFile hFile1 = new HFile();
                    hFile1.setMode(mode);
                    if (mode == HFile.SMB_MODE)
                        hFile1.setPath(mFileMang.getCurrentDir() + data[0]);
                    else
                        hFile1.setPath(mFileMang.getCurrentDir() + "/" + data[0]);

                    try {
                        if (hFile0.isFile())
                            ext = data[1].substring(data[1].lastIndexOf("."), data[1].length());
                    } catch (Exception e) {
                    }

                    String temp = data[1].substring(0, data[1].lastIndexOf("/"));
                    hFile1.setPath(temp + "/" + newName + ext);

                    Operations.rename(hFile0, hFile1, false, mContext, new Operations.ErrorCallBack() {
                        @Override
                        public void exists(HFile file) {
                            Toast.makeText(mContext, R.string.msg_prompt_file_already_exits, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void launchSAF(HFile file) {

                        }

                        @Override
                        public void launchSAF(HFile file, HFile file1) {
                            // Toast.makeText(mContext, "SAF", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void done(final HFile hFile, final boolean b) {
                            mainActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (b) {
                                        Toast.makeText(mContext, R.string.renamesuccesful, Toast.LENGTH_SHORT).show();

                                        if (StorageFragmentMain.mMultiSelectData != null && !StorageFragmentMain.mMultiSelectData.isEmpty()) {
                                            StorageFragmentMain.multi_select_flag = false;
                                            StorageFragmentMain.mMultiSelectData.clear();
                                        }

                                        try {
                                            bookmarkUtils.replaceBookmark(hFile0.getFile(), hFile1.getFile());
                                        } catch (Exception e) {
                                        }

                                        mDelegate.killMultiSelect(true);

                                        updateDirectory(mFileMang.getNextDir(mFileMang.getCurrentDir(), true));
                                    } else
                                        Toast.makeText(mContext, R.string.operationunsuccesful, Toast.LENGTH_SHORT).show();
                                }
                            });
                            //Toast.makeText(mContext, R.string.operationunsuccesful, Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {

                    for (int i = 1; i < data.length; i++) {
                        int mode = mFileMang.isSmb() ? HFile.SMB_MODE : HFile.LOCAL_MODE;
                        String ext = "";

                        final HFile hFile0 = new HFile();
                        hFile0.setMode(mode);
                        hFile0.setPath(data[i]);

                        final HFile hFile1 = new HFile();
                        hFile1.setMode(mode);
                        if (mode == HFile.SMB_MODE)
                            hFile1.setPath(mFileMang.getCurrentDir() + data[0] + "" + i);
                        else
                            hFile1.setPath(mFileMang.getCurrentDir() + "/" + data[0] + "" + i);

                        try {
                            if (hFile0.isFile())
                                ext = data[i].substring(data[i].lastIndexOf("."), data[i].length());
                        } catch (Exception e) {
                        }

                        String temp = data[i].substring(0, data[i].lastIndexOf("/"));
                        hFile1.setPath(temp + "/" + newName + "" + i + ext);

                        Operations.rename(hFile0, hFile1, false, mContext, new Operations.ErrorCallBack() {
                            @Override
                            public void exists(HFile file) {
                                Toast.makeText(mContext, R.string.msg_prompt_file_already_exits, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void launchSAF(HFile file) {

                            }

                            @Override
                            public void launchSAF(HFile file, HFile file1) {
                                // Toast.makeText(mContext, "SAF", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void done(final HFile hFile, final boolean b) {
                                mainActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (b) {
                                            Toast.makeText(mContext, R.string.renamesuccesful, Toast.LENGTH_SHORT).show();

                                            if (StorageFragmentMain.mMultiSelectData != null && !StorageFragmentMain.mMultiSelectData.isEmpty()) {
                                                StorageFragmentMain.multi_select_flag = false;
                                                StorageFragmentMain.mMultiSelectData.clear();
                                            }

                                            try {
                                                bookmarkUtils.replaceBookmark(hFile0.getFile(), hFile1.getFile());
                                            } catch (Exception e) {
                                            }

                                            mDelegate.killMultiSelect(true);

                                            updateDirectory(mFileMang.getNextDir(mFileMang.getCurrentDir(), true));
                                        } else
                                            Toast.makeText(mContext, R.string.operationunsuccesful, Toast.LENGTH_SHORT).show();
                                    }
                                });
                                //Toast.makeText(mContext, R.string.operationunsuccesful, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
           /* if (StorageFragmentMain.mMultiSelectData != null && !StorageFragmentMain.mMultiSelectData.isEmpty())
                if (StorageFragmentMain.mMultiSelectData.size() <= 1) {
                    Log.d("Rename", "Newfile name :- " + data[0] + ", Old filename :- " + data[1]);
                    //rename_rtn = mFileMang.renameTarget(params[1], params[0]);
                    mainActivityHelper.rename(mFileMang.isSmb() ? HFile.SMB_MODE : HFile.LOCAL_MODE, data[1], data[0], null);
                } else {
                    for (int i = 1; i < data.length; i++) {
                        Log.d("Rename", "Newfile name :- " + data[0] + "" + i + ", Old filename :- " + data[i]);
                      //  rename_rtn = mFileMang.renameTarget(params[i], params[0] + "" + i);
                    }
                }*/


            //  new BackgroundWork(RENAME_TYPE).execute(data);
        }
    }

    /**
     * This will extract a zip file to the same directory.
     *
     * @param file the zip file name
     * @param path the path were the zip file will be extracted (the current directory)
     */
    public void unZipFile(String file, String path) {
        new BackgroundWork(UNZIP_TYPE).execute(file, path);
    }

    /**
     * This method will take a zip file and extract it to another
     * location
     *
     * @param name   the name of the of the new file (the dir name is used)
     * @param newDir the dir where to extract to
     * @param oldDir the dir where the zip file is
     */
    public void unZipFileToDir(String name, String newDir, String oldDir) {
        new BackgroundWork(UNZIPTO_TYPE).execute(name, newDir, oldDir);
    }

    /**
     * Creates a zip file
     *
     * @param zipPath the path to the directory you want to zip
     */
    public void zipFile(String zipPath) {
        // new BackgroundWork(ZIP_TYPE).execute(zipPath);

        String name = StorageFragmentMain.mMultiSelectData.get(0).substring(StorageFragmentMain.mMultiSelectData.get(0).lastIndexOf("/"), StorageFragmentMain.mMultiSelectData.get(0).length());
        try {
            name = name.substring(0, name.lastIndexOf("."));
        } catch (Exception e) {
        }

        String Location = zipPath + name + ".zip";
        String ZipLocation = Location;

        Intent intent2 = new Intent(mContext, ZipTask.class);
        intent2.putExtra("name", ZipLocation);
        intent2.putExtra("files", StorageFragmentMain.mMultiSelectData);
        mContext.startService(intent2);

        try {
            mDelegate.killMultiSelect(true);
            updateDirectory(mFileMang.getNextDir(mFileMang.getCurrentDir(), true));
        } catch (Exception e) {
        }

    }

    /**
     * this will stop our background thread that creates thumbnail icons
     * if the thread is running. this should be stopped when ever
     * we leave the folder the image files are in.
     */
    public void stopThumbnailThread() {
        if (mThumbnail != null) {
            mThumbnail.setCancelThumbnails(true);
            mThumbnail = null;
        }
    }

    /**
     * this will stop our background thread that creates thumbnail icons
     * if the thread is running. this should be stopped when ever
     * we leave the folder the image files are in.
     */
    public void stopFileLoadThread() {
        if (mFileLoaderThread != null) {
            mFileLoaderThread.setCancelFile(true);
            mFileLoaderThread = null;
        }
    }

    /**
     * This method, handles the button presses of the top buttons found
     * in the Main activity.
     */
    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.back_button:
                if (mFileMang.getCurrentDir() != "/") {
                    if (StorageFragmentMain.multi_select_flag) {
                        mDelegate.killMultiSelect(true);
                        //Toast.makeText(mContext, "Multi-select is now off", Toast.LENGTH_SHORT).show();
                    }

                    stopThumbnailThread();
                    updateDirectory(mFileMang.getPreviousDir());
                    if (mPathLabel != null)
                        mPathLabel.setText(mFileMang.getCurrentDir());
                }
                break;

            case R.id.home_button:
                if (StorageFragmentMain.multi_select_flag) {
                    mDelegate.killMultiSelect(true);
                    //Toast.makeText(mContext, "Multi-select is now off", Toast.LENGTH_SHORT).show();
                }

                stopThumbnailThread();
                updateDirectory(mFileMang.setHomeDir("/sdcard"));
                if (mPathLabel != null)
                    mPathLabel.setText(mFileMang.getCurrentDir());
                break;

            case R.id.info_button:
                Intent info = new Intent(mContext, DirectoryInfo.class);
                info.putExtra("PATH_NAME", mFileMang.getCurrentDir());
                mContext.startActivity(info);
                break;

            case R.id.help_button:
                Intent help = new Intent(mContext, HelpManager.class);
                mContext.startActivity(help);
                break;

            case R.id.manage_button:
                display_dialog(MANAGE_DIALOG);
                break;

            case R.id.multiselect_button:
                if (StorageFragmentMain.multi_select_flag) {
                    mDelegate.killMultiSelect(true);
                } else {
                    StorageFragmentMain.multi_select_flag = true;
                    if (hidden_buttons != null)
                        hidden_buttons.setVisibility(LinearLayout.VISIBLE);
                }
                break;

            case R.id.hidden_move:
            case R.id.hidden_copy:
                /* check if user selected objects before going further */
                if (StorageFragmentMain.mMultiSelectData == null || StorageFragmentMain.mMultiSelectData.isEmpty()) {
                    mDelegate.killMultiSelect(true);
                    break;
                }

                if (v.getId() == R.id.hidden_move)
                    StorageFragmentMain.delete_after_copy = true;

                mInfoLabel.setText("Holding " + StorageFragmentMain.mMultiSelectData.size() +
                        " file(s)");

                mDelegate.killMultiSelect(false);

                if (isDownloadFolder || isLanConnetion) {
                    if (StorageFragmentMain.hidden_paste_layout != null)
                        StorageFragmentMain.hidden_paste_layout.setVisibility(View.GONE);

                    try {
                        CopyPasteDialog copyPasteDialog = new CopyPasteDialog();
                        copyPasteDialog.setHandler(this);
                        copyPasteDialog.setCancelable(false);

                        if (copyPasteDialog != null && copyPasteDialog.isAdded()) {
                            copyPasteDialog.dismiss();
                            copyPasteDialog = null;
                        } else {
                            copyPasteDialog.show(InternalStorageTabFragment.mContext.getChildFragmentManager(), "FragmentDialog");
                        }
                    } catch (Exception e) {
                    }
                }

                break;

            case R.id.hidden_delete:
                /* check if user selected objects before going further */

                if (StorageFragmentMain.mMultiSelectData == null || StorageFragmentMain.mMultiSelectData.isEmpty()) {
                    mDelegate.killMultiSelect(true);
                    break;
                }

                final String[] data = new String[StorageFragmentMain.mMultiSelectData.size()];
                int at = 0;

                for (String string : StorageFragmentMain.mMultiSelectData)
                    data[at++] = string;

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage("Are you sure you want to delete " +
                        data.length + " files? This cannot be " +
                        "undone.");
                builder.setCancelable(false);
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // new BackgroundWork(DELETE_TYPE).execute(data);

                        if (StorageFragmentMain.mMultiSelectData.size() > 0) {

                            File file = new File(StorageFragmentMain.mMultiSelectData.get(0));
                            int mode = mainActivityHelper.checkFolder(file.getParentFile(), mContext);
                            if (mode == 2) {
                                Toast.makeText(mContext, "Please give a permission for file operation", Toast.LENGTH_SHORT).show();
                                // mainActivityHelper.guideDialogForLEXA(file.getPath());
                            } else if (mode == 1 || mode == 0) {
                                Intent intent1 = new Intent(mContext, DeleteService.class);
                                intent1.putExtra("FILE_PATHS", (StorageFragmentMain.mMultiSelectData));
                                intent1.putExtra("MODE", mFileMang.isSmb() ? HFile.SMB_MODE : HFile.LOCAL_MODE);
                                intent1.putExtra("ServiceType", "show");
                                mContext.startService(intent1);

                                mDelegate.killMultiSelect(true);
                            }
                        }
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDelegate.killMultiSelect(true);
                        dialog.cancel();
                    }
                });

                builder.create().show();
                break;

            case R.id.hidden_detail:
                if (!FileUtil.FileOperation) {
                    HFile hFile = new HFile();
                    hFile.setMode(mFileMang.isSmb() ? hFile.SMB_MODE : HFile.LOCAL_MODE);
                    hFile.setPath(StorageFragmentMain.mMultiSelectData.get(0));

                    FileManager.file_detail_dialog(mContext, hFile);
                    // FileManager.file_detail_dialog(mContext, StorageFragmentMain.mMultiSelectData.get(0));
                } else {
                    Futils futils = new Futils();
                    ArrayList<HFile> hFileArray = new ArrayList<HFile>();

                    for (String path : StorageFragmentMain.mMultiSelectData) {
                        HFile hFile = new HFile();
                        if (mFileMang.isSmb())
                            hFile.setMode(HFile.SMB_MODE);
                        else
                            hFile.setMode(HFile.LOCAL_MODE);
                        hFile.setPath(path);

                        hFileArray.add(hFile);
                    }

                    futils.FilePropertyDialog(mContext, hFileArray);
                }

                // if (StorageFragmentMain.mMultiSelectData.size() == 1) {
                    /*HFile hFile = new HFile();
                    if(mFileMang.isSmb())
                        hFile.setMode(HFile.SMB_MODE);
                    else
                        hFile.setMode(HFile.LOCAL_MODE);
                    hFile.setPath(StorageFragmentMain.mMultiSelectData.get(0));

                    futils.FilePropertyDialog(mContext, hFile);*/
                //}
                break;

            case R.id.hidden_zip:
                File file1 = new File(mFileMang.getCurrentDir());
                int mode1 = mainActivityHelper.checkFolder(file1.getParentFile(), mContext);
                if (mode1 == 2) {
                    Toast.makeText(mContext, "Please give a permission for file operation", Toast.LENGTH_SHORT).show();
                    // mainActivityHelper.guideDialogForLEXA(file.getPath());
                } else if (mode1 == 1 || mode1 == 0) {
                    String dir = mFileMang.getCurrentDir();
                    zipFile(dir);
                }


                break;

            case R.id.hidden_rename:

                if (StorageFragmentMain.mMultiSelectData.size() > 0) {

                    File file = new File(StorageFragmentMain.mMultiSelectData.get(0));
                    int mode = mainActivityHelper.checkFolder(file.getParentFile(), mContext);
                    if (mode == 2) {
                        Toast.makeText(mContext, "Please give a permission for file operation", Toast.LENGTH_SHORT).show();
                        // mainActivityHelper.guideDialogForLEXA(file.getPath());
                    } else if (mode == 1 || mode == 0) {
                        RenameDialog();
                    }

                }
                // bookmarkUtils.removeAllBookmark();
                break;

            case R.id.hidden_share:

                ArrayList<Uri> files = new ArrayList<Uri>();

                for (String path : StorageFragmentMain.mMultiSelectData) {
                    File file = new File(path);
                    Uri uri = Uri.fromFile(file);
                    files.add(uri);
                }

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND_MULTIPLE);
                intent.setType("*/*");
                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
                mContext.startActivity(Intent.createChooser(intent, "Share files"));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mDelegate.killMultiSelect(true);
                    }
                }, 1000);

                break;

            case R.id.hidden_add_favourite:

                bookmarkUtils.addToBookmarkList(StorageFragmentMain.mMultiSelectData);
                mDelegate.killMultiSelect(true);
                Toast.makeText(mContext, "Bookmarked successfuly", Toast.LENGTH_SHORT).show();
                // bookmarkUtils.removeBookmark(StorageFragmentMain.mMultiSelectData.get(0));
                break;
        }
    }

    public void RenameDialog() {

        final Dialog rename_dialog = new Dialog(mContext);
        rename_dialog.setCancelable(false);
        rename_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (FileUtil.FileOperation == true)
            rename_dialog.setContentView(R.layout.rename_filedirectory_dialog);
        else
            rename_dialog.setContentView(R.layout.rename_filedirectory_dialog_new);


        rename_dialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(Color.TRANSPARENT));
        rename_dialog.getWindow().getAttributes().windowAnimations = R.style.confirmDeleteAnimation;
        rename_dialog.show();

        final EditText edt_rename = (EditText) rename_dialog.findViewById(R.id.edt_rename);

        Button dialog_close = (Button) rename_dialog.findViewById(R.id.dialaog_cancel);
        dialog_close.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                rename_dialog.dismiss();
                mDelegate.killMultiSelect(true);
            }
        });

        TextView txt_dialog_label = (TextView) rename_dialog.findViewById(R.id.txt_dialog_label);
        if (StorageFragmentMain.mMultiSelectData.size() == 1) {
            txt_dialog_label.setText(mContext.getResources().getString(R.string.rename));

            String name = StorageFragmentMain.mMultiSelectData.get(0).substring(StorageFragmentMain.mMultiSelectData.get(0).lastIndexOf("/"), StorageFragmentMain.mMultiSelectData.get(0).length());
            name = name.substring(1);
            try {
                name = name.substring(0, name.lastIndexOf("."));
            } catch (Exception e) {
            }

            edt_rename.setText(name);
            edt_rename.setSelection(0, name.length());
        } else {
            txt_dialog_label.setText(mContext.getResources().getString(R.string.batchrename));
        }

        Button dialog_ok = (Button) rename_dialog.findViewById(R.id.dialog_ok);
        dialog_ok.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(edt_rename.getText().toString())) {
                    edt_rename.setError("Please enter the name");
                } else {
                    edt_rename.setError(null);
                    renameFileMultiSelect(edt_rename.getText().toString());
                    rename_dialog.dismiss();
                           /* String[] stockArr = new String[StorageFragmentMain.mMultiSelectData.size()];
                            stockArr = StorageFragmentMain.mMultiSelectData.toArray(stockArr);
                            new BackgroundWork(RENAME_TYPE).execute(stockArr);*/
                }
            }
        });

    }

    /**
     * will return the data in the ArrayList that holds the dir contents.
     *
     * @param position the indext of the arraylist holding the dir content
     * @return the data in the arraylist at position (position)
     */
    public String getData(int position) {

        if (position > mDataSource.size() - 1 || position < 0)
            return null;

        return mDataSource.get(position);
    }

    /**
     * called to update the file contents as the user navigates there
     * phones file system.
     *
     * @param content an ArrayList of the file/folders in the current directory.
     */
    public void updateDirectory(ArrayList<String> content) {
        // LinearLayout hidden_layout;

        if (content.size() == 0)
            empty_layout.setVisibility(View.VISIBLE);
        else
            empty_layout.setVisibility(View.GONE);

        if (!mDataSource.isEmpty())
            mDataSource.clear();

        for (String data : content)
            mDataSource.add(data);

        filteredUserList = new ArrayList<String>();
        filteredUserList.addAll(mDataSource);

        mDelegate.notifyDataSetChanged();

        if (StorageFragmentMain.mMultiSelectData != null) {
            if (StorageFragmentMain.mMultiSelectData.size() > 0) {
                if (hidden_paste_layout != null)
                    hidden_paste_layout.setVisibility(LinearLayout.VISIBLE);
                StorageFragmentMain.multi_select_flag = false;
            } else {
                if (hidden_paste_layout != null)
                    hidden_paste_layout.setVisibility(LinearLayout.GONE);
                StorageFragmentMain.multi_select_flag = false;
            }
        }
    }

    /**
     * This private method is used to display options the user can select when
     * the tool box button is pressed. The WIFI option is commented out as it doesn't
     * seem to fit with the overall idea of the application. However to display it, just
     * uncomment the below code and the code in the AndroidManifest.xml file.
     */
    private void display_dialog(int type) {
        AlertDialog.Builder builder;
        AlertDialog dialog;

        switch (type) {
            case MANAGE_DIALOG:
                //un-comment WIFI Info here and in the manifest file
                //to display WIFI info. Also uncomment and change case number below
                CharSequence[] options = {"Process Info", /*"Wifi Info",*/ "Application backup"};

                builder = new AlertDialog.Builder(mContext);
                builder.setTitle("Tool Box");
                builder.setIcon(R.drawable.toolbox);
                builder.setItems(options, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int index) {
                        Intent i;

                        switch (index) {
                            case 0:
                                i = new Intent(mContext, ProcessManager.class);
                                mContext.startActivity(i);
                                break;
        /*
                            case 1:
    							i = new Intent(context, WirelessManager.class);
    							context.startActivity(i);
    							break;
    	*/
                            case 1:
                                i = new Intent(mContext, ApplicationBackup.class);
                                mContext.startActivity(i);
                                break;
                        }
                    }
                });
                dialog = builder.create();
                dialog.show();
                break;
        }
    }

    public ArrayList<String> getSelectedItems() {
        if (StorageFragmentMain.mMultiSelectData.size() > 0)
            return StorageFragmentMain.mMultiSelectData;

        return null;
    }

    private static class ViewHolder {
        TextView topView;
        TextView bottomView;
        ImageView icon;
        ImageView mSelect;    //multi-select check mark icon
    }

    /**
     * A nested class to handle displaying a custom view in the ListView that
     * is used in the Main activity. If any icons are to be added, they must
     * be implemented in the getView method. This class is instantiated once in Main
     * and has no reason to be instantiated again.
     *
     * @author Joe Berria
     */
    public class TableRow extends ArrayAdapter<String> {
        private final int KB = 1024;
        private final int MG = KB * KB;
        private final int GB = MG * KB;
        private String display_size;
        private ArrayList<Integer> positions;
        private LinearLayout hidden_layout;

        public TableRow() {
            super(mContext, R.layout.tablerow, mDataSource);
        }

        public void addMultiPosition(int index, String path) {
            if (positions == null)
                positions = new ArrayList<Integer>();

            if (StorageFragmentMain.mMultiSelectData == null) {
                positions.add(index);
                add_multiSelect_file(path);

            } else if (StorageFragmentMain.mMultiSelectData.contains(path)) {
                if (positions.contains(index))
                    positions.remove(new Integer(index));

                StorageFragmentMain.mMultiSelectData.remove(path);

            } else {
                positions.add(index);
                add_multiSelect_file(path);
            }

            notifyDataSetChanged();
        }

        /**
         * This will turn off multi-select and hide the multi-select buttons at the
         * bottom of the view.
         *
         * @param clearData if this is true any files/folders the user selected for multi-select
         *                  will be cleared. If false, the data will be kept for later use. Note:
         *                  multi-select copy and move will usually be the only one to pass false,
         *                  so we can later paste it to another folder.
         */
        public void killMultiSelect(boolean clearData) {
            // hidden_layout = (LinearLayout) ((Activity) mContext).findViewById(R.id.hidden_buttons);
            hidden_layout.setVisibility(LinearLayout.GONE);
            StorageFragmentMain.multi_select_flag = false;

            if (positions != null && !positions.isEmpty())
                positions.clear();

            if (clearData)
                if (StorageFragmentMain.mMultiSelectData != null && !StorageFragmentMain.mMultiSelectData.isEmpty())
                    StorageFragmentMain.mMultiSelectData.clear();

            notifyDataSetChanged();
        }

        public String getFilePermissions(File file) {
            String per = "-";

            if (file.isDirectory())
                per += "d";
            if (file.canRead())
                per += "r";
            if (file.canWrite())
                per += "w";

            return per;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder mViewHolder;
            int num_items = 0;
            String temp = mFileMang.getCurrentDir();
            File file = new File(temp + "/" + mDataSource.get(position));
            String[] list = file.list();

            if (list != null)
                num_items = list.length;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.
                        getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.tablerow, parent, false);

                mViewHolder = new ViewHolder();
                mViewHolder.topView = (TextView) convertView.findViewById(R.id.top_view);
                mViewHolder.bottomView = (TextView) convertView.findViewById(R.id.bottom_view);
                mViewHolder.icon = (ImageView) convertView.findViewById(R.id.row_image);
                mViewHolder.mSelect = (ImageView) convertView.findViewById(R.id.multiselect_icon);

                convertView.setTag(mViewHolder);

            } else {
                mViewHolder = (ViewHolder) convertView.getTag();
            }

            if (positions != null && positions.contains(position))
                mViewHolder.mSelect.setVisibility(ImageView.VISIBLE);
            else
                mViewHolder.mSelect.setVisibility(ImageView.GONE);

            mViewHolder.topView.setTextColor(mColor);
            mViewHolder.bottomView.setTextColor(mColor);

            if (mThumbnail == null)
                mThumbnail = new ThumbnailCreator(52, 52);

            if (file != null && file.isFile()) {
                String ext = file.toString();
                String sub_ext = ext.substring(ext.lastIndexOf(".") + 1);

    			/* This series of else if statements will determine which
                 * icon is displayed
    			 */
                if (sub_ext.equalsIgnoreCase("pdf")) {
                    mViewHolder.icon.setImageResource(R.drawable.pdf);

                } else if (sub_ext.equalsIgnoreCase("mp3") ||
                        sub_ext.equalsIgnoreCase("wma") ||
                        sub_ext.equalsIgnoreCase("m4a") ||
                        sub_ext.equalsIgnoreCase("m4p")) {

                    mViewHolder.icon.setImageResource(R.drawable.music);

                } else if (sub_ext.equalsIgnoreCase("png") ||
                        sub_ext.equalsIgnoreCase("jpg") ||
                        sub_ext.equalsIgnoreCase("jpeg") ||
                        sub_ext.equalsIgnoreCase("gif") ||
                        sub_ext.equalsIgnoreCase("tiff")) {

                    if (thumbnail_flag && file.length() != 0) {
                        Bitmap thumb = mThumbnail.isBitmapCached(file.getPath());

                        if (thumb == null) {

                            final Handler handle = new Handler(new Handler.Callback() {
                                public boolean handleMessage(Message msg) {
                                    notifyDataSetChanged();
                                    return true;
                                }
                            });

                            try {
                                mThumbnail.createNewThumbnail(mDataSource, mFileMang.getCurrentDir(), handle);

                                if (!mThumbnail.isAlive())
                                    mThumbnail.start();
                            } catch (Exception e) {
                            }

                        } else {
                            mViewHolder.icon.setImageBitmap(thumb);
                        }

                    } else {
                        mViewHolder.icon.setImageResource(R.drawable.image);
                    }

                } else if (sub_ext.equalsIgnoreCase("zip") ||
                        sub_ext.equalsIgnoreCase("gzip") ||
                        sub_ext.equalsIgnoreCase("gz")) {

                    mViewHolder.icon.setImageResource(R.drawable.zip);

                } else if (sub_ext.equalsIgnoreCase("m4v") ||
                        sub_ext.equalsIgnoreCase("wmv") ||
                        sub_ext.equalsIgnoreCase("3gp") ||
                        sub_ext.equalsIgnoreCase("mp4")) {

                    mViewHolder.icon.setImageResource(R.drawable.movies);

                } else if (sub_ext.equalsIgnoreCase("doc") ||
                        sub_ext.equalsIgnoreCase("docx")) {

                    mViewHolder.icon.setImageResource(R.drawable.word);

                } else if (sub_ext.equalsIgnoreCase("xls") ||
                        sub_ext.equalsIgnoreCase("xlsx")) {

                    mViewHolder.icon.setImageResource(R.drawable.excel);

                } else if (sub_ext.equalsIgnoreCase("ppt") ||
                        sub_ext.equalsIgnoreCase("pptx")) {

                    mViewHolder.icon.setImageResource(R.drawable.ppt);

                } else if (sub_ext.equalsIgnoreCase("html")) {
                    mViewHolder.icon.setImageResource(R.drawable.html32);

                } else if (sub_ext.equalsIgnoreCase("xml")) {
                    mViewHolder.icon.setImageResource(R.drawable.xml32);

                } else if (sub_ext.equalsIgnoreCase("conf")) {
                    mViewHolder.icon.setImageResource(R.drawable.config32);

                } else if (sub_ext.equalsIgnoreCase("apk")) {
                    mViewHolder.icon.setImageResource(R.drawable.appicon);

                } else if (sub_ext.equalsIgnoreCase("jar")) {
                    mViewHolder.icon.setImageResource(R.drawable.jar32);

                } else {
                    mViewHolder.icon.setImageResource(R.drawable.text);
                }

            } else if (file != null && file.isDirectory()) {
                if (file.canRead() && file.list().length > 0)
                    mViewHolder.icon.setImageResource(R.drawable.folder_full);
                else
                    mViewHolder.icon.setImageResource(R.drawable.folder);
            }

            String permission = getFilePermissions(file);

            if (file.isFile()) {
                double size = file.length();
                if (size > GB)
                    display_size = String.format("%.2f Gb ", (double) size / GB);
                else if (size < GB && size > MG)
                    display_size = String.format("%.2f Mb ", (double) size / MG);
                else if (size < MG && size > KB)
                    display_size = String.format("%.2f Kb ", (double) size / KB);
                else
                    display_size = String.format("%.2f bytes ", (double) size);

                if (file.isHidden())
                    mViewHolder.bottomView.setText("(hidden) | " + display_size + " | " + permission);
                else
                    mViewHolder.bottomView.setText(display_size + " | " + permission);

            } else {
                if (file.isHidden())
                    mViewHolder.bottomView.setText("(hidden) | " + num_items + " items | " + permission);
                else
                    mViewHolder.bottomView.setText(num_items + " items | " + permission);
            }

            mViewHolder.topView.setText(file.getName());

            return convertView;
        }

        private void add_multiSelect_file(String src) {
            if (StorageFragmentMain.mMultiSelectData == null)
                StorageFragmentMain.mMultiSelectData = new ArrayList<String>();

            StorageFragmentMain.mMultiSelectData.add(src);
        }
    }

    public class RecyclerViewTableRow extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_LOADING = 1;

        private final int KB = 1024;
        private final int MG = KB * KB;
        private final int GB = MG * KB;

        private String display_size;
        private ArrayList<Integer> positions;
        private AsyncTask<Void, Void, Long> asyncTask;
        private boolean changeLayout = true;

        public void addMultiPosition(int index, String path) {
            if (positions == null)
                positions = new ArrayList<Integer>();

            if (StorageFragmentMain.mMultiSelectData == null) {
                positions.add(index);
                add_multiSelect_file(path);

            } else if (StorageFragmentMain.mMultiSelectData.contains(path)) {
                if (positions.contains(index))
                    positions.remove(new Integer(index));

                StorageFragmentMain.mMultiSelectData.remove(path);

            } else {
                positions.add(index);
                add_multiSelect_file(path);
            }

            notifyDataSetChanged();
        }

        private void add_multiSelect_file(String src) {
            if (StorageFragmentMain.mMultiSelectData == null)
                StorageFragmentMain.mMultiSelectData = new ArrayList<String>();

            StorageFragmentMain.mMultiSelectData.add(src);
        }

        /**
         * This will turn off multi-select and hide the multi-select buttons at the
         * bottom of the view.
         *
         * @param clearData if this is true any files/folders the user selected for multi-select
         *                  will be cleared. If false, the data will be kept for later use. Note:
         *                  multi-select copy and move will usually be the only one to pass false,
         *                  so we can later paste it to another folder.
         */
        public void killMultiSelect(boolean clearData) {
            try {
                if (hidden_buttons != null)
                    hidden_buttons.setVisibility(LinearLayout.GONE);

                StorageFragmentMain.multi_select_flag = false;

                if (positions != null && !positions.isEmpty())
                    positions.clear();

                if (clearData)
                    if (StorageFragmentMain.mMultiSelectData != null && !StorageFragmentMain.mMultiSelectData.isEmpty())
                        StorageFragmentMain.mMultiSelectData.clear();

                if (StorageFragmentMain.mMultiSelectData != null) {
                    if (StorageFragmentMain.mMultiSelectData.size() > 0) {
                        if (hidden_paste_layout != null)
                            hidden_paste_layout.setVisibility(LinearLayout.VISIBLE);
                        StorageFragmentMain.multi_select_flag = false;
                    } else {
                        if (hidden_paste_layout != null)
                            hidden_paste_layout.setVisibility(LinearLayout.GONE);
                        StorageFragmentMain.multi_select_flag = false;
                    }
                }

                notifyDataSetChanged();
            } catch (Exception e) {
            }
        }

        public String getFilePermissions(HFile file) {
            String per = "-";

            if (file.isDirectory())
                per += "d";
            if (file.canRead())
                per += "r";
            if (file.canWrite())
                per += "w";

            return per;
        }

        public void chanageLayout(boolean changeLayout) {
            this.changeLayout = changeLayout;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            if (changeLayout)
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tablerow, parent, false);
            else
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tablerow_grid, parent, false);

            return new DataViewHolder(view);
        }

        public void disableFileOperationButton() {

            // Start Enable-Disable Detail Layout
            // LinearLayout hidden_detail = (LinearLayout) ((Activity) mContext).findViewById(R.id.hidden_detail);
            // LinearLayout hidden_add_favourite = (LinearLayout) ((Activity) mContext).findViewById(R.id.hidden_add_favourite);
            if (!FileUtil.FileOperation)
                if (StorageFragmentMain.mMultiSelectData != null && StorageFragmentMain.mMultiSelectData.size() == 1) {
                    hidden_detail.setClickable(true);
                    hidden_detail.setAlpha((float) 1);

                    hidden_add_favourite.setClickable(true);
                    hidden_add_favourite.setAlpha((float) 1);
                } else {
                    hidden_detail.setClickable(false);
                    hidden_detail.setAlpha((float) 0.5);

                    hidden_add_favourite.setClickable(false);
                    hidden_add_favourite.setAlpha((float) 0.5);
                }
            // End Enable-Disable Detail Layout


            //  Start Enable-Disable Detail Layout
            //  LinearLayout hidden_zip = (LinearLayout) ((Activity) mContext).findViewById(R.id.hidden_zip);
            //  LinearLayout hidden_move = (LinearLayout) ((Activity) mContext).findViewById(R.id.hidden_move);
            //  LinearLayout hidden_copy = (LinearLayout) ((Activity) mContext).findViewById(R.id.hidden_copy);
            //  LinearLayout hidden_delete = (LinearLayout) ((Activity) mContext).findViewById(R.id.hidden_delete);
            //  LinearLayout hidden_renmae = (LinearLayout) ((Activity) mContext).findViewById(R.id.hidden_rename);

            if (StorageFragmentMain.mMultiSelectData != null && StorageFragmentMain.mMultiSelectData.size() > 0) {
                hidden_rename.setClickable(true);
                hidden_rename.setAlpha((float) 1);

                hidden_move.setClickable(true);
                hidden_move.setAlpha((float) 1);

                hidden_copy.setClickable(true);
                hidden_copy.setAlpha((float) 1);

                hidden_delete.setClickable(true);
                hidden_delete.setAlpha((float) 1);

                hidden_zip.setClickable(true);
                hidden_zip.setAlpha((float) 1);


            } else {
                hidden_rename.setClickable(false);
                hidden_rename.setAlpha((float) 0.5);

                hidden_move.setClickable(false);
                hidden_move.setAlpha((float) 0.5);

                hidden_copy.setClickable(false);
                hidden_copy.setAlpha((float) 0.5);

                hidden_delete.setClickable(false);
                hidden_delete.setAlpha((float) 0.5);

                hidden_zip.setClickable(false);
                hidden_zip.setAlpha((float) 0.5);
            }
            // End Enable-Disable Detail Layout


            // Start Enable-Disable Share Layout
            boolean isDir = false;
            // LinearLayout hidden_share = (LinearLayout) ((Activity) mContext).findViewById(R.id.hidden_share);

            if (StorageFragmentMain.mMultiSelectData != null && StorageFragmentMain.mMultiSelectData.size() > 0) {
                for (String files : StorageFragmentMain.mMultiSelectData) {
                    File file = new File(files);
                    if (file.isDirectory())
                        isDir = true;
                }
                if (!isDir) {
                    hidden_share.setClickable(true);
                    hidden_share.setAlpha((float) 1);
                } else {
                    hidden_share.setClickable(false);
                    hidden_share.setAlpha((float) 0.5);
                }

            } else {
                hidden_share.setClickable(false);
                hidden_share.setAlpha((float) 0.5);
            }

            // End Enable-Disable Share Layout
        }

        public void filter(String charText) {
            charText = charText.toLowerCase(Locale.getDefault());
            mDataSource.clear();
            if (charText.length() == 0) {
                mDataSource.addAll(filteredUserList);
                updateDirectory(filteredUserList);
            } else {
                for (String filename : filteredUserList) {

                    Pattern pp = Pattern.compile("^.*" + charText.toLowerCase() + ".*");
                    Matcher m = pp.matcher(filename.toLowerCase());
                    if (m.matches()) {
                        mDataSource.add(filename);
                    }

                  /*  if (filename.indexOf(charText) > 0)
                        mDataSource.add(filename);*/
                    /*if (filename.contains(charText)) {

                    }*/
                }
            }
            notifyDataSetChanged();
        }

        private void setFileIcon(HFile file, DataViewHolder mViewHolder) {
            String ext = file.getName().toString();
            String sub_ext = ext.substring(ext.lastIndexOf(".") + 1);

                    /* This series of else if statements will determine which
                     * icon is displayed
                     */
            if (sub_ext.equalsIgnoreCase("pdf")) {
                mViewHolder.icon.setImageResource(R.drawable.pdf);

            } else if (sub_ext.equalsIgnoreCase("mp3") ||
                    sub_ext.equalsIgnoreCase("wma") ||
                    sub_ext.equalsIgnoreCase("m4a") ||
                    sub_ext.equalsIgnoreCase("m4p")) {

                mViewHolder.icon.setImageResource(R.drawable.music);

            } else if (sub_ext.equalsIgnoreCase("png") ||
                    sub_ext.equalsIgnoreCase("jpg") ||
                    sub_ext.equalsIgnoreCase("jpeg") ||
                    sub_ext.equalsIgnoreCase("gif") ||
                    sub_ext.equalsIgnoreCase("tiff")) {

                if (thumbnail_flag && file.length() != 0) {
                    Bitmap thumb = mThumbnail.isBitmapCached(file.getPath());

                    if (thumb == null) {

                        final Handler handle = new Handler(new Handler.Callback() {
                            public boolean handleMessage(Message msg) {
                                notifyDataSetChanged();
                                return true;
                            }
                        });

                        try {
                            mThumbnail.createNewThumbnail(mDataSource, mFileMang.getCurrentDir(), handle);

                            if (!mThumbnail.isAlive())
                                mThumbnail.start();
                        } catch (Exception e) {
                        }

                    } else {
                        mViewHolder.icon.setImageBitmap(thumb);
                    }

                } else {
                    mViewHolder.icon.setImageResource(R.drawable.image);
                }

            } else if (sub_ext.equalsIgnoreCase("zip") ||
                    sub_ext.equalsIgnoreCase("gzip") ||
                    sub_ext.equalsIgnoreCase("gz")) {

                mViewHolder.icon.setImageResource(R.drawable.zip);

            } else if (sub_ext.equalsIgnoreCase("m4v") ||
                    sub_ext.equalsIgnoreCase("wmv") ||
                    sub_ext.equalsIgnoreCase("3gp") ||
                    sub_ext.equalsIgnoreCase("mp4")) {

                mViewHolder.icon.setImageResource(R.drawable.movies);

            } else if (sub_ext.equalsIgnoreCase("doc") ||
                    sub_ext.equalsIgnoreCase("docx")) {

                mViewHolder.icon.setImageResource(R.drawable.word);

            } else if (sub_ext.equalsIgnoreCase("xls") ||
                    sub_ext.equalsIgnoreCase("xlsx")) {

                mViewHolder.icon.setImageResource(R.drawable.excel);

            } else if (sub_ext.equalsIgnoreCase("ppt") ||
                    sub_ext.equalsIgnoreCase("pptx")) {

                mViewHolder.icon.setImageResource(R.drawable.ppt);

            } else if (sub_ext.equalsIgnoreCase("html")) {
                mViewHolder.icon.setImageResource(R.drawable.html32);

            } else if (sub_ext.equalsIgnoreCase("xml")) {
                mViewHolder.icon.setImageResource(R.drawable.xml32);

            } else if (sub_ext.equalsIgnoreCase("conf")) {
                mViewHolder.icon.setImageResource(R.drawable.config32);

            } else if (sub_ext.equalsIgnoreCase("apk")) {

                if (file.isSmb()) {
                    mViewHolder.icon.setImageResource(R.drawable.appicon);
                } else {
                    try {
                        Bitmap bitmap = new Utils().GetIcon(mContext, file.getPath());
                        if (bitmap != null)
                            mViewHolder.icon.setImageBitmap(bitmap);
                        else
                            mViewHolder.icon.setImageResource(R.drawable.appicon);
                    } catch (Exception e) {
                        mViewHolder.icon.setImageResource(R.drawable.appicon);
                    }
                }

            } else if (sub_ext.equalsIgnoreCase("jar")) {
                mViewHolder.icon.setImageResource(R.drawable.jar32);

            } else {
                mViewHolder.icon.setImageResource(R.drawable.text);
            }
        }

        private void setFileIcon(String filename, String file, DataViewHolder mViewHolder) {
            String ext = filename;
            String sub_ext = ext.substring(ext.lastIndexOf(".") + 1);

            /* This series of else if statements will determine which
             * icon is displayed
             */
            if (sub_ext.equalsIgnoreCase("pdf")) {
                mViewHolder.icon.setImageResource(R.drawable.pdf);

            } else if (sub_ext.equalsIgnoreCase("mp3") ||
                    sub_ext.equalsIgnoreCase("wma") ||
                    sub_ext.equalsIgnoreCase("ogg") ||
                    sub_ext.equalsIgnoreCase("m4a") ||
                    sub_ext.equalsIgnoreCase("m4p")) {

                File imgFile = new File(file);

                Glide
                        .with(mContext)
                        .load(imgFile)
                        .centerCrop()
                        .error(R.drawable.music)
                        .placeholder(R.drawable.music)
                        .crossFade()
                        .into(mViewHolder.icon);

                // mViewHolder.icon.setImageResource(R.drawable.music);

            } else if (sub_ext.equalsIgnoreCase("png") ||
                    sub_ext.equalsIgnoreCase("jpg") ||
                    sub_ext.equalsIgnoreCase("jpeg") ||
                    sub_ext.equalsIgnoreCase("gif") ||
                    sub_ext.equalsIgnoreCase("tiff")) {

                if (thumbnail_flag && file.length() != 0) {
                    Bitmap thumb = mThumbnail.isBitmapCached(file);

                    if (thumb == null) {
                        final Handler handle = new Handler(new Handler.Callback() {
                            public boolean handleMessage(Message msg) {
                                notifyDataSetChanged();
                                return true;
                            }
                        });

                        try {
                            mThumbnail.createNewThumbnail(mDataSource, mFileMang.getCurrentDir(), handle);

                            if (!mThumbnail.isAlive())
                                mThumbnail.start();
                        } catch (Exception e) {
                        }

                    } else {
                        mViewHolder.icon.setImageBitmap(thumb);
                    }

                } else {
                    mViewHolder.icon.setImageResource(R.drawable.image);
                }

            } else if (sub_ext.equalsIgnoreCase("zip") ||
                    sub_ext.equalsIgnoreCase("gzip") ||
                    sub_ext.equalsIgnoreCase("gz")) {

                mViewHolder.icon.setImageResource(R.drawable.zip);

            } else if (sub_ext.equalsIgnoreCase("m4v") ||
                    sub_ext.equalsIgnoreCase("wmv") ||
                    sub_ext.equalsIgnoreCase("3gp") ||
                    sub_ext.equalsIgnoreCase("mp4")) {


                File imgFile = new File(file);
                Glide
                        .with(AppController.getInstance().getApplicationContext())
                        .load(imgFile)
                        .centerCrop()
                        .error(R.drawable.movies)
                        .placeholder(R.drawable.movies)
                        .crossFade()
                        .into(mViewHolder.icon);
                // mViewHolder.icon.setImageResource(R.drawable.movies);

            } else if (sub_ext.equalsIgnoreCase("doc") ||
                    sub_ext.equalsIgnoreCase("docx")) {

                mViewHolder.icon.setImageResource(R.drawable.word);

            } else if (sub_ext.equalsIgnoreCase("xls") ||
                    sub_ext.equalsIgnoreCase("xlsx")) {

                mViewHolder.icon.setImageResource(R.drawable.excel);

            } else if (sub_ext.equalsIgnoreCase("ppt") ||
                    sub_ext.equalsIgnoreCase("pptx")) {

                mViewHolder.icon.setImageResource(R.drawable.ppt);

            } else if (sub_ext.equalsIgnoreCase("html")) {
                mViewHolder.icon.setImageResource(R.drawable.html32);

            } else if (sub_ext.equalsIgnoreCase("xml")) {
                mViewHolder.icon.setImageResource(R.drawable.xml32);

            } else if (sub_ext.equalsIgnoreCase("conf")) {
                mViewHolder.icon.setImageResource(R.drawable.config32);

            } else if (sub_ext.equalsIgnoreCase("apk")) {
                // mViewHolder.icon.setImageResource(R.drawable.appicon);

                if (file.startsWith("smb://")) {
                    mViewHolder.icon.setImageResource(R.drawable.appicon);
                } else {
                    try {
                        Bitmap bitmap = new Utils().GetIcon(mContext, file);
                        if (bitmap != null)
                            mViewHolder.icon.setImageBitmap(bitmap);
                        else
                            mViewHolder.icon.setImageResource(R.drawable.appicon);
                    } catch (Exception e) {
                        mViewHolder.icon.setImageResource(R.drawable.appicon);
                    }
                }

            } else if (sub_ext.equalsIgnoreCase("jar")) {
                mViewHolder.icon.setImageResource(R.drawable.jar32);
            } else {
                mViewHolder.icon.setImageResource(R.drawable.text);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            if (holder instanceof DataViewHolder) {

                DataViewHolder mViewHolder = (DataViewHolder) holder;

                disableFileOperationButton();

                if (StorageFragmentMain.multi_select_flag)
                    mViewHolder.multiselect_check.setVisibility(View.VISIBLE);
                else
                    mViewHolder.multiselect_check.setVisibility(View.GONE);

                if (positions != null && positions.contains(position)) {
                    if (changeLayout)
                        mViewHolder.multiselect_check.setImageResource(R.drawable.ic_checked_round);
                    else
                        mViewHolder.multiselect_check.setImageResource(R.drawable.ic_checked_round);
                    // mViewHolder.mSelect.setVisibility(ImageView.VISIBLE);
                } else {
                    if (changeLayout)
                        mViewHolder.multiselect_check.setImageResource(R.drawable.ic_uncheck_round);
                    else
                        mViewHolder.multiselect_check.setImageResource(R.drawable.ic_uncheck_round);
                    // mViewHolder.mSelect.setVisibility(ImageView.GONE);
                }

                mViewHolder.topView.setTextColor(mColor);
                mViewHolder.bottomView.setTextColor(mColor);

                int num_items = 0;
                String temp = mFileMang.getCurrentDir();

                if (!mFileMang.isSmb()) {

                    /*HFile file = new HFile();
                    file.setMode(mFileMang.isSmb() ? HFile.SMB_MODE : HFile.LOCAL_MODE);
                    file.setPath(temp + "/" + mDataSource.get(position));*/
                    File file = new File(temp + "/" + mDataSource.get(position));

                    if (mFileMang.isSmb())
                        thumbnail_flag = false;

                    // Image Thumbnail Code
                    if (mThumbnail == null)
                        mThumbnail = new ThumbnailCreator(52, 52);

                    if (file != null && file.isFile()) {
                        // setFileIcon(file, mViewHolder);
                        setFileIcon(mDataSource.get(position), file.getPath(), mViewHolder);
                    } else if (file != null && file.isDirectory()) {
                        mViewHolder.icon.setImageResource(R.drawable.folder_default); // Not Empty folder
                    }


                    String per = "-";
                    if (file.isDirectory())
                        per += "d";
                    if (file.canRead())
                        per += "r";
                    if (file.canWrite())
                        per += "w";

                    String permission = per; //getFilePermissions(file);

                    if (file.isFile()) {
                        double size = file.length();
                        if (size > GB)
                            display_size = String.format("%.2f Gb ", (double) size / GB);
                        else if (size < GB && size > MG)
                            display_size = String.format("%.2f Mb ", (double) size / MG);
                        else if (size < MG && size > KB)
                            display_size = String.format("%.2f Kb ", (double) size / KB);
                        else
                            display_size = String.format("%.2f bytes ", (double) size);

                        if (file.isHidden()) {
                            mViewHolder.bottomView.setText(display_size);
                            mViewHolder.icon.setAlpha((float) 0.5);
                        } else {
                            mViewHolder.bottomView.setText(display_size);
                            mViewHolder.icon.setAlpha((float) 1);
                        }

                    } else {

                        String[] list = file.list();

                        if (list != null)
                            num_items = list.length;

                        if (file.isHidden()) {
                            mViewHolder.bottomView.setText(num_items + " items");
                            mViewHolder.icon.setAlpha((float) 0.5);
                        } else {
                            mViewHolder.bottomView.setText(num_items + " items");
                            mViewHolder.icon.setAlpha((float) 1);
                        }
                    }

                    Date lastModDate = null;
                    try {
                        lastModDate = new Date(file.lastModified());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mViewHolder.creation_datetime.setText(Utils.convertTimeFromUnixTimeStamp(lastModDate.toString()));
                    mViewHolder.topView.setText(mDataSource.get(position));

                } else {

                    loadNetworkFiles(position, mViewHolder);  // Smooth Working Function
                    //loadNetworkFiles2(position, mViewHolder);   // Hybrid Function ( On testing)
                }
            }
        }

        public void loadNetworkFiles2(int position, DataViewHolder mViewHolder) {

            int num_items = 0;
            String temp = mFileMang.getCurrentDir();
            HFile file = new HFile();
            file.setMode(mFileMang.isSmb() ? HFile.SMB_MODE : HFile.LOCAL_MODE);
            file.setPath(temp + "/" + mDataSource.get(position));
            // File file = new File(temp + "/" + mDataSource.get(position));

            if (mThumbnail == null)
                mThumbnail = new ThumbnailCreator(52, 52);

            if (file != null && file.isFile()) {

                //  setFileIcon(file, mViewHolder);
                setFileIcon(mDataSource.get(position), file.getPath(), mViewHolder);

            } else if (file != null && file.isDirectory()) {
                mViewHolder.icon.setImageResource(R.drawable.folder_default); // Not Empty folder
                   /* if (file.canRead() && file.list().length > 0)
                        mViewHolder.icon.setImageResource(R.drawable.folder_default); // Not Empty folder
                    else
                        mViewHolder.icon.setImageResource(R.drawable.folder); // Empty folder*/
            }

            String permission = getFilePermissions(file);

            if (file.isFile()) {
                double size = file.length();
                if (size > GB)
                    display_size = String.format("%.2f Gb ", (double) size / GB);
                else if (size < GB && size > MG)
                    display_size = String.format("%.2f Mb ", (double) size / MG);
                else if (size < MG && size > KB)
                    display_size = String.format("%.2f Kb ", (double) size / KB);
                else
                    display_size = String.format("%.2f bytes ", (double) size);

                if (file.isHidden()) {
                    mViewHolder.bottomView.setText("(hidden) | " + display_size + " | " + permission);
                    mViewHolder.icon.setAlpha((float) 0.5);
                } else {
                    mViewHolder.bottomView.setText(display_size + " | " + permission);
                    mViewHolder.icon.setAlpha((float) 1);
                }

            } else {

                String[] list = file.list();

                if (list != null)
                    num_items = list.length;

                if (file.isHidden()) {
                    mViewHolder.bottomView.setText("(hidden) | " + num_items + " items | " + permission);
                    mViewHolder.icon.setAlpha((float) 0.5);
                } else {
                    mViewHolder.bottomView.setText(num_items + " items | " + permission);
                    mViewHolder.icon.setAlpha((float) 1);
                }
            }

            Date lastModDate = null;
            try {
                lastModDate = new Date(file.getLastModified());
            } catch (Exception e) {
                e.printStackTrace();
            }
            mViewHolder.creation_datetime.setText(Utils.convertTimeFromUnixTimeStamp(lastModDate.toString()));

            mViewHolder.topView.setText(mDataSource.get(position));

        }

        public void loadNetworkFiles(int position, DataViewHolder mViewHolder) {
            String temp = mFileMang.getCurrentDir();
            String path = temp + mDataSource.get(position);

            if (!mDataSource.get(position).equalsIgnoreCase("Empty")) {

                if (mFileLoaderThread == null)
                    mFileLoaderThread = new FilesLoaderThread();

                if (mFileLoaderThread != null) {

                    MediaFileListModel data = mFileLoaderThread.isFileCached(path);

                    if (data == null) {

                        final Handler handle = new Handler(new Handler.Callback() {
                            public boolean handleMessage(Message msg) {
                                notifyDataSetChanged();
                                return true;
                            }
                        });

                        try {
                            mFileLoaderThread.createNewFile(mDataSource, mFileMang.getCurrentDir(), handle);

                            if (!mFileLoaderThread.isAlive())
                                mFileLoaderThread.start();
                        } catch (Exception e) {
                        }

                    } else {
                        //Load data

                        if (mThumbnail == null)
                            mThumbnail = new ThumbnailCreator(52, 52);


                        if (data != null && data.isDirectory()) {
                            mViewHolder.icon.setImageResource(R.drawable.folder_default);
                        } else {
                            setFileIcon(mDataSource.get(position), path, mViewHolder);
                        }

                        String permission = data.getFilePermission();

                        if (data.isDirectory()) {
                            if (data.isHidden()) {
                                mViewHolder.bottomView.setText("(hidden) | " + data.isFileListCount() + " items | " + permission);
                                mViewHolder.icon.setAlpha((float) 0.5);
                            } else {
                                mViewHolder.bottomView.setText(data.isFileListCount() + " items | " + permission);
                                mViewHolder.icon.setAlpha((float) 1);
                            }

                        } else {

                            if (data.isHidden()) {
                                mViewHolder.bottomView.setText("(hidden) | " + data.getFileSize() + " | " + permission);
                                mViewHolder.icon.setAlpha((float) 0.5);
                            } else {
                                mViewHolder.bottomView.setText(data.getFileSize() + " | " + permission);
                                mViewHolder.icon.setAlpha((float) 1);
                            }
                        }

                        mViewHolder.creation_datetime.setText(Utils.convertTimeFromUnixTimeStamp(data.getFileCreatedTimeDatel().toString()));
                        mViewHolder.topView.setText(data.getFileName());

                        //mViewHolder.icon.setImageBitmap(thumb);
                    }

                } else {
                    mViewHolder.icon.setImageResource(R.drawable.image);
                }
            } else {
                mViewHolder.topView.setText("Empty");
                mViewHolder.creation_datetime.setText("Empty");
            }
        }

        public void loadData(final Context mContext, final ArrayList<HFile> hFiles, final TextView file_size) {
            if (asyncTask != null && asyncTask.getStatus() == AsyncTask.Status.RUNNING)
                asyncTask.cancel(true);
            asyncTask = new AsyncTask<Void, Void, Long>() {


                @Override
                protected void onPreExecute() {
                    super.onPreExecute();

                }

                @Override
                protected Long doInBackground(Void... voids) {
                    long totalSize = 0;


                    return totalSize;
                }

                @Override
                protected void onPostExecute(Long totalFileSize) {
                    super.onPostExecute(totalFileSize);

                }

            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        @Override
        public int getItemCount() {
            return mDataSource == null ? 0 : mDataSource.size();
        }

        @Override
        public int getItemViewType(int position) {
            return mDataSource.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
        }

        public class DataViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

            private final TextView topView;
            private final TextView bottomView;
            private final ImageView icon;
            private final ImageView mSelect;
            private final ImageView multiselect_check;
            private final TextView creation_datetime;

            public DataViewHolder(final View itemLayoutView) {
                super(itemLayoutView);

                topView = (TextView) itemLayoutView.findViewById(R.id.top_view);
                bottomView = (TextView) itemLayoutView.findViewById(R.id.bottom_view);
                creation_datetime = (TextView) itemLayoutView.findViewById(R.id.creation_datetime);
                icon = (ImageView) itemLayoutView.findViewById(R.id.row_image);
                multiselect_check = (ImageView) itemLayoutView.findViewById(R.id.multiselect_check);
                mSelect = (ImageView) itemLayoutView.findViewById(R.id.multiselect_icon);
                itemLayoutView.setOnCreateContextMenuListener(this);
            }

            @Override
            public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
                if (MainActivity.recyclerViewContextmenuClickListener != null)
                    MainActivity.recyclerViewContextmenuClickListener.ContextmenuClick(contextMenu, view, contextMenuInfo);
            }
        }
    }

    /**
     * A private inner class of EventHandler used to perform time extensive
     * operations. So the user does not think the the application has hung,
     * operations such as copy/past, search, unzip and zip will all be performed
     * in the background. This class extends AsyncTask in order to give the user
     * a progress dialog to show that the app is working properly.
     * <p>
     * (note): this class will eventually be changed from using AsyncTask to using
     * Handlers and messages to perform background operations.
     *
     * @author Joe Berria
     */
    private class BackgroundWork extends AsyncTask<String, Void, ArrayList<String>> {
        private String file_name;
        private ProgressDialog pr_dialog;
        private int type;
        private int copy_rtn;
        private int rename_rtn;

        private BackgroundWork(int type) {
            this.type = type;
        }

        /**
         * This is done on the EDT thread. this is called before
         * doInBackground is called
         */
        @Override
        protected void onPreExecute() {

            switch (type) {
                case SEARCH_TYPE:
                    pr_dialog = ProgressDialog.show(mContext, "Searching",
                            "Searching current file system...",
                            true, true);
                    break;

                case COPY_TYPE:
                    pr_dialog = ProgressDialog.show(mContext, "Copying",
                            "Copying file...",
                            true, false);
                    break;

                case UNZIP_TYPE:
                    pr_dialog = ProgressDialog.show(mContext, "Unzipping",
                            "Unpacking zip file please wait...",
                            true, false);
                    break;

                case UNZIPTO_TYPE:
                    pr_dialog = ProgressDialog.show(mContext, "Unzipping",
                            "Unpacking zip file please wait...",
                            true, false);
                    break;

                case ZIP_TYPE:
                    pr_dialog = ProgressDialog.show(mContext, "Zipping",
                            "Zipping folder...",
                            true, false);
                    break;

                case DELETE_TYPE:
                    pr_dialog = ProgressDialog.show(mContext, "Deleting",
                            "Deleting files...",
                            true, false);
                    break;

                case RENAME_TYPE:
                    pr_dialog = ProgressDialog.show(mContext, "Renaming",
                            "Renaming files...",
                            true, false);
                    break;
            }
        }

        /**
         * background thread here
         */
        @Override
        protected ArrayList<String> doInBackground(String... params) {

            switch (type) {
                case SEARCH_TYPE:
                    file_name = params[0];
                    ArrayList<String> found = mFileMang.searchInDirectory(mFileMang.getCurrentDir(),
                            file_name);
                    return found;

                case COPY_TYPE:
                    int len = params.length;

                    if (StorageFragmentMain.mMultiSelectData != null && !StorageFragmentMain.mMultiSelectData.isEmpty()) {
                        for (int i = 1; i < len; i++) {
                            copy_rtn = mFileMang.copyToDirectory(params[i], params[0]);

                            if (StorageFragmentMain.delete_after_copy)
                                mFileMang.deleteTarget(params[i]);
                        }
                    } else {
                        copy_rtn = mFileMang.copyToDirectory(params[0], params[1]);

                        if (StorageFragmentMain.delete_after_copy)
                            mFileMang.deleteTarget(params[0]);
                    }

                    StorageFragmentMain.delete_after_copy = false;
                    return null;

                case UNZIP_TYPE:
                    mFileMang.extractZipFiles(params[0], params[1]);
                    return null;

                case UNZIPTO_TYPE:
                    mFileMang.extractZipFilesFromDir(params[0], params[1], params[2]);
                    return null;

                case ZIP_TYPE:
                    // mFileMang.createZipFile(params[0]);

                    String name = StorageFragmentMain.mMultiSelectData.get(0).substring(StorageFragmentMain.mMultiSelectData.get(0).lastIndexOf("/"), StorageFragmentMain.mMultiSelectData.get(0).length());
                    String Location = params[0] + name + ".zip";

                    String selectedPath = StorageFragmentMain.mMultiSelectData.get(0);
                    String ZipLocation = Location;

                    //mFileMang.zipFileAtPath(selectedPath, ZipLocation);

                    Intent intent2 = new Intent(mContext, ZipTask.class);
                    intent2.putExtra("name", ZipLocation);
                    intent2.putExtra("files", StorageFragmentMain.mMultiSelectData);
                    mContext.startService(intent2);

                    return null;

                case DELETE_TYPE:
                    int size = params.length;

                    for (int i = 0; i < size; i++)
                        mFileMang.deleteTarget(params[i]);

                    return null;

                case RENAME_TYPE:
                    int len1 = params.length;

                    if (StorageFragmentMain.mMultiSelectData != null && !StorageFragmentMain.mMultiSelectData.isEmpty())
                        if (StorageFragmentMain.mMultiSelectData.size() <= 1) {
                            Log.d("Rename", "Newfile name :- " + params[0] + ", Old filename :- " + params[1]);
                            rename_rtn = mFileMang.renameTarget(params[1], params[0]);
                        } else {
                            for (int i = 1; i < len1; i++) {
                                Log.d("Rename", "Newfile name :- " + params[0] + "" + i + ", Old filename :- " + params[i]);
                                rename_rtn = mFileMang.renameTarget(params[i], params[0] + "" + i);
                            }
                        }
                    return null;
            }
            return null;
        }

        /**
         * This is called when the background thread is finished. Like onPreExecute, anything
         * here will be done on the EDT thread.
         */
        @Override
        protected void onPostExecute(final ArrayList<String> file) {
            final CharSequence[] names;
            int len = file != null ? file.size() : 0;

            switch (type) {
                case SEARCH_TYPE:
                    if (len == 0) {
                        Toast.makeText(mContext, "Couldn't find " + file_name,
                                Toast.LENGTH_SHORT).show();

                    } else {
                        names = new CharSequence[len];

                        for (int i = 0; i < len; i++) {
                            String entry = file.get(i);
                            names[i] = entry.substring(entry.lastIndexOf("/") + 1, entry.length());
                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setTitle("Found " + len + " file(s)");
                        builder.setItems(names, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int position) {
                                String path = file.get(position);
                                updateDirectory(mFileMang.getNextDir(path.
                                        substring(0, path.lastIndexOf("/")), true));
                            }
                        });

                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }

                    pr_dialog.dismiss();
                    break;

                case COPY_TYPE:
                    if (StorageFragmentMain.mMultiSelectData != null && !StorageFragmentMain.mMultiSelectData.isEmpty()) {
                        StorageFragmentMain.multi_select_flag = false;
                        StorageFragmentMain.mMultiSelectData.clear();
                    }

                    if (copy_rtn == 0)
                        Toast.makeText(mContext, "File successfully copied and pasted",
                                Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(mContext, "Copy pasted failed", Toast.LENGTH_SHORT).show();

                    updateDirectory(mFileMang.getNextDir(mFileMang.getCurrentDir(), true));
                    pr_dialog.dismiss();
                    mInfoLabel.setText("");
                    break;

                case UNZIP_TYPE:
                    updateDirectory(mFileMang.getNextDir(mFileMang.getCurrentDir(), true));
                    pr_dialog.dismiss();
                    break;

                case UNZIPTO_TYPE:
                    updateDirectory(mFileMang.getNextDir(mFileMang.getCurrentDir(), true));
                    pr_dialog.dismiss();
                    break;

                case ZIP_TYPE:

                    mDelegate.killMultiSelect(true);

                    updateDirectory(mFileMang.getNextDir(mFileMang.getCurrentDir(), true));
                    pr_dialog.dismiss();
                    break;

                case DELETE_TYPE:

                    if (StorageFragmentMain.mMultiSelectData != null && !StorageFragmentMain.mMultiSelectData.isEmpty()) {
                        StorageFragmentMain.mMultiSelectData.clear();
                        StorageFragmentMain.multi_select_flag = false;
                    }

                    updateDirectory(mFileMang.getNextDir(mFileMang.getCurrentDir(), true));
                    pr_dialog.dismiss();
                    mInfoLabel.setText("");
                    break;

                case RENAME_TYPE:
                    if (StorageFragmentMain.mMultiSelectData != null && !StorageFragmentMain.mMultiSelectData.isEmpty()) {
                        StorageFragmentMain.multi_select_flag = false;
                        StorageFragmentMain.mMultiSelectData.clear();
                    }

                    mDelegate.killMultiSelect(true);

                    if (rename_rtn == 0)
                        Toast.makeText(mContext, "File successfully renamed",
                                Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(mContext, "Rename failed", Toast.LENGTH_SHORT).show();

                    updateDirectory(mFileMang.getNextDir(mFileMang.getCurrentDir(), true));
                    pr_dialog.dismiss();
                    break;
            }
        }
    }
}
