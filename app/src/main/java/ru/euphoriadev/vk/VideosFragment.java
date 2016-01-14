package ru.euphoriadev.vk;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;

import ru.euphoriadev.vk.adapter.VideosAdapter;
import ru.euphoriadev.vk.api.Api;
import ru.euphoriadev.vk.api.model.VKUser;
import ru.euphoriadev.vk.api.model.VKVideo;
import ru.euphoriadev.vk.util.AppLoader;
import ru.euphoriadev.vk.util.FileLogger;
import ru.euphoriadev.vk.util.ThreadExecutor;

/**
 * Created by user on 23.11.15.
 */
public class VideosFragment extends android.support.v4.app.Fragment {
    public static final String TAG = "VideosFragment";

    AppLoader loader;
    Api api;
    VideosAdapter adapter;
    ListView lv;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.videos_activity, container, false);

        lv = (ListView) rootView.findViewById(R.id.lvVideos);

        loader = AppLoader.getLoader();

        api = Api.get();
        getVideos();

        return rootView;
    }

    private void getVideos() {
        ThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final ArrayList<VKVideo> vkVideos = api.getVideo(null, api.getUserId(), null, null, 100L, 0L, api.getAccount().access_token);
                    if (vkVideos.isEmpty()) {
                        return;
                    }

                    HashMap<Long, VKUser> mapUsers = new HashMap<Long, VKUser>();
                    for (int i = 0; i < vkVideos.size(); i++) {

                        final VKVideo video = vkVideos.get(i);
                        if (video.owner_id < 0) {
                            continue;
                        }
                        mapUsers.put(video.owner_id, null);
                    }

                    final ArrayList<VKUser> vkUsers = api.getProfiles(mapUsers.keySet(), null, null, null, null);
                    for (int i = 0; i < vkUsers.size(); i++) {
                        VKUser user = vkUsers.get(i);
                        mapUsers.put(user.user_id, user);
                    }

                    final ArrayList<VideosAdapter.VideoItem> videoItems = new ArrayList<>(vkVideos.size());
                    for (int i = 0; i < vkVideos.size(); i++) {
                        VKVideo v = vkVideos.get(i);
                        VKUser u = mapUsers.get(v.owner_id);
                        videoItems.add(new VideosAdapter.VideoItem(u, v));
                    }

                    vkUsers.clear();
                    vkVideos.clear();
                    mapUsers.clear();

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter = new VideosAdapter(getActivity(), videoItems);
                            lv.setAdapter(adapter);

                        }
                    });
                } catch (Exception e) {
                    FileLogger.e(TAG, "Error get videos", e);
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        FileLogger.i(TAG, "onDestroy");
        if (adapter != null) {
            adapter.clear();
            adapter = null;
        }

    }
}
