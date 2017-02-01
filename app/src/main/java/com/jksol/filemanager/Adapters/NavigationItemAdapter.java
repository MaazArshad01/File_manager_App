package com.jksol.filemanager.Adapters;

/**
 * Created by Umiya Mataji on 1/5/2017.
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jksol.filemanager.FileOperation.FileManager;
import com.jksol.filemanager.R;

public class NavigationItemAdapter extends RecyclerView.Adapter<NavigationItemAdapter.ViewHolder> {

    private final FileManager fileManager;
    String[] titles;
    TypedArray icons;
    Context context;

    public NavigationItemAdapter(String[] titles, TypedArray icons, Context context) {

        this.titles = titles;
        this.icons = icons;
        this.context = context;
        fileManager = new FileManager();
    }

    @Override
    public NavigationItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (viewType == 1) {
            View itemLayout = layoutInflater.inflate(R.layout.nav_item_layout, null);
            return new ViewHolder(itemLayout, viewType, context);
        } else if (viewType == 0) {
            View itemHeader = layoutInflater.inflate(R.layout.nav_header_layout, null);
            return new ViewHolder(itemHeader, viewType, context);
        }


        return null;
    }

    @Override
    public void onBindViewHolder(NavigationItemAdapter.ViewHolder holder, int position) {

        if(position == 0){
           // holder.free_space_txt.setText("Free Space :- " + fileManager.getAvailableInternalMemorySize());
        }

        if (position != 0) {
            holder.navTitle.setText(titles[position - 1]);
            holder.navIcon.setImageResource(icons.getResourceId(position - 1, -1));
        }

    }

    @Override
    public int getItemCount() {
        return titles.length + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return 0;
        else return 1;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView free_space_txt;
        TextView navTitle;
        ImageView navIcon;
        Context context;

        public ViewHolder(View drawerItem, int itemType, Context context) {

            super(drawerItem);
            this.context = context;
          //  drawerItem.setOnClickListener(this);
            if (itemType == 0) {
                free_space_txt = (TextView) itemView.findViewById(R.id.free_space_txt);
            }
            if (itemType == 1) {
                navTitle = (TextView) itemView.findViewById(R.id.tv_NavTitle);
                navIcon = (ImageView) itemView.findViewById(R.id.iv_NavIcon);
            }
        }

        /**
         * This defines onClick for every item with respect to its position.
         */

      /*  @Override
        public void onClick(View v) {

            MainActivity mainActivity = (MainActivity) context;
            mainActivity.drawerLayout.closeDrawers();
            FragmentTransaction fragmentTransaction = mainActivity.getSupportFragmentManager().beginTransaction();

            switch (getPosition()) {
                case 1:
                    Fragment squadFragment = new SquadFragment();
                    fragmentTransaction.replace(R.id.containerView, squadFragment);
                    fragmentTransaction.commit();
                    break;
                case 2:
                    Fragment fixtureFragment = new FixtureFragment();
                    fragmentTransaction.replace(R.id.containerView, fixtureFragment);
                    fragmentTransaction.commit();
                    break;
                case 3:
                    Fragment tableFragment = new TableFragment();
                    fragmentTransaction.replace(R.id.containerView, tableFragment);
                    fragmentTransaction.commit();
                    break;
            }
        }*/
    }


}