package com.galaxy.quickfilemanager.Utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.galaxy.quickfilemanager.FileOperation.Operations;
import com.galaxy.quickfilemanager.Fragments.StorageFragment.InternalStorageTabFragment;
import com.galaxy.quickfilemanager.Fragments.StorageFragment.SDCardTabFragment;
import com.galaxy.quickfilemanager.MainActivity;
import com.galaxy.quickfilemanager.R;

import java.io.File;

/**
 * Created by Umiya Mataji on 2/10/2017.
 */

public class MainActivityHelper {

    MainActivity context;

    public MainActivityHelper(MainActivity context) {
        this.context = context;
    }

    public MainActivityHelper() {

    }

    public int checkFolder(final File folder, Context context) {
        boolean lol = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP, ext = FileUtil.isOnExtSdCard(folder, context);
        if (lol && ext) {
            if (!folder.exists() || !folder.isDirectory()) {
                return 0;
            }

            // On Android 5, trigger storage access framework.
            if (!FileUtil.isWritableNormalOrSaf(folder, context)) {
                guideDialogForLEXA(folder.getPath());
                return 2;
            }
            return 1;
        } else if (Build.VERSION.SDK_INT == 19 && FileUtil.isOnExtSdCard(folder, context)) {
            // Assume that Kitkat workaround works
            return 1;
        } else if (FileUtil.isWritable(new File(folder, "DummyFile"))) {
            return 1;
        } else {
            return 0;
        }
    }

