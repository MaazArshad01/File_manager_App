/*
    Open Manager For Tablets, an open source file manager for the Android system
    Copyright (C) 2011  Joe Berria <nexesdevelopment@gmail.com>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.jksol.filemanager.FileOperation;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;

import com.jksol.filemanager.R;
import com.jksol.filemanager.Utils.AppController;
import com.jksol.filemanager.Utils.FileUtil;
import com.jksol.filemanager.Utils.Utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import jcifs.smb.SmbFile;

/**
 * This class is completely modular, which is to say that it has
 * no reference to the any GUI activity. This class could be taken
 * and placed into in other java (not just Android) project and work.
 * <br>
 * <br>
 * This class handles all file and folder operations on the system.
 * This class dictates how files and folders are copied/pasted, (un)zipped
 * renamed and searched. The EventHandler class will generally call these
 * methods and have them performed in a background thread. Threading is not
 * done in this class.
 *
 * @author Joe Berria
 */
public class FileManager {
    private static final int BUFFER = 2048;
    private static final int SORT_NONE = 0;
    private static final int SORT_ALPHA = 1;
    private static final int SORT_TYPE = 2;
    private static final int SORT_SIZE = 3;

    private static final Comparator alph = new Comparator<String>() {
        @Override
        public int compare(String arg0, String arg1) {
            return arg0.toLowerCase().compareTo(arg1.toLowerCase());
        }
    };
    private static final Pattern DIR_SEPORATOR = Pattern.compile("/");
    private final Comparator type = new Comparator<String>() {
        @Override
        public int compare(String arg0, String arg1) {
            String ext = null;
            String ext2 = null;
            int ret;

            try {
                ext = arg0.substring(arg0.lastIndexOf(".") + 1, arg0.length()).toLowerCase();
                ext2 = arg1.substring(arg1.lastIndexOf(".") + 1, arg1.length()).toLowerCase();

            } catch (IndexOutOfBoundsException e) {
                return 0;
            }
            ret = ext.compareTo(ext2);

            if (ret == 0)
                return arg0.toLowerCase().compareTo(arg1.toLowerCase());

            return ret;
        }
    };

    private String SMBPath = "";
    private boolean mShowHiddenFiles = false;
    private int mSortType = SORT_ALPHA;
    private long mDirSize = 0;
    private Stack<String> mPathStack;
    private final Comparator size = new Comparator<String>() {
        @Override
        public int compare(String arg0, String arg1) {
            String dir = mPathStack.peek();
            Long first = new File(dir + "/" + arg0).length();
            Long second = new File(dir + "/" + arg1).length();

            return first.compareTo(second);
        }
    };
    private ArrayList<String> mDirContent;

    /**
     * Constructs an object of the class
     * <br>
     * this class uses a stack to handle the navigation of directories.
     */
    public FileManager() {
        mDirContent = new ArrayList<String>();
        mPathStack = new Stack<String>();

        mPathStack.push("/");
        mPathStack.push(mPathStack.peek() + "sdcard");

    }

    public FileManager(String SMBPath) {
        mDirContent = new ArrayList<String>();
        mPathStack = new Stack<String>();

        this.SMBPath = SMBPath;

        mPathStack.push("smb://");
        //mPathStack.push(mPathStack.peek() + "sdcard");
    }

    /**
     * Constructs an object of the class
     * <br>
     * this class uses a stack to handle the navigation of directories.
     */
 /*   public FileManager(String sdcard) {
        mDirContent = new ArrayList<String>();
        mPathStack = new Stack<String>();

        mPathStack.push("/");
        mPathStack.push(mPathStack.peek() + "storage");
        mPathStack.push("/");
        mPathStack.push(mPathStack.peek() + "sdcard0");
    }*/

    /**
     * converts integer from wifi manager to an IP address.
     *
     * @param
     * @return
     */
    public static String integerToIPAddress(int ip) {
        String ascii_address = "";
        int[] num = new int[4];

        num[0] = (ip & 0xff000000) >> 24;
        num[1] = (ip & 0x00ff0000) >> 16;
        num[2] = (ip & 0x0000ff00) >> 8;
        num[3] = ip & 0x000000ff;

        ascii_address = num[0] + "." + num[1] + "." + num[2] + "." + num[3];

        return ascii_address;
    }

