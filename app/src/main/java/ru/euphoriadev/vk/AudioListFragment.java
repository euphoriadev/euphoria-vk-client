package ru.euphoriadev.vk;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import ru.euphoriadev.vk.adapter.AudioAdapter;
import ru.euphoriadev.vk.api.Api;
import ru.euphoriadev.vk.api.model.VKAudio;
import ru.euphoriadev.vk.helper.DBHelper;
import ru.euphoriadev.vk.util.Account;
import ru.euphoriadev.vk.util.AndroidUtils;

/**
 * Created by user on 10.06.15.
 */
public class AudioListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    Api api;
    Account account;
    ListView lvAudios;
    AudioAdapter adapter;
    ArrayList<VKAudio> audios;
    SQLiteDatabase database;
    SwipeRefreshLayout refreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_audios, container, false);

        account = new Account(getActivity());
        account.restore();
        api = Api.get();
        lvAudios = (ListView) rootView.findViewById(R.id.lvAudios);
        refreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container_audios);

        getAudios(false);
        return rootView;

    }

    private void setRefreshing(final boolean refreshing) {
        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(refreshing);
            }
        });
    }

    @Override
    public void onStop() {
        setRefreshing(false);
        super.onStop();
    }

    private void getAudios(final boolean onlyUpdate) {
        setRefreshing(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (audios == null) {
                        audios = new ArrayList<>(200);
                    }

                    if (database == null || !database.isOpen()) {
                        database = DBHelper.get(getActivity()).getWritableDatabase();
                    }
                    if (onlyUpdate) {
                        ArrayList<VKAudio> vkAudios = api.getAudio(api.getUserId(), null, 200, 0, null, null, null);
                        if (!vkAudios.isEmpty()) {
                            audios.clear();
                            audios.addAll(vkAudios);
                            updateAudios(database, audios);
                            updateListView();

                            vkAudios.clear();
                            vkAudios.trimToSize();
                            vkAudios = null;
                        }
                        setRefreshing(false);
                        return;
                    }
                    Cursor cursor = database.rawQuery("SELECT * FROM " +
                            DBHelper.AUDIOS_TABLE +
                            " WHERE " + DBHelper.OWNER_ID +
                            " = " + api.getUserId(), null);

                    loadAudiosToList(cursor);
                    cursor.close();

                    updateListView();

                    ArrayList<VKAudio> vkAudios = api.getAudio(api.getUserId(), null, 200, 0, null, null, null);
                    if (!vkAudios.isEmpty()) {
                        audios.clear();
                        audios.addAll(vkAudios);
                        updateListView();

                        updateAudios(database, vkAudios);

                        vkAudios.clear();
                        vkAudios.trimToSize();
                        vkAudios = null;
                    }


                } catch (final Exception e) {
                    e.getStackTrace();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_LONG).show();
                        }
                    });
                } finally {
                    setRefreshing(false);
                }
            }
        }).start();
    }

    /**
     * Обновление списка
     * Если adapter == null, то создает новый
     */
    private void updateListView() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (adapter == null) {
                    adapter = new AudioAdapter(getActivity(), audios);
                    lvAudios.setAdapter(adapter);
                } else {
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    /**
     * Обновление или вставка песен в базу данных
     *
     * @param database база данных
     * @param audios список аудиозаписей, которые необходимо занести
     */
    private void updateAudios(SQLiteDatabase database, ArrayList<VKAudio> audios) {
        ContentValues cv = new ContentValues();
        database.beginTransaction();

        for (int i = 0; i < audios.size(); i++) {
            VKAudio audio = audios.get(i);

            cv.put(DBHelper.AUDIO_ID, audio.id);
            cv.put(DBHelper.OWNER_ID, audio.owner_id);
            cv.put(DBHelper.ARTIST, audio.artist);
            cv.put(DBHelper.TITLE, audio.title);
            cv.put(DBHelper.URL, audio.url);
            cv.put(DBHelper.LYRICS_ID, audio.lyrics_id);

            if (database.update(DBHelper.AUDIOS_TABLE, cv, DBHelper.AUDIO_ID + " = " + audio.id, null) <= 0) {
                database.insert(DBHelper.AUDIOS_TABLE, null, cv);
            }
        }
        database.setTransactionSuccessful();
        database.endTransaction();
        cv.clear();
        cv = null;
    }

    /**
     * Получение всех аудиозаписей из базы и занос в list
     *
     * @param cursor открытое подключение в базе
     */
    private void loadAudiosToList(Cursor cursor) {
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                VKAudio audio = new VKAudio();
                audio.id = cursor.getInt(1);
                audio.owner_id = cursor.getInt(2);
                audio.artist = cursor.getString(3);
                audio.title = cursor.getString(4);
                audio.duration = cursor.getInt(5);
                audio.url = cursor.getString(6);
                audio.lyrics_id = cursor.getInt(7);

                audios.add(audio);
            }
        }
    }

    @Override
    public void onDestroyView() {
        setRefreshing(false);
        super.onDestroyView();
    }

    @Override
    public void onRefresh() {
        if (AndroidUtils.isInternetConnection(getActivity())) {
            getAudios(true);
        } else {
            setRefreshing(false);
            Toast.makeText(getActivity(), R.string.check_internet, Toast.LENGTH_LONG).show();
        }
    }

}
