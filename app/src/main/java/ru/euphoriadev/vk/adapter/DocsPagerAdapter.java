package ru.euphoriadev.vk.adapter;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.Arrays;

import ru.euphoriadev.vk.DocsFragment;
import ru.euphoriadev.vk.R;

/**
 * Created by Igor on 09.03.16.
 */
public class DocsPagerAdapter extends FragmentPagerAdapter {
    private String[] titles;
    private DocsFragment[] fragments;
    private FragmentManager manager;

    public DocsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);

        this.manager = fm;
        this.titles = context.getResources().getStringArray(R.array.tab_docs_array);
        this.fragments = new DocsFragment[titles.length];
    }

    @Override
    public DocsFragment getItem(int position) {
        return fragments[position] = DocsFragment.newInstance(position);
    }

    @Override
    public int getCount() {
        return titles.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }

    public void filter(String q) {
        for (DocsFragment fragment : fragments) {
            fragment.getAdapter().filter(q);
        }
    }

    public DocsFragment fragmentAt(int index) {
        return fragments[index];
    }

    public void clear() {
        if (manager == null) {
            return;
        }
        for (DocsFragment fragment : fragments) {
            manager.beginTransaction().remove(fragment).commit();
        }

        Arrays.fill(fragments, null);
        Arrays.fill(titles, null);
        fragments = null;
        titles = null;
    }
}