    // Inspired by org.apache.commons.io.FileUtils.isSymlink()
    private static boolean isSymlink(File file) throws IOException {
        File fileInCanonicalDir = null;
        if (file.getParent() == null) {
            fileInCanonicalDir = file;
        } else {
            File canonicalDir = file.getParentFile().getCanonicalFile();
            fileInCanonicalDir = new File(canonicalDir, file.getName());
        }
        return !fileInCanonicalDir.getCanonicalFile().equals(fileInCanonicalDir.getAbsoluteFile());
    }

    /*
     * @param size
     * @param tag
     */
    private static String formatSize(long size, String tag) {
        String suffix = null;
        if (size >= 1024) {
            suffix = "KB";
            size /= 1024;
            if (size >= 1024) {
                suffix = "MB";
                size /= 1024;
                if (size >= 1024) {
                    suffix = "GB";
                    size /= 1024;
                }
            }
        }
        StringBuilder resultBuffer = new StringBuilder(Long.toString(size));
        int commaOffset = resultBuffer.length() - 3;
        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',');
            commaOffset -= 3;
        }

        if (suffix != null) resultBuffer.append(suffix);
        return resultBuffer.toString();
    }

    public static String getAvailableInternalMemorySize() {
     /*   File path = Environment.getDataDirectory();
        Log.d("getPath", path.getPath());
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        @SuppressWarnings("deprecation") long availableBlocks = stat.getAvailableBlocks();
        return formatSize(availableBlocks * blockSize, "free");*/

        long total, aval;
        int kb = 1024;
        File path = Environment.getDataDirectory();
        StatFs fs = new StatFs(path.getPath());

        total = fs.getBlockCount() * (fs.getBlockSize() / kb);
        aval = fs.getAvailableBlocks() * (fs.getBlockSize() / kb);

        return String.format(" %.2f GB / " +
                        "%.2f GB",
                (double) aval / (kb * kb), (double) total / (kb * kb));
    }

    public static void file_detail_dialog(Context mContext, String filePath) {
        final Dialog fileDetailsDialog = new Dialog(mContext, android.R.style.Theme_Translucent_NoTitleBar);
        fileDetailsDialog.setContentView(R.layout.custom_file_details_dialog);
        final TextView lblFileName = (TextView) fileDetailsDialog.findViewById(R.id.id_name);
        final TextView lblFilePath = (TextView) fileDetailsDialog.findViewById(R.id.id_path);
        final TextView lblSize = (TextView) fileDetailsDialog.findViewById(R.id.id_size);
        final TextView lblCreateAt = (TextView) fileDetailsDialog.findViewById(R.id.id_create_at);

        File file = new File(filePath);
        lblFileName.setText("Name : " + file.getName());
        lblFilePath.setText("Path : " + filePath);

        if (file.isDirectory()) {
            int subFolders = file.list().length;
            lblSize.setText("items : " + subFolders);
        } else {
            long length = file.length();
            length = length / 1024;
            if (length >= 1024) {
                length = length / 1024;
                lblSize.setText("Size : " + length + " MB");
            } else {
                lblSize.setText("Size : " + length + " KB");
            }
        }
        Date lastModDate = new Date(file.lastModified());
        lblCreateAt.setText("Created on : " + Utils.convertTimeFromUnixTimeStamp(lastModDate.toString()));

        Button btnOkay = (Button) fileDetailsDialog.findViewById(R.id.btn_okay);
        btnOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lblFileName.setText("");
                lblFilePath.setText("");
                lblSize.setText("");
                lblCreateAt.setText("");
                fileDetailsDialog.dismiss();
            }
        });
        fileDetailsDialog.show();
    }

    /**
     * Raturns all available SD-Cards in the system (include emulated)
     * <p>
     * Warning: Hack! Based on Android source code of version 4.3 (API 18)
     * Because there is no standart way to get it.
     * TODO: Test on future Android versions 4.4+
     *
     * @return paths to all available SD-Cards in the system (include emulated)
     */
    public static String[] getStorageDirectories() {
        // Final set of paths
        final Set<String> rv = new HashSet<String>();
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
                final String[] folders = DIR_SEPORATOR.split(path);
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
        return rv.toArray(new String[rv.size()]);
    }

    public static String getMimeType(String filePath) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(filePath);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public long folderSize(File directory) {
        long length = 0;
        try {
            for (File file : directory.listFiles()) {

                if (file.isFile())
                    length += file.length();
                else
                    length += folderSize(file);
            }
        } catch (Exception e) {
        }
        return length;
    }

    public String getAvailableExternalMemorySize() {
        if (android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED)) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            @SuppressWarnings("deprecation") long blockSize = stat.getBlockSize();
            @SuppressWarnings("deprecation") long availableBlocks = stat.getAvailableBlocks();
            return formatSize(availableBlocks * blockSize, "free");
        } else {
            return "0";
        }
    }

    /**
     * This will return a string of the current directory path
     *
     * @return the current directory
     */
    public String getCurrentDir() {
        return mPathStack.peek();
    }

    /**
     * This will return a string of the current home path.
     *
     * @return the home directory
     */
    public ArrayList<String> setHomeDir(String name) {
        //This will eventually be placed as a settings item
        mPathStack.clear();
        mPathStack.push("/");
        mPathStack.push(name);

        return populate_list();
    }

    /**
     * This will determine if hidden files and folders will be visible to the
     * user.
     *
     * @param choice true if user is veiwing hidden files, false otherwise
     */
    public void setShowHiddenFiles(boolean choice) {
        mShowHiddenFiles = choice;
    }

    /**
     * @param type
     */
    public void setSortType(int type) {
        mSortType = type;
    }

    /**
     * This will return a string that represents the path of the previous path
     *
     * @return returns the previous path
     */
    public ArrayList<String> getPreviousDir() {
        int size = mPathStack.size();

        if (size >= 2)
            mPathStack.pop();

        else if (size == 0)
            mPathStack.push("/");

        if (SMBPath.equals(""))
            return populate_list();
        else
            return populate_list_smb();
    }

    public boolean isSmb() {
        if (SMBPath.equals(""))
            return false;
        else
            return true;
    }

    /**
     * @param path
     * @param isFullPath
     * @return
     */
    public ArrayList<String> getNextDir(String path, boolean isFullPath) {
        int size = mPathStack.size();

        if (!path.equals(mPathStack.peek()) && !isFullPath) {
            if (size == 1) {
                if (SMBPath.equals(""))
                    mPathStack.push("/" + path);
                else
                    mPathStack.push(path + "/");
            } else {
                if (SMBPath.equals(""))
                    mPathStack.push(mPathStack.peek() + "/" + path);
                else
                    mPathStack.push(mPathStack.peek() + path + "/");
            }
        } else if (!path.equals(mPathStack.peek()) && isFullPath) {
            mPathStack.push(path);
        }

        if (SMBPath.equals(""))
            return populate_list();
        else
            return populate_list_smb();
    }

    /**
     * @param old    the file to be copied
     * @param newDir the directory to move the file to
     * @return
     */
    public int copyToDirectory(String old, String newDir) {

        if (isSmb())
            return copyToDirectorySMB(old, newDir);
        else
            return copyToDirectoryNormal(old, newDir);

    }

    private int copyToDirectoryNormal(String old, String newDir) {
        File old_file = new File(old); //source file
        File temp_dir = new File(newDir); //targetfile

        byte[] data = new byte[BUFFER];
        int read = 0;

        if (old_file.isFile() && temp_dir.isDirectory() && temp_dir.canWrite()) {

            String file_name = old.substring(old.lastIndexOf("/"), old.length());
            File cp_file = new File(newDir + file_name);

            try {
                BufferedOutputStream o_stream = new BufferedOutputStream(
                        new FileOutputStream(cp_file));
                BufferedInputStream i_stream = new BufferedInputStream(
                        new FileInputStream(old_file));

                while ((read = i_stream.read(data, 0, BUFFER)) != -1)
                    o_stream.write(data, 0, read);

                o_stream.flush();
                i_stream.close();
                o_stream.close();

            } catch (FileNotFoundException e) {
                Log.e("FileNotFoundException", e.getMessage());
                return -1;

            } catch (IOException e) {
                Log.e("IOException", e.getMessage());
                return -1;
            }

        } else if (old_file.isDirectory() && temp_dir.isDirectory() && temp_dir.canWrite()) {
            String files[] = old_file.list();
            String dir = newDir + old.substring(old.lastIndexOf("/"), old.length()); //target
            int len = files.length;

            if (!new File(dir).mkdir())
                return -1;

            for (int i = 0; i < len; i++)
                copyToDirectory(old + "/" + files[i], dir);

        } else if (!temp_dir.canWrite())
            return -1;

        return 0;
    }

    private int copyToDirectorySMB(String old, String newDir) {
        try {
            SmbFile old_file = new SmbFile(old);
            SmbFile temp_dir = new SmbFile(newDir);

            byte[] data = new byte[BUFFER];
            int read = 0;

            if (old_file.isFile() && temp_dir.isDirectory() && temp_dir.canWrite()) {
                String file_name = old.substring(old.lastIndexOf("/"), old.length());
                SmbFile cp_file = new SmbFile(newDir + file_name);

                try {

                    BufferedOutputStream o_stream = new BufferedOutputStream(
                            cp_file.getOutputStream());
                    BufferedInputStream i_stream = new BufferedInputStream(
                            old_file.getInputStream());

                    while ((read = i_stream.read(data, 0, BUFFER)) != -1)
                        o_stream.write(data, 0, read);

                    o_stream.flush();
                    i_stream.close();
                    o_stream.close();

                } catch (FileNotFoundException e) {
                    Log.e("FileNotFoundException", e.getMessage());
                    return -1;

                } catch (IOException e) {
                    Log.e("IOException", e.getMessage());
                    return -1;
                }

            } else if (old_file.isDirectory() && temp_dir.isDirectory() && temp_dir.canWrite()) {
                String files[] = old_file.list();
                String dir = newDir + old.substring(old.lastIndexOf("/"), old.length());
                int len = files.length;

                if (!new SmbFile(dir).exists()) {
                    new SmbFile(dir).mkdir();
                    if (!new SmbFile(dir).exists())
                        return -1;
                }

                for (int i = 0; i < len; i++)
                    copyToDirectory(old + "/" + files[i], dir);

            } else if (!temp_dir.canWrite())
                return -1;

        } catch (Exception e) {
            return -1;
        }
        return 0;
    }

    /**
     * @param zipName
     * @param toDir
     * @param fromDir
     */
    public void extractZipFilesFromDir(String zipName, String toDir, String fromDir) {
        if (!(toDir.charAt(toDir.length() - 1) == '/'))
            toDir += "/";
        if (!(fromDir.charAt(fromDir.length() - 1) == '/'))
            fromDir += "/";

        String org_path = fromDir + zipName;

        extractZipFiles(org_path, toDir);
    }

    /**
     * @param zip_file
     * @param directory
     */
    public void extractZipFiles(String zip_file, String directory) {
        byte[] data = new byte[BUFFER];
        String name, path, zipDir;
        ZipEntry entry;
        ZipInputStream zipstream;

        if (!(directory.charAt(directory.length() - 1) == '/'))
            directory += "/";

        if (zip_file.contains("/")) {
            path = zip_file;
            name = path.substring(path.lastIndexOf("/") + 1,
                    path.length() - 4);
            zipDir = directory + name + "/";

        } else {
            path = directory + zip_file;
            name = path.substring(path.lastIndexOf("/") + 1,
                    path.length() - 4);
            zipDir = directory + name + "/";
        }

        new File(zipDir).mkdir();

        try {
            zipstream = new ZipInputStream(new FileInputStream(path));

            while ((entry = zipstream.getNextEntry()) != null) {
                String buildDir = zipDir;
                String[] dirs = entry.getName().split("/");

                if (dirs != null && dirs.length > 0) {
                    for (int i = 0; i < dirs.length - 1; i++) {
                        buildDir += dirs[i] + "/";
                        new File(buildDir).mkdir();
                    }
                }

                int read = 0;
                FileOutputStream out = new FileOutputStream(
                        zipDir + entry.getName());
                while ((read = zipstream.read(data, 0, BUFFER)) != -1)
                    out.write(data, 0, read);

                zipstream.closeEntry();
                out.close();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param path
     */
    public void createZipFile(String path) {
        File dir = new File(path);
        String[] list = dir.list();
        String name = path.substring(path.lastIndexOf("/"), path.length());
        String _path;

        if (!dir.canRead() || !dir.canWrite())
            return;

        int len = list.length;

        if (path.charAt(path.length() - 1) != '/')
            _path = path + "/";
        else
            _path = path;

        try {
            ZipOutputStream zip_out = new ZipOutputStream(
                    new BufferedOutputStream(
                            new FileOutputStream(_path + name + ".zip"), BUFFER));

            for (int i = 0; i < len; i++)
                zip_folder(new File(_path + list[i]), zip_out);

            zip_out.close();

        } catch (FileNotFoundException e) {
            Log.e("File not found", e.getMessage());

        } catch (IOException e) {
            Log.e("IOException", e.getMessage());
        }
    }

    /**
     * @param filePath
     * @param newName
     * @return
     */
    public int renameTarget(String filePath, String newName) {
        File src = new File(filePath);
        String ext = "";
        File dest;

        if (src.isFile())
            /*get file extension*/
            ext = filePath.substring(filePath.lastIndexOf("."), filePath.length());

        if (newName.length() < 1)
            return -1;

        String temp = filePath.substring(0, filePath.lastIndexOf("/"));

        dest = new File(temp + "/" + newName + ext);
        if (src.renameTo(dest))
            return 0;
        else
            return -1;
    }

    /**
     * @param path
     * @param name
     * @return
     */
    public int createDir(String path, String name) {
        int len = path.length();

        if (len < 1 || len < 1)
            return -1;

        if (path.charAt(len - 1) != '/')
            path += "/";

        if (new File(path + name).mkdir())
            return 0;

        return -1;
    }

    public void deleteFiles(String path) {
        File file = new File(path);

        Context context = AppController.getInstance().getApplicationContext();
        ContentResolver contentResolver = context.getContentResolver();

        String canonicalPath;
        try {
            canonicalPath = file.getCanonicalPath();
        } catch (IOException e) {
            canonicalPath = file.getAbsolutePath();
        }
        final Uri uri = MediaStore.Files.getContentUri("external");
        final int result = contentResolver.delete(uri,
                MediaStore.Files.FileColumns.DATA + "=?", new String[]{canonicalPath});
        if (result == 0) {
            final String absolutePath = file.getAbsolutePath();
            if (!absolutePath.equals(canonicalPath)) {
                contentResolver.delete(uri,
                        MediaStore.Files.FileColumns.DATA + "=?", new String[]{absolutePath});
            }
        }
    }

    /* public int deleteTarget(String path) { //Working Function
        File target = new File(path);

        if (target.exists() && target.isFile() && target.canWrite()) {
            target.delete();
            return 0;
        } else if (target.exists() && target.isDirectory() && target.canRead()) {
            String[] file_list = target.list();

            if (file_list != null && file_list.length == 0) {
                target.delete();
                return 0;

            } else if (file_list != null && file_list.length > 0) {

                for (int i = 0; i < file_list.length; i++) {
                    File temp_f = new File(target.getAbsolutePath() + "/" + file_list[i]);

                    if (temp_f.isDirectory())
                        deleteTarget(temp_f.getAbsolutePath());
                    else if (temp_f.isFile())
                        temp_f.delete();
                }
            }
            if (target.exists())
                if (target.delete())
                    return 0;
        }
        return -1;
    }*/

    /**
     * The full path name of the file to delete.
     *
     * @param path name
     * @return
     */
    public int deleteTarget(String path) {
        File target = new File(path);

        if (target.exists() && target.isFile() && target.canWrite()) {
            deleteFiles(path);
            target.delete();
            return 0;
        } else if (target.exists() && target.isDirectory() && target.canRead()) {
            String[] file_list = target.list();

            if (file_list != null && file_list.length == 0) {
                target.delete();
                return 0;

            } else if (file_list != null && file_list.length > 0) {

                for (int i = 0; i < file_list.length; i++) {
                    File temp_f = new File(target.getAbsolutePath() + "/" + file_list[i]);

                    if (temp_f.isDirectory())
                        deleteTarget(temp_f.getAbsolutePath());
                    else if (temp_f.isFile()) {
                        deleteFiles(temp_f.getPath());
                        temp_f.delete();
                    }
                }
            }
            if (target.exists())
                if (target.delete())
                    return 0;
        }
        return -1;
    }

    /**
     * @param name
     * @return
     */
    public boolean isDirectory(String name) {
        return new File(mPathStack.peek() + "/" + name).isDirectory();
    }

    /**
     * @param dir
     * @param pathName
     * @return
     */
    public ArrayList<String> searchInDirectory(String dir, String pathName) {
        ArrayList<String> names = new ArrayList<String>();
        search_file(dir, pathName, names);

        return names;
    }

    /**
     * @param path
     * @return
     */
    public long getDirSize(String path) {
        get_dir_size(new File(path));

        return mDirSize;
    }

    /* (non-Javadoc)
     * this function will take the string from the top of the directory stack
     * and list all files/folders that are in it and return that list so
     * it can be displayed. Since this function is called every time we need
     * to update the the list of files to be shown to the user, this is where
     * we do our sorting (by type, alphabetical, etc).
     *
     * @return
     */
    private ArrayList<String> populate_list() {

        if (!mDirContent.isEmpty())
            mDirContent.clear();
        try {
            File file = new File(mPathStack.peek());

            if (file.exists() && file.canRead()) {
                String[] list = file.list();
                int len = list.length;

			/* add files/folder to arraylist depending on hidden status */
                for (int i = 0; i < len; i++) {
                    if (!mShowHiddenFiles) {
                        if (list[i].toString().charAt(0) != '.')
                            mDirContent.add(list[i]);

                    } else {
                        mDirContent.add(list[i]);
                    }
                }

			/* sort the arraylist that was made from above for loop */
                switch (mSortType) {
                    case SORT_NONE:
                        //no sorting needed
                        break;

                    case SORT_ALPHA:
                        Object[] tt = mDirContent.toArray();
                        mDirContent.clear();

                        Arrays.sort(tt, alph);

                        for (Object a : tt) {
                            mDirContent.add((String) a);
                        }
                        break;

                    case SORT_SIZE:
                        int index = 0;
                        Object[] size_ar = mDirContent.toArray();
                        String dir = mPathStack.peek();

                        Arrays.sort(size_ar, size);

                        mDirContent.clear();
                        for (Object a : size_ar) {
                            if (new File(dir + "/" + (String) a).isDirectory())
                                mDirContent.add(index++, (String) a);
                            else
                                mDirContent.add((String) a);
                        }
                        break;

                    case SORT_TYPE:
                        int dirindex = 0;
                        Object[] type_ar = mDirContent.toArray();
                        String current = mPathStack.peek();

                        Arrays.sort(type_ar, type);
                        mDirContent.clear();

                        for (Object a : type_ar) {
                            if (new File(current + "/" + (String) a).isDirectory())
                                mDirContent.add(dirindex++, (String) a);
                            else
                                mDirContent.add((String) a);
                        }
                        break;
                }

            } else {
                mDirContent.add("Emtpy");
            }
        } catch (Exception e) {
            Log.d("List Error", e.getMessage());

        }
        return mDirContent;
    }

    private ArrayList<String> populate_list_smb() {

        if (!mDirContent.isEmpty())
            mDirContent.clear();
        try {
            SmbFile[] mFile = FileUtil.getSmbFile(mPathStack.peek(), 5000).listFiles();
            for (int i = 0; i < mFile.length; i++) {

                String name = mFile[i].getName();
                name = (mFile[i].isDirectory() && name.endsWith("/")) ? name.substring(0, name.length() - 1) : name;
                if (SMBPath.equals(mPathStack.peek())) {
                    if (name.endsWith("$")) continue;
                }
                if (mFile[i].isDirectory()) {
                    mDirContent.add(name);
                    // Log.d("Folder", name + " Path :- " + mFile[i].getPath() + " Time :-  " + mFile[i].lastModified());
                  /*Layoutelements layoutelements = new Layoutelements(folder, name, mFile[i].getPath(), "", "", "", 0, false, mFile[i].lastModified() + "", true);
                    layoutelements.setMode(1);
                    searchHelper.add(layoutelements.generateBaseFile());
                    a.add(layoutelements);*/
                } else {

                    try {
                        mDirContent.add(name);
                        //  Log.d("File", name + " Path" + mFile[i].getPath() + " Time :-  " + mFile[i].lastModified());
                      /*Layoutelements layoutelements = new Layoutelements(Icons.loadMimeIcon(getActivity(), mFile[i].getPath(), !IS_LIST, res), name, mFile[i].getPath(), "", "", utils.readableFileSize(mFile[i].length()), mFile[i].length(), false, mFile[i].lastModified() + "", false);
                        layoutelements.setMode(1);
                        searchHelper.add(layoutelements.generateBaseFile());
                        a.add(layoutelements);*/
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }


        } catch (Exception e) {
            Log.d("List Error", e.getMessage());

        }
        return mDirContent;
    }

    /*
     * Zips a file at a location and places the resulting zip file at the toLocation
     * Example: zipFileAtPath("downloads/myfolder", "downloads/myFolder.zip");
     */

    /*
     * @param file
     * @param zout
     * @throws IOException
     */
    private void zip_folder(File file, ZipOutputStream zout) throws IOException {
        byte[] data = new byte[BUFFER];
        int read;

        if (file.isFile()) {
            ZipEntry entry = new ZipEntry(file.getName());
            zout.putNextEntry(entry);
            BufferedInputStream instream = new BufferedInputStream(
                    new FileInputStream(file));

            while ((read = instream.read(data, 0, BUFFER)) != -1)
                zout.write(data, 0, read);

            zout.closeEntry();
            instream.close();

        } else if (file.isDirectory()) {
            String[] list = file.list();
            int len = list.length;

            for (int i = 0; i < len; i++)
                zip_folder(new File(file.getPath() + "/" + list[i]), zout);
        }
    }

    /*
     * @param path
     */
    private void get_dir_size(File path) {
        File[] list = path.listFiles();
        int len;

        if (list != null) {
            len = list.length;

            for (int i = 0; i < len; i++) {
                try {
                    if (list[i].isFile() && list[i].canRead()) {
                        mDirSize += list[i].length();

                    } else if (list[i].isDirectory() && list[i].canRead() && !isSymlink(list[i])) {
                        get_dir_size(list[i]);
                    }
                } catch (IOException e) {
                    Log.e("IOException", e.getMessage());
                }
            }
        }
    }

    /*
     * (non-JavaDoc)
     * I dont like this method, it needs to be rewritten. Its hacky in that
     * if you are searching in the root dir (/) then it is not going to be treated
     * as a recursive method so the user dosen't have to sit forever and wait.
     *
     * I will rewrite this ugly method.
     *
     * @param dir		directory to search in
     * @param fileName	filename that is being searched for
     * @param n			ArrayList to populate results
     */
    private void search_file(String dir, String fileName, ArrayList<String> n) {
        File root_dir = new File(dir);
        String[] list = root_dir.list();

        if (list != null && root_dir.canRead()) {
            int len = list.length;

            for (int i = 0; i < len; i++) {
                File check = new File(dir + "/" + list[i]);
                String name = check.getName();

                if (check.isFile() && name.toLowerCase().
                        contains(fileName.toLowerCase())) {
                    n.add(check.getPath());
                } else if (check.isDirectory()) {
                    if (name.toLowerCase().contains(fileName.toLowerCase()))
                        n.add(check.getPath());

                    else if (check.canRead() && !dir.equals("/"))
                        search_file(check.getAbsolutePath(), fileName, n);
                }
            }
        }
    }

    public boolean zipFileAtPath(String sourcePath, String toLocation) {
        final int BUFFER = 2048;

       /* String name = toLocation.substring(toLocation.lastIndexOf("/"), toLocation.length());
        String _path;
        if (toLocation.charAt(toLocation.length() - 1) != '/')
            _path = toLocation ;
        else
            _path = toLocation;

        toLocation = _path + name + ".zip";*/

        File sourceFile = new File(sourcePath);
        try {
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(toLocation);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
                    dest));
            if (sourceFile.isDirectory()) {
                zipSubFolder(out, sourceFile, sourceFile.getParent().length());
            } else {
                byte data[] = new byte[BUFFER];
                FileInputStream fi = new FileInputStream(sourcePath);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(getLastPathComponent(sourcePath));
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /*
     * Zips a subfolder
     */
    private void zipSubFolder(ZipOutputStream out, File folder,
                              int basePathLength) throws IOException {

        final int BUFFER = 2048;

        File[] fileList = folder.listFiles();
        BufferedInputStream origin = null;
        for (File file : fileList) {
            if (file.isDirectory()) {
                zipSubFolder(out, file, basePathLength);
            } else {
                byte data[] = new byte[BUFFER];
                String unmodifiedFilePath = file.getPath();
                String relativePath = unmodifiedFilePath
                        .substring(basePathLength);
                FileInputStream fi = new FileInputStream(unmodifiedFilePath);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(relativePath);
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }
        }
    }

    /*
     * gets the last path component
     * Example: getLastPathComponent("downloads/example/fileToZip");
     * Result: "fileToZip"
     */
    public String getLastPathComponent(String filePath) {
        String[] segments = filePath.split("/");
        if (segments.length == 0)
            return "";
        String lastPathComponent = segments[segments.length - 1];
        return lastPathComponent;
    }
}
