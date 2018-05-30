package com.galaxy.quickfilemanager.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.galaxy.quickfilemanager.Advertize.LoadAds;
import com.galaxy.quickfilemanager.Fragments.GalleryFragment.Adapter.ImagesListAdapter;
import com.galaxy.quickfilemanager.Fragments.GalleryFragment.LoadFiles;
import com.galaxy.quickfilemanager.Interfaces.FragmentChange;
import com.galaxy.quickfilemanager.MainActivity;
import com.galaxy.quickfilemanager.Model.MediaFileListModel;
import com.galaxy.quickfilemanager.Utils.AppController;
import com.galaxy.quickfilemanager.Utils.Permission;
import com.galaxy.quickfilemanager.Utils.PreferencesUtils;
import com.galaxy.quickfilemanager.Utils.Utils;
import com.galaxy.quickfilemanager.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


public class HomeFragment extends Fragment implements View.OnClickListener, MainActivity.ButtonBackPressListener {


    private static final String FG_TAG_DEVICE = "device_fragment";
    private static final String FG_TAG_GALLARY = "gallary_fragment";
    private static final String FG_TAG_APK = "apk_fragment";
    private static final String FG_TAG_DOWNLOAD = "download_fragment";
    private static final String FG_TAG_DOC = "doc_fragment";

    // ================== Recent Photos Varible ==================
    RecyclerView recent_added_pics_recyclerview;
    private Context mContext;
    private LinearLayout noMediaLayout_recent_photos;
    private ArrayList<MediaFileListModel> imageListModelsArray;
    private ImagesListAdapter imagesListAdapter;
    private ProgressBar recentImage_loader;
    // =============================================================

    // ================== Recent File varible =======================
    private RecyclerView recent_added_file_recyclerview;
    private LinearLayout noMediaLayout_recent_files;
    private ProgressBar recentfile_loader;
    private ArrayList<MediaFileListModel> filesListModelsArray;
    private ImagesListAdapter fileListAdapter;
    private File root;
    // ===============================================================

    // ================= Layout Fragment Change =======================
    private LinearLayout file_layout, gallery_layout, apps_layout, music_layout, video_layout, doc_layout, download_layout;
    private CardView recent_added_pic_layout, recent_added_file_layout;
    private FragmentChange fragmentChangeListner;
    private boolean mUseBackKey;
    private Permission permission;
    private HomeFragment context;
    private FrameLayout recent_added_pic_framelayout, recent_added_file_framelayout;
    private Utils utils;
    private boolean isTablet = false;
    private FrameLayout slide_arrow;
    private MainActivity mainActivity;

    // ================================================================

    public void setFragmentChangeListner(FragmentChange fragmentChangeListner) {
        this.fragmentChangeListner = fragmentChangeListner;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            AppController.getInstance().setButtonBackPressed(this);
        } else {
            AppController.getInstance().setButtonBackPressed(null);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_home, null);

        setHasOptionsMenu(true);
        mContext = getActivity();
        context = this;

        mUseBackKey = true;
        AppController.getInstance().setButtonBackPressed(this);
        mainActivity = ((MainActivity) getActivity());

        permission = new Permission(getActivity());

        FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.fram);
        frameLayout.setFocusable(false);

        LoadAds loadAds = new LoadAds(context.getActivity());
        loadAds.LoardNativeAd(frameLayout);
        frameLayout.setVisibility(View.VISIBLE);

        utils = new Utils(mContext);
        isTablet = utils.isTablet();

        try {
            if (!permission.checkExternalStorageReadPermissionGranted()) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                alertDialog.setTitle("Permission");
                alertDialog.setMessage("For Access Video, Audio and Document please give a permission");
                alertDialog.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (permission.isExternalStorageReadPermissionGranted_Fragment(context))
                            initView(view);
                    }
                });

                alertDialog.setNegativeButton("Denied", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getActivity().finish();
                    }
                });

                alertDialog.show();
            } else {
                initView(view);
            }
        } catch (Exception e) {

        }

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case Permission.REQUEST_CODE_READ_EXTERNAL_STORAGE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initView(getView());
                } else {

                }

                break;
        }
    }

    private void initView(View view) {

        slide_arrow = (FrameLayout) view.findViewById(R.id.slide_arrow);
        if (isTablet)
            slide_arrow.setVisibility(View.GONE);
        else
            slide_arrow.setVisibility(View.VISIBLE);

        file_layout = (LinearLayout) view.findViewById(R.id.file_layout);
        file_layout.setOnClickListener(this);

        gallery_layout = (LinearLayout) view.findViewById(R.id.gallery_layout);
        gallery_layout.setOnClickListener(this);

        apps_layout = (LinearLayout) view.findViewById(R.id.apps_layout);
        apps_layout.setOnClickListener(this);

        music_layout = (LinearLayout) view.findViewById(R.id.music_layout);
        music_layout.setOnClickListener(this);

        video_layout = (LinearLayout) view.findViewById(R.id.video_layout);
        video_layout.setOnClickListener(this);

        doc_layout = (LinearLayout) view.findViewById(R.id.doc_layout);
        doc_layout.setOnClickListener(this);

        download_layout = (LinearLayout) view.findViewById(R.id.download_layout);
        download_layout.setOnClickListener(this);

        recent_added_pic_layout = (CardView) view.findViewById(R.id.recent_added_pic_layout);
        recent_added_pic_layout.setOnClickListener(this);
        recent_added_pic_framelayout = (FrameLayout) view.findViewById(R.id.recent_added_pic_framelayout);
        recent_added_pic_framelayout.setOnClickListener(this);


        recent_added_file_layout = (CardView) view.findViewById(R.id.recent_added_file_layout);
        recent_added_file_layout.setOnClickListener(this);
        recent_added_file_framelayout = (FrameLayout) view.findViewById(R.id.recent_added_file_framelayout);
        recent_added_file_framelayout.setOnClickListener(this);

        // ====================== Start Recently Photos  ===========================================
        recent_added_pics_recyclerview = (RecyclerView) view.findViewById(R.id.recent_added_pics_recyclerview);
        recent_added_pics_recyclerview.setFocusable(false);
        noMediaLayout_recent_photos = (LinearLayout) view.findViewById(R.id.noMediaLayout_recent_photos);
        recentImage_loader = (ProgressBar) view.findViewById(R.id.recentImage_loader);
        imageListModelsArray = new ArrayList<MediaFileListModel>();

        imagesListAdapter = new ImagesListAdapter(imageListModelsArray, "SmallGallery", mContext);
        recent_added_pics_recyclerview.setHasFixedSize(true);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext, isTablet ? 7 : 4) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        recent_added_pics_recyclerview.setLayoutManager(gridLayoutManager);
        recent_added_pics_recyclerview.setItemAnimator(new DefaultItemAnimator());
        recent_added_pics_recyclerview.setAdapter(imagesListAdapter);
        getImagesList();

        // ======================  End Recently Photos =============================================


        // ====================== Start Recently Files  ===========================================
        recent_added_file_recyclerview = (RecyclerView) view.findViewById(R.id.recent_added_file_recyclerview);
        recent_added_file_recyclerview.setFocusable(false);
        noMediaLayout_recent_files = (LinearLayout) view.findViewById(R.id.noMediaLayout_recent_files);
        recentfile_loader = (ProgressBar) view.findViewById(R.id.recentfile_loader);
        filesListModelsArray = new ArrayList<MediaFileListModel>();
        recent_added_file_recyclerview.setHasFixedSize(true);

        GridLayoutManager gridLayoutManager1 = new GridLayoutManager(mContext, isTablet ? 6 : 4) { //For table 4 rows and for device 3 rows
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        recent_added_file_recyclerview.setLayoutManager(gridLayoutManager1);
        recent_added_file_recyclerview.setItemAnimator(new DefaultItemAnimator());
       /* root = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath());
        new getTypedFile().execute(root.getPath(), "Documents");*/
        loadDocuments();

        // ======================  End Recently Files =============================================

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.file_layout:
                ads();
                fragmentChangeListner.OnFragmentChange(1, MainActivity.FG_TAG_DEVICE);
                break;

            case R.id.gallery_layout:
                ads();
                fragmentChangeListner.OnFragmentChange(6, MainActivity.FG_TAG_GALLARY);
                break;

            case R.id.apps_layout:
                ads();
                fragmentChangeListner.OnFragmentChange(10, MainActivity.FG_TAG_APK);
                break;

            case R.id.music_layout:
                ads();
                fragmentChangeListner.OnFragmentChange(7, MainActivity.FG_TAG_AUDIO);
                break;

            case R.id.video_layout:
                ads();
                fragmentChangeListner.OnFragmentChange(8, MainActivity.FG_TAG_VIDEO);
                break;

            case R.id.doc_layout:
                ads();
                fragmentChangeListner.OnFragmentChange(9, MainActivity.FG_TAG_DOC);
                break;

            case R.id.download_layout:
                ads();
                fragmentChangeListner.OnFragmentChange(3, FG_TAG_DOWNLOAD);
                break;

            case R.id.recent_added_pic_framelayout:
            case R.id.recent_added_pic_layout:
                fragmentChangeListner.OnFragmentChange(6, FG_TAG_GALLARY);
                break;

            case R.id.recent_added_file_framelayout:
            case R.id.recent_added_file_layout:
                fragmentChangeListner.OnFragmentChange(9, FG_TAG_DOC);
                break;
        }
    }

    public void loadDocuments() {

        LoadFiles loadFiles = new LoadFiles(mContext);
        loadFiles.setImageParams("", recent_added_file_recyclerview, noMediaLayout_recent_files, imagesListAdapter);
        loadFiles.setLoaderLayout(recentfile_loader);
        loadFiles.getFileList("Documents");
        loadFiles.setLoadCompletedListener(new LoadFiles.LoadCompletedListener() {
            @Override
            public void onLoadCompleted(ArrayList<MediaFileListModel> list) {
                filesListModelsArray = list;

                if (list.size() > 0) {
                    recent_added_file_recyclerview.setVisibility(View.VISIBLE);
                    noMediaLayout_recent_files.setVisibility(View.GONE);
                    List<MediaFileListModel> tmp;

                    if (filesListModelsArray.size() > (isTablet ? 24 : 12)) {
                        tmp = filesListModelsArray.subList(0, (isTablet ? 24 : 12));
                    } else {
                        tmp = filesListModelsArray;
                    }

                    fileListAdapter = new ImagesListAdapter(tmp, "Doc", mContext);
                    recent_added_file_recyclerview.setAdapter(fileListAdapter);
                    fileListAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void getImagesList() {
        new AsyncTask<Void, Void, ArrayList<MediaFileListModel>>() {

            Cursor mCursor;
            //  ProgressDialog progressDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                recentImage_loader.setVisibility(View.VISIBLE);
                imageListModelsArray.clear();
                mCursor = mContext.getContentResolver().query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // Uri
                        new String[]{MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATA}, // Projection
                        null,
                        null,
                        "LOWER(" + MediaStore.Images.Media.DATE_MODIFIED + ") DESC");
            }

            @Override
            protected ArrayList<MediaFileListModel> doInBackground(Void... voids) {
                if (mCursor != null) {
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
                            if (imageListModelsArray.size() == (isTablet ? 28 : 16)) {
                                break;
                            }
                        } while (mCursor.moveToNext());
                    }
                }

                return imageListModelsArray;
            }

            @Override
            protected void onPostExecute(ArrayList<MediaFileListModel> mediaFileListModels) {
                super.onPostExecute(mediaFileListModels);
                if (mCursor != null) {
                    if (mCursor.getCount() == 0) {
                        recent_added_pics_recyclerview.setVisibility(View.GONE);
                        noMediaLayout_recent_photos.setVisibility(View.VISIBLE);
                    } else {
                        recent_added_pics_recyclerview.setVisibility(View.VISIBLE);
                        noMediaLayout_recent_photos.setVisibility(View.GONE);
                        imagesListAdapter.notifyDataSetChanged();
                    }
                    recentImage_loader.setVisibility(View.GONE);
                } else {
                    recentImage_loader.setVisibility(View.GONE);
                    recent_added_pics_recyclerview.setVisibility(View.GONE);
                    noMediaLayout_recent_photos.setVisibility(View.VISIBLE);

                }
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
        MenuItem changelayout = menu.findItem(R.id.change_layout);
        changelayout.setVisible(false);

        MenuItem searchItem = menu.findItem(R.id.search);
        searchItem.setVisible(false);

        MenuItem menu_addblock = menu.findItem(R.id.menu_addblock);

        if ((boolean) PreferencesUtils.getValueFromPreference(getActivity(), Boolean.class, PreferencesUtils.PREF_IN_APP, false) == true) {
            menu_addblock.setVisible(false);
        }

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);


        MenuItem menu_addblock = menu.findItem(R.id.menu_addblock);

        if ((boolean) PreferencesUtils.getValueFromPreference(getActivity(), Boolean.class, PreferencesUtils.PREF_IN_APP, false) == true) {
            menu_addblock.setVisible(false);
        }
    }

    @Override
    public void onButtonBackPressed(int keycode, KeyEvent event) {
        if (keycode == KeyEvent.KEYCODE_BACK && mUseBackKey) {
            Toast.makeText(mContext, "Press back again to quit.", Toast.LENGTH_SHORT).show();
            mUseBackKey = false;
        } else if (keycode == KeyEvent.KEYCODE_BACK && !mUseBackKey) {
            getActivity().finish();
        }
    }

    class getTypedFile extends AsyncTask<String, Void, ArrayList<MediaFileListModel>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            filesListModelsArray.clear();
            recentfile_loader.setVisibility(View.VISIBLE);
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
                                    || listFile[i].getName().endsWith(".wmv") || listFile[i].getName().endsWith(".3gp") || listFile[i].getName().endsWith(".mp4")
                                    || listFile[i].getName().endsWith(".ppt")) {
                                AddingFiles(listFile[i], fileType);
                            }
                        }
                    }
                }
            }
            return filesListModelsArray;
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

            filesListModelsArray.add(mediaFileListModel);

        }

        @Override
        protected ArrayList<MediaFileListModel> doInBackground(String... params) {
            File dir = new File(params[0]);
            String FileType = params[1];
            getAllFile(dir, FileType);

            Collections.sort(filesListModelsArray, new Comparator<MediaFileListModel>() {
                @Override
                public int compare(MediaFileListModel mediaFileListModel, MediaFileListModel t1) {
                    // For Asseccing sort swipe the mediaFileListModel, and t1 object
                    return t1.getFileCreatedTimeDatel().compareTo(mediaFileListModel.getFileCreatedTimeDatel());
                }
            });

            return filesListModelsArray;
        }

        @Override
        protected void onPostExecute(ArrayList<MediaFileListModel> list) {
            super.onPostExecute(list);

            if (list != null) {
                if (list.size() == 0) {
                    recent_added_file_recyclerview.setVisibility(View.GONE);
                    noMediaLayout_recent_files.setVisibility(View.VISIBLE);
                } else {
                    recent_added_file_recyclerview.setVisibility(View.VISIBLE);
                    noMediaLayout_recent_files.setVisibility(View.GONE);
                    List<MediaFileListModel> tmp;

                    if (filesListModelsArray.size() > 12) {
                        tmp = filesListModelsArray.subList(0, 12);
                    } else {
                        tmp = filesListModelsArray;
                    }
                    fileListAdapter = new ImagesListAdapter(tmp, "Doc", mContext);
                    recent_added_file_recyclerview.setAdapter(fileListAdapter);
                    fileListAdapter.notifyDataSetChanged();
                }

                recentfile_loader.setVisibility(View.GONE);
            } else {
                recentfile_loader.setVisibility(View.GONE);
                recent_added_file_recyclerview.setVisibility(View.GONE);
                noMediaLayout_recent_files.setVisibility(View.VISIBLE);

            }
        }
    }
    public void ads(){

        LoadAds loadAds = new LoadAds(this.getActivity());
        loadAds.LoadFullScreenAdd();
    }
}
