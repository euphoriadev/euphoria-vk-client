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
import ru.euphoriadev.vk.adapter.VideosAdapter;
import ru.euphoriadev.vk.api.Api;
import ru.euphoriadev.vk.api.model.VKAudio;
import ru.euphoriadev.vk.api.model.VKMessageAttachment;
import ru.euphoriadev.vk.api.model.VKUser;
import ru.euphoriadev.vk.util.ThreadExecutor;
import ru.euphoriadev.vk.vkapi.VKApi;

/**
 * Created by Igor on 15.02.16.
 */
public class AudiosAttachmentFragment extends Fragment {
    ListView listView;
    AudioAdapter adapter;

    private int chat_id;
    private int user_id;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_audios, container, false);
        listView = (ListView) rootView.findViewById(R.id.lvAudios);

        chat_id = getArguments().getInt("chat_id");
        user_id = getArguments().getInt("user_id");
        loadAudios();
        return rootView;
    }

    public void setAdapter(AudioAdapter adapter) {
        this.adapter = adapter;

        listView.setAdapter(adapter);
    }

    public static AudiosAttachmentFragment newInstance(int chat_id, int user_id, int position) {

        Bundle args = new Bundle();
        args.putInt("chat_id", chat_id);
        args.putInt("user_id", user_id);
        args.putInt("position", position);

        AudiosAttachmentFragment fragment = new AudiosAttachmentFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void loadAudios() {
        ThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    final ArrayList<VKMessageAttachment> attachments = Api.get().getHistoryAttachments(chat_id != 0 ? VKApi.OFFSET_PEER_ID + chat_id : user_id, VKMessageAttachment.TYPE_AUDIO, 0, 200, null);

                    final ArrayList<VKAudio> audios = new ArrayList<>(attachments.size());
                    for (int i = 0; i < attachments.size(); i++) {
                        audios.add(attachments.get(i).audio);
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setAdapter(new AudioAdapter(getActivity(), audios));
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
