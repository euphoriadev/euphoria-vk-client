package ru.euphoriadev.vk.interfaces;

import android.view.View;

/**
 * Created by Igor on 09.02.16.
 * <p/>
 * Definition for a callback to be invoked when a view is twice clicked
 */
public abstract class OnTwiceClickListener implements View.OnClickListener {
    private long mLastClickedTime;

    /**
     * Create a new listener
     */
    public OnTwiceClickListener() {
        mLastClickedTime = System.currentTimeMillis();
    }

    @Override
    public void onClick(View v) {
        if ((mLastClickedTime + 500) > System.currentTimeMillis()) {
            onTwiceClick(v);
        } else {
            mLastClickedTime = System.currentTimeMillis();
        }
    }

    /**
     * Called when view has been twice clicked
     *
     * @param v the view that was clicked
     */
    public abstract void onTwiceClick(View v);


}
