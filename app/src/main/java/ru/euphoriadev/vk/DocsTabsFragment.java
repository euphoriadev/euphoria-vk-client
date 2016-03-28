package ru.euphoriadev.vk;

import android.Manifest;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.PermissionChecker;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.ArrayList;

import ru.euphoriadev.vk.adapter.DocsAdapter;
import ru.euphoriadev.vk.adapter.DocsPagerAdapter;
import ru.euphoriadev.vk.api.Api;
import ru.euphoriadev.vk.api.model.VKDocument;
import ru.euphoriadev.vk.async.ThreadExecutor;
import ru.euphoriadev.vk.common.ThemeManager;
import ru.euphoriadev.vk.helper.DBHelper;
import ru.euphoriadev.vk.helper.FileHelper;
import ru.euphoriadev.vk.interfaces.RunnableToast;
import ru.euphoriadev.vk.service.UploadService;
import ru.euphoriadev.vk.sqlite.CursorBuilder;
import ru.euphoriadev.vk.sqlite.VKInsertHelper;
import ru.euphoriadev.vk.sqlite.VKSqliteHelper;
import ru.euphoriadev.vk.util.AndroidUtils;
import ru.euphoriadev.vk.util.ArrayUtil;
import ru.euphoriadev.vk.util.FileChooser;
import ru.euphoriadev.vk.util.PermissionAllower;
import ru.euphoriadev.vk.util.ViewUtil;
import ru.euphoriadev.vk.view.fab.FloatingActionButton;

/**
 * Created by Igor on 09.03.16.
 */
public class DocsTabsFragment extends Fragment {
    public static final int REQUEST_CODE_PICK_FILE = 100;
    private DocsPagerAdapter adapter;
    private AppCompatActivity activity;
    private TabLayout tabLayout;
    private FloatingActionButton fab;
    private ViewPager viewPager;
    private ViewPager.OnPageChangeListener changeListener;

