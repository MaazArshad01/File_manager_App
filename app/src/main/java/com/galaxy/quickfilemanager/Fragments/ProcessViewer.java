package com.galaxy.quickfilemanager.Fragments;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.galaxy.quickfilemanager.FileOperation.ZipTask;
import com.galaxy.quickfilemanager.MainActivity;
import com.galaxy.quickfilemanager.Services.CopyService;
import com.galaxy.quickfilemanager.Services.DeleteService;
import com.galaxy.quickfilemanager.Utils.DataPackage;
import com.galaxy.quickfilemanager.Utils.Futils;
import com.galaxy.quickfilemanager.ProgressListener;
import com.galaxy.quickfilemanager.R;
import com.galaxy.quickfilemanager.RegisterCallback;

import java.util.ArrayList;

/**
 * Created by Umiya Mataji on 1/10/2017.
 */

public class ProcessViewer extends Fragment {

    boolean mBound = false;

    Futils utils = new Futils();

    ArrayList<Integer> DeleteIds = new ArrayList<Integer>();
    ArrayList<Integer> CancelledDeleteIds = new ArrayList<Integer>();

    ArrayList<Integer> CopyIds = new ArrayList<Integer>();
    ArrayList<Integer> CancelledCopyIds = new ArrayList<Integer>();

    ArrayList<Integer> ZipIds = new ArrayList<Integer>();
    ArrayList<Integer> CancelledZipIds = new ArrayList<Integer>();

    boolean running = false;
    MainActivity mainActivity;
    private LinearLayout rootView;
    private TextView nodatafound_txt;
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

   /* private ServiceConnection mCopyConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            CopyService.LocalBinder binder = (CopyService.LocalBinder) service;
            CopyService mService = binder.getService();
            mBound = true;

            for (int i : mService.hash1.keySet()) {
                processCopyResults(mService.hash1.get(i));
            }

            mService.setProgressListener(new CopyService.ProgressListener() {
                @Override
                public void onUpdate(DataPackage dataPackage) {
                    processCopyResults(dataPackage);
                }

                @Override
                public void refresh() {
                    clear();
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };*/

