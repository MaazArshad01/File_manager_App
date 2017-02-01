package com.jksol.filemanager.Fragments.GalleryFragment.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.jksol.filemanager.Fragments.GalleryFragment.AllFileTypeFragment;

/**
 * Created by Umiya Mataji on 1/6/2017.
 */

public class ImagesTabAdapter extends FragmentStatePagerAdapter {

    //integer to count number of tabs
    int tabCount;

    //Constructor to the class
    public ImagesTabAdapter(FragmentManager fm, int tabCount) {
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
                AllFileTypeFragment tab1 = new AllFileTypeFragment().newInstance("Gallery", "All");
                return tab1;
            case 1:
                AllFileTypeFragment tab2 = new AllFileTypeFragment().newInstance("Gallery", "Camera");
                return tab2;
            case 2:
                AllFileTypeFragment tab3 = new AllFileTypeFragment().newInstance("Gallery", "Others");
                return tab3;
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