package com.galaxy.quickfilemanager.Fragments.BookmarkFragment;

import android.content.Context;
import android.util.Log;

import com.galaxy.quickfilemanager.Model.BookmarkModel;
import com.galaxy.quickfilemanager.Utils.PreferencesUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Umiya Mataji on 2/9/2017.
 */

public class BookmarkUtils {

    private final Context mContext;

    Type bookmarkOfObjects = new TypeToken<List<BookmarkModel>>() {
    }.getType();

    ArrayList<BookmarkModel> mSelectedBookmarkList = new ArrayList<BookmarkModel>();
    Gson gson;

    public BookmarkUtils(Context mContext) {
        this.mContext = mContext;
        mSelectedBookmarkList.clear();
        gson = new Gson();
    }

    public void replaceBookmark(File oldFile, File newFile) {
        List<BookmarkModel> bookmarkedList = getBookmarkedList();

        if (bookmarkedList != null && bookmarkedList.size() > 0) {

            for (int i = 0; i < bookmarkedList.size(); i++) {

                if (bookmarkedList.get(i) != null && bookmarkedList.get(i).getFilePath().equalsIgnoreCase(oldFile.getPath())) {

                    BookmarkModel bookmarkModel = new BookmarkModel();

                    File file = newFile;
                    bookmarkModel.setFileName(file.getName());
                    bookmarkModel.setFilePath(file.getPath());

                    try {
                        long length = file.length();
                        length = length / 1024;
                        if (length >= 1024) {
                            length = length / 1024;
                            bookmarkModel.setFileSize(length + " MB");
                        } else {
                            bookmarkModel.setFileSize(length + " KB");
                        }

                        Date lastModDate = new Date(file.lastModified());
                        bookmarkModel.setFileCreatedTime(lastModDate.toString());
                        bookmarkModel.setFileCreatedTimeDate(lastModDate);

                        bookmarkModel.setDirectory(file.isDirectory());
                    } catch (Exception e) {
                        bookmarkModel.setFileSize("unknown");
                    }


                    bookmarkedList.set(i, bookmarkModel);
                }
            }
        }

        saveBookmarkedList(bookmarkedList);

    }

    public void removeBookmark(String path) {
        List<BookmarkModel> bookmarkedList = getBookmarkedList();

        if (bookmarkedList != null && bookmarkedList.size() > 0) {

            Iterator<BookmarkModel> iter = bookmarkedList.iterator();
            while (iter.hasNext()) {
                BookmarkModel str = iter.next();

                if (str != null && str.getFilePath().equalsIgnoreCase(path)) {
                    iter.remove();
                }
            }
        }

        saveBookmarkedList(bookmarkedList);
        Log.e("===>>>Remove new", "================================>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        for (BookmarkModel data : getBookmarkedList()) {
            Log.e("===>>>File Detail", "" + data.getFileName() + "\npath :- " + data.getFilePath());
        }
    }

    public void removeAllBookmark() {
        PreferencesUtils.removeValueFromPreference(mContext, "BookmarkedList");
    }

    public void addToBookmarkList(ArrayList<String> mMultiSelectData) {

        //  PreferencesUtils.removeValueFromPreference(mContext, "BookmarkedList");
        List<BookmarkModel> bookmarkedList = getBookmarkedList();
        mSelectedBookmarkList.clear();

        for (String path : mMultiSelectData) {

            if (bookmarkedList != null && bookmarkedList.size() > 0) {
                if (!containsPath(bookmarkedList, path)) {
                    addToBookmark(mSelectedBookmarkList, path);
                }

            } else {
                mSelectedBookmarkList.clear();
                addToBookmark(mSelectedBookmarkList, path);
            }
        }

        if (bookmarkedList == null)
            bookmarkedList = new ArrayList<>();

        bookmarkedList.addAll(mSelectedBookmarkList);
        saveBookmarkedList(bookmarkedList);

        /*Log.e("===>>>New", "=============================>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        for (BookmarkModel data : getBookmarkedList()) {
            Log.e("===>>>File Detail", "" + data.getFileName() + "\npath :- " + data.getFilePath());
        }*/
    }

    public boolean containsPath(Collection<BookmarkModel> c, String path) {
        for (BookmarkModel o : c) {
            if (o != null && o.getFilePath().equalsIgnoreCase(path)) {
                return true;
            }
        }
        return false;
    }

    private void addToBookmark(List<BookmarkModel> bookmarkedList, String path) {
        BookmarkModel bookmarkModel = new BookmarkModel();

        File file = new File(path);
        bookmarkModel.setFileName(file.getName());
        bookmarkModel.setFilePath(file.getPath());

        try {
            long length = file.length();
            length = length / 1024;
            if (length >= 1024) {
                length = length / 1024;
                bookmarkModel.setFileSize(length + " MB");
            } else {
                bookmarkModel.setFileSize(length + " KB");
            }

            Date lastModDate = new Date(file.lastModified());
            bookmarkModel.setFileCreatedTime(lastModDate.toString());
            bookmarkModel.setFileCreatedTimeDate(lastModDate);

            bookmarkModel.setDirectory(file.isDirectory());
        } catch (Exception e) {
            bookmarkModel.setFileSize("unknown");
        }

        bookmarkedList.add(bookmarkModel);
    }

    private void saveBookmarkedList(List<BookmarkModel> list) {
        String strObject = gson.toJson(list, bookmarkOfObjects);
        PreferencesUtils.saveToPreference(mContext, "BookmarkedList", strObject);
    }

    public List<BookmarkModel> getBookmarkedList() {
        String bookmarkedData = (String) PreferencesUtils.getValueFromPreference(mContext, String.class, "BookmarkedList", "");
        List<BookmarkModel> bookmarks = gson.fromJson(bookmarkedData, bookmarkOfObjects);
        return bookmarks;
    }
}
