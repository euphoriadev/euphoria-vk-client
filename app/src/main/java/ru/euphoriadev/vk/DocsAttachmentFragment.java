package ru.euphoriadev.vk;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

import ru.euphoriadev.vk.adapter.AudioAdapter;
import ru.euphoriadev.vk.adapter.DocsAdapter;
import ru.euphoriadev.vk.api.Api;
import ru.euphoriadev.vk.api.model.VKAudio;
import ru.euphoriadev.vk.api.model.VKDocument;
import ru.euphoriadev.vk.api.model.VKMessageAttachment;
import ru.euphoriadev.vk.util.ThreadExecutor;
import ru.euphoriadev.vk.vkapi.VKApi;

/**
 * Created by user on 16.02.16.
 */
public class DocsAttachmentFragment extends Fragment {
    ListView listView;
    DocsAdapter adapter;

    private int chat_id;
    private int user_id;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_docs, container, false);
        listView = (ListView) rootView.findViewById(R.id.lvDocs);

        chat_id = getArguments().getInt("chat_id");
        user_id = getArguments().getInt("user_id");
        loadDocs();
        return rootView;
    }

    public void setAdapter(DocsAdapter adapter) {
        this.adapter = adapter;

        listView.setAdapter(adapter);
    }

    public static DocsAttachmentFragment newInstance(int chat_id, int user_id, int position) {

        Bundle args = new Bundle();
        args.putInt("chat_id", chat_id);
        args.putInt("user_id", user_id);
        args.putInt("position", position);

        DocsAttachmentFragment fragment = new DocsAttachmentFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void loadDocs() {
        ThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    final ArrayList<VKMessageAttachment> attachments = Api.get().getHistoryAttachments(chat_id != 0 ? VKApi.OFFSET_PEER_ID + chat_id : user_id, VKMessageAttachment.TYPE_DOC, 0, 200, null);

                    final ArrayList<VKDocument> docs = new ArrayList<>(attachments.size());
                    for (int i = 0; i < attachments.size(); i++) {
                        docs.add(attachments.get(i).doc);
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setAdapter(new DocsAdapter(getActivity(), docs));
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}

