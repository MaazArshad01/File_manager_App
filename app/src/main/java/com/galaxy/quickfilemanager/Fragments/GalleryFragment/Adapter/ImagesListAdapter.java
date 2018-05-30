package com.galaxy.quickfilemanager.Fragments.GalleryFragment.Adapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.galaxy.quickfilemanager.FileOperation.AudioVideoThumbnailCreator;
import com.galaxy.quickfilemanager.FileOperation.FileManager;
import com.galaxy.quickfilemanager.FileOperation.Operations;
import com.galaxy.quickfilemanager.Fragments.BookmarkFragment.BookmarkUtils;
import com.galaxy.quickfilemanager.Fragments.DialogDirectory.CopyPasteDialog;
import com.galaxy.quickfilemanager.Fragments.GalleryFragment.AllFileTypeFragment;
import com.galaxy.quickfilemanager.MainActivity;
import com.galaxy.quickfilemanager.Model.MediaFileListModel;
import com.galaxy.quickfilemanager.Services.CopyService;
import com.galaxy.quickfilemanager.Services.DeleteService;
import com.galaxy.quickfilemanager.Utils.AppController;
import com.galaxy.quickfilemanager.Utils.FileUtil;
import com.galaxy.quickfilemanager.Utils.Futils;
import com.galaxy.quickfilemanager.Utils.HFile;
import com.galaxy.quickfilemanager.Utils.MainActivityHelper;
import com.galaxy.quickfilemanager.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by inventbird on 17/10/16.
 */
public class ImagesListAdapter extends RecyclerView.Adapter<ImagesListAdapter.MyViewHolder> implements View.OnClickListener {

    final int THUMB_SIZE = 64;
    private final String mParamFileType;
    private final Context mContext;
    private final MainActivity mainActivity;
    private final MainActivityHelper mainActivityHelper;
    private final BookmarkUtils bookmarkUtils;
    public boolean multi_select_flag = false;
    public ArrayList<String> mMultiSelectData;
    private ArrayList<MediaFileListModel> filteredUserList;
    private List<MediaFileListModel> mediaFileListModels;
    private AudioVideoThumbnailCreator mThumbnail;
    private ArrayList<Integer> positions;
    private LinearLayout hidden_layout, hidden_copy, hidden_move, hidden_delete, hidden_more, hidden_rename, hidden_share, hidden_detail;
    private PopupWindow pw;
    private boolean delete_after_copy = false;
    private AllFileTypeFragment context;

    public ImagesListAdapter(List<MediaFileListModel> mediaFileListModels, String mParamFileType, Context mContext) {
        this.mediaFileListModels = mediaFileListModels;
        this.mParamFileType = mParamFileType;

        this.filteredUserList = new ArrayList<MediaFileListModel>();
        this.filteredUserList.addAll(mediaFileListModels);
        this.mContext = mContext;

        mainActivity = ((MainActivity) mContext);
        mainActivityHelper = new MainActivityHelper(mainActivity);
        bookmarkUtils = new BookmarkUtils(mContext);
        //setHasStableIds(true);
    }

    public void updateFilterData(List<MediaFileListModel> mediaFileListModels) {
        this.filteredUserList = new ArrayList<MediaFileListModel>();
        this.filteredUserList.addAll(mediaFileListModels);
    }

    public void fregmentContext(AllFileTypeFragment context) {
        this.context = context;
    }

