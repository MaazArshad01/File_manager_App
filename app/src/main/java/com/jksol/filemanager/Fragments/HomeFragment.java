package com.jksol.filemanager.Fragments;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.jksol.filemanager.Fragments.GalleryFragment.Adapter.ImagesListAdapter;
import com.jksol.filemanager.Fragments.GalleryFragment.LoadFiles;
import com.jksol.filemanager.Interfaces.FragmentChange;
import com.jksol.filemanager.MainActivity;
import com.jksol.filemanager.Model.MediaFileListModel;
import com.jksol.filemanager.R;
import com.jksol.filemanager.Utils.AppController;

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
        View view = inflater.inflate(R.layout.fragment_home, null);

        mUseBackKey = true;
        AppController.getInstance().setButtonBackPressed(this);

        setHasOptionsMenu(true);
        mContext = getActivity();
        initView(view);

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.file_layout:
                fragmentChangeListner.OnFragmentChange(1, MainActivity.FG_TAG_DEVICE);
                break;

            case R.id.gallery_layout:
                fragmentChangeListner.OnFragmentChange(5, MainActivity.FG_TAG_GALLARY);
                break;

            case R.id.apps_layout:
                fragmentChangeListner.OnFragmentChange(9, MainActivity.FG_TAG_APK);
                break;

            case R.id.music_layout:
                fragmentChangeListner.OnFragmentChange(6, MainActivity.FG_TAG_AUDIO);
                break;

            case R.id.video_layout:
                fragmentChangeListner.OnFragmentChange(7, MainActivity.FG_TAG_VIDEO);
                break;

            case R.id.doc_layout:
                fragmentChangeListner.OnFragmentChange(8, MainActivity.FG_TAG_DOC);
                break;

            case R.id.download_layout:
                fragmentChangeListner.OnFragmentChange(3, FG_TAG_DOWNLOAD);
                break;

            case R.id.recent_added_pic_layout:
                fragmentChangeListner.OnFragmentChange(5, FG_TAG_GALLARY);
                break;

            case R.id.recent_added_file_layout:
                fragmentChangeListner.OnFragmentChange(8, FG_TAG_DOC);
                break;
        }
    }

    private void initView(View view) {

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

        recent_added_file_layout = (CardView) view.findViewById(R.id.recent_added_file_layout);
        recent_added_file_layout.setOnClickListener(this);

        // ====================== Start Recently Photos  ===========================================
        recent_added_pics_recyclerview = (RecyclerView) view.findViewById(R.id.recent_added_pics_recyclerview);
        recent_added_pics_recyclerview.setFocusable(false);
        noMediaLayout_recent_photos = (LinearLayout) view.findViewById(R.id.noMediaLayout_recent_photos);
        recentImage_loader = (ProgressBar) view.findViewById(R.id.recentImage_loader);
        imageListModelsArray = new ArrayList<MediaFileListModel>();

        imagesListAdapter = new ImagesListAdapter(imageListModelsArray, "SmallGallery");
        recent_added_pics_recyclerview.setHasFixedSize(true);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext, 4) {
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

        GridLayoutManager gridLayoutManager1 = new GridLayoutManager(mContext, 4) {
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

                    if (filesListModelsArray.size() > 12) {
                        tmp = filesListModelsArray.subList(0, 12);
                    } else {
                        tmp = filesListModelsArray;
                    }

                    fileListAdapter = new ImagesListAdapter(tmp, "Doc");
                    recent_added_file_recyclerview.setAdapter(fileListAdapter);
                    fileListAdapter.notifyDataSetChanged();
                }
            }
        });

        // ======================  End Recently Files =============================================

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
                    if (mCursor.getCount() == 0) {
                        noMediaLayout_recent_photos.setVisibility(View.VISIBLE);
                        recent_added_pics_recyclerview.setVisibility(View.GONE);
                    } else {
                        recent_added_pics_recyclerview.setVisibility(View.VISIBLE);
                        noMediaLayout_recent_photos.setVisibility(View.GONE);
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
                            if (imageListModelsArray.size() == 16) {
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
    }

    @Override
    public void onButtonBackPressed(int keycode, KeyEvent event) {
        if (keycode == KeyEvent.KEYCODE_BACK && mUseBackKey) {
            Toast.makeText(mContext, "Press back again to quit.", Toast.LENGTH_SHORT).show();
            mUseBackKey = false;
        } else if (keycode == KeyEvent.KEYCODE_BACK  && !mUseBackKey) {
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
                    fileListAdapter = new ImagesListAdapter(tmp, "Doc");
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
}
