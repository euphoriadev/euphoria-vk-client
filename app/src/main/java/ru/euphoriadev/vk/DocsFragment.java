package ru.euphoriadev.vk;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import ru.euphoriadev.vk.adapter.DocsAdapter;
import ru.euphoriadev.vk.api.Api;
import ru.euphoriadev.vk.api.model.VKDocument;
import ru.euphoriadev.vk.helper.FileHelper;
import ru.euphoriadev.vk.async.ThreadExecutor;
import ru.euphoriadev.vk.util.ViewUtil;

/**
 * Created by user on 13.12.15.
 */
public class DocsFragment extends Fragment {

    ListView listView;
    DocsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_docs, container, false);
        listView = (ListView) rootView.findViewById(R.id.lvDocs);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                VKDocument doc = (VKDocument) parent.getItemAtPosition(position);
                createDialog(doc);
                return true;
            }
        });
        setHasOptionsMenu(true);
        getDocs();
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        // Inflate the menu;
        inflater.inflate(R.menu.main_menu, menu);

        MenuItem itemDownloadAll = menu.add("Download All");
        itemDownloadAll.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView = null;
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }

        Log.w("GroupsFragment", "onCreateMenu = " + searchView);
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
            searchView.setQueryHint(getString(R.string.search));
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    adapter.filter(s);
                    return true;
                }
            });
        }

        ViewUtil.setColors(menu, ((BaseThemedActivity) getActivity()).getToolbar());
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().toString().equals("Download All")) {
            downloadAll();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (adapter != null) {
            adapter.clear();
            adapter = null;
        }
    }

    private void downloadAll() {
        ThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < adapter.getValues().size(); i++) {
                    VKDocument doc = adapter.getValues().get(i);
                    FileHelper.downloadFileWithDefaultManager(doc.url, doc.title, null);
                }
            }
        });
    }

    private void getDocs() {
        ThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final ArrayList<VKDocument> docs = Api.get().getDocs(Api.get().getUserId(), null, 0L);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter = new DocsAdapter(getActivity(), docs);
                            listView.setAdapter(adapter);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void deleteDoc(final VKDocument doc) {
        ThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final Boolean isDeleted = Api.get().deleteDoc(doc.id, doc.owner_id);
                    if (isDeleted) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.remove(doc);
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void createDialog(final VKDocument document) {
        CharSequence[] items = new CharSequence[]{getActivity().getResources().getString(R.string.delete_doc)};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteDoc(document);
            }
        });
        builder.create().show();

    }
}
