package com.galaxy.quickfilemanager.Fragments.StorageFragment;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.galaxy.quickfilemanager.Adapters.StorageTabAdapter;
import com.galaxy.quickfilemanager.FileOperation.EventHandler;
import com.galaxy.quickfilemanager.FileOperation.FileManager;
import com.galaxy.quickfilemanager.MainActivity;
import com.galaxy.quickfilemanager.Utils.DataPackage;
import com.galaxy.quickfilemanager.Utils.Futils;
import com.galaxy.quickfilemanager.Utils.HFile;
import com.galaxy.quickfilemanager.Utils.Utils;
import com.galaxy.quickfilemanager.ProgressListener;
import com.galaxy.quickfilemanager.R;
import com.galaxy.quickfilemanager.RegisterCallback;

import java.util.ArrayList;


public class StorageFragmentMain extends Fragment {

    public static ArrayList<String> mMultiSelectData;
    public static boolean multi_select_flag = false;
    public static boolean delete_after_copy = false;
    public static LinearLayout hidden_paste_layout;
    public static PasteLayoutListener pasteLayoutListener;
    public static Dialog copy_dialog;
    static ArrayList<Integer> CopyIds = new ArrayList<Integer>();
    static ArrayList<Integer> CancelledCopyIds = new ArrayList<Integer>();
    private static TextView current_progress_item;
    private static TextView total_progress_count;
    private static ProgressBar copy_progressbar;
    private static Futils futils;
    private static MainActivity mainActivity;
    private static Utils utils;
    private static LinearLayout progresslayout;
    private static Context mContext;
    public LinearLayout hidden_paste;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private LinearLayout hidden_cancel;
    private boolean mBound = false;
    private ServiceConnection mCopyConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            RegisterCallback binder = (RegisterCallback.Stub.asInterface(service));
            mBound = true;
            try {
                for (DataPackage dataPackage : binder.getCurrent()) {
                    processCopyResults(dataPackage);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            try {
                binder.registerCallBack(new ProgressListener.Stub() {
                    @Override
                    public void onUpdate(final DataPackage dataPackage) {
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                processCopyResults(dataPackage);
                            }
                        });
                    }

                    @Override
                    public void refresh() {
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                clear();
                            }
                        });
                    }
                });
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    public static void copyDialog() {
        final EventHandler mHandler = null;
        final FileManager mFileMag = null;

        /*if (InternalStorageTabFragment.fragmentVisible) {
            mHandler = InternalStorageTabFragment.mContext.mHandler;
            mFileMag = InternalStorageTabFragment.mContext.mFileMag;
        } else {
            mHandler = SDCardTabFragment.mContext.mHandler;
            mFileMag = SDCardTabFragment.mContext.mFileMag;
        }*/

        copy_dialog = new Dialog(mContext);
        copy_dialog.setCancelable(false);
        copy_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        copy_dialog.setContentView(R.layout.copy_dialog);
        copy_dialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        copy_dialog.getWindow().getAttributes().windowAnimations = R.style.confirmDeleteAnimation;
        copy_dialog.show();

        TextView dialog_cancel = (TextView) copy_dialog.findViewById(R.id.dialog_cancel);
        TextView dialog_hide = (TextView) copy_dialog.findViewById(R.id.dialog_hide);
        dialog_hide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copy_dialog.dismiss();

                mHandler.mDelegate.killMultiSelect(true);
                mHandler.updateDirectory(mFileMag.getNextDir(mFileMag.getCurrentDir(), true));
                StorageFragmentMain.delete_after_copy = false;

            }
        });

        dialog_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copy_dialog.dismiss();

                Toast.makeText(mContext, utils.getString(mainActivity, R.string.stopping), Toast.LENGTH_LONG).show();

                if (CopyIds.size() > 0) {
                    Intent i = new Intent("copycancel");
                    i.putExtra("id", CopyIds.get(0));
                    mContext.sendBroadcast(i);

                    CancelledCopyIds.add(CopyIds.get(0));
                    CopyIds.remove(CopyIds.indexOf(CopyIds.get(0)));

                }

                mHandler.mDelegate.killMultiSelect(true);
                mHandler.updateDirectory(mFileMag.getNextDir(mFileMag.getCurrentDir(), true));
                StorageFragmentMain.delete_after_copy = false;
            }
        });

        TextView copy_item_name = (TextView) copy_dialog.findViewById(R.id.copy_item_name);
        StringBuilder copying_filename = new StringBuilder();

        if (mHandler.getSelectedItems().size() > 0) {

            ArrayList<HFile> hFile = new ArrayList<HFile>();

            for (String filename : mHandler.getSelectedItems()) {

                HFile hFile1 = new HFile();
                hFile1.setMode(mFileMag.isSmb() ? HFile.SMB_MODE : HFile.LOCAL_MODE);
                hFile1.setPath(filename);
                hFile.add(hFile1);
            }

            for (HFile hFile2 : hFile) {
                String filename = hFile2.getName();
                copying_filename.append(filename + ", ");
            }
            copy_item_name.setText(copying_filename.toString());


            TextView total_item = (TextView) copy_dialog.findViewById(R.id.total_item);
            futils.countFileFolder(mContext, hFile, total_item);

            TextView total_size = (TextView) copy_dialog.findViewById(R.id.total_size);
            futils.FileFolderSize(mContext, hFile, total_size);

            TextView from_path = (TextView) copy_dialog.findViewById(R.id.from_path);
            from_path.setText(hFile.get(0).getParent());

            TextView to_path = (TextView) copy_dialog.findViewById(R.id.to_path);
            to_path.setText(mFileMag.getCurrentDir());

            progresslayout = (LinearLayout) copy_dialog.findViewById(R.id.progresslayout);
            current_progress_item = (TextView) copy_dialog.findViewById(R.id.current_progress_item);
            total_progress_count = (TextView) copy_dialog.findViewById(R.id.total_progress_count);
            copy_progressbar = (ProgressBar) copy_dialog.findViewById(R.id.copy_progressbar);

        }
    }

    void clear() {
        CancelledCopyIds.clear();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device, null);

        mContext = getActivity();
        mainActivity = ((MainActivity) getActivity());

        utils = new Utils(mContext);
        futils = new Futils();

        /*if ((boolean) PreferencesUtils.getValueFromPreference(mContext, Boolean.class, PreferencesUtils.PREF_IN_APP, false) == false) {
            LoadAds loadAds = new LoadAds(this.getActivity());
            loadAds.LoadFullScreenAdd();
        }*/


        setupTab(view);
        initPasteLayout(view);
        try {
            if (getArguments() != null) {
                String selected_fragment = getArguments().getString("select_tab");

                if (selected_fragment.equals("Internal Storage")) {
                    tabLayout.getTabAt(0).select();
                } else {
                    tabLayout.getTabAt(1).select();
                }
            }
        } catch (Exception e) {
        }
        return view;
    }

    private void initPasteLayout(View view) {
        hidden_paste_layout = (LinearLayout) view.findViewById(R.id.hidden_paste_layout);
        hidden_paste = (LinearLayout) view.findViewById(R.id.hidden_paste);
        hidden_cancel = (LinearLayout) view.findViewById(R.id.hidden_cancel);

        hidden_paste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pasteLayoutListener != null)
                    pasteLayoutListener.onPasteButtonPressed();
            }
        });

        hidden_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pasteLayoutListener != null)
                    pasteLayoutListener.onCancelButtonPressed();
            }
        });
    }

    public void setupTab(View view) {
        // Initializing the tablayout
        tabLayout = (TabLayout) view.findViewById(R.id.storageTabLayout);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);

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
                /*if (mCopyConnection != null && getActivity() != null && !mBound) {
                    Intent intent = new Intent(getActivity(), CopyService.class);
                    getActivity().bindService(intent, mCopyConnection, 0);
                }*/
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                /*if (mCopyConnection != null && getActivity() != null && mBound)
                    getActivity().unbindService(mCopyConnection);*/
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                /*if (mCopyConnection != null && getActivity() != null && !mBound) {
                    Intent intent = new Intent(getActivity(), CopyService.class);
                    getActivity().bindService(intent, mCopyConnection, 0);
                }*/
            }
        });
    }

    public int getTab() {
        return viewPager.getCurrentItem();
    }

    @Override
    public void onPause() {
        super.onPause();

        /*if (mMultiSelectData != null)
            mMultiSelectData.clear();

        multi_select_flag = false;*/

        /*if (mCopyConnection != null && getActivity() != null && mBound)
            getActivity().unbindService(mCopyConnection);*/
    }

    @Override
    public void onResume() {
        super.onResume();

        /*if (mCopyConnection != null && getActivity() != null) {
            Intent intent = new Intent(getActivity(), CopyService.class);
            getActivity().bindService(intent, mCopyConnection, 0);
        }*/
    }

    public void processCopyResults(final DataPackage b) {

        if (getResources() == null) return;
        if (b != null) {
            int id = b.getId();
            final Integer id1 = new Integer(id);
            if (!CancelledCopyIds.contains(id1)) {
                if (CopyIds.contains(id1)) {
                    boolean completed = b.isCompleted();
                    // View process = progresslayout.findViewWithTag("copy" + id);
                    if (completed) {
                        try {

                            CopyIds.remove(CopyIds.indexOf(id1));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        String name = b.getName();
                        int p1 = b.getP1();
                        int p2 = b.getP2();
                        long total = b.getTotal();
                        long done = b.getDone();
                        boolean move = b.isMove();
                        String text = futils.readableFileSize(done) + "/" + futils.readableFileSize(total);
                        if (move) {
                            text = futils.readableFileSize(done) + "/" + futils.readableFileSize(total);
                        }

                       /* ProgressBar p = (ProgressBar) process.findViewById(R.id.progressBar1);
                        p.setProgress(p1);*/

                        current_progress_item = (TextView) copy_dialog.findViewById(R.id.current_progress_item);
                        total_progress_count = (TextView) copy_dialog.findViewById(R.id.total_progress_count);
                        copy_progressbar = (ProgressBar) copy_dialog.findViewById(R.id.copy_progressbar);

                        current_progress_item.setText(name);
                        total_progress_count.setText(text);
                        copy_progressbar.setProgress(p1);

                    }
                } else {

                    /*CardView root = (android.support.v7.widget.CardView) getActivity()
                            .getLayoutInflater().inflate(R.layout.processrow, null);
                    root.setTag("copy" + id);
                    progresslayout.addView(root);*/

                    String name = b.getName();
                    int p1 = b.getP1();
                    long total = b.getTotal();
                    long done = b.getDone();
                    boolean move = b.isMove();
                    String text = futils.readableFileSize(done) + "/" + futils.readableFileSize(total);
                    if (move) {
                        text = futils.readableFileSize(done) + "/" + futils.readableFileSize(total);
                    }

                    current_progress_item = (TextView) copy_dialog.findViewById(R.id.current_progress_item);
                    total_progress_count = (TextView) copy_dialog.findViewById(R.id.total_progress_count);
                    copy_progressbar = (ProgressBar) copy_dialog.findViewById(R.id.copy_progressbar);

                    current_progress_item.setText(name);
                    total_progress_count.setText(text);
                    copy_progressbar.setProgress(p1);

                    CopyIds.add(id1);
                }
            }
        }
    }

    public interface PasteLayoutListener {
        void onPasteButtonPressed();

        void onCancelButtonPressed();
    }
}