    private ServiceConnection mDeleteConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            DeleteService.LocalBinder binder = (DeleteService.LocalBinder) service;
            DeleteService mService = binder.getService();
            mBound = true;
            for (int i : mService.hash1.keySet()) {
                processDeleteResults(mService.hash1.get(i));
            }
            mService.setProgressListener(new DeleteService.ProgressListener() {
                @Override
                public void onUpdate(DataPackage dataPackage) {
                    processDeleteResults(dataPackage);
                }

                @Override
                public void refresh() {
                    clear();
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    /*private ServiceConnection mCompressConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ZipTask.LocalBinder binder = (ZipTask.LocalBinder) service;
            ZipTask mService = binder.getService();
            mBound = true;
            for (int i : mService.hash1.keySet()) {
                processCompressResults(mService.hash1.get(i));
            }
            mService.setProgressListener(new ZipTask.ProgressListener() {
                @Override
                public void onUpdate(DataPackage dataPackage) {
                    processCompressResults(dataPackage);
                }

                @Override
                public void refresh() {
                    clear();
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };*/

    private ServiceConnection mCompressConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            RegisterCallback binder = (RegisterCallback.Stub.asInterface(service));
            mBound = true;
            try {
                for (DataPackage dataPackage : binder.getCurrent()) {
                    processCompressResults(dataPackage);
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
                                processCompressResults(dataPackage);
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = (ViewGroup) inflater.inflate(R.layout.processparent,
                container, false);
        setRetainInstance(false);
        setHasOptionsMenu(true);

        mainActivity = (MainActivity) getActivity();
        rootView = (LinearLayout) root.findViewById(R.id.secondbut);
        nodatafound_txt = (TextView) root.findViewById(R.id.nodatafound_txt);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        running = true;
        Intent intent = new Intent(getActivity(), CopyService.class);
        getActivity().bindService(intent, mCopyConnection, 0);

        Intent intent1 = new Intent(getActivity(), DeleteService.class);
        getActivity().bindService(intent1, mDeleteConnection, 0);

        Intent intent2 = new Intent(getActivity(), ZipTask.class);
        getActivity().bindService(intent2, mCompressConnection, 0);
    }

    @Override
    public void onPause() {
        super.onPause();
        running = false;
        getActivity().unbindService(mCopyConnection);
        getActivity().unbindService(mDeleteConnection);
        getActivity().unbindService(mCompressConnection);
        clear();
    }

    public void processDeleteResults(final DataPackage b) {
        if (!running) return;
        if (getResources() == null) return;
        if (b != null) {
            int id = b.getId();
            final Integer id1 = new Integer(id);
            if (!CancelledDeleteIds.contains(id1)) {
                if (DeleteIds.contains(id1)) {
                    boolean completed = b.isCompleted();
                    View process = rootView.findViewWithTag("delete" + id);
                    if (completed) {
                        try {
                            rootView.removeViewInLayout(process);
                            DeleteIds.remove(DeleteIds.indexOf(id1));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        try {
                            if (rootView != null) {
                                int countChildProcesses = rootView.getChildCount();
                                if (countChildProcesses == 0)
                                    nodatafound_txt.setVisibility(View.VISIBLE);
                                else
                                    nodatafound_txt.setVisibility(View.GONE);
                            }
                        } catch (Exception e) {
                        }
                    } else {
                        String name = b.getName();
                        int p1 = b.getP1();

                        int total = (int) b.getTotal();
                        int done = (int) b.getDone();

                        String text = utils.getString(getActivity(), R.string.deleting) + "\n" + name + "\n" + done + "/" + total + "\n" + p1 + "%";
                        try {
                            ((TextView) process.findViewById(R.id.progressText)).setText(text);
                            ProgressBar p = (ProgressBar) process.findViewById(R.id.progressBar1);
                            p.setProgress(p1);
                        } catch (Exception e) {
                        }
                    }
                } else {
                    CardView root = (CardView) getActivity()
                            .getLayoutInflater().inflate(R.layout.processrow, null);
                    root.setTag("delete" + id);

                    ImageButton cancel = (ImageButton) root.findViewById(R.id.delete_button);
                    TextView progressText = (TextView) root.findViewById(R.id.progressText);

                    // ((ImageView) root.findViewById(R.id.progressImage)).setImageDrawable(icon);
                    cancel.setOnClickListener(new View.OnClickListener() {

                        public void onClick(View p1) {
                            Toast.makeText(getActivity(), utils.getString(getActivity(), R.string.stopping), Toast.LENGTH_LONG).show();
                            Intent i = new Intent("deletecancel");
                            i.putExtra("id", id1);
                            getActivity().sendBroadcast(i);
                            rootView.removeView(rootView.findViewWithTag("delete" + id1));

                            DeleteIds.remove(DeleteIds.indexOf(id1));
                            CancelledDeleteIds.add(id1);
                            // TODO: Implement this method

                            try {
                                if (rootView != null) {
                                    int countChildProcesses = rootView.getChildCount();
                                    if (countChildProcesses == 0)
                                        nodatafound_txt.setVisibility(View.VISIBLE);
                                    else
                                        nodatafound_txt.setVisibility(View.GONE);
                                }
                            } catch (Exception e) {
                            }

                        }
                    });

                    String name = b.getName();
                    int p1 = b.getP1();

                    String text = utils.getString(getActivity(), R.string.deleting) + "\n" + name;

                    progressText.setText(text);
                    ProgressBar p = (ProgressBar) root.findViewById(R.id.progressBar1);
                    p.setMax((int) b.getTotal());
                    p.setProgress(p1);

                    DeleteIds.add(id1);
                    rootView.addView(root);
                }
            }
        }
    }

    public void processCopyResults(final DataPackage b) {
        if (!running) return;
        if (getResources() == null) return;
        if (b != null) {
            int id = b.getId();
            final Integer id1 = new Integer(id);
            if (!CancelledCopyIds.contains(id1)) {
                if (CopyIds.contains(id1)) {
                    boolean completed = b.isCompleted();
                    View process = rootView.findViewWithTag("copy" + id);
                    if (completed) {
                        try {
                            rootView.removeViewInLayout(process);
                            CopyIds.remove(CopyIds.indexOf(id1));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        try {
                            if (rootView != null) {
                                int countChildProcesses = rootView.getChildCount();
                                if (countChildProcesses == 0)
                                    nodatafound_txt.setVisibility(View.VISIBLE);
                                else
                                    nodatafound_txt.setVisibility(View.GONE);
                            }
                        } catch (Exception e) {
                        }

                    } else {
                        String name = b.getName();
                        int p1 = b.getP1();
                        int p2 = b.getP2();
                        long total = b.getTotal();
                        long done = b.getDone();
                        boolean move = b.isMove();
                        String text = utils.getString(getActivity(), R.string.copying) + "\n" + name + "\n" + utils.readableFileSize(done) + "/" + utils.readableFileSize(total) + "\n" + p1 + "%";
                        if (move) {
                            text = utils.getString(getActivity(), R.string.moving) + "\n" + name + "\n" + utils.readableFileSize(done) + "/" + utils.readableFileSize(total) + "\n" + p1 + "%";
                        }
                        ((TextView) process.findViewById(R.id.progressText)).setText(text);
                        ProgressBar p = (ProgressBar) process.findViewById(R.id.progressBar1);
                        p.setProgress(p1);
                        p.setSecondaryProgress(p2);
                    }
                } else {
                    CardView root = (CardView) getActivity()
                            .getLayoutInflater().inflate(R.layout.processrow, null);
                    root.setTag("copy" + id);

                    ImageButton cancel = (ImageButton) root.findViewById(R.id.delete_button);
                    TextView progressText = (TextView) root.findViewById(R.id.progressText);

                    boolean move = b.isMove();

                    // ((ImageView) root.findViewById(R.id.progressImage)).setImageDrawable(icon);
                    cancel.setOnClickListener(new View.OnClickListener() {

                        public void onClick(View p1) {
                            Toast.makeText(getActivity(), utils.getString(getActivity(), R.string.stopping), Toast.LENGTH_LONG).show();
                            Intent i = new Intent("copycancel");
                            i.putExtra("id", id1);
                            getActivity().sendBroadcast(i);
                            rootView.removeView(rootView.findViewWithTag("copy" + id1));

                            CopyIds.remove(CopyIds.indexOf(id1));
                            CancelledCopyIds.add(id1);
                            // TODO: Implement this method

                            try {
                                if (rootView != null) {
                                    int countChildProcesses = rootView.getChildCount();
                                    if (countChildProcesses == 0)
                                        nodatafound_txt.setVisibility(View.VISIBLE);
                                    else
                                        nodatafound_txt.setVisibility(View.GONE);
                                }
                            } catch (Exception e) {
                            }
                        }
                    });

                    String name = b.getName();
                    int p1 = b.getP1();
                    int p2 = b.getP2();

                    String text = utils.getString(getActivity(), R.string.copying) + "\n" + name;
                    if (move) {
                        text = utils.getString(getActivity(), R.string.moving) + "\n" + name;
                    }
                    progressText.setText(text);
                    ProgressBar p = (ProgressBar) root.findViewById(R.id.progressBar1);
                    p.setProgress(p1);
                    p.setSecondaryProgress(p2);
                    CopyIds.add(id1);
                    rootView.addView(root);
                }
            }
        }
    }

    void processCompressResults(DataPackage dataPackage) {
        final int id = dataPackage.getId();
        try {
            if (!CancelledZipIds.contains(id)) {
                if (ZipIds.contains(id)) {
                    boolean completed = dataPackage.isCompleted();
                    View process = rootView.findViewWithTag("zip" + id);
                    if (completed) {
                        rootView.removeViewInLayout(process);
                        ZipIds.remove(ZipIds.indexOf(id));

                        try {
                            if (rootView != null) {
                                int countChildProcesses = rootView.getChildCount();
                                if (countChildProcesses == 0)
                                    nodatafound_txt.setVisibility(View.VISIBLE);
                                else
                                    nodatafound_txt.setVisibility(View.GONE);
                            }
                        } catch (Exception e) { }

                    } else {
                        String name = dataPackage.getName();
                        int p1 = dataPackage.getP1();

                        ProgressBar p = (ProgressBar) process.findViewById(R.id.progressBar1);
                        if (p1 <= 100) {
                            ((TextView) process.findViewById(R.id.progressText)).setText("zipping" + "\n" + name + "\n" + p1 + "%");

                            p.setProgress(p1);
                        }
                    }
                } else {
                    CardView root = (CardView) getActivity().getLayoutInflater().inflate(R.layout.processrow, null);
                    root.setTag("zip" + id);

                    ImageView progressImage = ((ImageView) root.findViewById(R.id.progressImage));
                    ImageButton cancel = (ImageButton) root.findViewById(R.id.delete_button);
                    TextView progressText = (TextView) root.findViewById(R.id.progressText);

                    cancel.setOnClickListener(new View.OnClickListener() {

                        public void onClick(View p1) {
                            Toast.makeText(getActivity(), utils.getString(getActivity(), R.string.stopping), Toast.LENGTH_LONG).show();
                            Intent i = new Intent("zipcancel");
                            i.putExtra("id", id);
                            getActivity().sendBroadcast(i);
                            rootView.removeView(rootView.findViewWithTag("zip" + id));

                            ZipIds.remove(ZipIds.indexOf(id));
                            CancelledZipIds.add(id);

                            try {
                                if (rootView != null) {
                                    int countChildProcesses = rootView.getChildCount();
                                    if (countChildProcesses == 0)
                                        nodatafound_txt.setVisibility(View.VISIBLE);
                                    else
                                        nodatafound_txt.setVisibility(View.GONE);
                                }
                            } catch (Exception e) {
                            }

                        }
                    });

                    String name = dataPackage.getName();
                    int p1 = dataPackage.getP1();

                    ((TextView) root.findViewById(R.id.progressText)).setText("zipping" + "\n" + name);
                    ProgressBar p = (ProgressBar) root.findViewById(R.id.progressBar1);
                    p.setProgress(p1);

                    ZipIds.add(id);
                    rootView.addView(root);
                }
            }
        } catch (Exception e) {
        }
    }

    void clear() {
        rootView.removeAllViewsInLayout();
        ZipIds.clear();
        CancelledZipIds.clear();
        CancelledDeleteIds.clear();
        CancelledCopyIds.clear();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }
}
