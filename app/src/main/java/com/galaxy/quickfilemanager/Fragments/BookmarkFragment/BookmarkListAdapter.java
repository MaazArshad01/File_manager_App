package com.galaxy.quickfilemanager.Fragments.BookmarkFragment;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.galaxy.quickfilemanager.FileOperation.SingleThumbnailCreator;
import com.galaxy.quickfilemanager.Model.BookmarkModel;
import com.galaxy.quickfilemanager.Utils.Utils;
import com.galaxy.quickfilemanager.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by inventbird on 17/10/16.
 */
public class BookmarkListAdapter extends RecyclerView.Adapter<BookmarkListAdapter.MyViewHolder> {

    public boolean multi_select_flag = false;
    private List<BookmarkModel> lanList;
    private ArrayList<String> allFilePath;
    private boolean thumbnail_flag = true;
    private SingleThumbnailCreator mThumbnail;
    public ArrayList<String> mMultiSelectData;
    private ArrayList<Integer> positions;
    private LinearLayout hidden_layout;

    public BookmarkListAdapter(List<BookmarkModel> lanList) {
        this.lanList = lanList;
        allFilePath = new ArrayList<String>();
        allFilePath.clear();

        for (BookmarkModel file : lanList) {
            allFilePath.add(file.getFilePath());
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
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tablerow, parent, false);

        return new MyViewHolder(itemView);
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

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        BookmarkModel bookmarkListModel = lanList.get(position);

        if (multi_select_flag)
            holder.multiselect_check.setVisibility(View.VISIBLE);
        else
            holder.multiselect_check.setVisibility(View.GONE);

        if (positions != null && positions.contains(position)) {
            holder.multiselect_check.setImageResource(R.drawable.ic_checked);
            // mViewHolder.mSelect.setVisibility(ImageView.VISIBLE);
        } else {
            holder.multiselect_check.setImageResource(R.drawable.ic_uncheck);
            // mViewHolder.mSelect.setVisibility(ImageView.GONE);
        }


        if (mThumbnail == null)
            mThumbnail = new SingleThumbnailCreator(52, 52);

        if (bookmarkListModel.isDirectory()) {
            holder.icon.setImageResource(R.drawable.folder_default);
        } else {
            setFileIcon(bookmarkListModel.getFileName(), allFilePath, position, holder);
        }

        holder.file_name.setText(bookmarkListModel.getFileName());
        holder.creation_datetime.setText(Utils.convertTimeFromUnixTimeStamp(bookmarkListModel.getFileCreatedTimeDate().toString()));

    }

    private void setFileIcon(String filename, ArrayList<String> file, int position, MyViewHolder mViewHolder) {
        String ext = filename;
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

            if (thumbnail_flag && file.get(position).length() != 0) {
                Bitmap thumb = mThumbnail.isBitmapCached(file.get(position));

                if (thumb == null) {

                    final Handler handle = new Handler(new Handler.Callback() {
                        public boolean handleMessage(Message msg) {
                            notifyDataSetChanged();
                            return true;
                        }
                    });

                    try {
                        mThumbnail.createNewThumbnail(filename, file, handle);

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
    }

    @Override
    public int getItemCount() {
        return lanList.size();
    }

    public void setOperationLayout(LinearLayout hidden_layout) {
        this.hidden_layout = hidden_layout;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private final TextView file_name;
        private final TextView creation_datetime;
        private final TextView bottom_view;
        public ImageView multiselect_check;
        public ImageView icon;

        public MyViewHolder(View view) {
            super(view);

            multiselect_check = (ImageView) view.findViewById(R.id.multiselect_check);
            multiselect_check.setVisibility(View.GONE);

            icon = (ImageView) view.findViewById(R.id.row_image);

            file_name = (TextView) view.findViewById(R.id.top_view);
            creation_datetime = (TextView) view.findViewById(R.id.creation_datetime);
            bottom_view = (TextView) view.findViewById(R.id.bottom_view);

            bottom_view.setVisibility(View.GONE);
        }
    }

}
