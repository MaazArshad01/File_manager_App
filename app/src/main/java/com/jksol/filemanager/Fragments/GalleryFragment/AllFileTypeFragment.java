package com.jksol.filemanager.Fragments.GalleryFragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
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
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jksol.filemanager.FileOperation.FileManager;
import com.jksol.filemanager.Fragments.GalleryFragment.Adapter.ImagesListAdapter;
import com.jksol.filemanager.Model.MediaFileListModel;
import com.jksol.filemanager.R;
import com.jksol.filemanager.Utils.AppController;
import com.jksol.filemanager.Utils.FileUtil;
import com.jksol.filemanager.Utils.Futils;
import com.jksol.filemanager.Utils.RecyclerTouchListener;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class AllFileTypeFragment extends Fragment {

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

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser)
            if (mParamFRAGMENT_TYPE_PARAM != null)
                Log.d("Type Visible", mParamFRAGMENT_TYPE_PARAM);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParamFileType = getArguments().getString(ARG_PARAM_FILE_TYPE);
            mParamFRAGMENT_TYPE_PARAM = getArguments().getString(ARG_PARAM_FRAGMENT_TYPE);
        }
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
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_images_list, container, false);
        setHasOptionsMenu(true);
        fUtils = new Futils();
        mContext = getActivity();

        if (getArguments() != null) {

        }

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_images_list);
        noMediaLayout = (LinearLayout) view.findViewById(R.id.noMediaLayout);
        imageListModelsArray = new ArrayList<>();

        imagesListAdapter = new ImagesListAdapter(imageListModelsArray, mParamFileType);
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
            getFileLists("", "Apk");
        }

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(AppController.getInstance().getApplicationContext(), recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                /*AppController.getInstance().setMediaFileListArrayList(imageListModelsArray);
                Intent intent = new Intent(mContext, ImageViewActivity.class);
                intent.putExtra("imagePosition",  position);
                startActivity(intent);*/

                if (mParamFileType.equalsIgnoreCase("Gallery")) {
                    MediaFileListModel img = imageListModelsArray.get(position);
                    Intent picIntent = new Intent();
                    picIntent.setAction(android.content.Intent.ACTION_VIEW);
                    picIntent.setDataAndType(Uri.fromFile(new File(img.getFilePath())), "image/*");
                    startActivity(picIntent);
                } else if (mParamFileType.equalsIgnoreCase("Audio")) {
                    MediaFileListModel img = imageListModelsArray.get(position);
                    Intent picIntent = new Intent();
                    picIntent.setAction(android.content.Intent.ACTION_VIEW);
                    picIntent.setDataAndType(Uri.fromFile(new File(img.getFilePath())), "audio/*");
                    startActivity(picIntent);
                } else if (mParamFileType.equalsIgnoreCase("Video")) {
                    MediaFileListModel img = imageListModelsArray.get(position);
                    Intent picIntent = new Intent();
                    picIntent.setAction(android.content.Intent.ACTION_VIEW);
                    picIntent.setDataAndType(Uri.fromFile(new File(img.getFilePath())), "video/*");
                    startActivity(picIntent);
                } else if (mParamFileType.equalsIgnoreCase("Doc")) {
                    MediaFileListModel docFiles = imageListModelsArray.get(position);
                    openDocFileIntents(docFiles);
                } else if (mParamFileType.equalsIgnoreCase("Apk")) {
                    MediaFileListModel apkFiles = imageListModelsArray.get(position);
                    if (FileUtil.FileOperation)
                        propertyDialog(apkFiles);

                    /*FileManager fileManager = new FileManager();
                    fileManager.copyToDirectory(apkFiles.getFilePath(), "/storage/emulated/0");
                    fileManager.renameTarget("/storage/emulated/0/base.apk", apkFiles.getFileName());*/
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
        return view;
    }

    public void propertyDialog(final MediaFileListModel apkFiles) {

        final Dialog properties_dialog = new Dialog(mContext);
        properties_dialog.setCancelable(false);
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
        newIntent.setDataAndType(Uri.fromFile(new File(docFiles.getFilePath())), mimeType);
        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            mContext.startActivity(newIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(mContext, "No handler for this type of file.", Toast.LENGTH_LONG).show();
        }
    }

    public void changeRecycleViewLayout(String mParamFileType) {
        if (mParamFileType.equalsIgnoreCase("Gallery")) {

            GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext, 3);
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
            GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext, 4);
            recyclerView.setLayoutManager(gridLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());

        } else if (mParamFileType.equalsIgnoreCase("Doc")) {

            GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext, 4);
            recyclerView.setLayoutManager(gridLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
        } else if (mParamFileType.equalsIgnoreCase("Apk")) {

            GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext, 4);
            recyclerView.setLayoutManager(gridLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
        }
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

    private void getFileLists(String Path, String FileType) {

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

        LoadFiles loadFiles = new LoadFiles(mContext);
        loadFiles.setImageParams(mParamFRAGMENT_TYPE_PARAM, recyclerView, noMediaLayout, imagesListAdapter);
        loadFiles.getFileList(FileType);
        loadFiles.setLoadCompletedListener(new LoadFiles.LoadCompletedListener() {
            @Override
            public void onLoadCompleted(ArrayList<MediaFileListModel> imageListModels) {
                imageListModelsArray = imageListModels;
                imagesListAdapter = new ImagesListAdapter(imageListModelsArray, mParamFileType);
                recyclerView.setAdapter(imagesListAdapter);
                imagesListAdapter.notifyDataSetChanged();
            }
        });
    }

    boolean contains(String[] types, String path) {
        for (String string : types) {
            if (path.endsWith(string)) return true;
        }
        return false;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
       /* if (context instanceof OnFragmentInteractionListener) {
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
                                    || listFile[i].getName().endsWith(".wmv") || listFile[i].getName().endsWith(".3gp") || listFile[i].getName().endsWith(".mp4")
                                    || listFile[i].getName().endsWith(".ppt")) {
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
