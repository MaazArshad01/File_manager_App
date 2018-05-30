package com.galaxy.quickfilemanager.Utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.TextUtils;

import com.galaxy.quickfilemanager.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * Created by Umiya Mataji on 1/6/2017.
 */

public class Utils {

    Context mContext;

    public Utils() {
    }

    public Utils(Context mContext) {
        this.mContext = mContext;
    }

    public boolean isTablet(){
        boolean tabletSize = mContext.getResources().getBoolean(R.bool.isTablet);
        if (tabletSize) {
            return true;
        } else {
            return false;
        }

    }

    public static String convertTimeFromUnixTimeStamp(String date) {

        DateFormat inputFormat = new SimpleDateFormat("EE MMM dd HH:mm:ss zz yyy");
        Date d = null;
        try {
            d = inputFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        DateFormat outputFormat = new SimpleDateFormat("MMM dd, yyy h:mm a");
        return outputFormat.format(d);

    }

    public Bitmap convertDrawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public void showSMBHelpDialog(Context m) {

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(m);
        alertDialog.setMessage(Html.fromHtml("<html>\n" +
                "<body>\n" +
                "<center>\n" +
                "<h1>How to access shared windows folder on android (smb)</h1>\n" +
                "</center>\n" +
                "<ol>\n" +
                "<li>\n" +
                "<b>Enable File Sharing</b>\n" +
                "<br>Open the Control Panel, click Choose homegroup and sharing options under Network and Internet, and click Change advanced sharing settings. Enable the file and printer sharing feature.\n" +
                "</li><br><li><b>Additional File Sharing settings</b><br>You may also want to configure the other advanced sharing settings here. \n" +
                "For example, you could enable access to your files without a password if you trust all the devices on your local network.Once file and printer sharing is enabled, you can open File Explorer or Windows Explorer, right-click a folder you want to share, and select Properties. \n" +
                "Click the Share button and make the folder available on the network.\n" +
                "</li><li><br><b>Make sure both devices are on same Wifi</b><br> \n" +
                "This feature makes files available on the local network, so your PC and mobile devices have to be on the same local network. You can’t access a shared Windows folder over the Internet or when your smartphone is connected to its mobile data — it has to be connected to Wi-Fi.</li><li>\n" +
                "<br><b>Find IP Address</b>\n" +
                "<br>Open Command Prompt. Type 'ipconfig' and press Enter. Look for Default Gateway under your network adapter for your router's IP address. Look for \\\"IPv4 Address\\\" under the same adapter section to find your computer's IP address.</li><li><br>\n" +
                "<b>Enter details in smb dialog box</b>\n" +
                "<br>\n" +
                "</ol>\n" +
                "</body>\n" +
                "</html>"));

        alertDialog.setPositiveButton(m.getResources().getString(R.string.gotit), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        alertDialog.show();
    }

    public String getString(Context c, int a) {
        return c.getResources().getString(a);
    }

    public String getStoragePaths(String StorageType) {
        String Path = "";
        List<StorageUtils.StorageInfo> data = StorageUtils.getStorageList();

        //  StorageUtils.StorageInfo list = data.get(i);

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {

            if (StorageType.equalsIgnoreCase("ExternalStorage")) {

                try {
                    StorageUtils.StorageInfo externalPaths = data.get(0);
                    Path = externalPaths.getStoragePath();

                    if (data.size() > 2) {
                        Path = "/storage/sdcard0";
                    } else if (data.size() == 2) // if SdCard not Atteched.
                        Path = "Sdcard not found";
                    else
                        Path = "/storage/sdcard1";
                } catch (Exception e) {
                    Path = "Sdcard not found";
                }


            } else {
                if (data.size() == 2) // if SdCard not Atteched.
                    Path = "/storage/sdcard0";
                else
                    Path = "/storage/sdcard1";
            }

        } else {

            if (StorageType.equalsIgnoreCase("ExternalStorage")) {
                try {

                    StorageUtils.StorageInfo externalPaths = data.get(1);
                    Path = externalPaths.getStoragePath();

                    String tmp = Path.substring(Path.lastIndexOf("/"));
                    Path = "/storage" + tmp;
                } catch (Exception e) {
                    Path = "Sdcard not found";
                }
            } else {
                StorageUtils.StorageInfo externalPaths = data.get(0);
                Path = externalPaths.getStoragePath();
            }
        }
        return Path;
    }

    public Bitmap GetIcon(Context mContext, String pkgname) {

        Drawable icon = null;
        try {

            try {
               /* ApplicationInfo app = mContext.getPackageManager().getApplicationInfo(pkgname, 0);
                icon = mContext.getPackageManager().getApplicationIcon(app);*/

                String APKFilePath = pkgname; //For example...
                PackageManager pm = mContext.getPackageManager();
                PackageInfo pi = pm.getPackageArchiveInfo(APKFilePath, 0);

                // the secret are these two lines....
                pi.applicationInfo.sourceDir = APKFilePath;
                pi.applicationInfo.publicSourceDir = APKFilePath;

                icon = pi.applicationInfo.loadIcon(pm);
                //String   AppName = (String)pi.applicationInfo.loadLabel(pm);

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } catch (Exception e) {
        }

        return convertDrawableToBitmap(icon);
    }

    public List<String> getStorageDirectories() {

        final Pattern DIR_SEPARATOR = Pattern.compile("/");
        // Final set of paths
        final ArrayList<String> rv = new ArrayList<>();
        // Primary physical SD-CARD (not emulated)
        final String rawExternalStorage = System.getenv("EXTERNAL_STORAGE");
        // All Secondary SD-CARDs (all exclude primary) separated by ":"
        final String rawSecondaryStoragesStr = System.getenv("SECONDARY_STORAGE");
        // Primary emulated SD-CARD
        final String rawEmulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET");
        if (TextUtils.isEmpty(rawEmulatedStorageTarget)) {
            // Device has physical external storage; use plain paths.
            if (TextUtils.isEmpty(rawExternalStorage)) {
                // EXTERNAL_STORAGE undefined; falling back to default.
                rv.add("/storage/sdcard0");
            } else {
                rv.add(rawExternalStorage);
            }
        } else {
            // Device has emulated storage; external storage paths should have
            // userId burned into them.
            final String rawUserId;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                rawUserId = "";
            } else {
                final String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                final String[] folders = DIR_SEPARATOR.split(path);
                final String lastFolder = folders[folders.length - 1];
                boolean isDigit = false;
                try {
                    Integer.valueOf(lastFolder);
                    isDigit = true;
                } catch (NumberFormatException ignored) {
                }
                rawUserId = isDigit ? lastFolder : "";
            }
            // /storage/emulated/0[1,2,...]
            if (TextUtils.isEmpty(rawUserId)) {
                rv.add(rawEmulatedStorageTarget);
            } else {
                rv.add(rawEmulatedStorageTarget + File.separator + rawUserId);
            }
        }
        // Add all secondary storages
        if (!TextUtils.isEmpty(rawSecondaryStoragesStr)) {
            // All Secondary SD-CARDs splited into array
            final String[] rawSecondaryStorages = rawSecondaryStoragesStr.split(File.pathSeparator);
            Collections.addAll(rv, rawSecondaryStorages);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            rv.clear();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            String strings[] = FileUtil.getExtSdCardPathsForActivity(mContext);
            for (String s : strings) {
                File f = new File(s);
                if (!rv.contains(s) && new Futils().canListFiles(f))
                    rv.add(s);
            }
        }

      /*  rootmode = Sp.getBoolean("rootmode", false);
        if (rootmode)
            rv.add("/");*/
        File usb = getUsbDrive();
        if (usb != null && !rv.contains(usb.getPath())) rv.add(usb.getPath());

        /*for (String path : rv) {
            Log.e("Storage Paths", path);
            //Toast.makeText(mContext, path, Toast.LENGTH_SHORT).show();
        }*/
        return rv;
    }

    public String StoragePath(String StorageType) {
        List<String> paths = getStorageDirectories();
        if (paths.size() > 0) {
            try {
                if (StorageType.equalsIgnoreCase("InternalStorage")) {
                    return paths.get(0);

                } else if (StorageType.equalsIgnoreCase("ExternalStorage")) {
                    if (paths.size() >= 1)
                        return paths.get(1);

                } else if (StorageType.equalsIgnoreCase("UsbStorage")) {
                    if (paths.size() >= 2)
                        return paths.get(2);
                } else {

                }
            }catch (Exception e){}
        }
        return "";
    }

    public File getUsbDrive() {
        File parent;
        parent = new File("/storage");

        try {
            for (File f : parent.listFiles()) {
                if (f.exists() && f.getName().toLowerCase().contains("usb") && f.canExecute()) {
                    return f;
                }
            }
        } catch (Exception e) {
        }
        parent = new File("/mnt/sdcard/usbStorage");
        if (parent.exists() && parent.canExecute())
            return (parent);

        parent = new File("/mnt/sdcard/usb_storage");
        if (parent.exists() && parent.canExecute())
            return parent;

        return null;
    }

    public static class StorageUtils {

        private static final String TAG = "StorageUtils";

        public static List<StorageInfo> getStorageList() {

            List<StorageInfo> list = new ArrayList<StorageInfo>();
            String def_path = Environment.getExternalStorageDirectory().getPath();
            boolean def_path_removable = Environment.isExternalStorageRemovable();
            String def_path_state = Environment.getExternalStorageState();
            boolean def_path_available = def_path_state.equals(Environment.MEDIA_MOUNTED)
                    || def_path_state.equals(Environment.MEDIA_MOUNTED_READ_ONLY);
            boolean def_path_readonly = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY);

            HashSet<String> paths = new HashSet<String>();
            int cur_removable_number = 1;

            if (def_path_available) {
                paths.add(def_path);
                list.add(0, new StorageInfo(def_path, def_path_readonly, def_path_removable, def_path_removable ? cur_removable_number++ : -1));
            }

            BufferedReader buf_reader = null;
            try {
                buf_reader = new BufferedReader(new FileReader("/proc/mounts"));
                String line;

                while ((line = buf_reader.readLine()) != null) {

                    if (line.contains("vfat") || line.contains("/mnt")) {
                        StringTokenizer tokens = new StringTokenizer(line, " ");
                        String unused = tokens.nextToken(); //device
                        String mount_point = tokens.nextToken(); //mount point
                        if (paths.contains(mount_point)) {
                            continue;
                        }
                        unused = tokens.nextToken(); //file system
                        List<String> flags = Arrays.asList(tokens.nextToken().split(",")); //flags
                        boolean readonly = flags.contains("ro");

                        if (line.contains("/dev/block/vold")) {
                            if (!line.contains("/mnt/secure")
                                    && !line.contains("/mnt/asec")
                                    && !line.contains("/mnt/obb")
                                    && !line.contains("/dev/mapper")
                                    && !line.contains("tmpfs")) {
                                paths.add(mount_point);
                                list.add(new StorageInfo(mount_point, readonly, true, cur_removable_number++));
                            }
                        }
                    }
                }

            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                if (buf_reader != null) {
                    try {
                        buf_reader.close();
                    } catch (IOException ex) {
                    }
                }
            }
            return list;
        }

        public static class StorageInfo {

            public final String path;
            public final boolean readonly;
            public final boolean removable;
            public final int number;

            StorageInfo(String path, boolean readonly, boolean removable, int number) {
                this.path = path;
                this.readonly = readonly;
                this.removable = removable;
                this.number = number;
            }

            public int getStorageNumber() {
                return number;
            }

            public String getStoragePath() {
                return path;
            }

            public String getDisplayName() {
                StringBuilder res = new StringBuilder();
                if (!removable) {
                    res.append("Internal SD card" + " Path :- " + path);
                } else if (number > 1) {
                    res.append("SD card " + number + " Path :- " + path);
                } else {
                    res.append("SD card" + " Path:- " + path);
                }
                if (readonly) {
                    res.append(" (Read only)");
                }
                return res.toString();
            }
        }
    }

}
