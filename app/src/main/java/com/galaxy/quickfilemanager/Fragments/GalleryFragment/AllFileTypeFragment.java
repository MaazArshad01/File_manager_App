package com.galaxy.quickfilemanager.Fragments.GalleryFragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.galaxy.quickfilemanager.FileOperation.AudioVideoThumbnailCreator;
import com.galaxy.quickfilemanager.FileOperation.FileManager;
import com.galaxy.quickfilemanager.Fragments.GalleryFragment.Adapter.ImagesListAdapter;
import com.galaxy.quickfilemanager.Interfaces.FragmentChange;
import com.galaxy.quickfilemanager.MainActivity;
import com.galaxy.quickfilemanager.Model.MediaFileListModel;
import com.galaxy.quickfilemanager.Utils.AppController;
import com.galaxy.quickfilemanager.Utils.FileUtil;
import com.galaxy.quickfilemanager.Utils.Futils;
import com.galaxy.quickfilemanager.Utils.PreferencesUtils;
import com.galaxy.quickfilemanager.Utils.RecyclerTouchListener;
import com.galaxy.quickfilemanager.Utils.Utils;
import com.galaxy.quickfilemanager.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class AllFileTypeFragment extends Fragment implements MainActivity.ButtonBackPressListener {

    private static final String ARG_PARAM_FILE_TYPE = "FileType";
    private static final String ARG_PARAM_FRAGMENT_TYPE = "ImageTypeFragment";

    ArrayList<String> mDirContent = new ArrayList<String>();
    private String mParamFRAGMENT_TYPE_PARAM;
    private String mParamFileType;
    private ArrayList<MediaFileListModel> imageListModelsArray;
    private ImagesListAdapter imagesListAdapter;
    private LinearLayout noMediaLayout;
    private OnFragmentInteractionListener mListener;
    private RecyclerView recyclerView;
    private Context mContext;
    private File root;
    private Futils fUtils;
    private Utils utils;
    private boolean isTablet = false;
    private Spinner spinner_apktype;
    private String[] spinner_apk_entries;
    private LoadFiles loadFiles;
    private AudioVideoThumbnailCreator mThumbnail;
    private LinearLayout hidden_layout;
    private FragmentChange fragmentChangeListener;
    private boolean loadCompleted = false;
    private LinearLayout hidden_more;
    private PopupWindow pw;
    private LinearLayout hidden_copy, hidden_move, hidden_delete, hidden_rename, hidden_share, hidden_detail;

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
                            // Toast.makeText(context, intent.getStringExtra("delete_msg"), Toast.LENGTH_SHORT).show();
                        }

                        try {

                            LoadFiles loadFiles1 = new LoadFiles(mContext);
                            loadFiles1.setImageParams(mParamFRAGMENT_TYPE_PARAM, recyclerView, noMediaLayout, imagesListAdapter);
                            imageListModelsArray.clear();

                            if (mParamFileType.equalsIgnoreCase("Gallery")) {
                                loadFiles1.getFileList("Gallery");
                            } else if (mParamFileType.equalsIgnoreCase("Audio")) {
                                loadFiles1.getFileList("Audio");
                            } else if (mParamFileType.equalsIgnoreCase("Video")) {
                                loadFiles1.getFileList("Video");
                            } else if (mParamFileType.equalsIgnoreCase("Doc")) {
                                loadFiles1.getFileList("Documents");
                            }


                            loadFiles1.setLoadCompletedListener(new LoadFiles.LoadCompletedListener() {
                                @Override
                                public void onLoadCompleted(ArrayList<MediaFileListModel> imageListModels) {

                                    imageListModelsArray.clear();
                                    imageListModelsArray.addAll(imageListModels);
                                    imagesListAdapter.updateFilterData(imageListModels);
                                    imagesListAdapter.notifyDataSetChanged();


                                    if (mParamFileType.equalsIgnoreCase("Video") || mParamFileType.equalsIgnoreCase("Audio")) {
                                        if (mThumbnail == null)
                                            mThumbnail = new AudioVideoThumbnailCreator(52, 52);

                                        if (mParamFileType.equalsIgnoreCase("Video"))
                                            mThumbnail.setFileType("Video");
                                        else
                                            mThumbnail.setFileType("Audio");

                                        for (int i = 0; i < imageListModels.size(); i++) {
                                            //   for (MediaFileListModel mediaFileListModel : imageListModels)
                                            MediaFileListModel mediaFileListModel = imageListModels.get(i);
                                            if (mediaFileListModel.getFilePath().length() != 0) {
                                                Bitmap thumb = mThumbnail.isBitmapCached(mediaFileListModel.getFilePath());

                                                if (thumb == null) {

                                                    final Handler handle = new Handler(new Handler.Callback() {
                                                        public boolean handleMessage(Message msg) {
                                                            imagesListAdapter.notifyItemChanged((int) msg.obj);
                                                            //imagesListAdapter.notifyDataSetChanged();
                                                            return true;
                                                        }
                                                    });

                                                    try {
                                                        mThumbnail.createNewThumbnail(imageListModels, handle, mContext);
                                                        if (!mThumbnail.isAlive())
                                                            mThumbnail.start();
                                                    } catch (Exception e) {
                                                    }

                                                } else {
                                                    // holder.imgItemIcon.setImageBitmap(thumb);
                                                }

                                            } else {
                                                // holder.imgItemIcon.setImageResource(R.drawable.image);
                                            }
                                        }
                                    } else {
                                        // imageListModelsArray = imageListModels;
                                        // imagesListAdapter = new ImagesListAdapter(imageListModelsArray, mParamFileType, mContext);
                                        // recyclerView.setAdapter(imagesListAdapter);
                                        // imagesListAdapter.notifyDataSetChanged();
                                    }

                                    loadCompleted = true;
                                }
                            });

                        } catch (Exception e) {
                        }


                        if (pr_dialog != null)
                            pr_dialog.dismiss();


                    }
                }
            }
        }
    };

    public AllFileTypeFragment() {
        // Required empty public constructor
    }

    public static AllFileTypeFragment newInstance(String FileType, String ImageTypeFragment) {

        // FileType = "Gallery", "Audio", "Video", "Doc", "APK"
        // ImageTypeFragment = "All","Camera","Others"

        AllFileTypeFragment fragment = new AllFileTypeFragment();
        Bundle args = new Bundle();

        args.putString(ARG_PARAM_FILE_TYPE, FileType);
        args.putString(ARG_PARAM_FRAGMENT_TYPE, ImageTypeFragment);

        fragment.setArguments(args);
        return fragment;
    }

    public void refreshListData() {
        try {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    LoadFiles loadFiles1 = new LoadFiles(mContext);
                    loadFiles1.setImageParams(mParamFRAGMENT_TYPE_PARAM, recyclerView, noMediaLayout, imagesListAdapter);
                    imageListModelsArray.clear();

                    loadFiles1.setObserver(new LoadFiles.ScanObserver() {
                        @Override
                        public void fileFound(MediaFileListModel mediaFileListModel) {
                            imageListModelsArray.add(mediaFileListModel);
                            imagesListAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void searchFinished() {
                            // imagesListAdapter = new ImagesListAdapter(imageListModelsArray, mParamFileType);
                            // recyclerView.setAdapter(imagesListAdapter);
                            // imagesListAdapter.notifyDataSetChanged();
                        }
                    });

                    if (mParamFileType.equalsIgnoreCase("Gallery")) {
                        loadFiles1.getFileList("Gallery");
                    } else if (mParamFileType.equalsIgnoreCase("Audio")) {
                        loadFiles1.getFileList("Audio");
                    } else if (mParamFileType.equalsIgnoreCase("Video")) {
                        loadFiles1.getFileList("Video");
                    } else if (mParamFileType.equalsIgnoreCase("Doc")) {
                        loadFiles1.getFileList("Documents");
                    }


                    loadFiles1.setLoadCompletedListener(new LoadFiles.LoadCompletedListener() {
                        @Override
                        public void onLoadCompleted(ArrayList<MediaFileListModel> imageListModels) {

                            if (mThumbnail != null) {

                            }

                            imageListModelsArray.clear();
                            imageListModelsArray.addAll(imageListModels);
                            imagesListAdapter.updateFilterData(imageListModels);
                            imagesListAdapter.notifyDataSetChanged();

                            if (mParamFileType.equalsIgnoreCase("Video") || mParamFileType.equalsIgnoreCase("Audio")) {
                                if (mThumbnail == null)
                                    mThumbnail = new AudioVideoThumbnailCreator(52, 52);

                                if (mParamFileType.equalsIgnoreCase("Video"))
                                    mThumbnail.setFileType("Video");
                                else
                                    mThumbnail.setFileType("Audio");

                                for (int i = 0; i < imageListModels.size(); i++) {
                                    //   for (MediaFileListModel mediaFileListModel : imageListModels)
                                    MediaFileListModel mediaFileListModel = imageListModels.get(i);
                                    if (mediaFileListModel.getFilePath().length() != 0) {
                                        Bitmap thumb = mThumbnail.isBitmapCached(mediaFileListModel.getFilePath());

                                        if (thumb == null) {

                                            final Handler handle = new Handler(new Handler.Callback() {
                                                public boolean handleMessage(Message msg) {
                                                    imagesListAdapter.notifyItemChanged((int) msg.obj);
                                                    //imagesListAdapter.notifyDataSetChanged();
                                                    return true;
                                                }
                                            });

                                            try {
                                                mThumbnail.createNewThumbnail(imageListModels, handle, mContext);
                                                if (!mThumbnail.isAlive())
                                                    mThumbnail.start();
                                            } catch (Exception e) {
                                            }

                                        } else {
                                            // holder.imgItemIcon.setImageBitmap(thumb);
                                        }

                                    } else {
                                        // holder.imgItemIcon.setImageResource(R.drawable.image);
                                    }
                                }
                            } else {
                                // imageListModelsArray = imageListModels;
                                // imagesListAdapter = new ImagesListAdapter(imageListModelsArray, mParamFileType, mContext);
                                // recyclerView.setAdapter(imagesListAdapter);
                                // imagesListAdapter.notifyDataSetChanged();
                            }

                            loadCompleted = true;
                        }
                    });
                }
            }, 500);

            imagesListAdapter.notifyDataSetChanged();
        } catch (Exception e) {
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (mParamFRAGMENT_TYPE_PARAM != null)
                Log.d("Type Visible", mParamFRAGMENT_TYPE_PARAM);
            AppController.getInstance().setButtonBackPressed(this);
        } else {
            AppController.getInstance().setButtonBackPressed(null);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(DeleteCompletedBroadcast, new IntentFilter("DeleteCompleted"));
    }

    @Override
    public void onPause() {
        super.onPause();
        /*if (imagesListAdapter.mMultiSelectData != null)
            imagesListAdapter.mMultiSelectData.clear();
        imagesListAdapter.multi_select_flag = false;*/

        getActivity().unregisterReceiver(DeleteCompletedBroadcast);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParamFileType = getArguments().getString(ARG_PARAM_FILE_TYPE);
            mParamFRAGMENT_TYPE_PARAM = getArguments().getString(ARG_PARAM_FRAGMENT_TYPE);
        }
    }

    private void initLayout(View view) {
        hidden_copy = (LinearLayout) view.findViewById(R.id.hidden_copy);
        hidden_move = (LinearLayout) view.findViewById(R.id.hidden_move);
        hidden_delete = (LinearLayout) view.findViewById(R.id.hidden_delete);

        hidden_copy.setOnClickListener(imagesListAdapter);
        hidden_move.setOnClickListener(imagesListAdapter);
        hidden_delete.setOnClickListener(imagesListAdapter);

        hidden_layout = (LinearLayout) view.findViewById(R.id.hidden_layout);
        imagesListAdapter.setHidden_layout(hidden_layout);

        hidden_more = (LinearLayout) view.findViewById(R.id.hidden_more);
        hidden_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initiatePopupWindow(view);
            }
        });

        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //Inflate the view from a predefined XML layout
        View layout = inflater.inflate(R.layout.popup, null);
        // create a 300px width and 470px height PopupWindow
        pw = new PopupWindow(layout, 450, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        /*Button cancelButton = (Button) pw.getContentView().findViewById(R.id.btn_close_popup);
        cancelButton.setOnClickListener(imagesListAdapter);*/

        hidden_rename = (LinearLayout) layout.findViewById(R.id.hidden_rename);
        hidden_share = (LinearLayout) layout.findViewById(R.id.hidden_share);
        hidden_detail = (LinearLayout) layout.findViewById(R.id.hidden_detail);
        hidden_rename.setOnClickListener(imagesListAdapter);
        hidden_share.setOnClickListener(imagesListAdapter);
        hidden_detail.setOnClickListener(imagesListAdapter);

        imagesListAdapter.setUpdateFileOperationViews(hidden_copy, hidden_move, hidden_delete, hidden_more, hidden_rename, hidden_share, hidden_detail, pw);
    }

    private void initiatePopupWindow(View v) {

        try {
            pw.setOutsideTouchable(true);
            pw.setTouchable(true);
            pw.setBackgroundDrawable(new BitmapDrawable());
            // display the popup in the center
            pw.showAtLocation(v, Gravity.BOTTOM, v.getWidth() + 10, v.getHeight() + 5);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_images_list, container, false);
        if (mParamFRAGMENT_TYPE_PARAM != null)
            Log.d("Type Visible", mParamFRAGMENT_TYPE_PARAM);
        AppController.getInstance().setButtonBackPressed(this);

        setRetainInstance(false);
        setHasOptionsMenu(true);

        mContext = getActivity();

        if (getActivity() instanceof FragmentChange) {
            fragmentChangeListener = (FragmentChange) getActivity();
        }


        fUtils = new Futils();
        utils = new Utils(mContext);
        isTablet = utils.isTablet();

        spinner_apktype = (Spinner) view.findViewById(R.id.spinner_apktype);
        spinner_apk_entries = getResources().getStringArray(R.array.spinner_apk_entries);
        spinner_apktype.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                String apkType = String.valueOf(spinner_apk_entries[pos]);

                LoadFiles loadFiles = new LoadFiles(mContext);
                loadFiles.setImageParams(mParamFRAGMENT_TYPE_PARAM, recyclerView, noMediaLayout, imagesListAdapter);

                loadFiles.setApkListType(apkType);
                loadFiles.getFileList("Apk");
                loadFiles.setLoadCompletedListener(new LoadFiles.LoadCompletedListener() {
                    @Override
                    public void onLoadCompleted(ArrayList<MediaFileListModel> imageListModels) {
                        if (mParamFileType.equalsIgnoreCase("Apk")) {
                            imageListModelsArray.clear();
                            if (imageListModelsArray != null) {
                                imagesListAdapter.updateFilterData(imageListModels);
                            }
                            imageListModelsArray.addAll(imageListModels);
                            imagesListAdapter.notifyDataSetChanged();
                            loadCompleted = true;
                        }
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_images_list);
        noMediaLayout = (LinearLayout) view.findViewById(R.id.noMediaLayout);
        imageListModelsArray = new ArrayList<>();

        imagesListAdapter = new ImagesListAdapter(imageListModelsArray, mParamFileType, mContext);
        imagesListAdapter.fregmentContext(this);

        recyclerView.setHasFixedSize(true);
        changeRecycleViewLayout(mParamFileType);
        recyclerView.setAdapter(imagesListAdapter);

        if (mParamFileType.equalsIgnoreCase("Gallery")) {
            getFileLists("", "Gallery");
        } else if (mParamFileType.equalsIgnoreCase("Audio")) {
            getFileLists("", "Audio");
        } else if (mParamFileType.equalsIgnoreCase("Video")) {
            root = new File(Environment.getExternalStorageDirectory()
                    .getAbsolutePath());
            getFileLists(root.getPath(), "Video");
        } else if (mParamFileType.equalsIgnoreCase("Doc")) {
            root = new File(Environment.getExternalStorageDirectory()
                    .getAbsolutePath());
            getFileLists(root.getPath(), "Documents");
        } else if (mParamFileType.equalsIgnoreCase("Apk")) {
            spinner_apktype.setVisibility(View.VISIBLE);
            getFileLists("", "Apk");
        }

        initLayout(view);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(AppController.getInstance().getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                RecyclerClick(view, position);
            }

            @Override
            public void onLongClick(View view, int position) {
                if (!mParamFileType.equalsIgnoreCase("Apk")) {
                    if (loadCompleted) {
                        if (imagesListAdapter.multi_select_flag) {
                            imagesListAdapter.killMultiSelect(true);
                        } else {
                            imagesListAdapter.multi_select_flag = true;
                            hidden_layout.setVisibility(LinearLayout.VISIBLE);
                            // bookmarkListAdapter.killMultiSelect(false);
                        }

                        imagesListAdapter.notifyItemChanged(position);
                        RecyclerClick(view, position);
                    } else {
                        Toast.makeText(mContext, "Loading files please wait..", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }));
        return view;
    }

    private void RecyclerClick(View view, int position) {

        boolean multiSelect = imagesListAdapter.isMultiSelected();
        if (multiSelect) {
            imagesListAdapter.addMultiPosition(position, imageListModelsArray.get(position).getFilePath());
        } else {
            if (mParamFileType.equalsIgnoreCase("Gallery")) {
                MediaFileListModel img = imageListModelsArray.get(position);
                Intent picIntent = new Intent();
                picIntent.setAction(Intent.ACTION_VIEW);
                // Uri uri = FileProvider.getUriForFile(mContext, mContext.getPackageName() + ".provider", new File(img.getFilePath()));
                Uri uri;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    uri = FileProvider.getUriForFile(mContext, mContext.getApplicationContext().getPackageName() + ".provider", new File(img.getFilePath()));
                } else {
                    uri = Uri.fromFile(new File(img.getFilePath()));
                }

                picIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                picIntent.setDataAndType(uri, "image/*");
                startActivity(picIntent);
            } else if (mParamFileType.equalsIgnoreCase("Audio")) {
                MediaFileListModel img = imageListModelsArray.get(position);
                Intent picIntent = new Intent();
                picIntent.setAction(Intent.ACTION_VIEW);
                //Uri uri = FileProvider.getUriForFile(mContext, mContext.getPackageName() + ".provider", new File(img.getFilePath()));
                Uri uri;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    uri = FileProvider.getUriForFile(mContext, mContext.getApplicationContext().getPackageName() + ".provider", new File(img.getFilePath()));
                } else {
                    uri = Uri.fromFile(new File(img.getFilePath()));
                }

                picIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                picIntent.setDataAndType(uri, "audio/*");
                startActivity(picIntent);
            } else if (mParamFileType.equalsIgnoreCase("Video")) {
                MediaFileListModel img = imageListModelsArray.get(position);
                Intent picIntent = new Intent();
                picIntent.setAction(Intent.ACTION_VIEW);
                // Uri uri = FileProvider.getUriForFile(mContext, mContext.getPackageName() + ".provider", new File(img.getFilePath()));
                Uri uri;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    uri = FileProvider.getUriForFile(mContext, mContext.getApplicationContext().getPackageName() + ".provider", new File(img.getFilePath()));
                } else {
                    uri = Uri.fromFile(new File(img.getFilePath()));
                }

                picIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                picIntent.setDataAndType(uri, "video/*");
                startActivity(picIntent);
            } else if (mParamFileType.equalsIgnoreCase("Doc")) {
                MediaFileListModel docFiles = imageListModelsArray.get(position);
                openDocFileIntents(docFiles);
            } else if (mParamFileType.equalsIgnoreCase("Apk")) {
                MediaFileListModel apkFiles = imageListModelsArray.get(position);
                if (FileUtil.FileOperation)
                    propertyDialog(apkFiles);
                else
                    propertyDialogC(apkFiles);
                    /*FileManager fileManager = new FileManager();
                    fileManager.copyToDirectory(apkFiles.getFilePath(), "/storage/emulated/0");
                    fileManager.renameTarget("/storage/emulated/0/base.apk", apkFiles.getFileName());*/
            }
        }
    }

    public void propertyDialog(final MediaFileListModel apkFiles) {

        final Dialog properties_dialog = new Dialog(mContext);
        properties_dialog.setCancelable(true);
        properties_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        properties_dialog.setContentView(R.layout.property_dialog);
        properties_dialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        properties_dialog.getWindow().getAttributes().windowAnimations = R.style.confirmDeleteAnimation;
        properties_dialog.show();

        //Field Init
        LinearLayout app_property_layout = (LinearLayout) properties_dialog.findViewById(R.id.app_property_layout);
        app_property_layout.setVisibility(View.VISIBLE);
        LinearLayout apps_bottom_btns = (LinearLayout) properties_dialog.findViewById(R.id.apps_bottom_btns);
        apps_bottom_btns.setVisibility(View.VISIBLE);

        LinearLayout apps_notinstalled_bottom_btns = (LinearLayout) properties_dialog.findViewById(R.id.apps_notinstalled_bottom_btns);


        if (apkFiles.getApkType().equalsIgnoreCase("InstalledApps")) {
            apps_notinstalled_bottom_btns.setVisibility(View.GONE);
            apps_bottom_btns.setVisibility(View.VISIBLE);
        } else {
            apps_notinstalled_bottom_btns.setVisibility(View.VISIBLE);
            apps_bottom_btns.setVisibility(View.GONE);
        }


        ImageView file_icon = (ImageView) properties_dialog.findViewById(R.id.file_icon);
        TextView file_name = (TextView) properties_dialog.findViewById(R.id.file_name);
        TextView versionCode = (TextView) properties_dialog.findViewById(R.id.versionCode);
        TextView filesize = (TextView) properties_dialog.findViewById(R.id.filesize);
        TextView packagename = (TextView) properties_dialog.findViewById(R.id.packagename);

        file_icon.setImageBitmap(apkFiles.getMediaBitmap());
        file_name.setText(apkFiles.getFileName());
        versionCode.setText(apkFiles.getAppVersionName());
        filesize.setText(apkFiles.getFileSize());
        packagename.setText(apkFiles.getAppPackageName());


        // ===========================   Installed app button   ======================================
        Button dialog_cancel = (Button) properties_dialog.findViewById(R.id.dialog_cancel);
        dialog_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                properties_dialog.dismiss();
            }
        });

        Button dialog_open = (Button) properties_dialog.findViewById(R.id.dialog_open);
        dialog_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openApp(apkFiles.getAppPackageName());
            }
        });

        Button dialog_uninstall = (Button) properties_dialog.findViewById(R.id.dialog_uninstall);
        dialog_uninstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri packageUri = Uri.parse("package:" + apkFiles.getAppPackageName());
                Intent uninstallIntent =
                        new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
                startActivity(uninstallIntent);
            }
        });
        // ===========================  End Installed app button   ======================================


        Button dialog_app_cancel = (Button) properties_dialog.findViewById(R.id.dialog_app_cancel);
        dialog_app_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                properties_dialog.dismiss();
            }
        });


        Button dialog_install = (Button) properties_dialog.findViewById(R.id.dialog_install);
        dialog_install.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent installIntent =
                        new Intent(Intent.ACTION_INSTALL_PACKAGE);
                String mimeType = FileManager.getMimeType(apkFiles.getFilePath()); //myMime.getMimeTypeFromExtension(fileExt(getFile()).substring(1));
                Uri uri = FileProvider.getUriForFile(mContext, mContext.getPackageName() + ".provider", new File(apkFiles.getFilePath()));
                installIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                installIntent.setDataAndType(uri, mimeType);
                startActivity(installIntent);
            }
        });
    }

    public void propertyDialogC(final MediaFileListModel apkFiles) {

        final Dialog properties_dialog = new Dialog(mContext);
        properties_dialog.setCancelable(true);
        properties_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        properties_dialog.setContentView(R.layout.property_dialog_c);
        properties_dialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        properties_dialog.getWindow().getAttributes().windowAnimations = R.style.confirmDeleteAnimation;
        properties_dialog.show();

        //Field Init
        LinearLayout apps_bottom_btns = (LinearLayout) properties_dialog.findViewById(R.id.apps_bottom_btns);
        apps_bottom_btns.setVisibility(View.VISIBLE);

        LinearLayout apps_notinstalled_bottom_btns = (LinearLayout) properties_dialog.findViewById(R.id.apps_notinstalled_bottom_btns);

        if (apkFiles.getApkType().equalsIgnoreCase("InstalledApps")) {
            apps_notinstalled_bottom_btns.setVisibility(View.GONE);
            apps_bottom_btns.setVisibility(View.VISIBLE);
        } else {
            apps_notinstalled_bottom_btns.setVisibility(View.VISIBLE);
            apps_bottom_btns.setVisibility(View.GONE);
        }

        ImageView file_icon = (ImageView) properties_dialog.findViewById(R.id.file_icon);
        TextView file_name = (TextView) properties_dialog.findViewById(R.id.file_name);

        file_icon.setImageBitmap(apkFiles.getMediaBitmap());
        file_name.setText(apkFiles.getFileName());

        // ===========================   Installed app button   ======================================

        Button dialog_open = (Button) properties_dialog.findViewById(R.id.dialog_open);
        dialog_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openApp(apkFiles.getAppPackageName());
            }
        });

        Button dialog_backup = (Button) properties_dialog.findViewById(R.id.dialog_backup);
        dialog_backup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backupApkFile(apkFiles);
                properties_dialog.dismiss();
            }
        });
        // ===========================  End Installed app button   ======================================


        // ===========================  Not Installed app button   ======================================
        Button dialog_app_cancel = (Button) properties_dialog.findViewById(R.id.dialog_app_cancel);
        dialog_app_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                properties_dialog.dismiss();
            }
        });

        Button dialog_install = (Button) properties_dialog.findViewById(R.id.dialog_install);
        dialog_install.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent installIntent =
                            new Intent(Intent.ACTION_INSTALL_PACKAGE);
                    String mimeType = FileManager.getMimeType(apkFiles.getFilePath()); //myMime.getMimeTypeFromExtension(fileExt(getFile()).substring(1));
                    //Uri uri = FileProvider.getUriForFile(mContext, mContext.getPackageName() + ".provider", new File(apkFiles.getFilePath()));

                    Uri uri;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        uri = FileProvider.getUriForFile(mContext, mContext.getApplicationContext().getPackageName() + ".provider", new File(apkFiles.getFilePath()));
                    } else {
                        uri = Uri.fromFile(new File(apkFiles.getFilePath()));
                    }

                    installIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    installIntent.setDataAndType(uri, mimeType);
                    startActivity(installIntent);


                } catch (Exception e) {
                }
            }
        });
        // ===========================  End Not Installed app button   ======================================

    }

    private void backupApkFile(MediaFileListModel apkFiles) {

        try {

            File apkfile = new File(apkFiles.getFilePath());

            File destinationPath = new File(Environment.getExternalStorageDirectory().toString() + "/FileExplorer");
            destinationPath.mkdirs();
            destinationPath = new File(destinationPath.getPath() + "/" + apkFiles.getFileName() + ".apk");
            destinationPath.createNewFile();

            InputStream in = new FileInputStream(apkfile);
            OutputStream out = new FileOutputStream(destinationPath);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();

            Toast.makeText(mContext, "Backup :- " + destinationPath.getAbsolutePath(), Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
        }

    }

    public void openApp(String packageName) {
        PackageManager manager = mContext.getPackageManager();
        try {
            Intent i = manager.getLaunchIntentForPackage(packageName);
            if (i == null) {
                Toast.makeText(mContext, "Can't open app", Toast.LENGTH_SHORT).show();
                //throw new PackageManager.NameNotFoundException();
            }
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            mContext.startActivity(i);
        } catch (Exception e) {
            Toast.makeText(mContext, "Can't open app", Toast.LENGTH_SHORT).show();
        }
    }

    public void openDocFileIntents(MediaFileListModel docFiles) {

        Intent newIntent = new Intent(Intent.ACTION_VIEW);
        String mimeType = FileManager.getMimeType(docFiles.getFilePath()); //myMime.getMimeTypeFromExtension(fileExt(getFile()).substring(1));

        //Uri uri = FileProvider.getUriForFile(mContext, mContext.getPackageName() + ".provider", new File(docFiles.getFilePath()));
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(mContext, mContext.getApplicationContext().getPackageName() + ".provider", new File(docFiles.getFilePath()));
        } else {
            uri = Uri.fromFile(new File(docFiles.getFilePath()));
        }

        newIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        // newIntent.setDataAndType(Uri.fromFile(new File(docFiles.getFilePath())), mimeType);
        newIntent.setDataAndType(uri, mimeType);
        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            mContext.startActivity(newIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(mContext, "No handler for this type of file.", Toast.LENGTH_LONG).show();
        }
    }

    public void changeRecycleViewLayout(String mParamFileType) {
        if (mParamFileType.equalsIgnoreCase("Gallery")) {

            GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext, isTablet ? 6 : 3);
            recyclerView.setLayoutManager(gridLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());

        } else if (mParamFileType.equalsIgnoreCase("Audio")) {

            RecyclerView.LayoutManager mAudioLayoutManager = new LinearLayoutManager(AppController.getInstance().getApplicationContext());
            recyclerView.setLayoutManager(mAudioLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());

        } else if (mParamFileType.equalsIgnoreCase("Video")) {

          /*  RecyclerView.LayoutManager mVideoLayoutManager = new LinearLayoutManager(AppController.getInstance().getApplicationContext());
            recyclerView.setLayoutManager(mVideoLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());*/
            GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext, isTablet ? 6 : 4);
            recyclerView.setLayoutManager(gridLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());

        } else if (mParamFileType.equalsIgnoreCase("Doc")) {

            GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext, isTablet ? 6 : 4);
            recyclerView.setLayoutManager(gridLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
        } else if (mParamFileType.equalsIgnoreCase("Apk")) {

            GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext, isTablet ? 6 : 4);
            recyclerView.setLayoutManager(gridLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
        }
    }

    private void getFileLists(String Path, final String FileType) {

        /*if (FileType.equalsIgnoreCase("Documents")) {
            getNewDocList();
        } else if (FileType.equalsIgnoreCase("Video")) {
            // new getTypedFile().execute(Path, FileType);
            getVideosList();
        } else if (FileType.equalsIgnoreCase("Apk")) {
            new getApkFile().execute(Path, FileType);
        } else if (FileType.equalsIgnoreCase("Audio")) {
            getMusicList();
        } else if (FileType.equalsIgnoreCase("Gallery")) {
            //getImagesList();

            LoadFiles loadFiles = new LoadFiles(mContext);
            loadFiles.setImageParams(mParamFRAGMENT_TYPE_PARAM, recyclerView, noMediaLayout, imagesListAdapter);
            loadFiles.getFileList("Gallery");
            loadFiles.setLoadCompletedListener(new LoadFiles.LoadCompletedListener() {
                @Override
                public void onLoadCompleted(ArrayList<MediaFileListModel> imageListModelsArray) {
                    imagesListAdapter = new ImagesListAdapter(imageListModelsArray, mParamFileType);
                    recyclerView.setAdapter(imagesListAdapter);
                    imagesListAdapter.notifyDataSetChanged();
                }
            });
        }*/

        loadFiles = new LoadFiles(mContext);
        loadFiles.setImageParams(mParamFRAGMENT_TYPE_PARAM, recyclerView, noMediaLayout, imagesListAdapter);

        loadFiles.setObserver(new LoadFiles.ScanObserver() {
            @Override
            public void fileFound(MediaFileListModel mediaFileListModel) {
                imageListModelsArray.add(mediaFileListModel);
                imagesListAdapter.notifyDataSetChanged();
            }

            @Override
            public void searchFinished() {
                // imagesListAdapter = new ImagesListAdapter(imageListModelsArray, mParamFileType);
                // recyclerView.setAdapter(imagesListAdapter);
                // imagesListAdapter.notifyDataSetChanged();
            }
        });

        loadFiles.getFileList(FileType);
        loadFiles.setLoadCompletedListener(new LoadFiles.LoadCompletedListener() {
            @Override
            public void onLoadCompleted(ArrayList<MediaFileListModel> imageListModels) {

                imageListModelsArray.clear();
                imageListModelsArray.addAll(imageListModels);
                imagesListAdapter.notifyDataSetChanged();

                if (imageListModelsArray != null) {
                    imagesListAdapter.updateFilterData(imageListModels);
                }

                if (FileType.equalsIgnoreCase("Video") || FileType.equalsIgnoreCase("Audio")) {
                    if (mThumbnail == null)
                        mThumbnail = new AudioVideoThumbnailCreator(52, 52);

                    if (FileType.equalsIgnoreCase("Video"))
                        mThumbnail.setFileType("Video");
                    else
                        mThumbnail.setFileType("Audio");

                    for (int i = 0; i < imageListModels.size(); i++) {
                        //   for (MediaFileListModel mediaFileListModel : imageListModels)
                        MediaFileListModel mediaFileListModel = imageListModels.get(i);
                        if (mediaFileListModel.getFilePath().length() != 0) {
                            Bitmap thumb = mThumbnail.isBitmapCached(mediaFileListModel.getFilePath());

                            if (thumb == null) {

                                final Handler handle = new Handler(new Handler.Callback() {
                                    public boolean handleMessage(Message msg) {
                                        imagesListAdapter.notifyItemChanged((int) msg.obj);
                                        //imagesListAdapter.notifyDataSetChanged();
                                        return true;
                                    }
                                });

                                try {
                                    mThumbnail.createNewThumbnail(imageListModels, handle, mContext);
                                    if (!mThumbnail.isAlive())
                                        mThumbnail.start();
                                } catch (Exception e) {
                                }

                            } else {
                                // holder.imgItemIcon.setImageBitmap(thumb);
                            }

                        } else {
                            // holder.imgItemIcon.setImageResource(R.drawable.image);
                        }
                    }
                } else {
                    // imageListModelsArray = imageListModels;
                    // imagesListAdapter = new ImagesListAdapter(imageListModelsArray, mParamFileType, mContext);
                    // recyclerView.setAdapter(imagesListAdapter);
                    // imagesListAdapter.notifyDataSetChanged();
                }

                loadCompleted = true;
            }
        });
    }

    boolean contains(String[] types, String path) {
        for (String string : types) {
            if (path.endsWith(string)) return true;
        }
        return false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
       /*if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // AppController.getInstance().setButtonBackPressed(null);
    }

    private void getNewDocList() {

        new AsyncTask<Void, Void, ArrayList<MediaFileListModel>>() {
            Cursor mCursor;
            ProgressDialog progressDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                imageListModelsArray.clear();
                progressDialog = ProgressDialog.show(mContext, "",
                        "Loading Documents", true);
                String sortOrder = "LOWER(" + MediaStore.Audio.Media.DATE_MODIFIED + ") DESC"; // unordered
                final String[] projection = {MediaStore.Files.FileColumns.DATA};
                mCursor = AppController.getInstance().getApplicationContext().getContentResolver().query(MediaStore.Files
                                .getContentUri("external"), projection,
                        null,
                        null, sortOrder);
            }

            @Override
            protected ArrayList<MediaFileListModel> doInBackground(Void... voids) {
                String[] types = new String[]{".pdf", ".xml", ".html", ".asm", ".text/x-asm", ".def", ".in", ".rc",
                        ".list", ".pl", ".prop", ".properties", ".rc", ".xls", ".xlsx", ".ppt", ".pptx",
                        ".doc", ".docx", ".msg", ".odt", ".pages", ".rtf", ".txt", ".wpd", ".wps"};
                if (mCursor.getCount() > 0 && mCursor.moveToFirst()) {
                    do {
                        String path = mCursor.getString(mCursor.getColumnIndex
                                (MediaStore.Files.FileColumns.DATA));
                        if (path != null && contains(types, path)) {
                            File listFile = new File(path);
                            MediaFileListModel mediaFileListModel = new MediaFileListModel();

                            mediaFileListModel.setFileName(listFile.getName());
                            mediaFileListModel.setFilePath(listFile.getPath());

                            try {
                                File file = new File(listFile.getPath());
                                long length = file.length();
                                length = length / 1024;
                                if (length >= 1024) {
                                    length = length / 1024;
                                    mediaFileListModel.setFileSize(length + " MB");
                                } else {
                                    mediaFileListModel.setFileSize(length + " KB");
                                }
                                Date lastModDate = new Date(file.lastModified());
                                mediaFileListModel.setFileCreatedTime(lastModDate.toString());
                                mediaFileListModel.setFileCreatedTimeDatel(lastModDate);

                            } catch (Exception e) {
                                mediaFileListModel.setFileSize("unknown");
                            }

                            String ext = listFile.toString();
                            String sub_ext = ext.substring(ext.lastIndexOf(".") + 1);
                            mediaFileListModel.setFileType(sub_ext);

                            imageListModelsArray.add(mediaFileListModel);
                            /*BaseFile strings = RootHelper.generateBaseFile(new File(path), ma.SHOW_HIDDEN);
                            if (strings != null) songs.add(strings);*/


                        }
                    } while (mCursor.moveToNext());
                }

                return imageListModelsArray;
            }

            @Override
            protected void onPostExecute(ArrayList<MediaFileListModel> list) {
                super.onPostExecute(list);

                if (mCursor != null) {
                    if (mCursor.getCount() == 0) {
                        noMediaLayout.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        noMediaLayout.setVisibility(View.GONE);
                    }
                    progressDialog.dismiss();
                } else {
                    progressDialog.dismiss();
                    noMediaLayout.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
                mCursor.close();
            }
        }.execute();
    }

    private void getImagesList() {
        imageListModelsArray.clear();
        @SuppressWarnings("deprecation") Cursor mCursor = null;

        if (mParamFRAGMENT_TYPE_PARAM.equalsIgnoreCase("All")) {
            mCursor = mContext.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // Uri
                    new String[]{MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATA}, // Projection
                    null,
                    null,
                    "LOWER(" + MediaStore.Images.Media.DATE_MODIFIED + ") DESC");

        } else if (mParamFRAGMENT_TYPE_PARAM.equalsIgnoreCase("Camera")) {

            String condition = MediaStore.Images.Media.DATA + " like '%/DCIM/%'";
            mCursor = mContext.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATA},
                    condition,
                    null,
                    "LOWER(" + MediaStore.Images.Media.DATE_MODIFIED + ") DESC");
        } else {
            /*mCursor = mContext.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATA}, null, null,
                    "LOWER(" + MediaStore.Images.Media.TITLE + ") DESC");*/
            String condition = MediaStore.Images.Media.DATA + " not like '%/DCIM/%'";
            mCursor = mContext.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATA},
                    condition,
                    null,
                    "LOWER(" + MediaStore.Images.Media.DATE_MODIFIED + ") DESC");
        }
        if (mCursor != null) {
            if (mCursor.getCount() == 0) {
                noMediaLayout.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                noMediaLayout.setVisibility(View.GONE);
            }
            if (mCursor.moveToFirst()) {
                do {
                    MediaFileListModel mediaFileListModel = new MediaFileListModel();
                    mediaFileListModel.setFileName(mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)));
                    mediaFileListModel.setFilePath(mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)));
                    try {
                        File file = new File(mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)));
                        long length = file.length();
                        length = length / 1024;
                        if (length >= 1024) {
                            length = length / 1024;
                            mediaFileListModel.setFileSize(length + " MB");
                        } else {
                            mediaFileListModel.setFileSize(length + " KB");
                        }
                        Date lastModDate = new Date(file.lastModified());

                        mediaFileListModel.setFileCreatedTime(lastModDate.toString());
                    } catch (Exception e) {
                        mediaFileListModel.setFileSize("unknown");
                    }
                    imageListModelsArray.add(mediaFileListModel);
                } while (mCursor.moveToNext());
            }
            mCursor.close();
        } else {
            noMediaLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    private void getMusicList() {

        new AsyncTask<Void, Void, ArrayList<MediaFileListModel>>() {
            Cursor mCursor;
            ProgressDialog progressDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                imageListModelsArray.clear();
                progressDialog = ProgressDialog.show(mContext, "",
                        "Loading Musics", true);
                mCursor = AppController.getInstance().getApplicationContext().getContentResolver().query(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        new String[]{MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DATA},
                        null,
                        null,
                        "LOWER(" + MediaStore.Audio.Media.TITLE + ") ASC");
            }

            @Override
            protected ArrayList<MediaFileListModel> doInBackground(Void... voids) {
                if (mCursor != null) {
                    if (mCursor.moveToFirst()) {
                        do {
                            MediaFileListModel mediaFileListModel = new MediaFileListModel();

                            mediaFileListModel.setFileName(mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)));
                            mediaFileListModel.setFilePath(mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));

                            try {
                                File file = new File(mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)));
                                long length = file.length();
                                length = length / 1024;
                                if (length >= 1024) {
                                    length = length / 1024;
                                    mediaFileListModel.setFileSize(length + " MB");
                                } else {
                                    mediaFileListModel.setFileSize(length + " KB");
                                }
                                Date lastModDate = new Date(file.lastModified());
                                mediaFileListModel.setFileCreatedTime(lastModDate.toString());
                            } catch (Exception e) {
                                mediaFileListModel.setFileSize("unknown");
                            }

                            try {

                                int posColId = mCursor.getColumnIndex(MediaStore.Audio.Media._ID);
                                long songId = mCursor.getLong(posColId);
                                Uri songUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId);
                                String[] dataColumn = {MediaStore.Audio.Media.DATA};
                                Cursor coverCursor = AppController.getInstance().getApplicationContext().getContentResolver().query(songUri, dataColumn, null, null, null);
                                coverCursor.moveToFirst();
                                int dataIndex = coverCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                                String filePath = coverCursor.getString(dataIndex);
                                coverCursor.close();
                                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                                retriever.setDataSource(filePath);
                                byte[] coverBytes = retriever.getEmbeddedPicture();
                                Bitmap songCover;
                                if (coverBytes != null) //se l'array di byte non è vuoto, crea una bitmap
                                    songCover = BitmapFactory.decodeByteArray(coverBytes, 0, coverBytes.length);
                                else
                                    songCover = BitmapFactory.decodeResource(getResources(), R.drawable.music);
                                mediaFileListModel.setMediaBitmap(songCover);

                            } catch (Exception e) {
                                mediaFileListModel.setMediaBitmap(null);
                                progressDialog.dismiss();
                            }

                            imageListModelsArray.add(mediaFileListModel);
                        } while (mCursor.moveToNext());
                    }

                }
                return imageListModelsArray;
            }

            @Override
            protected void onPostExecute(ArrayList<MediaFileListModel> list) {
                super.onPostExecute(list);

                if (mCursor != null) {
                    if (mCursor.getCount() == 0) {
                        noMediaLayout.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        noMediaLayout.setVisibility(View.GONE);
                    }
                    progressDialog.dismiss();
                } else {
                    progressDialog.dismiss();
                    noMediaLayout.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
                mCursor.close();
            }
        }.execute();

    }

    private void getVideosList() {

        new AsyncTask<Void, Void, ArrayList<MediaFileListModel>>() {
            Cursor mCursor;
            ProgressDialog progressDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                imageListModelsArray.clear();
                progressDialog = ProgressDialog.show(mContext, "",
                        "Loading Videos", true);


                mCursor = AppController.getInstance().getApplicationContext().getContentResolver().query(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        new String[]{MediaStore.Video.Media.DISPLAY_NAME, MediaStore.Video.Media.DATA}, null, null,
                        "LOWER(" + MediaStore.Video.Media.TITLE + ") ASC");
            }

            @Override
            protected ArrayList<MediaFileListModel> doInBackground(Void... voids) {

                if (mCursor != null) {
                    if (mCursor.moveToFirst()) {
                        do {
                            final MediaFileListModel mediaFileListModel = new MediaFileListModel();
                            mediaFileListModel.setFileName(mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)));
                            mediaFileListModel.setFilePath(mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)));
                            final String path = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));

                            try {
                                File file = new File(path);
                                long length = file.length();
                                length = length / 1024;
                                if (length >= 1024) {
                                    length = length / 1024;
                                    mediaFileListModel.setFileSize(length + " MB");
                                } else {
                                    mediaFileListModel.setFileSize(length + " KB");
                                }
                                Date lastModDate = new Date(file.lastModified());
                                mediaFileListModel.setFileCreatedTime(lastModDate.toString());
                                mediaFileListModel.setFileCreatedTimeDatel(lastModDate);

                            } catch (Exception e) {
                                mediaFileListModel.setFileSize("unknown");
                            }

                            String ext = path.toString();
                            String sub_ext = ext.substring(ext.lastIndexOf(".") + 1);
                            mediaFileListModel.setFileType(sub_ext);

                            // mediaFileListModel.setMediaBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.music));

                           /* Bitmap bMap = ThumbnailUtils.createVideoThumbnail(mediaFileListModel.getFilePath(), MediaStore.Video.Thumbnails.MINI_KIND);
                            if (bMap != null)
                                mediaFileListModel.setMediaBitmap(bMap);
                            else
                                mediaFileListModel.setMediaBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.music));*/

                            imageListModelsArray.add(mediaFileListModel);
                        } while (mCursor.moveToNext());
                    }
                }
                return imageListModelsArray;
            }

            @Override
            protected void onPostExecute(ArrayList<MediaFileListModel> list) {
                super.onPostExecute(list);

                if (mCursor != null) {
                    if (mCursor.getCount() == 0) {
                        noMediaLayout.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        noMediaLayout.setVisibility(View.GONE);
                    }
                    progressDialog.dismiss();
                } else {
                    progressDialog.dismiss();
                    noMediaLayout.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
                imagesListAdapter.notifyDataSetChanged();
                mCursor.close();
            }

        }.execute();

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();

        inflater.inflate(R.menu.storage_menu, menu);
        MenuItem menu_new_file = menu.findItem(R.id.menu_new_file);
        menu_new_file.setVisible(false);
        MenuItem menu_new_folder = menu.findItem(R.id.menu_new_folder);
        menu_new_folder.setVisible(false);

        MenuItem searchItem = menu.findItem(R.id.search);
        if (mParamFileType.equalsIgnoreCase("Gallery")) {
            searchItem.setVisible(false);
        }

        MenuItem menu_addblock = menu.findItem(R.id.menu_addblock);
        if ((boolean) PreferencesUtils.getValueFromPreference(mContext, Boolean.class, PreferencesUtils.PREF_IN_APP, false) == true)
            menu_addblock.setVisible(false);
        else
            menu_addblock.setVisible(true);

        SearchView mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                imagesListAdapter.filter(newText);
                return false;
            }
        });
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        try {
           /* MenuItem searchItem = menu.findItem(R.id.search);
            if (mParamFileType.equalsIgnoreCase("Gallery")) {
                searchItem.setVisible(false);
            }*/

            MenuItem change_layout = menu.findItem(R.id.change_layout);
            if (FileUtil.FileOperation == true) {
                change_layout.setVisible(true);
            } else {
                change_layout.setVisible(false);
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void onButtonBackPressed(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {

            if (imagesListAdapter.isMultiSelected()) {
                imagesListAdapter.killMultiSelect(true);

               /* if (imagesListAdapter.mMultiSelectData != null) {
                    if (imagesListAdapter.mMultiSelectData.size() > 0) {
                        if (hidden_layout != null)
                            hidden_layout.setVisibility(LinearLayout.VISIBLE);
                        imagesListAdapter.multi_select_flag = false;
                    } else {
                        if (hidden_layout != null)
                            hidden_layout.setVisibility(LinearLayout.GONE);
                        imagesListAdapter.multi_select_flag = false;
                    }
                }*/
            } else {

                if (loadFiles != null)
                    loadFiles.stopFileLoading();

                fragmentChangeListener.OnFragmentChange(0, MainActivity.FG_TAG_HOME);
            }
        }
    }

    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    class getTypedFile extends AsyncTask<String, Void, ArrayList<MediaFileListModel>> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            imageListModelsArray.clear();
            progressDialog = ProgressDialog.show(mContext, "",
                    "Loading", false);
        }

        public ArrayList<MediaFileListModel> getAllFile(File dir, String fileType) {
            File listFile[] = dir.listFiles();
            if (listFile != null && listFile.length > 0) {
                for (int i = 0; i < listFile.length; i++) {

                    if (listFile[i].isDirectory()) {
                        //  fileList.add(listFile[i]); // Enter Directory
                        getAllFile(listFile[i], fileType);
                    } else {

                        if (fileType.equalsIgnoreCase("Documents")) {

                            if (listFile[i].getName().endsWith(".pdf")
                                    || listFile[i].getName().endsWith(".xls") || listFile[i].getName().endsWith(".xlsx") || listFile[i].getName().endsWith(".xml")
                                    || listFile[i].getName().endsWith(".ppt") || listFile[i].getName().endsWith(".pptx")
                                    || listFile[i].getName().endsWith(".txt") || listFile[i].getName().endsWith(".html")
                                    || listFile[i].getName().endsWith(".doc") || listFile[i].getName().endsWith(".docx")) {

                                AddingFiles(listFile[i], fileType);
                            }
                        } else if (fileType.equalsIgnoreCase("Video")) {
                            if (listFile[i].getName().endsWith(".m4v")
                                    || listFile[i].getName().endsWith(".wmv") || listFile[i].getName().endsWith(".3gp")
                                    || listFile[i].getName().endsWith(".mp4") || listFile[i].getName().endsWith(".avi")) {
                                AddingFiles(listFile[i], fileType);
                            }
                        }
                    }
                }
            }
            return imageListModelsArray;
        }

        public void AddingFiles(File listFile, String fileType) {
            MediaFileListModel mediaFileListModel = new MediaFileListModel();

            mediaFileListModel.setFileName(listFile.getName());
            mediaFileListModel.setFilePath(listFile.getPath());

            try {
                File file = new File(listFile.getPath());
                long length = file.length();
                length = length / 1024;
                if (length >= 1024) {
                    length = length / 1024;
                    mediaFileListModel.setFileSize(length + " MB");
                } else {
                    mediaFileListModel.setFileSize(length + " KB");
                }
                Date lastModDate = new Date(file.lastModified());
                mediaFileListModel.setFileCreatedTime(lastModDate.toString());
                mediaFileListModel.setFileCreatedTimeDatel(lastModDate);

            } catch (Exception e) {
                mediaFileListModel.setFileSize("unknown");
            }

            String ext = listFile.toString();
            String sub_ext = ext.substring(ext.lastIndexOf(".") + 1);
            mediaFileListModel.setFileType(sub_ext);


            if (fileType.equalsIgnoreCase("Documents")) {

            } else if (fileType.equalsIgnoreCase("Video")) {
                try {
                    Bitmap bMap = ThumbnailUtils.createVideoThumbnail(listFile.getPath(), MediaStore.Video.Thumbnails.MINI_KIND);
                    if (bMap != null)
                        mediaFileListModel.setMediaBitmap(bMap);
                    else
                        mediaFileListModel.setMediaBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.music));
                } catch (Exception e) {
                    mediaFileListModel.setMediaBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.music));
                }
            }

            imageListModelsArray.add(mediaFileListModel);
        }

        @Override
        protected ArrayList<MediaFileListModel> doInBackground(String... params) {
            File dir = new File(params[0]);
            String FileType = params[1];
            progressDialog.setMessage("Loading " + FileType);

            getAllFile(dir, FileType);

            Collections.sort(imageListModelsArray, new Comparator<MediaFileListModel>() {
                @Override
                public int compare(MediaFileListModel mediaFileListModel, MediaFileListModel t1) {
                    // For Asseccing sort swipe the mediaFileListModel, and t1 object
                    return t1.getFileCreatedTimeDatel().compareTo(mediaFileListModel.getFileCreatedTimeDatel());
                }
            });

            return imageListModelsArray;
        }

        @Override
        protected void onPostExecute(ArrayList<MediaFileListModel> list) {
            super.onPostExecute(list);

            if (list != null) {
                if (list.size() == 0) {
                    noMediaLayout.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    noMediaLayout.setVisibility(View.GONE);
                }
                progressDialog.dismiss();
            } else {
                progressDialog.dismiss();
                noMediaLayout.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }

        }
    }

    class getApkFile extends AsyncTask<String, Void, ArrayList<MediaFileListModel>> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            imageListModelsArray.clear();
            progressDialog = ProgressDialog.show(mContext, "",
                    "Loading Apk", false);
        }

        @Override
        protected ArrayList<MediaFileListModel> doInBackground(String... params) {

            final PackageManager pm = mContext.getPackageManager();

            List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

            for (ApplicationInfo ai : packages) {
                if ((ai.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                    try {
                        final MediaFileListModel mediaFileListModel = new MediaFileListModel();
                        PackageInfo p = pm.getPackageInfo(ai.packageName, PackageManager.GET_META_DATA);
                        /*String appName = p.applicationInfo.loadLabel(
                                mContext.getPackageManager()).toString();
                        String pname = p.packageName;
                        Drawable icon = p.applicationInfo.loadIcon(mContext
                                .getPackageManager());*/

                        String appName = (String) ai.loadLabel(pm);
                        String pName = ai.packageName;
                        Drawable appIcon = ai.loadIcon(pm);
                        String apkPath = ai.publicSourceDir;


                        Method getPackageSizeInfo = pm.getClass().getMethod(
                                "getPackageSizeInfo", String.class, IPackageStatsObserver.class);
                        getPackageSizeInfo.invoke(pm, pName,
                                new IPackageStatsObserver.Stub() {

                                    @Override
                                    public void onGetStatsCompleted(PackageStats pStats, boolean succeeded)
                                            throws RemoteException {
                                        mediaFileListModel.setFileSize(fUtils.readableFileSize(pStats.codeSize));
                                        // Log.i(TAG, "codeSize: " + pStats.codeSize);
                                    }
                                });

                        mediaFileListModel.setFileName(appName);
                        mediaFileListModel.setFilePath(apkPath);
                        mediaFileListModel.setAppPackageName(pName);
                        mediaFileListModel.setAppVersionCode(String.valueOf(p.versionCode));
                        mediaFileListModel.setAppVersionName(p.versionName);

                        Bitmap icon = ((BitmapDrawable) appIcon).getBitmap();
                        mediaFileListModel.setMediaBitmap(icon);

                        imageListModelsArray.add(mediaFileListModel);
                    } catch (Exception e) {
                        e.getMessage();
                    }
                }
                //  Log.d(TAG, "Installed package :" + packageInfo.packageName);
                //  Log.d(TAG, "Launch Activity :" + pm.getLaunchIntentForPackage(packageInfo.packageName));
            }


            return imageListModelsArray;
        }

        @Override
        protected void onPostExecute(ArrayList<MediaFileListModel> list) {
            super.onPostExecute(list);

            if (list != null) {
                if (list.size() == 0) {
                    noMediaLayout.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    noMediaLayout.setVisibility(View.GONE);
                }
                progressDialog.dismiss();
            } else {
                progressDialog.dismiss();
                noMediaLayout.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }

        }
    }
}
