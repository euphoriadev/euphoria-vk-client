package ru.euphoriadev.vk;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

import ru.euphoriadev.vk.adapter.MessageAdapter;
import ru.euphoriadev.vk.adapter.MessageItem;

/**
 * Created by Igor on 28.09.15.
 */
public class MaterialsFragment extends Fragment {

    public MessageAdapter adapter = null;
    View view;
    int position = -1;

    public static MaterialsFragment newInstance(int position) {
        MaterialsFragment fragment = new MaterialsFragment();
        Bundle arguments = new Bundle();
        arguments.putInt("position", position);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_materials_list, container, false);
        ListView listView = (ListView) view.findViewById(R.id.lvMaterials);

        ArrayList<MessageItem> items = new ArrayList<>();
        adapter = new MessageAdapter(getActivity(), items, 0, 0);
        position = getArguments().getInt("position");
        listView.setAdapter(adapter);
        return view;
    }

    public final MessageAdapter getAdapter() {
        return adapter;
    }

    public int getPosition() {
       return position;
    }

}

