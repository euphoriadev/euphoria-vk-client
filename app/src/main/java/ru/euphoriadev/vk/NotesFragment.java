package ru.euphoriadev.vk;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

import ru.euphoriadev.vk.adapter.NoteAdapter;
import ru.euphoriadev.vk.api.Api;
import ru.euphoriadev.vk.api.model.VKNote;
import ru.euphoriadev.vk.async.ThreadExecutor;

/**
 * Created by Igor on 09.12.15.
 */
public class NotesFragment extends Fragment {

    ListView lvNotes;
    NoteAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_notes, container, false);

        lvNotes = (ListView) rootView.findViewById(R.id.lvNotes);

        getNotes();
        return rootView;
    }

    private void getNotes() {
        ThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Api api = Api.get();
                    final ArrayList<VKNote> vkNotes = api.getNotes(api.getUserId(), null, "0", 100L, 0L);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter = new NoteAdapter(getActivity(), vkNotes);
                            lvNotes.setAdapter(adapter);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (adapter != null) {
            adapter.clear();
        }
    }
}
