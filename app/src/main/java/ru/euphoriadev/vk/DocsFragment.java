package ru.euphoriadev.vk;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import ru.euphoriadev.vk.adapter.DocsAdapter;
import ru.euphoriadev.vk.api.Api;
import ru.euphoriadev.vk.api.model.VKDocument;
import ru.euphoriadev.vk.async.ThreadExecutor;
import ru.euphoriadev.vk.common.ThemeManager;
import ru.euphoriadev.vk.interfaces.RunnableToast;
import ru.euphoriadev.vk.util.AndroidUtils;
import ru.euphoriadev.vk.util.ArrayUtil;

/**
 * Created by Igor on 13.12.15.
 */
public class DocsFragment extends AbstractFragment {
    private static final String TAG = "DocsFragment";
    int position;
    ListView listView;
    DocsAdapter adapter;
    TextView globalSearchView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_docs, container, false);
        position = getArguments().getInt("position");

        Log.w(TAG, "onCreateView: " + position);

        listView = (ListView) rootView.findViewById(R.id.lvDocs);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                VKDocument doc = (VKDocument) parent.getItemAtPosition(position);
                createDialog(doc);
                return true;
            }
        });
//        globalSearchView = new TextView(getActivity());
//        globalSearchView.setText("Global Search");
//        globalSearchView.setTextColor(ThemeManager.getSecondaryTextColor());
//        globalSearchView.setTypeface(TypefaceManager.getTypeface(getActivity()));
//        globalSearchView.setTextSize(16);
//        globalSearchView.setPadding(16, 16, 16, 16);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                VKDocument doc = (VKDocument) parent.getItemAtPosition(position);
                if (doc.isImage()) {
                    View itemView = AndroidUtils.getViewByPosition(position, listView);
                    ImageView imageView = (ImageView) itemView.findViewById(R.id.ivOocCircle);
                    Drawable drawable = imageView.getDrawable();
                    if (drawable != null && drawable instanceof BitmapDrawable) {
                        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                        PhotoViewerActivity.start(getActivity(), doc.url, bitmap, false);
                        Log.w("Docs", "start with placeholder!");
                    } else {
                        PhotoViewerActivity.start(getActivity(), doc.url, false);
                    }
                } else if (doc.isGif()) {
                    Log.w("Docs", "start with gif!");
                    PhotoViewerActivity.start(getActivity(), doc.url, true);
                } else if (doc.type == VKDocument.TYPE_TEXT || doc.isCode()) {
                    startActivity(new Intent(getActivity(), DocTextViewActivity.class).putExtra("title", doc.title).putExtra("data", doc.url));
                }
            }
        });
        ThemeManager.initDivider(listView);

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (adapter != null) {
            adapter.clear();
            adapter = null;
        }
    }

    public DocsAdapter getAdapter() {
        return adapter;
    }

    public static DocsFragment newInstance(int position) {

        Bundle args = new Bundle();
        args.putInt("position", position);
        DocsFragment fragment = new DocsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    void updateAdapter(final ArrayList<VKDocument> docs) {
        if (adapter == null) {
            ArrayList<VKDocument> values = ArrayUtil.copyOf(docs);
            filter(values);
            adapter = new DocsAdapter(getActivity(), values);
            listView.setAdapter(adapter);
        } else {
            adapter.getValues().clear();
            adapter.getValues().addAll(docs);
            filter(adapter.getValues());
            adapter.notifyDataSetChanged();
        }
    }

    private void filter(ArrayList<VKDocument> docs) {
        if (position == 0) {
            return;
        }
        if (ArrayUtil.isEmpty(docs)) {
            return;
        }

        for (int i = docs.size() - 1; i >= 0; i--) {
            VKDocument doc = docs.get(i);
            if (position != doc.type) {
                if (position == VKDocument.TYPE_TEXT && doc.isCode()) {
                    continue;
                }
                docs.remove(i);
            }
        }
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

    private void renameDoc(final VKDocument doc) {

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        final EditText editText = new AppCompatEditText(getActivity());
        editText.setText(doc.title);
        editText.setHint(doc.title);
        editText.setSingleLine();
        editText.setLayoutParams(params);

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(params);
        layout.setPadding(
                AndroidUtils.pxFromDp(getActivity(), 16),
                AndroidUtils.pxFromDp(getActivity(), 6),
                AndroidUtils.pxFromDp(getActivity(), 16),
                AndroidUtils.pxFromDp(getActivity(), 6));
        layout.addView(editText);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.edit_doc);
        builder.setView(layout);
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ThreadExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final Boolean aBoolean = Api.get().editDoc(doc.id, doc.owner_id, editText.getText().toString());
                            if (aBoolean) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getActivity(), "Success", Toast.LENGTH_SHORT).show();
                                        doc.title = editText.getText().toString();
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            getActivity().runOnUiThread(new RunnableToast(getActivity(), R.string.error, true));
                        }
                    }
                });
            }
        });
        builder.show();
    }

    private void createDialog(final VKDocument document) {
        CharSequence[] items = new CharSequence[]{
                getActivity().getResources().getString(R.string.edit_doc),
                getActivity().getResources().getString(R.string.delete_doc),
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        renameDoc(document);
                        break;
                    case 1:
                        deleteDoc(document);
                        break;

                }
            }
        });
        builder.create().show();

    }

    @Override
    public void setRefreshing(final boolean refreshing) {
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        ((BasicActivity) getActivity()).getSupportActionBar().setSubtitle(null);
    }
}
