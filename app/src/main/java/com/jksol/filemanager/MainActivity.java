package com.jksol.filemanager;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.jksol.filemanager.Adapters.NavigationItemAdapter;
import com.jksol.filemanager.Fragments.GalleryFragment.AllFileTypeFragment;
import com.jksol.filemanager.Fragments.GalleryFragment.ImageMain;
import com.jksol.filemanager.Fragments.HomeFragment;
import com.jksol.filemanager.Fragments.NetworkFragment.LanComputers;
import com.jksol.filemanager.Fragments.NetworkFragment.NetworkMain;
import com.jksol.filemanager.Fragments.ProcessViewer;
import com.jksol.filemanager.Fragments.SettingFragment;
import com.jksol.filemanager.Fragments.StorageFragment.InternalStorageTabFragment;
import com.jksol.filemanager.Fragments.StorageFragment.StorageFragmentMain;
import com.jksol.filemanager.Interfaces.FragmentChange;
import com.jksol.filemanager.Interfaces.RecyclerViewContextmenuClick;

public class MainActivity extends AppCompatActivity implements FragmentChange, LanComputers.SmbConnectionListener {

    public static final String FG_TAG_DEVICE = "device_fragment";
    public static final String FG_TAG_SDCARD = "sdcard_fragment";
    public static final String FG_TAG_DOWNLOAD = "download_fragment";
    public static final String FG_TAG_GALLARY = "gallary_fragment";
    public static final String FG_TAG_AUDIO = "audio_fragment";
    public static final String FG_TAG_VIDEO = "video_fragment";
    public static final String FG_TAG_DOC = "doc_fragment";
    public static final String FG_TAG_APK = "apk_fragment";
    public static final String FG_TAG_LAN_CONNECTION = "lanconnetion_fragment";
    public static final String FG_TAG_HOME = "fragment_home";
    public static final String FG_TAG_NETWORK = "network_fragment";
    private static final String FG_TAG_SETTING = "setting_fragment";
    private static final String FG_TAG_HELP = "help_fragment";
    private static final String FG_TAG_PROCESSVIEWER = "processviewer";

    public static int navCurrentIndex = 0;

    public static ButtonBackPressListener buttonBackPressListener;
    public static RecyclerViewContextmenuClick recyclerViewContextmenuClickListener;
    String[] FG_TAG_ARRAY = {FG_TAG_HOME, FG_TAG_DEVICE, FG_TAG_SDCARD, FG_TAG_DOWNLOAD, FG_TAG_NETWORK,
            FG_TAG_GALLARY, FG_TAG_AUDIO, FG_TAG_VIDEO, FG_TAG_DOC, FG_TAG_APK, FG_TAG_SETTING, FG_TAG_HELP};
    boolean openprocesses = false;
    private DrawerLayout mDrawerLayout;
    private Toolbar toolbar;
    // private NavigationView navigationView;
    private TextView lblFreeStorage;
    private RecyclerView nav_recyclerView;
    private NavigationItemAdapter navigationItemAdapter;
    private String[] navTitles;
    private TypedArray navIcons;
    private int folder_count, file_count;
    private String SMBPath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupDrawer();

