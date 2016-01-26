package ru.euphoriadev.vk.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

import ru.euphoriadev.vk.util.ThemeManagerOld;

/**
 * Created by Igor on 09.12.15.
 */
public class BaseArrayAdapter<E> extends BaseAdapter {

    private Context context;
    private ArrayList<E> mValues;
    private ArrayList<E> mCleanValues;
    private ArrayList<E> mSelectedItems;
    private LayoutInflater inflater;
    private ThemeManagerOld tm;
    private Typeface typeface;
    private OnMultiModeCloseListener closeListener;

    public BaseArrayAdapter(Context context, ArrayList<E> values) {
        this.context = context;
        this.mValues = values;

        this.inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        this.tm = ThemeManagerOld.get(context);
        if (tm.isSystemFont()) {
            typeface = Typeface.createFromAsset(context.getAssets(), tm.getFont());
        }
        mSelectedItems = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return mValues.size();
    }

    @Override
    public E getItem(int position) {
        if (position < getCount()) {
            return mValues.get(position);
        } else {
            return mValues.get(0);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }

    public void clear() {
        mValues.clear();
        mValues.trimToSize();
    }

    public void setCloseListener(OnMultiModeCloseListener listener) {
        this.closeListener = listener;
    }


    public ArrayList<E> getValues() {
        return mValues;
    }

    public boolean compareTo(String q, E value) {
        return false;
    }

    public void filter(String q) {
        q = q.toLowerCase();
        if (mCleanValues == null) {
            mCleanValues = new ArrayList<>(mValues.size());
            mCleanValues.addAll(mValues);
        }

        mValues.clear();
        if (TextUtils.isEmpty(q)) {
            mValues.addAll(mCleanValues);

            mCleanValues.clear();
            mCleanValues.trimToSize();
            mCleanValues = null;
            notifyDataSetChanged();
            return;
        }

        final int sizeFriends = mCleanValues.size();
        for (int i = 0; i < sizeFriends; i++) {
            E value = mCleanValues.get(i);

            if (compareTo(q, value)) {
                mValues.add(value);
            }
        }
        notifyDataSetChanged();
    }


    public void remove(int index) {
        mValues.remove(index);
    }

    public void remove(E value) {
        mValues.remove(value);
    }

    public Context getContext() {
        return context;
    }

    public LayoutInflater getInflater() {
        return inflater;
    }

    public ThemeManagerOld getThemeManager() {
        return tm;
    }

    public boolean isInMultiSelectMode() {
        return !mSelectedItems.isEmpty();
    }

    public boolean isSelectedItem(E value) {
        return mSelectedItems.contains(value);
    }

    public void setUnselectedItem(E value) {
        if (isSelectedItem(value)) {
            mSelectedItems.remove(value);
            notifyDataSetChanged();

            if (mSelectedItems.isEmpty()) {
                if (closeListener != null) closeListener.onClose();
            }
        }

    }


    public void setSelectedItem(E value) {
        if (!isSelectedItem(value)) {
            mSelectedItems.add(value);
            notifyDataSetChanged();
        }
    }

    public void toggleSelection(E value) {
        if (isSelectedItem(value)) {
            setUnselectedItem(value);
        } else {
            setSelectedItem(value);
        }
    }

    public void disableMultiSelectMode() {
        mSelectedItems.clear();
        notifyDataSetChanged();

        if (closeListener != null) closeListener.onClose();
    }

    public ArrayList<E> getSelectedItems() {
        return mSelectedItems;
    }


    public interface OnMultiModeCloseListener {
        void onClose();
    }

}