    private boolean localSearch = true;
    private boolean globalSearch;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_tabs_docs, container, false);

        activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setTitle(getResources().getString(R.string.docs));
        changeElevation(true);

        adapter = new DocsPagerAdapter(activity, activity.getSupportFragmentManager());
        viewPager = (ViewPager) rootView.findViewById(R.id.viewPagerDocs);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(adapter.getCount());

        int themeColor = ThemeManager.getPrimaryTextColorOnThemeColor(getActivity());
        tabLayout = (TabLayout) rootView.findViewById(R.id.tabLayoutDocs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabTextColors(ThemeManager.alphaColor(themeColor, 0.7f), themeColor);

        fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setColorNormal(ThemeManager.getColorAccent(getActivity()));
        fab.setColorPressed(ViewUtil.getPressedColor(fab.getColorNormal()));
        fab.setShadow(true);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileManager();
            }
        });

        changeListener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                fab.attachToListView(adapter.fragmentAt(position).listView);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        };
        viewPager.addOnPageChangeListener(changeListener);


        setHasOptionsMenu(true);
        getDocs(false);
        return rootView;
    }

    private void openFileManager() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if (PermissionChecker.checkSelfPermission(getActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PermissionChecker.PERMISSION_GRANTED) {
                PermissionAllower.allowPermission(getActivity(),
                        Manifest.permission.READ_EXTERNAL_STORAGE);
                return;
            }
        }

        pickFileIntent();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_FILE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            String path = FileChooser.getPath(getActivity(), uri);
            if (path != null) {
                File file = new File(path);
                if (file.getName().endsWith(".apk")) {
                    createRenameAlert(file);
                } else {
                    uploadFile(file);
                }
            }
        }
    }

    private void createRenameAlert(final File file) {

        final String rename = file.getName() + file.getName().charAt(file.getName().length() - 1);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.edit_doc);
        builder.setMessage(String.format(activity.getResources().getString(R.string.cannot_upload), file.getName(), rename));
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                uploadFile(file);
            }
        });
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                uploadFile(AndroidUtils.rename(file, rename));
            }
        });

        builder.show();
    }

    private void uploadFile(final File file) {
        UploadService.upload(getActivity(), file);
    }

    private void pickFileIntent() {
        Intent intent = new Intent();
        intent.setType("*/*");

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
//            intent.addCategory(Intent.CATEGORY_OPENABLE);
//        } else {
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent = Intent.createChooser(intent, "Select file");
//        }
        startActivityForResult(intent, REQUEST_CODE_PICK_FILE);
    }

    private void displaySize(ArrayList<VKDocument> docs) {
        if (adapter != null) {
            Toolbar toolbar = ((BasicActivity) getActivity()).getToolbar();
            if (toolbar != null) {
                toolbar.setSubtitle(DocsAdapter.convertBytes(getSizeOf(docs)));
            }
        }
    }

    private void displayCount(ArrayList<VKDocument> docs) {
        if (adapter != null && getActivity() != null) {
            ((BasicActivity) getActivity())
                    .getSupportActionBar()
                    .setTitle(getResources()
                            .getString(R.string.docs) + " (" + String.format("%,d", docs.size()) + ")");
        }
    }


    private long getSizeOf(ArrayList<VKDocument> docs) {
        long sizeBytes = 0;
        if (ArrayUtil.isEmpty(docs)) {
            return sizeBytes;
        }

        for (int i = 0; i < docs.size(); i++) {
            sizeBytes += docs.get(i).size;
        }
        return sizeBytes;
    }

    private void showToastForEmpty() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new RunnableToast(getActivity(), "Docs is empty", true));
        }
    }

    private void getDocs(final boolean onlyUpdate) {
        ThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    SQLiteDatabase database = DBHelper.get(getActivity()).getWritableDatabase();
                    if (!onlyUpdate) {
                        final ArrayList<VKDocument> docsFrom = getDocsFrom(database);
                        if (!docsFrom.isEmpty()) {
                            updateAdapter(docsFrom);
                        }
                    }
                    if (!AndroidUtils.hasConnection(getActivity())) {
                        getActivity().runOnUiThread(new RunnableToast(getActivity(), R.string.check_internet, true));
                        return;
                    }

                    final ArrayList<VKDocument> docs = Api.get().getDocs(Api.get().getUserId(), null, 0L);
                    if (ArrayUtil.isEmpty(docs)) {
                        showToastForEmpty();
                        return;
                    }
                    updateDocs(docs, database);
                    updateAdapter(docs);

                    Runtime.getRuntime().gc();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private ArrayList<VKDocument> getDocsFrom(SQLiteDatabase database) {
        return VKSqliteHelper.getDocs(database);
    }

    private void updateDocs(ArrayList<VKDocument> docs, SQLiteDatabase database) {
        Cursor cursor = CursorBuilder.create()
                .selectAllFrom(DBHelper.DOCS_TABLE)
                .where(DBHelper.OWNER_ID + " = " + Api.get().getUserId())
                .cursor(database);

        if (cursor.getCount() > 0) {
            final String[] whereArgs = new String[1];
            while (cursor.moveToNext()) {
                int ownerId = cursor.getInt(cursor.getColumnIndex(DBHelper.OWNER_ID));
                whereArgs[0] = String.valueOf(ownerId);
                database.delete(DBHelper.DOCS_TABLE, "owner_id = ?", whereArgs);
            }
        }
        VKInsertHelper.insertDocs(database, docs, true);
    }

    private void updateAdapter(final ArrayList<VKDocument> docs) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < adapter.getCount(); i++) {
                    adapter.fragmentAt(i).updateAdapter(docs);
                }
                displaySize(docs);
                displayCount(docs);

                // Select the first tab for FAB
                if (viewPager.getCurrentItem() == 0) {
                    changeListener.onPageSelected(0);
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        changeElevation(false);

        try {
            adapter.clear();
        } catch (Exception e) {
            // ignored
        }
        System.gc();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//      Inflate the menu;
        inflater.inflate(R.menu.main_menu, menu);

        MenuItem itemDownloadAll = menu.add("Download All");
        itemDownloadAll.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView = null;
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }

        if (searchView != null) {
            searchView.setOnSearchClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    createDialog();
                }
            });
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
            searchView.setQueryHint(getString(R.string.search));
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    adapter.filter(localSearch ? s : "clear all docs values");

                    return true;
                }
            });
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().toString().equals("Download All")) {
            downloadAll();
        }
        return super.onOptionsItemSelected(item);
    }

    private void createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Where to search?");
        builder.setMultiChoiceItems(new CharSequence[]{
                "Local Search",
                "Global Search"
        }, new boolean[]{localSearch, globalSearch}, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                switch (which) {
                    case 0: localSearch = isChecked; break;
                    case 1: globalSearch = isChecked; break;
                }
            }
        });
        builder.setPositiveButton(android.R.string.ok, null);
        builder.show();
    }

    private void downloadAll() {
        ThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final ArrayList<VKDocument> documents = getDocsFrom(DBHelper.get(getActivity()).getWritableDatabase());
                for (int i = 0; i < documents.size(); i++) {
                    VKDocument doc = documents.get(i);
                    FileHelper.downloadFileWithDefaultManager(doc.url, doc.title, null);
                }

                documents.clear();
                documents.trimToSize();
            }
        });
    }

    private void changeElevation(boolean hide) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AppBarLayout barLayout = (AppBarLayout) activity.findViewById(R.id.appBarLayout);
            ViewCompat.setElevation(barLayout, hide ? 0 : AndroidUtils.pxFromDp(activity, 8));
        }
    }
}
