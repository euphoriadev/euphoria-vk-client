package ru.euphoriadev.vk.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import ru.euphoriadev.vk.HistoryAttachmentFragment;
import ru.euphoriadev.vk.R;
import ru.euphoriadev.vk.api.model.VKMessageAttachment;

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

    public static String typeFrom(int position) {
        String type = "";
        switch (position) {
            case POSITION_PICTURES:
                type = VKMessageAttachment.TYPE_PHOTO;
                break;
            case POSITION_VIDEO:
                type = VKMessageAttachment.TYPE_VIDEO;
                break;
            case POSITION_AUDIO:
                type = VKMessageAttachment.TYPE_AUDIO;
                break;
            case POSITION_DOC:
                type = VKMessageAttachment.TYPE_DOC;
                break;
        }
        return type;
    }

//    @Override
//    public CharSequence getPageTitle(int position) {
//        return tabs[position];
//    }

    @Override
    public Fragment getItem(int position) {
        fragments[position] = HistoryAttachmentFragment.newInstance(chat_id, user_id, position);
        return fragments[position];
    }

    @Override
    public int getCount() {
        return tabs.length;
    }
}
