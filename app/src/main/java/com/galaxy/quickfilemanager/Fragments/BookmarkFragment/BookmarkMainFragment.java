package com.galaxy.quickfilemanager.Fragments.BookmarkFragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.galaxy.quickfilemanager.Fragments.StorageFragment.OpenFiles;
import com.galaxy.quickfilemanager.Model.BookmarkModel;
import com.galaxy.quickfilemanager.Utils.AppController;
import com.galaxy.quickfilemanager.Utils.RecyclerTouchListener;
import com.galaxy.quickfilemanager.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Umiya Mataji on 1/24/2017.
 */

public class BookmarkMainFragment extends Fragment {

    Context context;
    String path = "", name = "";

    private List<BookmarkModel> bookmarks = new ArrayList<>();

    private RecyclerView recycler_view_bookmark_list;
    private LinearLayout noBookmarkLayout;
    private BookmarkListAdapter bookmarkListAdapter;

    private ProgressBar lan_loader;

    private BookmarkListener bookmarkListener;
    private BookmarkUtils bookmarkUtils;
    private boolean mReturnIntent = false;
    private LinearLayout hidden_layout, hidden_cancel, hidden_delete;
    private OpenFiles openFiles;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lan_layout, container, false);
        context = getActivity();
        setHasOptionsMenu(true);
        init(view);

        openFiles = new OpenFiles(context);
        openFiles.enableZipOptions(false);

        return view;
    }

    public void init(View view) {

        bookmarkUtils = new BookmarkUtils(getActivity());

        if (getActivity() instanceof BookmarkListener) {
            bookmarkListener = (BookmarkListener) getActivity();
        }

        hidden_layout = (LinearLayout) view.findViewById(R.id.hidden_layout);
        hidden_cancel = (LinearLayout) view.findViewById(R.id.hidden_cancel);
        hidden_delete = (LinearLayout) view.findViewById(R.id.hidden_delete);

        hidden_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT).show();
                bookmarkListAdapter.killMultiSelect(true);
            }
        });

        hidden_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean multi_select = bookmarkListAdapter.hasMultiSelectData();
                if (multi_select) {

                    for (String path : bookmarkListAdapter.mMultiSelectData)
                        bookmarkUtils.removeBookmark(path);

                    bookmarkListAdapter.killMultiSelect(true);
                    loadBookmarkData();
                }
            }
        });


        lan_loader = (ProgressBar) view.findViewById(R.id.lan_loader);
        lan_loader.setVisibility(View.GONE);

        recycler_view_bookmark_list = (RecyclerView) view.findViewById(R.id.recycler_view_lan_list);
        RecyclerView.LayoutManager mAudioLayoutManager = new LinearLayoutManager(context);
        recycler_view_bookmark_list.setLayoutManager(mAudioLayoutManager);
        recycler_view_bookmark_list.setItemAnimator(new DefaultItemAnimator());

        noBookmarkLayout = (LinearLayout) view.findViewById(R.id.noBookmarkLayout);

        loadBookmarkData();

        recycler_view_bookmark_list.addOnItemTouchListener(new RecyclerTouchListener(AppController.getInstance().getApplicationContext(), recycler_view_bookmark_list, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                RecyclerClick(view, position);
            }

            @Override
            public void onLongClick(View view, int position) {

                if (bookmarkListAdapter.multi_select_flag) {
                    bookmarkListAdapter.killMultiSelect(true);
                } else {
                    bookmarkListAdapter.multi_select_flag = true;
                    hidden_layout.setVisibility(LinearLayout.VISIBLE);
                    // bookmarkListAdapter.killMultiSelect(false);
                }

                bookmarkListAdapter.notifyDataSetChanged();
                RecyclerClick(view, position);
            }

        }));
    }


    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    public void RecyclerClick(View view, int position) {
        boolean multiSelect = bookmarkListAdapter.isMultiSelected();

        if (multiSelect) {
            bookmarkListAdapter.addMultiPosition(position, bookmarks.get(position).getFilePath());
        } else {

            if (bookmarkListener != null) {
                if (bookmarks.get(position).isDirectory())
                    bookmarkListener.openBookmark(bookmarks.get(position).getFilePath());
                else {
                    openFiles.open(bookmarks.get(position).getFilePath());
                }
            }
        }
    }

    /*(non Java-Doc)
    * Returns the file that was selected to the intent that
    * called this activity. usually from the caller is another application.
    */
    private void returnIntentResults(File data) {
        mReturnIntent = false;

        Intent ret = new Intent();
        Uri uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", data);
        ret.setData(uri);
        getActivity().setResult(getActivity().RESULT_OK, ret);
        getActivity().finish();
    }

    public void loadBookmarkData() {
        bookmarks = new ArrayList<>();
        bookmarks.clear();
        bookmarks = bookmarkUtils.getBookmarkedList();

        if (bookmarks != null) {
            if (bookmarks.size() > 0) {
                noBookmarkLayout.setVisibility(View.GONE);
                recycler_view_bookmark_list.setVisibility(View.VISIBLE);


                bookmarkListAdapter = new BookmarkListAdapter(bookmarks);
                recycler_view_bookmark_list.setHasFixedSize(true);
                recycler_view_bookmark_list.setAdapter(bookmarkListAdapter);

                bookmarkListAdapter.setOperationLayout(hidden_layout);

            } else {
                noBookmarkLayout.setVisibility(View.VISIBLE);
                recycler_view_bookmark_list.setVisibility(View.GONE);
            }
        }else{
            noBookmarkLayout.setVisibility(View.VISIBLE);
            recycler_view_bookmark_list.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(bookmarkListAdapter != null)
        bookmarkListAdapter.stopThumbnailThread();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.lan_computer_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_refresh:
                loadBookmarkData();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public interface BookmarkListener {
        void openBookmark(String path);
    }

}
