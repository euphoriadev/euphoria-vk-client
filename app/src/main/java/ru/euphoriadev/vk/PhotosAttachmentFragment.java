package ru.euphoriadev.vk;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import ru.euphoriadev.vk.adapter.PhotosRecyclerAdapter;
import ru.euphoriadev.vk.api.Api;
import ru.euphoriadev.vk.api.model.VKMessageAttachment;
import ru.euphoriadev.vk.api.model.VKPhoto;
import ru.euphoriadev.vk.util.ThreadExecutor;
import ru.euphoriadev.vk.vkapi.VKApi;

/**
 * Created by user on 15.02.16.
 */
public class PhotosAttachmentFragment extends Fragment {
    RecyclerView recyclerView;
    PhotosRecyclerAdapter adapter;

    private int chat_id;
    private int user_id;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_photos, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerViewPhotos);

        chat_id = getArguments().getInt("chat_id");
        user_id = getArguments().getInt("user_id");
        loadPhotos();
        return rootView;
    }

    public void setAdapter(PhotosRecyclerAdapter adapter) {
        this.adapter = adapter;
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

        recyclerView.setAdapter(adapter);
    }

    public static PhotosAttachmentFragment newInstance(int chat_id, int user_id, int position) {

        Bundle args = new Bundle();
        args.putInt("chat_id", chat_id);
        args.putInt("user_id", user_id);
        args.putInt("position", position);

        PhotosAttachmentFragment fragment = new PhotosAttachmentFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void loadPhotos() {
        ThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final ArrayList<VKMessageAttachment> attachments = Api.get().getHistoryAttachments(chat_id != 0 ? VKApi.OFFSET_PEER_ID + chat_id : user_id, VKMessageAttachment.TYPE_PHOTO, 0, 200, null);

                    final ArrayList<VKPhoto> photos = new ArrayList<>(attachments.size());
                    for (int i = 0; i < attachments.size(); i++) {
                        photos.add(attachments.get(i).photo);
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setAdapter(new PhotosRecyclerAdapter(getActivity(), photos));
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
