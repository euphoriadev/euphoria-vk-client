package ru.euphoriadev.vk;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import ru.euphoriadev.vk.adapter.AudioAdapter;
import ru.euphoriadev.vk.adapter.DocsAdapter;
import ru.euphoriadev.vk.adapter.MaterialsPageAdapter;
import ru.euphoriadev.vk.adapter.PhotosRecyclerAdapter;
import ru.euphoriadev.vk.adapter.VideosAdapter;
import ru.euphoriadev.vk.api.Api;
import ru.euphoriadev.vk.api.model.VKAudio;
import ru.euphoriadev.vk.api.model.VKDocument;
import ru.euphoriadev.vk.api.model.VKMessageAttachment;
import ru.euphoriadev.vk.api.model.VKPhoto;
import ru.euphoriadev.vk.api.model.VKUser;
import ru.euphoriadev.vk.async.ThreadExecutor;
import ru.euphoriadev.vk.common.ThemeManager;
import ru.euphoriadev.vk.napi.VKApi;
import ru.euphoriadev.vk.util.AndroidUtils;
import ru.euphoriadev.vk.util.ArrayUtil;
import ru.euphoriadev.vk.util.ViewUtil;

/**
 * Created by user on 17.02.16.
 */
public class HistoryAttachmentFragment extends Fragment {

    int position;
    int chat_id;
    int user_id;

    ListView listView;
    BaseAdapter adapter;

    RecyclerView recyclerView;
    PhotosRecyclerAdapter photoAdapter;

    public static HistoryAttachmentFragment newInstance(int chat_id, int user_id, int position) {

        Bundle args = new Bundle();
        args.putInt("chat_id", chat_id);
        args.putInt("user_id", user_id);
        args.putInt("position", position);

        HistoryAttachmentFragment fragment = new HistoryAttachmentFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        position = getArguments().getInt("position");
        chat_id = getArguments().getInt("chat_id");
        user_id = getArguments().getInt("user_id");

        int resource = position == MaterialsPageAdapter.POSITION_PICTURES ?
                R.layout.fragment_photos :
                R.layout.fragment_attachs;


        View rootView = inflater.inflate(resource, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerViewPhotos);
        listView = (ListView) rootView.findViewById(R.id.lvAttachments);

        if (listView != null) {
            View emptyView = inflater.inflate(R.layout.empty_layout, container, false);
            TextView emptyTextView = (TextView) emptyView.findViewById(R.id.tvEmpty);
            emptyTextView.setText("Вложений с данным типом нет :(");
            listView.setEmptyView(emptyView);
        }

        loadAttachments();
        return rootView;
    }

    public void setPhotoAdapter(PhotosRecyclerAdapter adapter) {
        this.photoAdapter = adapter;
        this.recyclerView.setHasFixedSize(true);
        this.recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

        this.recyclerView.setAdapter(adapter);
    }

    public void setAdapter(BaseAdapter adapter) {
        this.adapter = adapter;
        this.listView.setAdapter(adapter);
    }

    private void loadAttachments() {
        if (!AndroidUtils.hasConnection(getActivity())) {
            Toast.makeText(getActivity(), R.string.check_internet, Toast.LENGTH_LONG).show();
            return;
        }
        ThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ArrayList<VKMessageAttachment> attachments = Api.get().getHistoryAttachments(chat_id == 0 ? user_id : VKApi.OFFSET_PEER_ID + chat_id, MaterialsPageAdapter.typeFrom(position), 0, 200, null);
                    if (ArrayUtil.isEmpty(attachments)) {
//                        Snackbar.make(getView(), "Вложений с типом  :( ", Snackbar.LENGTH_LONG)
//                                .show();
                        Log.w("AttachmentFragment", "attachments is empty");
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                inflateEmptyView();
                            }
                        });
                        return;
                    }

                    // generic type throws ClassCastException =(
                    // use for a specific type of a separate list
                    ArrayList<VKPhoto> photos = null;
                    ArrayList<VideosAdapter.VideoItem> videos = null;
                    ArrayList<VKDocument> docs = null;
                    ArrayList<VKAudio> audios = null;

                    for (int i = 0; i < attachments.size(); i++) {
                        VKMessageAttachment messageAttachment = attachments.get(i);
                        switch (position) {
                            case MaterialsPageAdapter.POSITION_PICTURES:
                                if (photos == null) {
                                    photos = new ArrayList<>(attachments.size());
                                }
                                photos.add(messageAttachment.photo);
                                break;

                            case MaterialsPageAdapter.POSITION_VIDEO:
                                if (videos == null) {
                                    videos = new ArrayList<>(attachments.size());
                                }
                                videos.add(new VideosAdapter.VideoItem(VKUser.EMPTY, messageAttachment.video));
                                break;

                            case MaterialsPageAdapter.POSITION_AUDIO:
                                if (audios == null) {
                                    audios = new ArrayList<>(attachments.size());
                                }
                                audios.add(messageAttachment.audio);
                                break;

                            case MaterialsPageAdapter.POSITION_DOC:
                                if (docs == null) {
                                    docs = new ArrayList<>(attachments.size());
                                }
                                docs.add(messageAttachment.doc);
                                break;
                        }
                    }

                    final ArrayList<VKPhoto> finalPhotos = photos;
                    final ArrayList<VideosAdapter.VideoItem> finalVideos = videos;
                    final ArrayList<VKAudio> finalAudios = audios;
                    final ArrayList<VKDocument> finalDocs = docs;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (position == MaterialsPageAdapter.POSITION_PICTURES) {
                                setPhotoAdapter(new PhotosRecyclerAdapter(getActivity(), finalPhotos));
                            } else {
                                switch (position) {
                                    case MaterialsPageAdapter.POSITION_VIDEO:
                                        setAdapter(new VideosAdapter(getActivity(), finalVideos));
                                        break;

                                    case MaterialsPageAdapter.POSITION_AUDIO:
                                        setAdapter(new AudioAdapter(getActivity(), finalAudios));
                                        break;

                                    case MaterialsPageAdapter.POSITION_DOC:
                                        setAdapter(new DocsAdapter(getActivity(), finalDocs));
                                        break;
                                }
                            }
                        }
                    });


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void inflateEmptyView() {
        View rootView = getView();
//        ViewStub stub = (ViewStub) rootView.findViewById(R.id.viewStub);

//        View emptyView = stub.inflate();
        if (listView != null) {
            listView.setVisibility(View.GONE);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(View.GONE);
        }

        LinearLayout group = (LinearLayout) rootView;
        if (group != null) {
            View emptyView = LayoutInflater.from(getActivity()).inflate(R.layout.empty_layout, group, false);
            group.addView(emptyView);

            ImageView sentiment = (ImageView) emptyView.findViewById(R.id.ivSentiment);
            sentiment.setAlpha(0.5f);
            ViewUtil.setFilter(sentiment, ThemeManager.getSecondaryTextColor());

        }
    }

}
