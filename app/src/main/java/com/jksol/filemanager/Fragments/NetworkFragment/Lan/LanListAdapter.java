package com.jksol.filemanager.Fragments.NetworkFragment.Lan;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jksol.filemanager.R;

import java.util.List;

/**
 * Created by inventbird on 17/10/16.
 */
public class LanListAdapter extends RecyclerView.Adapter<LanListAdapter.MyViewHolder> {

    final int THUMB_SIZE = 64;

    private List<Computer> lanList;

    public LanListAdapter(List<Computer> lanList) {
        this.lanList = lanList;

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.lan_computer_list_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        Computer lanListModel = lanList.get(position);

        holder.pc_name.setText(lanListModel.name);
        holder.pc_address.setText(lanListModel.addr);
    }

    @Override
    public int getItemCount() {
        return lanList.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        private final TextView pc_name;
        private final TextView pc_address;
        public ImageView imgItemIcon;

        public MyViewHolder(View view) {
            super(view);
            pc_name = (TextView) view.findViewById(R.id.pc_name);
            pc_address = (TextView) view.findViewById(R.id.pc_address);
            imgItemIcon = (ImageView) view.findViewById(R.id.icon);
        }
    }

}
