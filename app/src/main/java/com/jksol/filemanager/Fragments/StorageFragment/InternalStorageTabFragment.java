package com.jksol.filemanager.Fragments.StorageFragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jksol.filemanager.FileOperation.EventHandler;
import com.jksol.filemanager.FileOperation.FileManager;
import com.jksol.filemanager.Fragments.GalleryFragment.AllFileTypeFragment;
import com.jksol.filemanager.Interfaces.FragmentChange;
import com.jksol.filemanager.Interfaces.RecyclerViewContextmenuClick;
import com.jksol.filemanager.MainActivity;
import com.jksol.filemanager.R;
import com.jksol.filemanager.Utils.AppController;
import com.jksol.filemanager.Utils.Constats;
import com.jksol.filemanager.Utils.FileUtil;
import com.jksol.filemanager.Utils.MimeTypes;
import com.jksol.filemanager.Utils.RecyclerTouchListener;
import com.jksol.filemanager.Utils.SmbStreamer.Streamer;
import com.jksol.filemanager.Utils.Utils;

import java.io.File;
import java.util.List;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

public class InternalStorageTabFragment extends Fragment implements MainActivity.ButtonBackPressListener, RecyclerViewContextmenuClick {

    public static ProgressBar file_loader;
    LinearLayout hidden_rename, hidden_add_favourite, hidden_zip, hidden_share, hidden_copy, hidden_move, hidden_delete, hidden_detail;
    private InternalStorageTabFragment mContext;
    private SharedPreferences mSettings;
    private FileManager mFileMag;
    private EventHandler mHandler;
    private EventHandler.RecyclerViewTableRow mTable;
    // private ListView list;
    private RecyclerView files_recyclerView;
    private boolean mReturnIntent = false;
    private boolean mHoldingZip = false;
    private boolean mUseBackKey = true;
    private Context context;
    private TextView mDetailLabel;
    private TextView mPathLabel;
    private String mZippedTarget;
    private String mSelectedListItem;
    private LinearLayout hidden_paste_layout;
    private LinearLayout hidden_paste;
    private LinearLayout hidden_cancel;

