package ru.euphoriadev.vk;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;

import ru.euphoriadev.vk.adapter.GiftItem;
import ru.euphoriadev.vk.adapter.GiftsAdapter;
import ru.euphoriadev.vk.api.Api;
import ru.euphoriadev.vk.api.model.VKGift;
import ru.euphoriadev.vk.api.model.VKUser;
import ru.euphoriadev.vk.util.Account;
import ru.euphoriadev.vk.util.ThemeManagerOld;
import ru.euphoriadev.vk.util.ThreadExecutor;

/**
 * Created by user on 24.07.15.
 */
public class GiftsFragment extends Fragment {

    Api api;
    Account account;
    Activity activity;
    ListView lv;
    ThemeManagerOld tm;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_gifts, container, false);
        activity = getActivity();

        account = new Account(activity).restore();
        api = Api.init(account.access_token, Account.API_ID);

        lv = (ListView) rootView.findViewById(R.id.lvGifts);

        loadGifts();
        return rootView;
    }

    private void loadGifts() {
        ThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    HashMap<Integer, VKUser> mapUsers = new HashMap<>();

                    ArrayList<VKGift> apiGifts = api.getGifts(account.user_id, 100, 0);
                    for (VKGift g : apiGifts) {
                        mapUsers.put(g.from_id, null);
                    }



                    ArrayList<VKUser> apiProfiles = api.getProfiles(mapUsers.keySet(), null, null, null, null);
                    for (VKUser u : apiProfiles) {
                        mapUsers.put(u.user_id, u);
                    }

                    final ArrayList<GiftItem> items = new ArrayList<>();
                    for (VKGift g : apiGifts) {
                        items.add(new GiftItem(mapUsers.get(g.from_id), g));
                    }

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            GiftsAdapter adapter = new GiftsAdapter(activity, items);
                            lv.setAdapter(adapter);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
