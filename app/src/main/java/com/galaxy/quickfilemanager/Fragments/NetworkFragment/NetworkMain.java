package com.galaxy.quickfilemanager.Fragments.NetworkFragment;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.galaxy.quickfilemanager.Adapters.NetworkTabAdapter;
import com.galaxy.quickfilemanager.R;


public class NetworkMain extends Fragment {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private Context mContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_networkmain, null);
        setupTab(view);

        mContext = getActivity();
        /*if ((boolean) PreferencesUtils.getValueFromPreference(mContext, Boolean.class, PreferencesUtils.PREF_IN_APP, false) == false) {
            LoadAds loadAds = new LoadAds(this.getActivity());
            loadAds.LoadFullScreenAdd();
        }*/


        return view;
    }

    public void setupTab(View view) {
        // Initializing the tablayout
        tabLayout = (TabLayout) view.findViewById(R.id.networkTabLayout);
        // Adding the tabs using addTab() method
        tabLayout.addTab(tabLayout.newTab().setText("Lan"));
        tabLayout.addTab(tabLayout.newTab().setText("Ftp"));

        viewPager = (ViewPager) view.findViewById(R.id.networkPager);
        NetworkTabAdapter adapter = new NetworkTabAdapter(getActivity().getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);

        viewPager.setOffscreenPageLimit(tabLayout.getTabCount());
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
       /* tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
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
        });*/
    }
}
