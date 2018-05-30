package com.galaxy.quickfilemanager.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.galaxy.quickfilemanager.Fragments.StorageFragment.InternalStorageTabFragment;
import com.galaxy.quickfilemanager.Fragments.StorageFragment.SDCardTabFragment;

/**
 * Created by Umiya Mataji on 1/6/2017.
 */

public class StorageTabAdapter extends FragmentStatePagerAdapter {

    //integer to count number of tabs
    int tabCount;

    //Constructor to the class
    public StorageTabAdapter(FragmentManager fm, int tabCount) {
        super(fm);
        //Initializing tab count
        this.tabCount= tabCount;
    }

    //Overriding method getItem
    @Override
    public Fragment getItem(int position) {
        //Returning the current tabs
        switch (position) {
            case 0:
                InternalStorageTabFragment tab1 = new InternalStorageTabFragment();
                return tab1;
            case 1:
                SDCardTabFragment tab2 = new SDCardTabFragment();
                return tab2;

            /*case 0:
                Bundle bundle2 = new Bundle();
                bundle2.putString("Path", "InternalStorage");
                InternalStorageTabFragment tab1 = new InternalStorageTabFragment();
                tab1.setArguments(bundle2);
                return tab1;
            case 1:
                *//*SDCardTabFragment tab2 = new SDCardTabFragment();
                return tab2;*//*
                Bundle bundle1 = new Bundle();
                bundle1.putString("Path", "ExternalStorage");
                InternalStorageTabFragment tab2 = new InternalStorageTabFragment();
                tab2.setArguments(bundle1);
                return tab2;*/


            default:
                return null;
        }
    }

    //Overriden method getCount to get the number of tabs
    @Override
    public int getCount() {
        return tabCount;
    }
}