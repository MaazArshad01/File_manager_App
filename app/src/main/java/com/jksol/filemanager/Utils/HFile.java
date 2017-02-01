package com.jksol.filemanager.Utils;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

/**
 * Created by Umiya Mataji on 1/30/2017.
 */

public class HFile implements Parcelable {

    public static final int ROOT_MODE = 3, LOCAL_MODE = 0, SMB_MODE = 1, UNKNOWN = -1;
    public static final Creator<HFile> CREATOR = new Creator<HFile>() {
        @Override
        public HFile createFromParcel(Parcel in) {
            return new HFile(in);
        }

        @Override
        public HFile[] newArray(int size) {
            return new HFile[size];
        }
    };
    int mode = 0;
    int countFile = 0;
    int countFolder = 0;
    int totalFileFolder[] = new int[2];
    private Context mContext;
    private String path;

    public HFile() {
        mContext = AppController.getInstance().getApplicationContext();
    }

    public HFile(int mode, String path, String name, boolean isDirectory) {
        this.mode = mode;
        if (path.startsWith("smb://") || isSmb()) {
            if (!isDirectory) this.path = path + name;
            else if (!name.endsWith("/")) this.path = path + name + "/";
            else this.path = path + name;
        } else this.path = path + "/" + name;
    }

    protected HFile(Parcel in) {
        mode = in.readInt();
        path = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mode);
        dest.writeString(path);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    File getFile() {
        return new File(path);
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public boolean isLocal() {
        return mode == LOCAL_MODE;
    }

    public boolean isRoot() {
        return mode == ROOT_MODE;
    }

    public boolean isSmb() {
        return mode == SMB_MODE;
    }

    public SmbFile getSmbFile(int timeout) {
        try {
            SmbFile smbFile = new SmbFile(path);
            smbFile.setConnectTimeout(timeout);
            return smbFile;
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public SmbFile getSmbFile() {
        try {
            return new SmbFile(path);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public long folderSize() {
        long size = 0l;
        if (isSmb()) {
            try {
                String pth = "";
                if (!path.endsWith("/"))
                    pth = path + "/";
                else
                    pth = path;

                size = new Futils().folderSize(new SmbFile(pth));
            } catch (MalformedURLException e) {
                size = 0l;
                e.printStackTrace();
            }
        } else
            size = new Futils().folderSize(new File(path));
        return size;
    }

    public String getName() {
        String name = null;
        switch (mode) {
            case SMB_MODE:
                SmbFile smbFile = getSmbFile();
                if (smbFile != null)
                    return smbFile.getName();
                break;
            case LOCAL_MODE:
                return new File(path).getName();
            case ROOT_MODE:
                return new File(path).getName();
        }
        return name;
    }

    public String[] list() {
        String name[] = null;
        switch (mode) {
            case SMB_MODE:
                SmbFile smbFile = getSmbFile();
                if (smbFile != null)
                    try {
                        return smbFile.list();
                    } catch (SmbException e) {
                        e.printStackTrace();
                    }
                break;
            case LOCAL_MODE:
                return new File(path).list();
            case ROOT_MODE:
                return new File(path).list();
        }
        return name;
    }

    public boolean isDirectory() {
        boolean isDirectory = false;
        if (isSmb()) {
            try {
                isDirectory = new SmbFile(path).isDirectory();
            } catch (SmbException e) {
                isDirectory = false;
                e.printStackTrace();
            } catch (MalformedURLException e) {
                isDirectory = false;
                e.printStackTrace();
            }
        } else if (isLocal()) isDirectory = new File(path).isDirectory();

        return isDirectory;
    }

    public boolean isFile() {
        boolean isFile = false;
        if (isSmb()) {
            try {
                isFile = new SmbFile(path).isFile();
            } catch (SmbException e) {
                isFile = false;
                e.printStackTrace();
            } catch (MalformedURLException e) {
                isFile = false;
                e.printStackTrace();
            }
        } else if (isLocal()) isFile = new File(path).isFile();

        return isFile;
    }

    public boolean isDirectory(String path) {
        boolean isDirectory = false;
        if (isSmb()) {
            try {
                isDirectory = new SmbFile(path).isDirectory();
            } catch (SmbException e) {
                isDirectory = false;
                e.printStackTrace();
            } catch (MalformedURLException e) {
                isDirectory = false;
                e.printStackTrace();
            }
        } else if (isLocal()) isDirectory = new File(path).isDirectory();

        return isDirectory;
    }

    public long lastModified() throws MalformedURLException, SmbException {
        switch (mode) {
            case SMB_MODE:
                SmbFile smbFile = getSmbFile();
                if (smbFile != null)
                    return smbFile.lastModified();
                break;
            case LOCAL_MODE:
                new File(path).lastModified();
                break;
            /*case ROOT_MODE:
                BaseFile baseFile=generateBaseFileFromParent();
                if(baseFile!=null)
                    return baseFile.getDate();*/
        }
        return new File("/").lastModified();
    }

    public long length() {
        long s = 0l;
        switch (mode) {
            case SMB_MODE:
                SmbFile smbFile = getSmbFile();
                if (smbFile != null)
                    try {
                        s = smbFile.length();
                    } catch (SmbException e) {
                    }
                return s;
            case LOCAL_MODE:
                s = new File(path).length();
                return s;
           /* case ROOT_MODE:
                BaseFile baseFile=generateBaseFileFromParent();
                if(baseFile!=null)
                    return baseFile.getSize();*/
        }
        return s;
    }

    public long getUsableSpace() {
        long size = 0l;
        if (isSmb()) {
            try {
                size = (new SmbFile(path).getDiskFreeSpace());
            } catch (MalformedURLException e) {
                size = 0l;
                e.printStackTrace();
            } catch (SmbException e) {
                size = 0l;
                e.printStackTrace();
            }
        } else
            size = (new File(path).getUsableSpace());
        return size;
    }

    public InputStream getInputStream() {
        InputStream inputStream = null;
        if (isSmb()) {
            try {
                inputStream = new SmbFile(path).getInputStream();
            } catch (IOException e) {
                inputStream = null;
                e.printStackTrace();
            }
        } else {
            try {
                inputStream = new FileInputStream(path);
            } catch (FileNotFoundException e) {
                inputStream = null;
                e.printStackTrace();
            }
        }
        return inputStream;
    }

    public OutputStream getOutputStream(Context context) {
        OutputStream inputStream = null;
        if (isSmb()) {
            try {
                inputStream = new SmbFile(path).getOutputStream();
            } catch (IOException e) {
                inputStream = null;
                e.printStackTrace();
            }
        } else {
            try {
                inputStream = FileUtil.getOutputStream(new File(path), context, length());
            } catch (Exception e) {
                inputStream = null;
            }

        }
        return inputStream;
    }

    public boolean exists() {
        boolean exists = false;
        if (isSmb()) {
            try {
                SmbFile smbFile = getSmbFile(2000);
                exists = smbFile != null ? smbFile.exists() : false;
            } catch (SmbException e) {
                exists = false;
            }
        } else if (isLocal()) exists = new File(path).exists();

        return exists;
    }

    public void mkdir(Context context) {
        if (isSmb()) {
            try {
                new SmbFile(path).mkdirs();
            } catch (SmbException e) {
                // Logger.log(e,path,context);
            } catch (MalformedURLException e) {
                //  Logger.log(e,path,context);
            }
        } else
            FileUtil.mkdir(new File(path), context);
    }

    public boolean delete(Context context) {
        if (isSmb()) {
            try {
                new SmbFile(path).delete();
            } catch (SmbException e) {
                // Logger.log(e,path,context);
            } catch (MalformedURLException e) {
                // Logger.log(e,path,context);
            }
        } else {
            boolean b = FileUtil.deleteFile(new File(path), context);
            /*if(!b && rootmode){
                setMode(ROOT_MODE);
                RootTools.remount(getParent(),"rw");
                String s=RootHelper.runAndWait("rm -r \""+getPath()+"\"",true);
                RootTools.remount(getParent(),"ro");
            }*/

        }
        return !exists();
    }

    public String getFilePermissions(File file) {
        String per = "-";

        if (file.isDirectory())
            per += "d";
        if (file.canRead())
            per += "r";
        if (file.canWrite())
            per += "w";

        return per;
    }

    public boolean canWrite() {
        boolean canWrite = false;
        if (isSmb()) {
            try {
                canWrite = new SmbFile(path).canWrite();
            } catch (SmbException e) {
                canWrite = false;
                e.printStackTrace();
            } catch (MalformedURLException e) {
                canWrite = false;
                e.printStackTrace();
            }
        } else if (isLocal()) canWrite = new File(path).canWrite();

        return canWrite;
    }

    public boolean canRead() {
        boolean canRead = false;
        if (isSmb()) {
            try {
                canRead = new SmbFile(path).canRead();
            } catch (SmbException e) {
                canRead = false;
                e.printStackTrace();
            } catch (MalformedURLException e) {
                canRead = false;
                e.printStackTrace();
            }
        } else if (isLocal()) canRead = new File(path).canRead();

        return canRead;
    }

    public String[] getListFiles() {
        String files[];
        if (isSmb()) {

            try {
                SmbFile smbFile = new SmbFile(path);
                files = smbFile.list();
                return files;
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            try {
                File smbFile = new File(path);
                files = smbFile.list();
                return files;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public int[] countFolderFile() {
        int countFile = 0;
        int countFolder = 0;
        int totalFileFolder[] = new int[2];

        if (isSmb()) {
            try {
                SmbFile smbFile = new SmbFile(path);
                for (SmbFile file : smbFile.listFiles()) {

                    if (file.isFile())
                        countFile += 1;
                    else
                        countFolder += 1;
                }

                totalFileFolder = new int[]{countFile, countFolder};

            } catch (Exception e) {
                e.printStackTrace();
            }
            return totalFileFolder;

        } else if (isLocal()) {

            try {
                File files = new File(path);
                for (File file : files.listFiles()) {

                    if (file.isFile())
                        countFile += 1;
                    else
                        countFolder += 1;
                }
                totalFileFolder = new int[]{countFile, countFolder};
            } catch (Exception e) {
                e.printStackTrace();
            }
            return totalFileFolder;
        }

        return totalFileFolder;
    }

    public int[] countInnerFolderFile(String path) {

        if (isSmb()) {
            try {
                SmbFile smbFile = new SmbFile(path);
                if(smbFile.isDirectory()) {
                    for (SmbFile file : smbFile.listFiles()) {

                        if (file.isFile())
                            countFile += 1;
                        else {
                            countFolder += 1;
                            countInnerFolderFile(file.getPath());
                        }
                    }
                }else {
                    countFile += 1;
                }

                totalFileFolder = new int[]{countFile, countFolder};

            } catch (Exception e) {
                e.printStackTrace();
            }
            return totalFileFolder;

        } else if (isLocal()) {

            try {
                File files = new File(path);
                for (File file : files.listFiles()) {

                    if (file.isFile())
                        countFile += 1;
                    else {
                        countFolder += 1;
                        countInnerFolderFile(file.getPath());
                    }
                }
                totalFileFolder = new int[]{countFile, countFolder};
            } catch (Exception e) {
                e.printStackTrace();
            }
            return totalFileFolder;
        }

        return totalFileFolder;
    }

    public int deleteTarget(HFile hfile) {
        HFile target = hfile;

        if (target.exists() && target.isFile() && target.canWrite()) {
            deleteFiles(hfile);
            target.delete(mContext);
            return 0;

        } else if (target.exists() && target.isDirectory() && target.canRead()) {
            String[] file_list = target.list();

            if (file_list != null && file_list.length == 0) {
                target.delete(mContext);
                return 0;

            } else if (file_list != null && file_list.length > 0) {

                for (int i = 0; i < file_list.length; i++) {
                    HFile temp_f = new HFile();
                    temp_f.setMode(target.getMode());
                    temp_f.setPath(target.getAbsolutePath() + "/" + file_list[i]);
                    // File temp_f = new File(target.getAbsolutePath() + "/" + file_list[i]);

                    if (temp_f.isDirectory())
                        deleteTarget(temp_f);
                    else if (temp_f.isFile()) {
                        deleteFiles(temp_f);
                        temp_f.delete(mContext);
                    }

                }
            }

            if (target.exists())
                if (target.delete(mContext))
                    return 0;
        }
        return -1;
    }

    public void deleteFiles(HFile hfile) {
        HFile file = hfile;

        Context context = AppController.getInstance().getApplicationContext();
        ContentResolver contentResolver = context.getContentResolver();

        String canonicalPath;
        try {
            canonicalPath = file.getCanonicalPath();
        } catch (Exception e) {
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


    public String getCanonicalPath() {
        String CanonicalPath = "";
        switch (mode) {
            case SMB_MODE:
                SmbFile smbFile = getSmbFile();
                if (smbFile != null)
                    return smbFile.getCanonicalPath();
                break;
            case LOCAL_MODE:
                try {
                    return new File(path).getCanonicalPath();
                } catch (Exception e) {

                }
            case ROOT_MODE:
                try {
                    return new File(path).getCanonicalPath();
                } catch (Exception e) {
                }
        }
        return CanonicalPath;
    }

    public String getAbsolutePath() {
        String AbsolutePath = "";
        switch (mode) {
            case SMB_MODE:
                SmbFile smbFile = getSmbFile();
                if (smbFile != null)
                    return smbFile.getPath();
                break;
            case LOCAL_MODE:
                try {
                    return new File(path).getAbsolutePath();
                } catch (Exception e) {

                }
            case ROOT_MODE:
                try {
                    return new File(path).getAbsolutePath();
                } catch (Exception e) {
                }
        }
        return AbsolutePath;
    }
}
