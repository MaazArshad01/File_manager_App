package com.galaxy.quickfilemanager.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.galaxy.quickfilemanager.Fragments.NetworkFragment.LanComputers;

/**
 * Created by Umiya Mataji on 1/6/2017.
 */

public class NetworkTabAdapter extends FragmentStatePagerAdapter {

    //integer to count number of tabs
    int tabCount;

    //Constructor to the class
    public NetworkTabAdapter(FragmentManager fm, int tabCount) {
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
                LanComputers tab1 = new LanComputers();
                return tab1;
            case 1:
                LanComputers tab2 = new LanComputers();
                return tab2;
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