        openprocesses = getIntent().getBooleanExtra("openprocesses", false);
        if (openprocesses) {
            navCurrentIndex = 11;
            loadFragments(11, FG_TAG_PROCESSVIEWER);
        } else {
            // Set Default Home Fragment
            navCurrentIndex = 0;
            loadFragments(0, FG_TAG_HOME);
        }
    }

    private void setupDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };

        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        //Setup Titles and Icons of Navigation Drawer
        navTitles = getResources().getStringArray(R.array.navDrawerItems);
        navIcons = getResources().obtainTypedArray(R.array.navDrawerIcons);

        nav_recyclerView = (RecyclerView) findViewById(R.id.nav_recyclerView);
        navigationItemAdapter = new NavigationItemAdapter(navTitles, navIcons, this);
        nav_recyclerView.setAdapter(navigationItemAdapter);

        nav_recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final GestureDetector mGestureDetector = new GestureDetector(MainActivity.this, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }

        });

        nav_recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
                View child = nav_recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());

                if (child != null && mGestureDetector.onTouchEvent(motionEvent)) {
                    mDrawerLayout.closeDrawers();

                    //int a = recyclerView.getChildPosition(child);
                    int a = recyclerView.getChildAdapterPosition(child);
                    loadFragments((a - 1), FG_TAG_ARRAY[(a - 1)]);

                    return true;

                }

                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });

        /*navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerLayout = navigationView.getHeaderView(0);
        lblFreeStorage = (TextView) headerLayout.findViewById(R.id.id_free_space);
        navigationView.setNavigationItemSelectedListener(this);*/

    }

    private void loadFragments(int navIndex, String TAG) {
        //  setActivityTitle();
        invalidateOptionsMenu();
        Fragment fragment = getHomeFragment(navIndex);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        /*fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                android.R.anim.fade_out);*/
        fragmentTransaction.replace(R.id.frame, fragment, TAG);
        fragmentTransaction.commitAllowingStateLoss();
    }

    private Fragment getHomeFragment(int navItemIndex) {
        switch (navItemIndex) {
            case 0: // Home Fragment
                navCurrentIndex = 0;
                HomeFragment homeFragment = new HomeFragment();
                homeFragment.setFragmentChangeListner(this);
                return homeFragment;

            case 1: // Internal External Tab Fragment
                navCurrentIndex = 1;
                Bundle bundle = new Bundle();
                bundle.putString("select_tab", "Internal Storage");
                StorageFragmentMain DV = new StorageFragmentMain();
                DV.setArguments(bundle);
                return DV;

            case 2: // External Storage Fragment
                navCurrentIndex = 2;
                Bundle bundle1 = new Bundle();
                bundle1.putString("select_tab", "Sdcard Storage");
                StorageFragmentMain DV1 = new StorageFragmentMain();
                DV1.setArguments(bundle1);
                return DV1;

            case 3: // Download Fragment
                navCurrentIndex = 3;
                Bundle bundle2 = new Bundle();
                bundle2.putString("Path", "Download");
                InternalStorageTabFragment internal = new InternalStorageTabFragment();
                internal.setArguments(bundle2);
                return internal;

            case 4: // Network
                navCurrentIndex = 4;
                /*NetworkMain networkFragment = new NetworkMain();
                return networkFragment;*/
                LanComputers networkFragment = new LanComputers();
                return networkFragment;

            case 5: // Gallery
                navCurrentIndex = 5;
                return new ImageMain();

            case 6: // Audio
                navCurrentIndex = 6;
                return new AllFileTypeFragment().newInstance("Audio", "");

            case 7: // Video
                navCurrentIndex = 7;
                return new AllFileTypeFragment().newInstance("Video", "");

            case 8: // Doc
                navCurrentIndex = 8;
                return new AllFileTypeFragment().newInstance("Doc", "");

            case 9: // Apk
                navCurrentIndex = 9;
                return new AllFileTypeFragment().newInstance("Apk", "");

            case 10: // Setting
                navCurrentIndex = 10;
                return new SettingFragment();

            case 11: // Help
                navCurrentIndex = 11;
                return new ProcessViewer();

            case 12:
                navCurrentIndex = 3;
                Bundle bundle3 = new Bundle();
                bundle3.putString("Path", "LanConnection");
                bundle3.putString("FullPath", SMBPath);
                InternalStorageTabFragment internal1 = new InternalStorageTabFragment();
                internal1.setArguments(bundle3);
                return internal1;

            default:
                navCurrentIndex = 0;
                HomeFragment homeFragment1 = new HomeFragment();
                homeFragment1.setFragmentChangeListner(this);
                return homeFragment1;
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        try {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
            } else {
                if (navCurrentIndex > 3) {
                    navCurrentIndex = 0;
                    loadFragments(navCurrentIndex, FG_TAG_HOME);
                } else {
                    if (buttonBackPressListener != null)
                        buttonBackPressListener.onButtonBackPressed(keyCode, event);
                }
            }
        } catch (Exception e) {
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void OnFragmentChange(int Index, String Tag) {
        loadFragments(Index, Tag);
    }

    @Override
    public void addConnection(boolean edit, String name1, String path, String oldname, String oldPath) {
        try {

            this.SMBPath = path;
            loadFragments(12, FG_TAG_LAN_CONNECTION);
           /* FileUtil futils = new FileUtil();
            folder_count = 0;
            file_count = 0;
            SmbFile[] mFile = futils.getSmbFile(path, 5000).listFiles();
            for (int i = 0; i < mFile.length; i++) {

                String name = mFile[i].getName();
                name = (mFile[i].isDirectory() && name.endsWith("/")) ? name.substring(0, name.length() - 1) : name;
                if (path.equals(path)) {
                    if (name.endsWith("$")) continue;
                }
                if (mFile[i].isDirectory()) {
                    folder_count++;
                    Log.d("Folder", name + " Path :- " + mFile[i].getPath() + " Time :-  " + mFile[i].lastModified());

                    *//*Layoutelements layoutelements = new Layoutelements(folder, name, mFile[i].getPath(), "", "", "", 0, false, mFile[i].lastModified() + "", true);
                    layoutelements.setMode(1);
                    searchHelper.add(layoutelements.generateBaseFile());
                    a.add(layoutelements);*//*
                } else {
                    file_count++;
                    try {
                        Log.d("File", name + " Path" + mFile[i].getPath() + " Time :-  " + mFile[i].lastModified());
                        *//*Layoutelements layoutelements = new Layoutelements(Icons.loadMimeIcon(getActivity(), mFile[i].getPath(), !IS_LIST, res), name, mFile[i].getPath(), "", "", utils.readableFileSize(mFile[i].length()), mFile[i].length(), false, mFile[i].lastModified() + "", false);
                        layoutelements.setMode(1);
                        searchHelper.add(layoutelements.generateBaseFile());
                        a.add(layoutelements);*//*
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }*/

        } catch (Exception e) {

        }
    }

    @Override
    public void deleteConnection(String name, String path) {

    }

    /*
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent objEvent) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                finish();
                return true;
            }
        return false;
    }*/

    public interface ButtonBackPressListener {
        void onButtonBackPressed(int keyCode, KeyEvent event);
    }

}