    public void guideDialogForLEXA(String path) {
        final MaterialDialog.Builder x = new MaterialDialog.Builder(context);
        x.theme(Theme.DARK);
        x.title(R.string.needsaccess);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.lexadrawer, null);
        x.customView(view, true);
        // textView
        TextView textView = (TextView) view.findViewById(R.id.description);
        textView.setText(new Utils().getString(context, R.string.needsaccesssummary) + path + new Utils().getString(context, R.string.needsaccesssummary1));
        ((ImageView) view.findViewById(R.id.icon)).setImageResource(R.drawable.sd_operate_step);
        x.positiveText(R.string.open);
        x.negativeText(R.string.cancel);
        x.positiveColor(Color.parseColor("#039BE5"));
        x.negativeColor(Color.RED);
        x.callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog materialDialog) {
                triggerStorageAccessFramework();
            }

            @Override
            public void onNegative(MaterialDialog materialDialog) {
                //Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();
            }
        });
        final MaterialDialog y = x.build();
        y.show();
    }

    private void triggerStorageAccessFramework() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        context.startActivityForResult(intent, 3);
    }

    public void mkFile(final HFile path, final InternalStorageTabFragment ma) {

        Operations.mkfile(path, ma.getActivity(), false, new Operations.ErrorCallBack() {
            @Override
            public void exists(final HFile file) {
                ma.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (ma != null && ma.getActivity() != null)
                            Toast.makeText(context, new Utils().getString(context, R.string.msg_prompt_file_already_exits), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void launchSAF(HFile file) {

                ma.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        context.oppathe = path.getPath();
                        context.operation = FileUtil.NEW_FILE;
                        guideDialogForLEXA(path.getPath());
                    }
                });

            }

            @Override
            public void launchSAF(HFile file, HFile file1) {

            }

            @Override
            public void done(HFile hFile, final boolean b) {
                ma.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (b)
                            ma.mHandler.updateDirectory(ma.mFileMag.getNextDir(ma.mFileMag.getCurrentDir(), true));
                        else
                            Toast.makeText(ma.getActivity(), "Operation Failed", Toast.LENGTH_SHORT).show();

                        try {
                            if (ma.create_dialog != null)
                                ma.create_dialog.dismiss();
                        } catch (Exception e) {
                        }

                    }
                });
            }
        });
    }

    public void mkFile(final HFile path, final SDCardTabFragment ma) {

        Operations.mkfile(path, ma.getActivity(), false, new Operations.ErrorCallBack() {
            @Override
            public void exists(final HFile file) {
                ma.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (ma != null && ma.getActivity() != null)
                            Toast.makeText(context, new Utils().getString(context, R.string.msg_prompt_file_already_exits), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void launchSAF(HFile file) {

                ma.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        guideDialogForLEXA(path.getPath());
                    }
                });

            }

            @Override
            public void launchSAF(HFile file, HFile file1) {

            }

            @Override
            public void done(HFile hFile, final boolean b) {
                ma.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (b)
                            ma.mHandler.updateDirectory(ma.mFileMag.getNextDir(ma.mFileMag.getCurrentDir(), true));
                        else
                            Toast.makeText(ma.getActivity(), "Operation Failed", Toast.LENGTH_SHORT).show();
                        try {
                            if (ma.create_dialog != null)
                                ma.create_dialog.dismiss();
                        } catch (Exception e) {
                        }
                    }
                });
            }
        });
    }

    public void mkDir(final HFile path, final InternalStorageTabFragment ma) {

        Operations.mkdir(path, ma.getActivity(), false, new Operations.ErrorCallBack() {
            @Override
            public void exists(final HFile file) {
                ma.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (ma != null && ma.getActivity() != null)
                            Toast.makeText(context, new Utils().getString(context, R.string.msg_prompt_file_already_exits), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void launchSAF(HFile file) {
                ma.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        context.oppathe = path.getPath();
                        context.operation = FileUtil.NEW_FOLDER;
                        guideDialogForLEXA(context.oppathe);
                    }
                });

            }

            @Override
            public void launchSAF(HFile file, HFile file1) {

            }

            @Override
            public void done(HFile hFile, final boolean b) {
                ma.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (b)
                            ma.mHandler.updateDirectory(ma.mFileMag.getNextDir(ma.mFileMag.getCurrentDir(), true));
                        else
                            Toast.makeText(ma.getActivity(), "Operation Failed", Toast.LENGTH_SHORT).show();

                        try {
                            if (ma.create_dialog != null)
                                ma.create_dialog.dismiss();
                        } catch (Exception e) {
                        }
                    }
                });
            }
        });
    }

    public void mkDir(final HFile path, final SDCardTabFragment ma) {

        Operations.mkdir(path, ma.getActivity(), false, new Operations.ErrorCallBack() {
            @Override
            public void exists(final HFile file) {
                ma.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (ma != null && ma.getActivity() != null)
                            Toast.makeText(context, new Utils().getString(context, R.string.msg_prompt_file_already_exits), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void launchSAF(HFile file) {
                ma.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        context.oppathe = path.getPath();
                        context.operation = FileUtil.NEW_FOLDER;
                        guideDialogForLEXA(context.oppathe);
                    }
                });

            }

            @Override
            public void launchSAF(HFile file, HFile file1) {

            }

            @Override
            public void done(HFile hFile, final boolean b) {
                ma.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (b)
                            ma.mHandler.updateDirectory(ma.mFileMag.getNextDir(ma.mFileMag.getCurrentDir(), true));
                        else
                            Toast.makeText(ma.getActivity(), "Operation Failed", Toast.LENGTH_SHORT).show();

                        try {
                            if (ma.create_dialog != null)
                                ma.create_dialog.dismiss();
                        } catch (Exception e) {
                        }
                    }
                });
            }
        });
    }

    public void rename(int mode, String f, String f1, final Context ma) {

        HFile hFile = new HFile();
        hFile.setMode(mode);
        hFile.setPath(f);

        HFile hFile1 = new HFile();
        hFile1.setMode(mode);
        hFile1.setPath(f1);

        Operations.rename(hFile, hFile1, false, context.getFragment().getActivity(), new Operations.ErrorCallBack() {
            @Override
            public void exists(HFile file) {
                context.getFragment().getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, (R.string.msg_prompt_file_already_exits), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void launchSAF(HFile file) {

            }

            @Override
            public void launchSAF(final HFile file, final HFile file1) {

                context.getFragment().getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        context.oppathe = file.getPath();
                        context.oppathe1 = file1.getPath();
                        context.operation = FileUtil.RENAME;
                        guideDialogForLEXA(context.oppathe1);
                    }
                });
            }

            @Override
            public void done(HFile hFile, final boolean b) {
                context.getFragment().getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                       /* if (b) {
                            ma.mHandler.updateDirectory(ma.mFileMag.getNextDir(ma.mFileMag.getCurrentDir(), true));
                        } else
                            Toast.makeText(context, R.string.operationunsuccesful, Toast.LENGTH_SHORT).show();*/

                    }
                });
            }
        });
    }
}
