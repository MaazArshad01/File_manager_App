package com.jksol.filemanager.Fragments.GalleryFragment.Adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jksol.filemanager.Model.MediaFileListModel;
import com.jksol.filemanager.R;
import com.jksol.filemanager.Utils.AppController;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by inventbird on 17/10/16.
 */
public class ImagesListAdapter extends RecyclerView.Adapter<ImagesListAdapter.MyViewHolder> {

    final int THUMB_SIZE = 64;
    private final String mParamFileType;
    private final ArrayList<MediaFileListModel> filteredUserList;
    private List<MediaFileListModel> mediaFileListModels;

    public ImagesListAdapter(List<MediaFileListModel> mediaFileListModels, String mParamFileType) {
        this.mediaFileListModels = mediaFileListModels;
        this.mParamFileType = mParamFileType;

        this.mediaFileListModels = mediaFileListModels;
        this.filteredUserList = new ArrayList<MediaFileListModel>();
        this.filteredUserList.addAll(mediaFileListModels);
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

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        MediaFileListModel mediaFileListModel = mediaFileListModels.get(position);
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

        } else if (mParamFileType.equalsIgnoreCase("Video")) {

            holder.lblFileName.setText(mediaFileListModel.getFileName());
            holder.imgItemIcon.setImageBitmap(mediaFileListModel.getMediaBitmap());
            //holder.imgItemIcon.setImageResource(R.drawable.movies);

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
        public TextView lblFileName, lblFileSize, lblFileCreated;
        public ImageView imgItemIcon;

        public MyViewHolder(View view) {
            super(view);
            lblFileName = (TextView) view.findViewById(R.id.file_name);
            lblFileCreated = (TextView) view.findViewById(R.id.file_created);
            imgItemIcon = (ImageView) view.findViewById(R.id.icon);
            lblFileSize = (TextView) view.findViewById(R.id.file_size);
        }
    }

}
