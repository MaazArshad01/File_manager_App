package com.jksol.filemanager.Fragments.StorageFragment;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jksol.filemanager.Adapters.StorageTabAdapter;
import com.jksol.filemanager.R;
import com.jksol.filemanager.Utils.AppController;


public class StorageFragmentMain extends Fragment {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device, null);
        setupTab(view);

        try {
            if (getArguments() != null) {
                String selected_fragment = getArguments().getString("select_tab");

                if (selected_fragment.equals("Internal Storage")) {
                    tabLayout.getTabAt(0).select();
                } else {
                    tabLayout.getTabAt(1).select();
                }
            }
        }catch (Exception e){  }
        return view;
    }

    public void setupTab(View view) {
        //Initializing the tablayout
        tabLayout = (TabLayout) view.findViewById(R.id.storageTabLayout);
        //Adding the tabs using addTab() method
        tabLayout.addTab(tabLayout.newTab().setText("Internal Storage"));
        tabLayout.addTab(tabLayout.newTab().setText("SD Card"));

        viewPager = (ViewPager) view.findViewById(R.id.pager);
        StorageTabAdapter adapter = new StorageTabAdapter(getActivity().getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);

        viewPager.setOffscreenPageLimit(tabLayout.getTabCount());
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }
}