    public void setUpdateFileOperationViews(LinearLayout hidden_copy, LinearLayout hidden_move, LinearLayout hidden_delete,
                                            LinearLayout hidden_more, LinearLayout hidden_rename, LinearLayout hidden_share,
                                            LinearLayout hidden_detail, PopupWindow pw) {

        this.hidden_copy = hidden_copy;
        this.hidden_move = hidden_move;
        this.hidden_delete = hidden_delete;
        this.hidden_more = hidden_more;

        this.hidden_rename = hidden_rename;
        this.hidden_share = hidden_share;
        this.hidden_detail = hidden_detail;

        this.pw = pw;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            /*case R.id.hidden_copy:
                try {
                    Toast.makeText(mContext, "Copy : " + this.mMultiSelectData.size(), Toast.LENGTH_SHORT).show();
                }catch (Exception e){}
                break;*/

            case R.id.hidden_move:
            case R.id.hidden_copy:
                if (mMultiSelectData == null || mMultiSelectData.isEmpty()) {
                    killMultiSelect(true);
                    break;
                }

                if (view.getId() == R.id.hidden_move)
                    delete_after_copy = true;

                killMultiSelect(false);

                if (hidden_layout != null)
                    hidden_layout.setVisibility(View.GONE);

                try {
                    CopyPasteDialog copyPasteDialog = new CopyPasteDialog();
                    copyPasteDialog.setHandler(this);
                    copyPasteDialog.setCancelable(false);

                    if (copyPasteDialog != null && copyPasteDialog.isAdded()) {
                        copyPasteDialog.dismiss();
                        copyPasteDialog = null;
                    } else {
                        copyPasteDialog.show(mainActivity.getSupportFragmentManager(), "FragmentDialog");
                    }
                } catch (Exception e) {
                }

                break;

            case R.id.hidden_delete:
                delete();
                break;

            case R.id.hidden_rename:
                try {
                    if (mMultiSelectData.size() > 0) {

                        File file = new File(mMultiSelectData.get(0));
                        int mode = mainActivityHelper.checkFolder(file.getParentFile(), mContext);
                        if (mode == 2) {
                            Toast.makeText(mContext, "Please give a permission for file operation", Toast.LENGTH_SHORT).show();
                            // mainActivityHelper.guideDialogForLEXA(file.getPath());
                        } else if (mode == 1 || mode == 0) {
                            RenameDialog();
                        }
                    }
                } catch (Exception e) {
                }
                pw.dismiss();
                break;

            case R.id.hidden_share:

                ArrayList<Uri> files = new ArrayList<Uri>();

                for (String path : mMultiSelectData) {
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
                        killMultiSelect(true);
                    }
                }, 1000);

                pw.dismiss();

                break;

            case R.id.hidden_detail:
                try {
                    if (!FileUtil.FileOperation) {
                        HFile hFile = new HFile();
                        hFile.setMode(HFile.LOCAL_MODE);
                        hFile.setPath(mMultiSelectData.get(0));

                        FileManager.file_detail_dialog(mContext, hFile);
                        // FileManager.file_detail_dialog(mContext, StorageFragmentMain.mMultiSelectData.get(0));
                    } else {
                        Futils futils = new Futils();
                        ArrayList<HFile> hFileArray = new ArrayList<HFile>();

                        for (String path : mMultiSelectData) {
                            HFile hFile = new HFile();
                            hFile.setMode(HFile.LOCAL_MODE);
                            hFile.setPath(path);

                            hFileArray.add(hFile);
                        }

                        futils.FilePropertyDialog(mContext, hFileArray);
                    }
                } catch (Exception e) {
                }
                pw.dismiss();
                break;
        }
    }

    /**
     * Use this method to determine if the user has selected multiple files/folders
     *
     * @return returns true if the user is holding multiple objects (multi-select)
     */
    public boolean hasMultiSelectData() {
        return (mMultiSelectData != null && mMultiSelectData.size() > 0);
    }

    public void setHidden_layout(LinearLayout hidden_layout) {
        this.hidden_layout = hidden_layout;
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
        return multi_select_flag;
    }

    /**
     * @param newLocation
     */
    public void copyFileMultiSelect(String newLocation) {

        if (mMultiSelectData.size() > 0) {

            File file = new File(newLocation);
            int mode = mainActivityHelper.checkFolder(file, mContext);
            if (mode == 2) {
                Toast.makeText(mContext, "Please give a permission for file operation", Toast.LENGTH_SHORT).show();
                //  mainActivityHelper.guideDialogForLEXA(file.getPath());
            } else if (mode == 1 || mode == 0) {
                Intent intent1 = new Intent(mContext, CopyService.class);
                intent1.putExtra("FILE_PATHS", (mMultiSelectData));
                intent1.putExtra("COPY_DIRECTORY", newLocation);

                String path = mMultiSelectData.get(0);

                intent1.putExtra("MODE", path.startsWith("smb://") ? HFile.SMB_MODE : HFile.LOCAL_MODE);
                intent1.putExtra("move", delete_after_copy);
                mContext.startService(intent1);
            }

            try {
                killMultiSelect(true);
                delete_after_copy = false;
            } catch (Exception e) {
            }
        }
    }

    public void delete() {

        if (mMultiSelectData == null || mMultiSelectData.isEmpty()) {
            killMultiSelect(true);
            return;
        }

        final String[] data = new String[mMultiSelectData.size()];
        int at = 0;

        for (String string : mMultiSelectData)
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

                if (mMultiSelectData.size() > 0) {

                    File file = new File(mMultiSelectData.get(0));
                    int mode = mainActivityHelper.checkFolder(file.getParentFile(), mContext);
                    if (mode == 2) {
                        Toast.makeText(mContext, "Please give a permission for file operation", Toast.LENGTH_SHORT).show();
                    } else if (mode == 1 || mode == 0) {
                        Intent intent1 = new Intent(mContext, DeleteService.class);
                        intent1.putExtra("FILE_PATHS", (mMultiSelectData));
                        intent1.putExtra("MODE", HFile.LOCAL_MODE);
                        intent1.putExtra("ServiceType", "show");
                        mContext.startService(intent1);

                        killMultiSelect(true);
                    }
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                killMultiSelect(true);
                dialog.cancel();
            }
        });

        builder.create().show();

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
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        rename_dialog.getWindow().getAttributes().windowAnimations = R.style.confirmDeleteAnimation;
        rename_dialog.show();

        final EditText edt_rename = (EditText) rename_dialog.findViewById(R.id.edt_rename);

        Button dialog_close = (Button) rename_dialog.findViewById(R.id.dialaog_cancel);
        dialog_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rename_dialog.dismiss();
                killMultiSelect(true);
            }
        });

        TextView txt_dialog_label = (TextView) rename_dialog.findViewById(R.id.txt_dialog_label);
        if (mMultiSelectData.size() == 1) {
            txt_dialog_label.setText(mContext.getResources().getString(R.string.rename));

            String name = mMultiSelectData.get(0).substring(mMultiSelectData.get(0).lastIndexOf("/"), mMultiSelectData.get(0).length());
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
        dialog_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(edt_rename.getText().toString())) {
                    edt_rename.setError("Please enter the name");
                } else {
                    edt_rename.setError(null);
                    renameFileMultiSelect(edt_rename.getText().toString());
                    rename_dialog.dismiss();
                }
            }
        });
    }

    /**
     * @param newName
     */
    public void renameFileMultiSelect(String newName) {
        final String[] data;
        int index = 1;

        if (mMultiSelectData.size() > 0) {

            data = new String[mMultiSelectData.size() + 1];
            data[0] = newName;

            for (String s : mMultiSelectData) {
                data[index++] = s;
            }

            if (mMultiSelectData != null && !mMultiSelectData.isEmpty()) {

                if (mMultiSelectData.size() <= 1) {

                    int mode = HFile.LOCAL_MODE;
                    String ext = "";

                    final HFile hFile0 = new HFile();
                    hFile0.setMode(mode);
                    hFile0.setPath(data[1]);

                    final HFile hFile1 = new HFile();
                    hFile1.setMode(mode);

                    try {
                        if (hFile0.isFile())
                            ext = data[1].substring(data[1].lastIndexOf("."), data[1].length());
                    } catch (Exception e) {
                    }
                    ext = data[1].substring(data[1].lastIndexOf("."), data[1].length());

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

                                        if (mMultiSelectData != null && !mMultiSelectData.isEmpty()) {
                                            multi_select_flag = false;
                                            mMultiSelectData.clear();
                                        }

                                        try {
                                            bookmarkUtils.replaceBookmark(hFile0.getFile(), hFile1.getFile());
                                        } catch (Exception e) {
                                        }

                                        killMultiSelect(true);

                                        context.refreshListData();

                                    } else
                                        Toast.makeText(mContext, R.string.operationunsuccesful, Toast.LENGTH_SHORT).show();
                                }
                            });
                            //Toast.makeText(mContext, R.string.operationunsuccesful, Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {

                    for (int i = 1; i < data.length; i++) {
                        int mode = HFile.LOCAL_MODE;
                        String ext = "";

                        final HFile hFile0 = new HFile();
                        hFile0.setMode(mode);
                        hFile0.setPath(data[i]);

                        final HFile hFile1 = new HFile();
                        hFile1.setMode(mode);
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

                                            if (mMultiSelectData != null && !mMultiSelectData.isEmpty()) {
                                                multi_select_flag = false;
                                                mMultiSelectData.clear();
                                            }

                                            try {
                                                bookmarkUtils.replaceBookmark(hFile0.getFile(), hFile1.getFile());
                                            } catch (Exception e) {
                                            }

                                            killMultiSelect(true);

                                            context.refreshListData();
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

    public void addMultiPosition(int index, String path) {
        if (positions == null)
            positions = new ArrayList<Integer>();

        if (mMultiSelectData == null) {
            positions.add(index);
            add_multiSelect_file(path);

        } else if (mMultiSelectData.contains(path)) {
            if (positions.contains(index))
                positions.remove(new Integer(index));

            mMultiSelectData.remove(path);

        } else {
            positions.add(index);
            add_multiSelect_file(path);
        }

        notifyDataSetChanged();
    }

    private void add_multiSelect_file(String src) {
        if (mMultiSelectData == null)
            mMultiSelectData = new ArrayList<String>();

        mMultiSelectData.add(src);
    }

    public void killMultiSelect(boolean clearData) {
        try {
            multi_select_flag = false;

            if (positions != null && !positions.isEmpty())
                positions.clear();

            if (clearData)
                if (mMultiSelectData != null && !mMultiSelectData.isEmpty())
                    mMultiSelectData.clear();

            if (mMultiSelectData != null) {
                if (mMultiSelectData.size() > 0) {
                    if (hidden_layout != null)
                        hidden_layout.setVisibility(LinearLayout.VISIBLE);
                    multi_select_flag = false;
                } else {
                    if (hidden_layout != null)
                        hidden_layout.setVisibility(LinearLayout.GONE);
                    multi_select_flag = false;
                }
            }

            notifyDataSetChanged();
        } catch (Exception e) {
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = null;

        if (mParamFileType.equalsIgnoreCase("Gallery")) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.images_list_item_view, parent, false);
        } else if (mParamFileType.equalsIgnoreCase("SmallGallery")) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.images_list_item_small_view, parent, false);
        } else if (mParamFileType.equalsIgnoreCase("Audio")) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.audio_video_list_item, parent, false);
        } else if (mParamFileType.equalsIgnoreCase("Video")) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.document_apk_list_item_view, parent, false);
        } else if (mParamFileType.equalsIgnoreCase("Doc")) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.document_apk_list_item_view, parent, false);
        } else if (mParamFileType.equalsIgnoreCase("Apk")) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.document_apk_list_item_view, parent, false);
        }
        return new MyViewHolder(itemView);
    }

    public void disableFileOperationButton() {

        if (!FileUtil.FileOperation)
            if (hidden_detail != null)
                if (mMultiSelectData != null && mMultiSelectData.size() == 1) {
                    hidden_detail.setClickable(true);
                    hidden_detail.setAlpha((float) 1);

                    hidden_rename.setClickable(true);
                    hidden_rename.setAlpha((float) 1);
                } else {
                    hidden_detail.setClickable(false);
                    hidden_detail.setAlpha((float) 0.5);

                    hidden_rename.setClickable(false);
                    hidden_rename.setAlpha((float) 0.5);
                }
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        MediaFileListModel mediaFileListModel = mediaFileListModels.get(position);

        disableFileOperationButton();

        if (mParamFileType.equalsIgnoreCase("Gallery")) {
            if (multi_select_flag)
                holder.multiselect_check.setVisibility(View.VISIBLE);
            else
                holder.multiselect_check.setVisibility(View.GONE);
        } else {
            if (multi_select_flag)
                holder.multiselect_check_img.setVisibility(View.VISIBLE);
            else
                holder.multiselect_check_img.setVisibility(View.GONE);
        }

        if (positions != null && positions.contains(position)) {
            holder.multiselect_check_img.setImageResource(R.drawable.ic_checked_round);
        } else {
            holder.multiselect_check_img.setImageResource(R.drawable.ic_uncheck_round);
        }

        if (mParamFileType.equalsIgnoreCase("Gallery") || mParamFileType.equalsIgnoreCase("SmallGallery")) {
            holder.lblFileName.setText(mediaFileListModel.getFileName());
            holder.lblFileSize.setText(mediaFileListModel.getFileSize());
            holder.lblFileCreated.setText(mediaFileListModel.getFileCreatedTime());
            File imgFile = new File(mediaFileListModel.getFilePath());

            Glide
                    .with(AppController.getInstance().getApplicationContext())
                    .load(imgFile)
                    .centerCrop()
                    .error(R.drawable.image)
                    .placeholder(R.drawable.image)
                    .crossFade()
                    .into(holder.imgItemIcon);

        } else if (mParamFileType.equalsIgnoreCase("Audio")) {

            holder.lblFileName.setText(mediaFileListModel.getFileName());
            holder.lblFileSize.setText(mediaFileListModel.getFileSize());
            holder.lblFileCreated.setText(mediaFileListModel.getFileCreatedTime().substring(0, 19));
            // holder.imgItemIcon.setImageResource(R.drawable.music);
            holder.imgItemIcon.setImageBitmap(mediaFileListModel.getMediaBitmap());


            if (mThumbnail == null)
                mThumbnail = new AudioVideoThumbnailCreator(52, 52);

            mThumbnail.setFileType("Audio");

            if (mediaFileListModel.getFilePath().length() != 0) {
                Bitmap thumb = mThumbnail.isBitmapCached(mediaFileListModel.getFilePath());

                if (thumb == null) {

                } else {
                    holder.imgItemIcon.setImageBitmap(thumb);
                }

            } else {
                holder.imgItemIcon.setImageResource(R.drawable.music);
            }


        } else if (mParamFileType.equalsIgnoreCase("Video")) {

            holder.lblFileName.setText(mediaFileListModel.getFileName());
            holder.imgItemIcon.setImageBitmap(mediaFileListModel.getMediaBitmap());
            File imgFile = new File(mediaFileListModel.getFilePath());

           /* Glide
                    .with(mContext)
                    .load(imgFile)
                    .centerCrop()
                    .error(R.drawable.movies)
                    .placeholder(R.drawable.movies)
                    .crossFade()
                    .into(holder.imgItemIcon);*/

            if (mThumbnail == null)
                mThumbnail = new AudioVideoThumbnailCreator(52, 52);

            mThumbnail.setFileType("Video");

            if (mediaFileListModel.getFilePath().length() != 0) {
                Bitmap thumb = mThumbnail.isBitmapCached(mediaFileListModel.getFilePath());

                if (thumb == null) {

                } else {
                    holder.imgItemIcon.setImageBitmap(thumb);
                }

            } else {
                holder.imgItemIcon.setImageResource(R.drawable.image);
            }


            // holder.imgItemIcon.setImageResource(R.drawable.movies);

        } else if (mParamFileType.equalsIgnoreCase("Doc")) {

            holder.lblFileName.setText(mediaFileListModel.getFileName());
            holder.lblFileSize.setText(mediaFileListModel.getFileSize());
            holder.lblFileCreated.setText(mediaFileListModel.getFileCreatedTime().substring(0, 19));

            if (mediaFileListModel.getFileType().equalsIgnoreCase("pdf")) {
                holder.imgItemIcon.setImageResource(R.drawable.pdf);
            } else if (mediaFileListModel.getFileType().equalsIgnoreCase("xml")) {
                holder.imgItemIcon.setImageResource(R.drawable.xml32);
            } else if (mediaFileListModel.getFileType().equalsIgnoreCase("txt") || mediaFileListModel.getFileType().equalsIgnoreCase("log") || mediaFileListModel.getFileType().equalsIgnoreCase("properties")) {
                holder.imgItemIcon.setImageResource(R.drawable.text);
            } else if (mediaFileListModel.getFileType().equalsIgnoreCase("doc") || mediaFileListModel.getFileType().equalsIgnoreCase("docx")) {
                holder.imgItemIcon.setImageResource(R.drawable.word);
            } else if (mediaFileListModel.getFileType().equalsIgnoreCase("ppt") || mediaFileListModel.getFileType().equalsIgnoreCase("pptx")) {
                holder.imgItemIcon.setImageResource(R.drawable.ppt);
            } else if (mediaFileListModel.getFileType().equalsIgnoreCase("xls") || mediaFileListModel.getFileType().equalsIgnoreCase("xlsx")) {
                holder.imgItemIcon.setImageResource(R.drawable.excel);
            } else if (mediaFileListModel.getFileType().equalsIgnoreCase("html")) {
                holder.imgItemIcon.setImageResource(R.drawable.html32);
            } else {
                holder.imgItemIcon.setImageResource(R.drawable.text);
            }
        } else if (mParamFileType.equalsIgnoreCase("Apk")) {

            holder.lblFileName.setText(mediaFileListModel.getFileName());
            holder.imgItemIcon.setImageBitmap(mediaFileListModel.getMediaBitmap());
            //holder.imgItemIcon.setImageResource(R.drawable.movies);

        }
    }

    @Override
    public int getItemCount() {
        return mediaFileListModels.size();
    }

    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        mediaFileListModels.clear();
        if (charText.length() == 0) {
            mediaFileListModels.addAll(filteredUserList);
        } else {
            for (MediaFileListModel locitem : filteredUserList) {
                if (locitem.getFileName().toLowerCase(Locale.getDefault()).contains(charText)) {
                    mediaFileListModels.add(locitem);
                }
            }
        }
        notifyDataSetChanged();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private final ImageView multiselect_check_img;
        private final RelativeLayout multiselect_check;
        public TextView lblFileName, lblFileSize, lblFileCreated;
        public ImageView imgItemIcon;

        public MyViewHolder(View view) {
            super(view);
            lblFileName = (TextView) view.findViewById(R.id.file_name);
            lblFileCreated = (TextView) view.findViewById(R.id.file_created);
            imgItemIcon = (ImageView) view.findViewById(R.id.icon);
            lblFileSize = (TextView) view.findViewById(R.id.file_size);
            multiselect_check = (RelativeLayout) view.findViewById(R.id.multiselect_check);
            multiselect_check_img = (ImageView) view.findViewById(R.id.multiselect_check_img);
        }
    }

}
