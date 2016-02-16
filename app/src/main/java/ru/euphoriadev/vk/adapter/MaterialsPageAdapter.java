package ru.euphoriadev.vk.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import ru.euphoriadev.vk.AudiosAttachmentFragment;
import ru.euphoriadev.vk.DocsAttachmentFragment;
import ru.euphoriadev.vk.DocsFragment;
import ru.euphoriadev.vk.PhotosAttachmentFragment;
import ru.euphoriadev.vk.R;
import ru.euphoriadev.vk.VideosAttachmentFragment;

/**
 * Created by Igor on 28.09.15.
 */
public class MaterialsPageAdapter extends FragmentPagerAdapter {

    public static final int POSITION_PICTURES = 0;
    public static final int POSITION_VIDEO = 1;
    public static final int POSITION_AUDIO = 2;
    public static final int POSITION_DOC = 3;

    private String[] tabs;
    private Fragment[] fragments;
    private int chat_id;
    private int user_id;

    public MaterialsPageAdapter(Context context, FragmentManager fm, int chat_id, int user_id) {
        super(fm);

        this.tabs = context.getResources().getStringArray(R.array.tabs_material_of_dialog_array);
        this.fragments = new Fragment[tabs.length];
        this.chat_id = chat_id;
        this.user_id = user_id;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabs[position];
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case POSITION_PICTURES:
                fragments[position] = PhotosAttachmentFragment.newInstance(chat_id, user_id, position);
                break;
            case POSITION_VIDEO:
                fragments[position] = VideosAttachmentFragment.newInstance(chat_id, user_id, position);
                break;
            case POSITION_AUDIO:
                fragments[position] = AudiosAttachmentFragment.newInstance(chat_id, user_id, position);
                break;
            case POSITION_DOC:
                fragments[position] = DocsAttachmentFragment.newInstance(chat_id, user_id, position);

        }
        return fragments[position];
    }

    public Fragment[] getFragments() {
        return fragments;
    }

    @Override
    public int getCount() {
        return tabs.length;
    }
}