    private BroadcastReceiver ZipCompletedBroadcast = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (context != null) {
                mHandler.mDelegate.killMultiSelect(true);
                mHandler.updateDirectory(mFileMag.getNextDir(mFileMag.getCurrentDir(), true));
            }
        }
    };

    private BroadcastReceiver CopyCompletedBroadcast = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (context != null) {
                mHandler.mDelegate.killMultiSelect(true);
                mHandler.updateDirectory(mFileMag.getNextDir(mFileMag.getCurrentDir(), true));
                mHandler.delete_after_copy = false;
            }
        }
    };

    private BroadcastReceiver DeleteCompletedBroadcast = new BroadcastReceiver() {

        public ProgressDialog pr_dialog;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (context != null) {
                if (intent != null) {
                    if (intent.getBooleanExtra("started", false)) {
                        pr_dialog = ProgressDialog.show(context, "Deleting",
                                "Deleting files...",
                                true, false);
                    }

                    if (intent.getBooleanExtra("completed", false)) {
                        mHandler.mDelegate.killMultiSelect(true);
                        mHandler.updateDirectory(mFileMag.getNextDir(mFileMag.getCurrentDir(), true));
                        if (pr_dialog != null)
                            pr_dialog.dismiss();
                    }
                }
            }
        }
    };
    private LinearLayout hidden_buttons;
    private boolean isDownloadFolder = false;
    private boolean isLanConnetion = false;
    private String smb_path = "";
    private AsyncTask<Void, Void, Void> loadFile_AsyncTask;
    private String rootPath = "";
    private FragmentChange fragmentChangeListener;
    private LinearLayout operationlayout_one;
    private Streamer s;
    private LinearLayout empty_layout;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            AppController.getInstance().setButtonBackPressed(this);
            AppController.getInstance().setRecyclerviewContextmenuClick(this);
           /* try{
                Inialize();
            }catch (Exception e){}*/
        } else {
            AppController.getInstance().setButtonBackPressed(null);
            AppController.getInstance().setRecyclerviewContextmenuClick(null);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Toast.makeText(context, "Internal onresume", Toast.LENGTH_SHORT).show();
        getActivity().registerReceiver(ZipCompletedBroadcast, new IntentFilter("ZipCompleted"));
        getActivity().registerReceiver(CopyCompletedBroadcast, new IntentFilter("CopyCompleted"));
        getActivity().registerReceiver(DeleteCompletedBroadcast, new IntentFilter("DeleteCompleted"));

    }

    @Override
    public void onPause() {
        super.onPause();
       /* AppController.getInstance().setButtonBackPressed(null);
        AppController.getInstance().setRecyclerviewContextmenuClick(null);*/
        //  Toast.makeText(context, "Internal onpause", Toast.LENGTH_SHORT).show();
        getActivity().unregisterReceiver(ZipCompletedBroadcast);
        getActivity().unregisterReceiver(CopyCompletedBroadcast);
        getActivity().unregisterReceiver(DeleteCompletedBroadcast);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_internalstorage, null);

        if (getActivity() instanceof FragmentChange) {
            fragmentChangeListener = (FragmentChange) getActivity();
        }

        file_loader = (ProgressBar) view.findViewById(R.id.file_loader);
        file_loader.setVisibility(View.VISIBLE);

        Toast.makeText(getActivity(), "Oncreate", Toast.LENGTH_SHORT).show();
        try {
            if (getArguments() != null) {
                String selected_fragment = getArguments().getString("Path");

                if (selected_fragment.equals("Download")) {
                    isDownloadFolder = true;
                    AppController.getInstance().setButtonBackPressed(this);
                    AppController.getInstance().setRecyclerviewContextmenuClick(this);

                } else if (selected_fragment.equalsIgnoreCase("LanConnection")) {
                    isLanConnetion = true;
                    smb_path = getArguments().getString("FullPath");
                    AppController.getInstance().setButtonBackPressed(this);
                    AppController.getInstance().setRecyclerviewContextmenuClick(this);
                } else {
                    isDownloadFolder = false;
                    isLanConnetion = false;
                }
            }
        } catch (Exception e) {
        }

        setRetainInstance(true);
        initView(view);
        setupPreference(savedInstanceState, view);
        // FileOperationLayouts(view);
        setHasOptionsMenu(true);

        return view;
    }

    private void initView(View view) {
        mContext = this;
        context = mContext.getActivity();

        mDetailLabel = (TextView) view.findViewById(R.id.detail_label);
        mPathLabel = (TextView) view.findViewById(R.id.path_label);
        mPathLabel.setText("path: /storage/emulated/0");
        // list = (ListView) view.findViewById(R.id.files_listview);
        files_recyclerView = (RecyclerView) view.findViewById(R.id.files_recyclerView);
        files_recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        empty_layout = (LinearLayout) view.findViewById(R.id.empty_layout);
        hidden_rename = (LinearLayout) view.findViewById(R.id.hidden_rename);
        hidden_add_favourite = (LinearLayout) view.findViewById(R.id.hidden_add_favourite);
        hidden_zip = (LinearLayout) view.findViewById(R.id.hidden_zip);
        hidden_share = (LinearLayout) view.findViewById(R.id.hidden_share);

        hidden_copy = (LinearLayout) view.findViewById(R.id.hidden_copy);
        hidden_move = (LinearLayout) view.findViewById(R.id.hidden_move);
        hidden_delete = (LinearLayout) view.findViewById(R.id.hidden_delete);
        hidden_detail = (LinearLayout) view.findViewById(R.id.hidden_detail);

        operationlayout_one = (LinearLayout) view.findViewById(R.id.operationlayout_one);
        hidden_buttons = (LinearLayout) view.findViewById(R.id.hidden_buttons);
        hidden_paste_layout = (LinearLayout) view.findViewById(R.id.hidden_paste_layout);
        hidden_paste = (LinearLayout) view.findViewById(R.id.hidden_paste);
        hidden_cancel = (LinearLayout) view.findViewById(R.id.hidden_cancel);
        hidden_paste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paste();
            }
        });

        hidden_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHandler.mDelegate.killMultiSelect(true);
            }
        });
    }

    public void paste() {
        boolean multi_select = mHandler.hasMultiSelectData();
        if (multi_select) {
            mHandler.copyFileMultiSelect(mFileMag.getCurrentDir());
        }
    }

    private void setupPreference(Bundle savedInstanceState, View view) {
        //  StorageList();

        /*read settings*/
        mSettings = mContext.getContext().getSharedPreferences(Constats.PREFS_NAME, 0);
        boolean hide = mSettings.getBoolean(Constats.PREFS_HIDDEN_FILES, false);
        boolean thumb = mSettings.getBoolean(Constats.PREFS_IMAGE_THUMBNAIL, true);
        int space = mSettings.getInt(Constats.PREFS_STORAGE, View.VISIBLE);
        int color = mSettings.getInt(Constats.PREFS_COLOR, -1);
        int sort = mSettings.getInt(Constats.PREFS_SORT_FILES, 1);

        Utils utils = new Utils();
        /*Log.d("External storage", utils.getStoragePaths("ExternalStorage"));
        Log.d("Internal storage", utils.getStoragePaths("InternalStorage"));*/

        String path = "";

        if (isDownloadFolder) {
            mFileMag = new FileManager();
            path = utils.getStoragePaths("InternalStorage") + "/Download";
        } else if (isLanConnetion) {
            path = smb_path;
            mFileMag = new FileManager(path);
            operationlayout_one.setVisibility(View.GONE);

        } else {
            mFileMag = new FileManager();
            path = utils.getStoragePaths("InternalStorage");
        }

        rootPath = path;

        mFileMag.setShowHiddenFiles(hide);
        mFileMag.setSortType(sort);

        if (savedInstanceState != null)
            loadFiles(savedInstanceState.getString("location"), view);
        else
            loadFiles(path, view);

        //  Log.d("Document Type", getMimeType("/storage/emulated/0/abc2.docx"));
      /*  Log.d("Document File","Internal File Count :- " + String.valueOf(mFileMag.FindDifferentFile(new File(path)).size()));*/

    }

    private void loadFiles(final String path, final View view) {

        if (loadFile_AsyncTask != null && loadFile_AsyncTask.getStatus() == AsyncTask.Status.RUNNING)
            loadFile_AsyncTask.cancel(true);
        loadFile_AsyncTask = new AsyncTask<Void, Void, Void>() {
            int p1, p2;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                file_loader.setVisibility(View.VISIBLE);
            }

            @Override
            protected Void doInBackground(Void... voids) {
                mHandler = new EventHandler(context, mFileMag, path);
                return null;
            }

            @Override
            protected void onPostExecute(Void arr) {
                super.onPostExecute(arr);

                file_loader.setVisibility(View.GONE);

                boolean thumb = mSettings.getBoolean(Constats.PREFS_IMAGE_THUMBNAIL, true);
                mHandler.setUpdateLabels(mPathLabel, mDetailLabel, empty_layout);

                mHandler.setUpdateFileOperationLayout(hidden_buttons, hidden_paste_layout);
                mHandler.setUpdateFileOperationViews(hidden_rename, hidden_add_favourite, hidden_zip, hidden_share, hidden_copy, hidden_move, hidden_delete, hidden_detail);

                // mHandler.setTextColor(color);
                mHandler.setShowThumbnails(thumb);
                // mTable = mHandler.new TableRow();
                mTable = mHandler.new RecyclerViewTableRow();

                /*  sets the ListAdapter for our ListActivity and
                 *  gives our EventHandler class the same adapter
                */
                mHandler.setListAdapter(mTable);
                //list.setAdapter(mTable);
                files_recyclerView.setAdapter(mTable);
                recyclerviewClick();
                FileOperationLayouts(view);
            }

        }.execute();//.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void FileOperationLayouts(View view) {

        int[] button_id = {R.id.hidden_copy, R.id.hidden_delete,
                R.id.hidden_move, R.id.hidden_detail, R.id.hidden_add_favourite,
                R.id.hidden_zip, R.id.hidden_rename, R.id.hidden_share};
        LinearLayout[] bt = new LinearLayout[button_id.length];

        for (int i = 0; i < bt.length; i++) {
            bt[i] = (LinearLayout) view.findViewById(button_id[i]);
            bt[i].setOnClickListener(mHandler);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();

        inflater.inflate(R.menu.storage_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mHandler.mDelegate.filter(newText);

                // mHandler.searchForFile(newText);

                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                handleMenuSearch();
                break;

            case R.id.menu_new_file:
                Toast.makeText(context, "file", Toast.LENGTH_SHORT).show();
                CreateFileAndFolder("File");
                break;

            case R.id.menu_new_folder:
                Toast.makeText(context, "folder", Toast.LENGTH_SHORT).show();
                CreateFileAndFolder("Folder");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleMenuSearch() {
        //  actionBar = getActivity().getSupportActionBar(); //get the actionbar

      /*  if(isSearchOpened){ //test if the search is open
            if(actionBar != null)
            {
                actionBar.setDisplayShowCustomEnabled(false);   //disable a custom view inside the actionbar
                actionBar.setDisplayShowTitleEnabled(true);     //show the title in the action bar
            }

            // hides the keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);

            // add the search icon in the action bar
            mSearchAction.setIcon(getResources().getDrawable(R.drawable.ic_search));

            isSearchOpened = false;
        } else { // open the search entry
            if(actionBar != null)
            {
                actionBar.setDisplayShowCustomEnabled(true); //enable it to display a
                // custom view in the action bar.
                // action.setCustomView(R.layout.search_bar);//add the custom view

                actionBar.setDisplayShowTitleEnabled(false); //hide the title
            }
            // open the keyboard focused in the edtSearch
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT);
            // add the close icon
            mSearchAction.setIcon(getResources().getDrawable(R.drawable.ic_search));

            isSearchOpened = true;
        }*/
    }

    public void CreateFileAndFolder(final String type) {
        final Dialog create_dialog = new Dialog(context);
        create_dialog.setCancelable(false);
        create_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (FileUtil.FileOperation)
            create_dialog.setContentView(R.layout.rename_filedirectory_dialog);
        else
            create_dialog.setContentView(R.layout.rename_filedirectory_dialog_new);

        create_dialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        create_dialog.getWindow().getAttributes().windowAnimations = R.style.confirmDeleteAnimation;

        if (!create_dialog.isShowing())
            create_dialog.show();

        final EditText edt_rename = (EditText) create_dialog.findViewById(R.id.edt_rename);

        Button dialog_close = (Button) create_dialog.findViewById(R.id.dialaog_cancel);
        dialog_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                create_dialog.dismiss();
                // mHandler.mDelegate.killMultiSelect(true);
            }
        });

        final TextView txt_dialog_label = (TextView) create_dialog.findViewById(R.id.txt_dialog_label);
        if (type.equalsIgnoreCase("File")) {
            edt_rename.setHint(mContext.getResources().getString(R.string.newfile));
            txt_dialog_label.setText(mContext.getResources().getString(R.string.newfile));
        } else {
            edt_rename.setHint(mContext.getResources().getString(R.string.newfolder));
            txt_dialog_label.setText(mContext.getResources().getString(R.string.newfolder));
        }

        Button dialog_ok = (Button) create_dialog.findViewById(R.id.dialog_ok);
        dialog_ok.setText("Create");
        dialog_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = edt_rename.getText().toString().trim();
                if (!TextUtils.isEmpty(name)) {

                    try {

                        if (type.equalsIgnoreCase("File")) {

                            File file = new File(mFileMag.getCurrentDir() + "/" + name + ".txt");
                            if (file.exists()) {
                                Toast.makeText(getActivity().getApplicationContext(), getActivity().getApplicationContext().getString(R.string.msg_prompt_file_already_exits), Toast.LENGTH_SHORT).show();
                            } else {
                                boolean isCreated = file.createNewFile();
                                if (isCreated) {
                                    create_dialog.dismiss();
                                    String temp = mFileMag.getCurrentDir();
                                    mHandler.updateDirectory(mFileMag.getNextDir(temp, true));
                                } else {
                                    Toast.makeText(getActivity().getApplicationContext(), getActivity().getApplicationContext().getString(R.string.msg_prompt_file_not_created), Toast.LENGTH_SHORT).show();
                                }
                            }

                        } else {
                            if (mFileMag.createDir(mFileMag.getCurrentDir() + "/", name) == 0)
                                Toast.makeText(context,
                                        "Folder " + name + " created",
                                        Toast.LENGTH_LONG).show();
                            else
                                Toast.makeText(context, "New folder was not created", Toast.LENGTH_SHORT).show();

                            create_dialog.dismiss();
                            String temp = mFileMag.getCurrentDir();
                            mHandler.updateDirectory(mFileMag.getNextDir(temp, true));

                        }
                    } catch (Exception e) {
                    }

                    edt_rename.setError(null);
                } else {
                    edt_rename.setError(mContext.getResources().getString(R.string.entername));
                }
            }
        });
    }

    public void StorageList() {

        List<Utils.StorageUtils.StorageInfo> data = Utils.StorageUtils.getStorageList();

        for (Utils.StorageUtils.StorageInfo list : data) {
            Log.d("Card :- ", list.getDisplayName());
        }

        Log.d("Storage Size", "Size :- " + data.size());
    }

    private void recyclerviewClick() {

        files_recyclerView.addOnItemTouchListener(new RecyclerTouchListener(AppController.getInstance().getApplicationContext(), files_recyclerView, new AllFileTypeFragment.ClickListener() {
            @Override
            public void onClick(View view, int position) {

                RecyclerClick(view, position);
            }

            @Override
            public void onLongClick(View view, int position) {
                if (mHandler.multi_select_flag) {
                    mHandler.mDelegate.killMultiSelect(true);
                } else {
                    LinearLayout hidden_lay =
                            (LinearLayout) getView().findViewById(R.id.hidden_buttons);

                    mHandler.multi_select_flag = true;
                    hidden_lay.setVisibility(LinearLayout.VISIBLE);
                }
                //Toast.makeText(context, "Long press2", Toast.LENGTH_SHORT).show();
                mHandler.mDelegate.notifyDataSetChanged();

                RecyclerClick(view, position);
                //Toast.makeText(context, "Long press1", Toast.LENGTH_SHORT).show();
            }
        }

        ));
    }

    private void RecyclerClick(View view, int position1) {
        View child = view;

        if (child != null) {

            int position = position1;
            position = position;

            final String item = mHandler.getData(position);
            boolean multiSelect = mHandler.isMultiSelected();

            String fullpath = "";

            if (isLanConnetion) {
                try {
                    fullpath = mFileMag.getCurrentDir() + item;
                    SmbFile smbFile = new SmbFile(fullpath);
                    fullpath = smbFile.getPath();
                } catch (Exception e) {
                }
            } else {
                fullpath = mFileMag.getCurrentDir() + "/" + item;
            }

            /*
             * If the user has multi-select on, we just need to record the file
             * not make an intent for it.
             */
            if (multiSelect) {
                mTable.addMultiPosition(position, fullpath);
            } else {
                if (isLanConnetion) {
                    try {
                        SmbFile smbFile = new SmbFile(fullpath);
                        openSMBFile(item, smbFile);
                    } catch (Exception e) {
                    }
                } else {
                    final File file = new File(fullpath);
                    openFile(item, file);
                }
            }

        }
    }

    private void openSMBFile(final String item, final SmbFile file) throws SmbException {
        String item_ext = null;

        try {
            item_ext = item.substring(item.lastIndexOf("."), item.length());
        } catch (IndexOutOfBoundsException e) {
            item_ext = "";
        }
        if (file.isDirectory()) {
            if (file.canRead()) {
                mHandler.stopFileLoadThread();
                mHandler.stopThumbnailThread();
                mHandler.updateDirectory(mFileMag.getNextDir(item, false));
                mPathLabel.setText(mFileMag.getCurrentDir());

                if (!mUseBackKey)
                    mUseBackKey = true;

            } else {
                Toast.makeText(mContext.getActivity(), "Can't read folder due to permissions",
                        Toast.LENGTH_SHORT).show();
            }
        }

        /*music file selected--add more audio formats*/
        else {

            try {

                s = Streamer.getInstance();
                new Thread() {
                    public void run() {
                        try {
                            s.setStreamSrc(file, null, file.length());//the second argument can be a list of subtitle files
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    try {
                                        Uri uri = Uri.parse(Streamer.URL + Uri.fromFile(new File(Uri.parse(file.getPath()).getPath())).getEncodedPath());
                                        Intent i = new Intent(Intent.ACTION_VIEW);
                                        i.setDataAndType(uri, MimeTypes.getMimeType(new File(file.getPath())));
                                        PackageManager packageManager = getActivity().getPackageManager();
                                        List<ResolveInfo> resInfos = packageManager.queryIntentActivities(i, 0);
                                        if (resInfos != null && resInfos.size() > 0)
                                            startActivity(i);
                                        else
                                            Toast.makeText(getActivity(), "You will need to copy this file to storage to open it", Toast.LENGTH_SHORT).show();
                                    } catch (ActivityNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }.start();

            } catch (Exception e) {
            }
        }
    }

    private void openFile(final String item, final File file) {
        String item_ext = null;

        try {
            item_ext = item.substring(item.lastIndexOf("."), item.length());
        } catch (IndexOutOfBoundsException e) {
            item_ext = "";
        }
        if (file.isDirectory()) {
            if (file.canRead()) {
                mHandler.stopFileLoadThread();
                mHandler.stopThumbnailThread();
                mHandler.updateDirectory(mFileMag.getNextDir(item, false));
                mPathLabel.setText(mFileMag.getCurrentDir());

                                /*set back button switch to true
                                 * (this will be better implemented later)
                                 */
                if (!mUseBackKey)
                    mUseBackKey = true;

            } else {
                Toast.makeText(mContext.getActivity(), "Can't read folder due to permissions",
                        Toast.LENGTH_SHORT).show();
            }
        }

	    	            /*music file selected--add more audio formats*/
        else if (item_ext.equalsIgnoreCase(".mp3") ||
                item_ext.equalsIgnoreCase(".m4a") ||
                item_ext.equalsIgnoreCase(".mp4")) {

            if (mReturnIntent) {
                returnIntentResults(file);
            } else {
                Intent i = new Intent();
                i.setAction(android.content.Intent.ACTION_VIEW);
                i.setDataAndType(Uri.fromFile(file), "audio/*");
                startActivity(i);
            }
        }

	    	            /*photo file selected*/
        else if (item_ext.equalsIgnoreCase(".jpeg") ||
                item_ext.equalsIgnoreCase(".jpg") ||
                item_ext.equalsIgnoreCase(".png") ||
                item_ext.equalsIgnoreCase(".gif") ||
                item_ext.equalsIgnoreCase(".tiff")) {

            if (file.exists()) {
                if (mReturnIntent) {
                    returnIntentResults(file);

                } else {
                    Intent picIntent = new Intent();
                    picIntent.setAction(android.content.Intent.ACTION_VIEW);
                    picIntent.setDataAndType(Uri.fromFile(file), "image/*");
                    startActivity(picIntent);
                }
            }
        }

	    	            /*video file selected--add more video formats*/
        else if (item_ext.equalsIgnoreCase(".m4v") ||
                item_ext.equalsIgnoreCase(".3gp") ||
                item_ext.equalsIgnoreCase(".wmv") ||
                item_ext.equalsIgnoreCase(".mp4") ||
                item_ext.equalsIgnoreCase(".ogg") ||
                item_ext.equalsIgnoreCase(".wav")) {

            if (file.exists()) {
                if (mReturnIntent) {
                    returnIntentResults(file);

                } else {
                    Intent movieIntent = new Intent();
                    movieIntent.setAction(android.content.Intent.ACTION_VIEW);
                    movieIntent.setDataAndType(Uri.fromFile(file), "video/*");
                    startActivity(movieIntent);
                }
            }
        }

	    	            /*zip file */
        else if (item_ext.equalsIgnoreCase(".zip")) {

            if (mReturnIntent) {
                returnIntentResults(file);

            } else {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                AlertDialog alert;
                mZippedTarget = mFileMag.getCurrentDir() + "/" + item;
                CharSequence[] option = {"View", "Extract here"};

                builder.setTitle("Zip");
                builder.setItems(option, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:

                                Intent movieIntent = new Intent();
                                movieIntent.setAction(android.content.Intent.ACTION_VIEW);
                                movieIntent.setDataAndType(Uri.fromFile(file), "application/zip");
                                startActivity(movieIntent);

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

            }
        }

	    	            /* gzip files, this will be implemented later */
        else if (item_ext.equalsIgnoreCase(".gzip") ||
                item_ext.equalsIgnoreCase(".gz")) {

            if (mReturnIntent) {
                returnIntentResults(file);

            } else {
                //TODO:
            }
        }

	    	            /*pdf file selected*/
        else if (item_ext.equalsIgnoreCase(".pdf")) {

            if (file.exists()) {
                if (mReturnIntent) {
                    returnIntentResults(file);

                } else {
                    Intent pdfIntent = new Intent();
                    pdfIntent.setAction(android.content.Intent.ACTION_VIEW);
                    pdfIntent.setDataAndType(Uri.fromFile(file),
                            "application/pdf");

                    try {
                        startActivity(pdfIntent);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(context, "Sorry, couldn't find a pdf viewer",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

	    	            /*Android application file*/
        else if (item_ext.equalsIgnoreCase(".apk")) {

            if (file.exists()) {
                if (mReturnIntent) {
                    returnIntentResults(file);

                } else {
                    Intent apkIntent = new Intent();
                    apkIntent.setAction(android.content.Intent.ACTION_VIEW);
                    apkIntent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                    startActivity(apkIntent);
                }
            }
        }

	    	            /* HTML file */
        else if (item_ext.equalsIgnoreCase(".html")) {

            if (file.exists()) {
                if (mReturnIntent) {
                    returnIntentResults(file);

                } else {
                    Intent htmlIntent = new Intent();
                    htmlIntent.setAction(android.content.Intent.ACTION_VIEW);
                    htmlIntent.setDataAndType(Uri.fromFile(file), "text/html");

                    try {
                        startActivity(htmlIntent);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(context, "Sorry, couldn't find a HTML viewer",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

	    	            /* text file*/
        else if (item_ext.equalsIgnoreCase(".txt")) {

            if (file.exists()) {
                if (mReturnIntent) {
                    returnIntentResults(file);

                } else {
                    Intent txtIntent = new Intent();
                    txtIntent.setAction(android.content.Intent.ACTION_VIEW);
                    txtIntent.setDataAndType(Uri.fromFile(file), "text/plain");

                    try {
                        startActivity(txtIntent);
                    } catch (ActivityNotFoundException e) {
                        txtIntent.setType("text/*");
                        startActivity(txtIntent);
                    }
                }
            }
        }

	    	            /* generic intent */
        else {
            if (file.exists()) {
                if (mReturnIntent) {
                    returnIntentResults(file);

                } else {
                    Intent generic = new Intent();
                    generic.setAction(android.content.Intent.ACTION_VIEW);
                    generic.setDataAndType(Uri.fromFile(file), "text/plain");

                    try {
                        startActivity(generic);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(context, "Sorry, couldn't find anything " +
                                        "to open " + file.getName(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("location", mFileMag.getCurrentDir());
    }

    /*(non Java-Doc)
     * Returns the file that was selected to the intent that
     * called this activity. usually from the caller is another application.
     */
    private void returnIntentResults(File data) {
        mReturnIntent = false;

        Intent ret = new Intent();
        ret.setData(Uri.fromFile(data));
        getActivity().setResult(getActivity().RESULT_OK, ret);
        getActivity().finish();
    }

    @Override
    public void onButtonBackPressed(int keycode, KeyEvent event) {
        String current = mFileMag.getCurrentDir();

        if (keycode == KeyEvent.KEYCODE_SEARCH) {
            // showDialog(SEARCH_B);

        } else if (keycode == KeyEvent.KEYCODE_BACK && mUseBackKey && !current.equals(rootPath)) {
            if (mHandler.isMultiSelected()) {
                mTable.killMultiSelect(true);
                Toast.makeText(context, "Multi-select is now off", Toast.LENGTH_SHORT).show();

            } else {
                //stop updating thumbnail icons if its running
                mHandler.stopFileLoadThread();
                mHandler.stopThumbnailThread();
                mHandler.updateDirectory(mFileMag.getPreviousDir());
                mPathLabel.setText(mFileMag.getCurrentDir());
            }

        } else if (keycode == KeyEvent.KEYCODE_BACK && mUseBackKey && current.equals(rootPath)) {
            // Toast.makeText(context, "Press back again to quit.", Toast.LENGTH_SHORT).show();

            if (mHandler.isMultiSelected()) {
                mTable.killMultiSelect(true);
                Toast.makeText(context, "Multi-select is now off", Toast.LENGTH_SHORT).show();
            }

            mUseBackKey = false;
            mPathLabel.setText(mFileMag.getCurrentDir());
            if (mFileMag.isSmb()) {
                fragmentChangeListener.OnFragmentChange(4, MainActivity.FG_TAG_NETWORK);
            } else {
                // fragmentChangeListener.OnFragmentChange(0, MainActivity.FG_TAG_HOME);
            }

        } else if (keycode == KeyEvent.KEYCODE_BACK && !mUseBackKey && current.equals("/")) {
            //mContext.getActivity().finish();
        }
    }

    @Override
    public void ContextmenuClick(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {


    }
}
