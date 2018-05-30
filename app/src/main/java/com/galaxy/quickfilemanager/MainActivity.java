package com.galaxy.quickfilemanager;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.galaxy.quickfilemanager.Adapters.NavigationItemAdapter;
import com.galaxy.quickfilemanager.Advertize.LoadAds;
import com.galaxy.quickfilemanager.Fragments.BookmarkFragment.BookmarkMainFragment;
import com.galaxy.quickfilemanager.Fragments.GalleryFragment.AllFileTypeFragment;
import com.galaxy.quickfilemanager.Fragments.GalleryFragment.ImageMain;
import com.galaxy.quickfilemanager.Fragments.HomeFragment;
import com.galaxy.quickfilemanager.Fragments.NetworkFragment.LanComputers;
import com.galaxy.quickfilemanager.Fragments.ProcessViewer;
import com.galaxy.quickfilemanager.Fragments.SettingFragment;
import com.galaxy.quickfilemanager.Fragments.StorageFragment.InternalStorageTabFragment;
import com.galaxy.quickfilemanager.Fragments.StorageFragment.StorageFragmentMain;
import com.galaxy.quickfilemanager.InnerAppPurchase.BillingProcessor;
import com.galaxy.quickfilemanager.InnerAppPurchase.TransactionDetails;
import com.galaxy.quickfilemanager.Interfaces.FragmentChange;
import com.galaxy.quickfilemanager.Interfaces.RecyclerViewContextmenuClick;
import com.galaxy.quickfilemanager.Utils.FileUtil;
import com.galaxy.quickfilemanager.Utils.MainActivityHelper;
import com.galaxy.quickfilemanager.Utils.Permission;
import com.galaxy.quickfilemanager.Utils.PreferencesUtils;
import com.galaxy.quickfilemanager.Utils.StorageAnalyzer;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements FragmentChange, LanComputers.SmbConnectionListener, BookmarkMainFragment.BookmarkListener {

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
    public static final String FG_TAG_BOOKMARK = "bookmark_fragment";

    private static final String PRODUCT_ID = "android.test.purchased";
    private static final String LICENSE_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqXw7RRx6vMRGBG70nAM02Gv6XuTToHN5629aqIOPuiYMWo+AZKFQjxgxTty1lLMb7DJxDVWftSqUVbSFWVQBMs11SW5X6IxpZvDym0NxJ0f8C6MvsPk/XZv6FCYQ6ahTBWzyiu+kQPAgoYf7z/Hnq2OfvCP9TsiVoGkT3mlRvgSwZYSMWa3ppFW+Ub0Im/K8I9ulCoGk/bevdd5uURP1n7khmakO7mnPtp2oYd1LUPHh+VJcsvMqp0hB+0rY205078r+yVQcQSpYPxRvCHFmCqa9qwUnZW4zVMofhsO7BxwXzW8oVB1V5jhbYR8i8tijjD4HCc9G2s32i5GjaQxs7wIDAQAB"; // PUT YOUR MERCHANT KEY HERE;

    private static final String FG_TAG_SETTING = "setting_fragment";
    private static final String FG_TAG_HELP = "help_fragment";
    private static final String FG_TAG_PROCESSVIEWER = "processviewer";
    public static int navCurrentIndex = 0;

    public static ButtonBackPressListener buttonBackPressListener;
    public static RecyclerViewContextmenuClick recyclerViewContextmenuClickListener;

    private static ArrayList<? extends Parcelable> COPY_PATH = null;
    private static ArrayList<? extends Parcelable> MOVE_PATH = null;
    private static ArrayList<? extends Parcelable> oparrayList = null;
    public String oppathe = "", oppathe1 = "";
    public int operation;
    String[] FG_TAG_ARRAY = {FG_TAG_HOME, FG_TAG_DEVICE, FG_TAG_SDCARD, FG_TAG_DOWNLOAD, FG_TAG_BOOKMARK, FG_TAG_NETWORK,
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
    private Permission permission;
    private String BookmarkPath = "";
    private MainActivityHelper mainActivityHelper;
    private Bundle savedInstanceState = null;

    private BillingProcessor billingProcessor;
    private boolean readyToPurchase = false;

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        if (billingProcessor != null)
            billingProcessor.release();
        super.onDestroy();
    }

    public void innerAppPurchaseInit() {
        if (!BillingProcessor.isIabServiceAvailable(MainActivity.this)) {
            showToast("In-app billing service is unavailable, please upgrade Android Market/Play to version >= 3.9.16");
        }

        billingProcessor = new BillingProcessor(MainActivity.this, LICENSE_KEY, new BillingProcessor.IBillingHandler() {
            @Override
            public void onProductPurchased(String productId, TransactionDetails details) {
                if ((boolean) PreferencesUtils.getValueFromPreference(MainActivity.this, Boolean.class, PreferencesUtils.PREF_RESTORE, false) == true) {
                    showToast("Purchases Restored.");
                }
                PreferencesUtils.saveToPreference(MainActivity.this, PreferencesUtils.PREF_IN_APP, true);

                PreferencesUtils.saveToPreference(MainActivity.this, PreferencesUtils.PREF_BANNER, "");
                PreferencesUtils.saveToPreference(MainActivity.this, PreferencesUtils.PREF_INTER, "");
                loadFragments(0, FG_TAG_HOME);
            }

            @Override
            public void onBillingError(int errorCode, Throwable error) {
                showToast("Error! Please try again...");
            }

            @Override
            public void onBillingInitialized() {
                readyToPurchase = true;
                if ((boolean) PreferencesUtils.getValueFromPreference(MainActivity.this, Boolean.class, PreferencesUtils.PREF_RESTORE, false) == true) {

                }
            }

            @Override
            public void onPurchaseHistoryRestored() {
                for (String sku : billingProcessor.listOwnedProducts()) {
                    Log.d("data", "Owned Managed Product: " + sku);
                    if (sku != null && !sku.equals("")) {
                        // navigationView.getMenu().findItem(R.id.nav_in_app_purchase).setVisible(true);
                        PreferencesUtils.saveToPreference(MainActivity.this, PreferencesUtils.PREF_RESTORE, true);
                        // navigationView.getMenu().findItem(R.id.nav_in_app_purchase).setTitle("Restore Purchases");
                        PreferencesUtils.saveToPreference(MainActivity.this, PreferencesUtils.PREF_IN_APP, false);
                    }
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.savedInstanceState = savedInstanceState;

        BookmarkMainFragment.BookmarkListener bookmarkListener = new BookmarkMainFragment.BookmarkListener() {
            @Override
            public void openBookmark(String path) {

            }
        };

        mainActivityHelper = new MainActivityHelper(this);
        permission = new Permission(this);

        setSupportActionBar(toolbar);
        setupDrawer();

        try {
            PreferencesUtils.saveToPreference(MainActivity.this, PreferencesUtils.PREF_IN_APP, true);
         //    innerAppPurchaseInit();
        } catch (Exception e) {
        }

        try {
            if (!permission.checkExternalStorageReadPermissionGranted()) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle("Permission");
                alertDialog.setMessage("For Access Video, Audio and Document please give a permission");
                alertDialog.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (permission.isExternalStorageReadPermissionGranted())
                            loadFragment();
                    }
                });

                alertDialog.setNegativeButton("Denied", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });

                alertDialog.show();
            } else {
                loadFragment();
            }
        } catch (Exception e) { }
    }

    public void setActionBarTitle(String title) {
        toolbar.setTitle(title);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("selectitem", navCurrentIndex);



    }

    private void loadFragment() {


        if (savedInstanceState == null) {

            openprocesses = getIntent().getBooleanExtra("openprocesses", false);
            if (openprocesses) {
                navCurrentIndex = 101;
                loadFragments(101, FG_TAG_PROCESSVIEWER);
            } else {
                // Set Default Home Fragment
                navCurrentIndex = 0;
                loadFragments(0, FG_TAG_HOME);
            }
        } else {

            navCurrentIndex = savedInstanceState.getInt("selectitem", 0);

            if (navCurrentIndex != 101)
                loadFragments(navCurrentIndex, FG_TAG_ARRAY[navCurrentIndex]);
            else {
                loadFragments(navCurrentIndex, FG_TAG_PROCESSVIEWER);
            }
        }
    }

    private void loadFragments(int navIndex, String TAG) {
        // setActivityTitle();
        invalidateOptionsMenu();
        Fragment fragment = getHomeFragment(navIndex);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        fragmentTransaction.replace(R.id.frame, fragment, TAG);
        fragmentTransaction.commitAllowingStateLoss();
    }

    private Fragment getHomeFragment(int navItemIndex) {
        switch (navItemIndex) {
            case 0: // Home Fragment

                ads();
                setActionBarTitle(getString(R.string.app_name));
                navCurrentIndex = 0;
                HomeFragment homeFragment = new HomeFragment();
                homeFragment.setFragmentChangeListner(this);
                return homeFragment;

            case 1: // Internal External Tab Fragment

                ads();
                setActionBarTitle("Storage");
                navCurrentIndex = 1;
                Bundle bundle = new Bundle();
                bundle.putString("select_tab", "Internal Storage");
                StorageFragmentMain DV = new StorageFragmentMain();
                DV.setArguments(bundle);
                return DV;

            case 2: // External Storage Fragment
                ads();
                setActionBarTitle("Storage");
                navCurrentIndex = 2;
                Bundle bundle1 = new Bundle();
                bundle1.putString("select_tab", "Sdcard Storage");
                StorageFragmentMain DV1 = new StorageFragmentMain();
                DV1.setArguments(bundle1);
                return DV1;

            case 3: // Download Fragment
                ads();
                setActionBarTitle("Download");
                navCurrentIndex = 3;
                Bundle bundle2 = new Bundle();
                bundle2.putString("Path", "Download");
                InternalStorageTabFragment internal = new InternalStorageTabFragment();
                internal.setArguments(bundle2);
                return internal;

            case 4: // Bookmark
                ads();
                setActionBarTitle("Bookmark");
                navCurrentIndex = 4;
                BookmarkMainFragment bookmarkMainFragment = new BookmarkMainFragment();
                return bookmarkMainFragment;

            case 5: // Network
                ads();
                setActionBarTitle("Network");
                navCurrentIndex = 5;
                /*NetworkMain networkFragment = new NetworkMain();
                return networkFragment;*/
                LanComputers networkFragment = new LanComputers();
                return networkFragment;

            case 6: // Gallery
                ads();
                setActionBarTitle("Gallery");
                navCurrentIndex = 6;
                return new ImageMain();

            case 7: // Audio
                ads();
                setActionBarTitle("Audio");
                navCurrentIndex = 7;
                return new AllFileTypeFragment().newInstance("Audio", "");

            case 8: // Video
                ads();
                setActionBarTitle("Video");
                navCurrentIndex = 8;
                return new AllFileTypeFragment().newInstance("Video", "");

            case 9: // Doc
                ads();
                setActionBarTitle("Documents");
                navCurrentIndex = 9;
                return new AllFileTypeFragment().newInstance("Doc", "");

            case 10: // Apk
                ads();
                setActionBarTitle("Apk");
                navCurrentIndex = 10;
                return new AllFileTypeFragment().newInstance("Apk", "");

            case 11: // Setting
                setActionBarTitle("Setting");
                navCurrentIndex = 11;
                return new SettingFragment();

            case 12: // Help
                ads();
                if (FileUtil.FileOperation) {
                    setActionBarTitle("Storage Analyzer");
                    navCurrentIndex = 12;
                    return new StorageAnalyzer();
                } else {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    String[] strTo = {"rakholiyakomal@gmail.com"};
                    intent.putExtra(Intent.EXTRA_EMAIL, strTo);
                    intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                    intent.setType("message/rfc822");
                    intent.setPackage("com.google.android.gm");
                    startActivity(intent);
                }

                setActionBarTitle("Home");
                navCurrentIndex = 0;
                HomeFragment homeFragment2 = new HomeFragment();
                homeFragment2.setFragmentChangeListner(this);
                return homeFragment2;

            case 101: // Help
                ads();
                setActionBarTitle("Process");
                navCurrentIndex = 101;
                return new ProcessViewer();

            case 102:
                ads();
                setActionBarTitle("Network");
                navCurrentIndex = 3;
                Bundle bundle3 = new Bundle();
                bundle3.putString("Path", "LanConnection");
                bundle3.putString("FullPath", SMBPath);
                InternalStorageTabFragment internal1 = new InternalStorageTabFragment();
                internal1.setArguments(bundle3);
                return internal1;

            case 103:
                ads();
                setActionBarTitle("Bookmark");
                navCurrentIndex = 3;
                Bundle bundle4 = new Bundle();
                bundle4.putString("Path", "Bookmark");
                bundle4.putString("FullPath", BookmarkPath);
                InternalStorageTabFragment internal2 = new InternalStorageTabFragment();
                internal2.setArguments(bundle4);
                return internal2;


            default:
                setActionBarTitle("Home");
                navCurrentIndex = 0;
                HomeFragment homeFragment1 = new HomeFragment();
                homeFragment1.setFragmentChangeListner(this);
                return homeFragment1;
        }
    }

    public StorageFragmentMain getFragment() {
        Fragment fragment = getDFragment();
        if (fragment == null) return null;
        if (fragment instanceof StorageFragmentMain) {
            StorageFragmentMain tabFragment = (StorageFragmentMain) fragment;
            return tabFragment;
        }

        return null;
    }

    public Fragment getDFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.frame);
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        super.onActivityResult(requestCode, responseCode, intent);

        if (requestCode == 3) {
            String p = (String) PreferencesUtils.getValueFromPreference(MainActivity.this, String.class, "URI", null); //Sp.getString("URI", null);
            Uri oldUri = null;
            if (p != null) oldUri = Uri.parse(p);
            Uri treeUri = null;
            if (responseCode == Activity.RESULT_OK) {
                // Get Uri from Storage Access Framework.
                treeUri = intent.getData();
                // Persist URI - this is required for verification of writability.
                if (treeUri != null)
                    PreferencesUtils.saveToPreference(MainActivity.this, "URI", treeUri.toString());
                // Sp.edit().putString("URI", treeUri.toString()).commit();
            }

            // If not confirmed SAF, or if still not writable, then revert settings.
            if (responseCode != Activity.RESULT_OK) {
               /* DialogUtil.displayError(getActivity(), R.string.message_dialog_cannot_write_to_folder_saf, false,
                        currentFolder);||!FileUtil.isWritableNormalOrSaf(currentFolder)
                */
                if (treeUri != null)
                    PreferencesUtils.saveToPreference(MainActivity.this, "URI", oldUri.toString());
                // Sp.edit().putString("URI", oldUri.toString()).commit();
                return;
            }

            // After confirmation, update stored value of folder.
            // Persist access permissions.
            final int takeFlags = intent.getFlags()
                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            getContentResolver().takePersistableUriPermission(treeUri, takeFlags);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case Permission.REQUEST_CODE_READ_EXTERNAL_STORAGE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadFragment();
                } else {

                }

                break;
        }
    }

  /*  @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (openprocesses = intent.getBooleanExtra("openprocesses", false)) {
            navCurrentIndex = 101;
            loadFragments(101, FG_TAG_PROCESSVIEWER);
        } else {
            // Set Default Home Fragment
            navCurrentIndex = 0;
            loadFragments(0, FG_TAG_HOME);
        }

    }*/

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
                    final int a = recyclerView.getChildAdapterPosition(child);

                    if (!permission.checkExternalStorageReadPermissionGranted()) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                        alertDialog.setTitle("Permission");
                        alertDialog.setMessage("For Access Video, Audio and Document please give a permission");
                        alertDialog.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (permission.isExternalStorageReadPermissionGranted())
                                    loadFragments((a - 1), FG_TAG_ARRAY[(a - 1)]);
                            }
                        });

                        alertDialog.setNegativeButton("Denied", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        });

                        alertDialog.show();
                    } else {
                        try {
                            if (a == 0) {
                                //Toast.makeText(MainActivity.this, "Clicked", Toast.LENGTH_SHORT).show();
                            } else {
                                loadFragments((a - 1), FG_TAG_ARRAY[(a - 1)]);
                            }
                        } catch (Exception e) {
                        }
                    }

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
                if (navCurrentIndex > 3 && navCurrentIndex < 6 && navCurrentIndex > 10) {
                    if (buttonBackPressListener != null)
                        buttonBackPressListener.onButtonBackPressed(keyCode, event);
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
            loadFragments(102, FG_TAG_LAN_CONNECTION);

        } catch (Exception e) {

        }
    }

    @Override
    public void deleteConnection(String name, String path) {

    }

    @Override
    public void openBookmark(String path) {
        this.BookmarkPath = path;
        loadFragments(103, FG_TAG_BOOKMARK);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.storage_menu, menu);

        MenuItem changelayout = menu.findItem(R.id.change_layout);
        changelayout.setVisible(false);

        MenuItem menu_addblock = menu.findItem(R.id.menu_addblock);
        if ((boolean) PreferencesUtils.getValueFromPreference(MainActivity.this, Boolean.class, PreferencesUtils.PREF_IN_APP, false) == true)
            menu_addblock.setVisible(false);
        else
            menu_addblock.setVisible(true);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_addblock:
                if (!readyToPurchase) {
                    showToast("Billing not initialized.");
                    return false;
                } else {
                    billingProcessor.purchase(MainActivity.this, PRODUCT_ID);
                }
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    public interface ButtonBackPressListener {
        void onButtonBackPressed(int keyCode, KeyEvent event);
    }
    public void ads(){

        LoadAds loadAds = new LoadAds(this);
        loadAds.LoadFullScreenAdd();
    }
}
