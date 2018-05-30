package com.galaxy.quickfilemanager.Fragments.StorageFragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.galaxy.quickfilemanager.FileOperation.EventHandler;
import com.galaxy.quickfilemanager.FileOperation.FileManager;
import com.galaxy.quickfilemanager.Interfaces.FragmentChange;
import com.galaxy.quickfilemanager.Interfaces.RecyclerViewContextmenuClick;
import com.galaxy.quickfilemanager.MainActivity;
import com.galaxy.quickfilemanager.Utils.AppController;
import com.galaxy.quickfilemanager.Utils.Constats;
import com.galaxy.quickfilemanager.Utils.DataPackage;
import com.galaxy.quickfilemanager.Utils.FileUtil;
import com.galaxy.quickfilemanager.Utils.Futils;
import com.galaxy.quickfilemanager.Utils.HFile;
import com.galaxy.quickfilemanager.Utils.MainActivityHelper;
import com.galaxy.quickfilemanager.Utils.PreferencesUtils;
import com.galaxy.quickfilemanager.Utils.RecyclerTouchListener;
import com.galaxy.quickfilemanager.Utils.Utils;
import com.galaxy.quickfilemanager.ProgressListener;
import com.galaxy.quickfilemanager.R;
import com.galaxy.quickfilemanager.RegisterCallback;

import java.io.File;
import java.util.ArrayList;

public class SDCardTabFragment extends Fragment implements MainActivity.ButtonBackPressListener, RecyclerViewContextmenuClick, StorageFragmentMain.PasteLayoutListener {

    public FileManager mFileMag;
    public EventHandler mHandler;
    public Dialog create_dialog;
    LinearLayout hidden_rename, hidden_add_favourite, hidden_zip, hidden_share, hidden_copy, hidden_move, hidden_delete, hidden_detail;
    boolean mBound = false;
    ArrayList<Integer> CopyIds = new ArrayList<Integer>();
    ArrayList<Integer> CancelledCopyIds = new ArrayList<Integer>();
    private SDCardTabFragment mContext;
    private boolean fragmentVisible = false;
    private SharedPreferences mSettings;
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
    private LinearLayout hidden_buttons;
    private AsyncTask<Void, Void, Void> loadFile_AsyncTask;
    private ProgressBar file_loader;
    private String rootPath = "";
    private FragmentChange fragmentChangeListener;
    private LinearLayout empty_layout;

