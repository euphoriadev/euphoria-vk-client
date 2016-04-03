package ru.euphoriadev.vk.interfaces;

import ru.euphoriadev.vk.util.AndroidUtils;

/**
 * Created by Igor on 03.04.16.
 *
 * A small version of {@link android.os.AsyncTask}, provided as a {@link Runnable}
 * for execute on {@link Thread}
 */
public abstract class AsyncRunnable implements Runnable {

    /**
     * Override this method to run code on a background thread,
     * you can throw a {@link Exception}, not putting it in a try/catch block :D
     */
    protected abstract void doInBackground() throws Exception;

    /**
     * Override this method to run code on a UI thread,
     * runs after {@link AsyncRunnable#doInBackground()}
     */
    protected abstract void onPostExecute();

    @Override
    public void run() {
        try {
            doInBackground();
        } catch (Exception e) {
            e.printStackTrace();
        }
        AndroidUtils.post(new Runnable() {
            @Override
            public void run() {
                onPostExecute();
            }
        });
    }
}