    private BroadcastReceiver ZipCompletedBroadcast = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (fragmentVisible)
                Toast.makeText(context, "Zipped file successfully", Toast.LENGTH_SHORT).show();
            mHandler.mDelegate.killMultiSelect(true);
            mHandler.updateDirectory(mFileMag.getNextDir(mFileMag.getCurrentDir(), true));
        }
    };

    private BroadcastReceiver DeleteCompletedBroadcast = new BroadcastReceiver() {

        public ProgressDialog pr_dialog;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (context != null) {
                if (intent != null) {

                    if (intent.getBooleanExtra("started", false)) {
                        try {
                            pr_dialog = ProgressDialog.show(context, "Deleting",
                                    "Deleting files...",
                                    true, false);
                        } catch (Exception e) {
                        }
                    }

                    if (intent.getBooleanExtra("completed", false)) {

                        if (intent != null) {
                            /*if (fragmentVisible)
                                Toast.makeText(context, intent.getStringExtra("delete_msg"), Toast.LENGTH_SHORT).show();*/
                        }

                        mHandler.mDelegate.killMultiSelect(true);
                        mHandler.updateDirectory(mFileMag.getNextDir(mFileMag.getCurrentDir(), true));
                        if (pr_dialog != null)
                            pr_dialog.dismiss();
                    }

                }
            }
        }
    };

    private TextView current_progress_item, total_progress_count;
    private ProgressBar copy_progressbar;
    private Futils futils;
    private Dialog copy_dialog;

    private BroadcastReceiver CopyCompletedBroadcast = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (context != null) {
                if (intent != null) {
                    /*if (fragmentVisible)
                        Toast.makeText(context, intent.getStringExtra("copy_msg"), Toast.LENGTH_SHORT).show();*/
                }
                try {
                    /*if (StorageFragmentMain.copy_dialog != null)
                        StorageFragmentMain.copy_dialog.dismiss();*/

                    mHandler.mDelegate.killMultiSelect(true);
                    mHandler.updateDirectory(mFileMag.getNextDir(mFileMag.getCurrentDir(), true));
                    StorageFragmentMain.delete_after_copy = false;
                } catch (Exception e) {
                }
            }
        }
    };

    private ImageView no_storage_found_img;
    private TextView no_storage_found_txt;
    private String data;
    private MainActivity mainActivity;

    private ServiceConnection mCopyConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            RegisterCallback binder = (RegisterCallback.Stub.asInterface(service));
            mBound = true;
            try {
                for (DataPackage dataPackage : binder.getCurrent()) {
                    processCopyResults(dataPackage);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            try {
                binder.registerCallBack(new ProgressListener.Stub() {
                    @Override
                    public void onUpdate(final DataPackage dataPackage) {
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                processCopyResults(dataPackage);
                            }
                        });
                    }

                    @Override
                    public void refresh() {
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                clear();
                            }
                        });
                    }
                });
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    private MainActivityHelper mainActivityHelper;
    private boolean chnageLayout;
    private Utils utils;
    private boolean isTablet = false;
    private OpenFiles openFiles;

    void clear() {
        CancelledCopyIds.clear();
    }

    public void processCopyResults(final DataPackage b) {
        if (!fragmentVisible) return;
        if (getResources() == null) return;
        if (b != null) {
            int id = b.getId();
            final Integer id1 = new Integer(id);
            if (!CancelledCopyIds.contains(id1)) {
                if (CopyIds.contains(id1)) {
                    boolean completed = b.isCompleted();

                    if (completed) {
                        try {

                            CopyIds.remove(CopyIds.indexOf(id1));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        String name = b.getName();
                        int p1 = b.getP1();
                        int p2 = b.getP2();
                        long total = b.getTotal();
                        long done = b.getDone();
                        boolean move = b.isMove();
                        String text = futils.readableFileSize(done) + "/" + futils.readableFileSize(total);
                        if (move) {
                            text = futils.readableFileSize(done) + "/" + futils.readableFileSize(total);
                        }

                        current_progress_item = (TextView) copy_dialog.findViewById(R.id.current_progress_item);
                        total_progress_count = (TextView) copy_dialog.findViewById(R.id.total_progress_count);
                        copy_progressbar = (ProgressBar) copy_dialog.findViewById(R.id.copy_progressbar);

                        current_progress_item.setText(name);
                        total_progress_count.setText(text);
                        copy_progressbar.setProgress(p1);

                    }
                } else {
                    boolean move = b.isMove();
                    CopyIds.add(id1);
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        getActivity().registerReceiver(ZipCompletedBroadcast, new IntentFilter("ZipCompleted"));
        getActivity().registerReceiver(CopyCompletedBroadcast, new IntentFilter("CopyCompleted"));
        getActivity().registerReceiver(DeleteCompletedBroadcast, new IntentFilter("DeleteCompleted"));
    }

    @Override
    public void onPause() {
        super.onPause();

        getActivity().unregisterReceiver(ZipCompletedBroadcast);
        getActivity().unregisterReceiver(CopyCompletedBroadcast);
        getActivity().unregisterReceiver(DeleteCompletedBroadcast);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            fragmentVisible = true;
            /*if (mCopyConnection != null && getActivity() != null) {
                Intent intent = new Intent(getActivity(), CopyService.class);
                getActivity().bindService(intent, mCopyConnection, 0);
            }*/

            AppController.getInstance().setPasteLayoutPressed(this);
            AppController.getInstance().setButtonBackPressed(this);
            AppController.getInstance().setRecyclerviewContextmenuClick(this);

        } else {
            fragmentVisible = false;

           /* if (mCopyConnection != null && getActivity() != null && mBound)
                getActivity().unbindService(mCopyConnection);*/

            AppController.getInstance().setPasteLayoutPressed(null);
            AppController.getInstance().setButtonBackPressed(null);
            AppController.getInstance().setRecyclerviewContextmenuClick(null);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_internalstorage, null);

        futils = new Futils();
        mContext = this;
        context = mContext.getActivity();

        mainActivity = ((MainActivity) mContext.getActivity());
        mainActivityHelper = new MainActivityHelper(mainActivity);

        if (getActivity() instanceof FragmentChange) {
            fragmentChangeListener = (FragmentChange) getActivity();
        }

       /* AppController.getInstance().setButtonBackPressed(this);
        AppController.getInstance().setRecyclerviewContextmenuClick(this);*/


        initView(view);
        setupPreference(savedInstanceState, view);
        // FileOperationLayouts(view);
        setRetainInstance(false);
        setHasOptionsMenu(true);

        return view;
    }

    private void initView(View view) {

        utils = new Utils(context);
        isTablet = utils.isTablet();

        file_loader = (ProgressBar) view.findViewById(R.id.file_loader);
        mDetailLabel = (TextView) view.findViewById(R.id.detail_label);
        mPathLabel = (TextView) view.findViewById(R.id.path_label);
        mPathLabel.setText("path: /sdcard");
        // list = (ListView) view.findViewById(R.id.files_listview);

        files_recyclerView = (RecyclerView) view.findViewById(R.id.files_recyclerView);

        if (FileUtil.FileOperation) {
            chnageLayout = (boolean) PreferencesUtils.getValueFromPreference(context, Boolean.class, "layout", false);
            if (chnageLayout) {
                files_recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
            } else {
                GridLayoutManager gridLayoutManager = new GridLayoutManager(context, isTablet ? 6 : 4);
                files_recyclerView.setLayoutManager(gridLayoutManager);
            }
        } else
            files_recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        empty_layout = (LinearLayout) view.findViewById(R.id.empty_layout);
        no_storage_found_img = (ImageView) view.findViewById(R.id.no_storage_found_img);
        no_storage_found_txt = (TextView) view.findViewById(R.id.no_storage_found_txt);


        hidden_rename = (LinearLayout) view.findViewById(R.id.hidden_rename);
        hidden_add_favourite = (LinearLayout) view.findViewById(R.id.hidden_add_favourite);
        hidden_zip = (LinearLayout) view.findViewById(R.id.hidden_zip);
        hidden_share = (LinearLayout) view.findViewById(R.id.hidden_share);

        hidden_copy = (LinearLayout) view.findViewById(R.id.hidden_copy);
        hidden_move = (LinearLayout) view.findViewById(R.id.hidden_move);
        hidden_delete = (LinearLayout) view.findViewById(R.id.hidden_delete);
        hidden_detail = (LinearLayout) view.findViewById(R.id.hidden_detail);

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

    private void setupPreference(Bundle savedInstanceState, View view) {

        /*read settings*/
        mSettings = mContext.getContext().getSharedPreferences(Constats.PREFS_NAME, 0);
        boolean hide = mSettings.getBoolean(Constats.PREFS_HIDDEN_FILES, false);
        int sort = mSettings.getInt(Constats.PREFS_SORT_FILES, 1);

        Utils utils = new Utils(context);
       /* Log.d("External storage", utils.getStoragePaths("ExternalStorage"));
        Log.d("Internal storage", utils.getStoragePaths("InternalStorage"));*/

        mFileMag = new FileManager();
        mFileMag.setShowHiddenFiles(hide);
        mFileMag.setSortType(sort);

        // rootPath = utils.getStoragePaths("ExternalStorage");
        rootPath = utils.StoragePath("ExternalStorage");

        /*if (savedInstanceState != null)
            loadFiles(savedInstanceState.getString("location"), view);
        else*/
        loadFiles(utils.StoragePath("ExternalStorage"), view);

    }

    private void loadFiles(final String path, final View view) {

        if (loadFile_AsyncTask != null && loadFile_AsyncTask.getStatus() == AsyncTask.Status.RUNNING)
            loadFile_AsyncTask.cancel(true);
        loadFile_AsyncTask = new AsyncTask<Void, Void, Void>() {

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

                if (mHandler.mDataSource.size() > 0) {
                    data = mHandler.mDataSource.get(0);
                    empty_layout.setVisibility(View.GONE);
                    files_recyclerView.setVisibility(View.VISIBLE);
                    mPathLabel.setVisibility(View.VISIBLE);
                } else {

                    if (mHandler.mDataSource.size() <= 0) {
                        no_storage_found_img.setImageResource(R.drawable.ic_menu_sdcard_storage);
                        no_storage_found_txt.setText("No SD Card found");

                        empty_layout.setVisibility(View.VISIBLE);
                        files_recyclerView.setVisibility(View.GONE);
                        mPathLabel.setVisibility(View.GONE);
                    }
                }

                file_loader.setVisibility(View.GONE);
                boolean thumb = mSettings.getBoolean(Constats.PREFS_IMAGE_THUMBNAIL, true);
                mHandler.setUpdateLabels(mPathLabel, mDetailLabel, empty_layout);

                mHandler.setFragmentContext(mContext);
                mHandler.setStorageType(false, false);
                mHandler.setUpdateFileOperationLayout(hidden_buttons, StorageFragmentMain.hidden_paste_layout);
                // mHandler.setUpdateFileOperationLayout(hidden_buttons, hidden_paste_layout);
                mHandler.setUpdateFileOperationViews(hidden_rename, hidden_add_favourite, hidden_zip, hidden_share, hidden_copy, hidden_move, hidden_delete, hidden_detail);

                // mHandler.setTextColor(color);
                mHandler.setShowThumbnails(thumb);
                // mTable = mHandler.new TableRow();
                mTable = mHandler.new RecyclerViewTableRow();

                /*  sets the ListAdapter for our ListActivity and
                 *  gives our EventHandler class the same adapter
                */
                mHandler.setListAdapter(mTable);
                if (FileUtil.FileOperation) {
                    chnageLayout = (boolean) PreferencesUtils.getValueFromPreference(context, Boolean.class, "layout", false);
                    mHandler.mDelegate.chanageLayout(chnageLayout);
                }

                files_recyclerView.setAdapter(mTable);
                mHandler.mDelegate.killMultiSelect(true);

                openFiles = new OpenFiles(context);
                openFiles.setHandler(mFileMag, mHandler);
                openFiles.enableZipOptions(true);

                recyclerviewClick();
                FileOperationLayouts(view);

            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

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
        // Toast.makeText(context, "Path :- " + mFileMag.getCurrentDir(), Toast.LENGTH_SHORT).show();

        create_dialog = new Dialog(context);
        create_dialog.setCancelable(false);
        create_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (FileUtil.FileOperation)
            create_dialog.setContentView(R.layout.rename_filedirectory_dialog);
        else
            create_dialog.setContentView(R.layout.rename_filedirectory_dialog_new);

        create_dialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        create_dialog.getWindow().getAttributes().windowAnimations = R.style.confirmDeleteAnimation;
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

                            HFile hFile = new HFile();
                            hFile.setMode(mFileMag.isSmb() ? HFile.SMB_MODE : HFile.LOCAL_MODE);
                            hFile.setPath(mFileMag.getCurrentDir() + "/" + name + ".txt");

                            mainActivityHelper.mkFile(hFile, mContext);
                            create_dialog.dismiss();
                            /*File file = new File(mFileMag.getCurrentDir() + "/" + name + ".txt");
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
                            }*/

                        } else {

                            HFile hFile = new HFile();
                            hFile.setMode(mFileMag.isSmb() ? HFile.SMB_MODE : HFile.LOCAL_MODE);
                            hFile.setPath(mFileMag.getCurrentDir() + "/" + name);

                            mainActivityHelper.mkDir(hFile, mContext);
                            create_dialog.dismiss();
                            /* if (mFileMag.createDir(mFileMag.getCurrentDir() + "/", name) == 0)
                                Toast.makeText(context,
                                        "Folder " + name + " created",
                                        Toast.LENGTH_LONG).show();
                            else
                                Toast.makeText(context, "New folder was not created", Toast.LENGTH_SHORT).show();

                            create_dialog.dismiss();
                            String temp = mFileMag.getCurrentDir();
                            mHandler.updateDirectory(mFileMag.getNextDir(temp, true));*/

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

    public void paste() {
        boolean multi_select = mHandler.hasMultiSelectData();
        if (multi_select) {
            // copyDialog();
            // StorageFragmentMain.copyDialog();
            mHandler.copyFileMultiSelect(mFileMag.getCurrentDir());
        }
    }

    private void copyDialog() {

        copy_dialog = new Dialog(getActivity());
        copy_dialog.setCancelable(false);
        copy_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        copy_dialog.setContentView(R.layout.copy_dialog);
        copy_dialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        copy_dialog.getWindow().getAttributes().windowAnimations = R.style.confirmDeleteAnimation;
        copy_dialog.show();

        TextView dialog_cancel = (TextView) copy_dialog.findViewById(R.id.dialog_cancel);
        TextView dialog_hide = (TextView) copy_dialog.findViewById(R.id.dialog_hide);
        dialog_hide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copy_dialog.dismiss();

                mHandler.mDelegate.killMultiSelect(true);
                mHandler.updateDirectory(mFileMag.getNextDir(mFileMag.getCurrentDir(), true));
                StorageFragmentMain.delete_after_copy = false;

            }
        });

        dialog_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copy_dialog.dismiss();

                Toast.makeText(getActivity(), utils.getString(getActivity(), R.string.stopping), Toast.LENGTH_LONG).show();

                if (CopyIds.size() > 0) {
                    Intent i = new Intent("copycancel");
                    i.putExtra("id", CopyIds.get(0));
                    getActivity().sendBroadcast(i);

                    CancelledCopyIds.add(CopyIds.get(0));
                    CopyIds.remove(CopyIds.indexOf(CopyIds.get(0)));

                }

                mHandler.mDelegate.killMultiSelect(true);
                mHandler.updateDirectory(mFileMag.getNextDir(mFileMag.getCurrentDir(), true));
                StorageFragmentMain.delete_after_copy = false;

            }
        });


        TextView copy_item_name = (TextView) copy_dialog.findViewById(R.id.copy_item_name);
        StringBuilder copying_filename = new StringBuilder();

        if (mHandler.getSelectedItems().size() > 0) {

            ArrayList<HFile> hFile = new ArrayList<HFile>();

            for (String filename : mHandler.getSelectedItems()) {

                HFile hFile1 = new HFile();
                hFile1.setMode(mFileMag.isSmb() ? HFile.SMB_MODE : HFile.LOCAL_MODE);
                hFile1.setPath(filename);
                hFile.add(hFile1);
            }

            for (HFile hFile2 : hFile) {
                String filename = hFile2.getName();
                copying_filename.append(filename + ", ");
            }
            copy_item_name.setText(copying_filename.toString());


            TextView total_item = (TextView) copy_dialog.findViewById(R.id.total_item);
            futils.countFileFolder(mainActivity, hFile, total_item);

            TextView total_size = (TextView) copy_dialog.findViewById(R.id.total_size);
            futils.FileFolderSize(mainActivity, hFile, total_size);

            TextView from_path = (TextView) copy_dialog.findViewById(R.id.from_path);
            from_path.setText(hFile.get(0).getParent());

            TextView to_path = (TextView) copy_dialog.findViewById(R.id.to_path);
            to_path.setText(mFileMag.getCurrentDir());

            current_progress_item = (TextView) copy_dialog.findViewById(R.id.current_progress_item);
            total_progress_count = (TextView) copy_dialog.findViewById(R.id.total_progress_count);
            copy_progressbar = (ProgressBar) copy_dialog.findViewById(R.id.copy_progressbar);

        }
    }

    private void recyclerviewClick() {

        files_recyclerView.addOnItemTouchListener(new RecyclerTouchListener(AppController.getInstance().getApplicationContext(), files_recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                try {
                    RecyclerClick(view, position);
                } catch (Exception e) {
                }
            }

            @Override
            public void onLongClick(View view, int position) {
                if (StorageFragmentMain.multi_select_flag) {
                    mHandler.mDelegate.killMultiSelect(true);
                } else {
                    LinearLayout hidden_lay =
                            (LinearLayout) getView().findViewById(R.id.hidden_buttons);

                    StorageFragmentMain.multi_select_flag = true;
                    hidden_lay.setVisibility(LinearLayout.VISIBLE);
                }
                //Toast.makeText(context, "Long press2", Toast.LENGTH_SHORT).show();
                mHandler.mDelegate.notifyDataSetChanged();
                try {
                    RecyclerClick(view, position);
                } catch (Exception e) {
                }
                //Toast.makeText(context, "Long press1", Toast.LENGTH_SHORT).show();
            }
        }));
    }

    private void RecyclerClick(View view, int position1) {
        View child = view;

        if (child != null) {

            int position = position1;
            position = position;
            // mSelectedListItem = mHandler.getData(position);

            final String item = mHandler.getData(position);
            boolean multiSelect = mHandler.isMultiSelected();
            final File file = new File(mFileMag.getCurrentDir() + "/" + item);
            String item_ext = null;

            try {
                if (file.isFile())
                    item_ext = item.substring(item.lastIndexOf("."), item.length());
            } catch (IndexOutOfBoundsException e) {
                item_ext = "";
            }

            /*
             * If the user has multi-select on, we just need to record the file
             * not make an intent for it.
             */
            if (multiSelect) {
                mTable.addMultiPosition(position, file.getPath());

            } else {
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
                } else {
                    openFiles.open(file.getAbsolutePath());
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
        //Uri uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", data);
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", data);
        } else {
            uri = Uri.fromFile(data);
        }

        ret.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        ret.setData(uri);
        getActivity().setResult(getActivity().RESULT_OK, ret);
        getActivity().finish();
    }

    @Override
    public void onButtonBackPressed(int keycode, KeyEvent event) {
        onBackPressed(keycode, event);
    }

    private void onBackPressed(int keycode, KeyEvent event) {
        String current = mFileMag.getCurrentDir();

        if (keycode == KeyEvent.KEYCODE_SEARCH) {
            // showDialog(SEARCH_B);

        } else if (keycode == KeyEvent.KEYCODE_BACK && mUseBackKey && !current.equals(rootPath)) {
            if (mHandler.isMultiSelected()) {
                mTable.killMultiSelect(true);
                //Toast.makeText(context, "Multi-select is now off", Toast.LENGTH_SHORT).show();

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
                //Toast.makeText(context, "Multi-select is now off", Toast.LENGTH_SHORT).show();
            } else {

                // mUseBackKey = false;
                mPathLabel.setText(mFileMag.getCurrentDir());
                if (mFileMag.isSmb()) {
                    fragmentChangeListener.OnFragmentChange(5, MainActivity.FG_TAG_NETWORK);
                } else {
                    fragmentChangeListener.OnFragmentChange(0, MainActivity.FG_TAG_HOME);
                }
            }
        } else if (keycode == KeyEvent.KEYCODE_BACK && !mUseBackKey && current.equals("/")) {
            //mContext.getActivity().finish();
        }
    }

    @Override
    public void ContextmenuClick(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {

   /*     if (mHandler.multi_select_flag) {
            mHandler.mDelegate.killMultiSelect(true);
        } else {
            LinearLayout hidden_lay =
                    (LinearLayout) getView().findViewById(R.id.hidden_buttons);

            mHandler.multi_select_flag = true;
            hidden_lay.setVisibility(LinearLayout.VISIBLE);
        }
        mHandler.mDelegate.notifyDataSetChanged();*/
    }

    @Override
    public void onPasteButtonPressed() {
        paste();
    }

    @Override
    public void onCancelButtonPressed() {
        Toast.makeText(context, "paste cancelled", Toast.LENGTH_SHORT).show();
        mHandler.mDelegate.killMultiSelect(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();

        inflater.inflate(R.menu.storage_menu, menu);
        MenuItem changelayout = menu.findItem(R.id.menu_addblock);
        changelayout.setVisible(false);

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


        MenuItem createFile = menu.findItem(R.id.menu_new_file);
        MenuItem createFolder = menu.findItem(R.id.menu_new_folder);
        final MenuItem change_layout = menu.findItem(R.id.change_layout);

        createFile.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (mHandler.mDataSource.size() > 0)
                    CreateFileAndFolder("File");
                else
                    Toast.makeText(context, "No SD Card found", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        createFolder.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (mHandler.mDataSource.size() > 0)
                    CreateFileAndFolder("Folder");
                else
                    Toast.makeText(context, "No SD Card found", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        change_layout.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                chnageLayout = (boolean) PreferencesUtils.getValueFromPreference(context, Boolean.class, "layout", false);
                chnageLayout = !chnageLayout;
                if (chnageLayout) {
                    change_layout.setTitle("Grid View");
                    files_recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
                } else {
                    change_layout.setTitle("List View");
                    GridLayoutManager gridLayoutManager = new GridLayoutManager(context, isTablet ? 6 : 4);
                    files_recyclerView.setLayoutManager(gridLayoutManager);
                }

                mHandler.mDelegate.chanageLayout(chnageLayout);
                files_recyclerView.setAdapter(mTable);
                mHandler.mDelegate.notifyDataSetChanged();
                PreferencesUtils.saveToPreference(context, "layout", chnageLayout);
                return false;
            }
        });
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        try {

            MenuItem change_layout = menu.findItem(R.id.change_layout);
            if (FileUtil.FileOperation == true) {
                change_layout.setVisible(true);
            } else {
                change_layout.setVisible(false);
            }
        } catch (Exception e) {
        }
    }

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.search:
                handleMenuSearch();
                break;

            case R.id.menu_new_file:
                // Toast.makeText(mainActivity, "External open", Toast.LENGTH_SHORT).show();
                if (mHandler.mDataSource.size() > 0)
                    CreateFileAndFolder("File");
                else
                    Toast.makeText(context, "No SD Card found", Toast.LENGTH_SHORT).show();

                return true;

            case R.id.menu_new_folder:

                if (mHandler.mDataSource.size() > 0)
                    CreateFileAndFolder("Folder");
                else
                    Toast.makeText(context, "No SD Card found", Toast.LENGTH_SHORT).show();

                return true;

            case R.id.change_layout:
                chnageLayout = (boolean) PreferencesUtils.getValueFromPreference(context, Boolean.class, "layout", false);
                chnageLayout = !chnageLayout;
                if (chnageLayout) {
                    item.setTitle("Grid View");
                    files_recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
                } else {
                    item.setTitle("List View");
                    GridLayoutManager gridLayoutManager = new GridLayoutManager(context, isTablet ? 6 : 4);
                    files_recyclerView.setLayoutManager(gridLayoutManager);
                }

                mHandler.mDelegate.chanageLayout(chnageLayout);
                files_recyclerView.setAdapter(mTable);
                mHandler.mDelegate.notifyDataSetChanged();
                PreferencesUtils.saveToPreference(context, "layout", chnageLayout);

                break;
        }
        return true;
    }*/

}
