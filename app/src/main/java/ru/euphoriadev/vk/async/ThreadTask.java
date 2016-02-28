package ru.euphoriadev.vk.async;

import android.os.Handler;
import android.os.Looper;
import android.os.Process;

/**
 * Created by Igor on 26.02.16.
 */
public abstract class ThreadTask extends LowThread {
    public static final Handler sHandler = new Handler(Looper.getMainLooper());


    private final Runnable preExecute = new Runnable() {
        @Override
        public void run() {
            onPreExecute();
        }
    };

    private final Runnable postExecute = new Runnable() {
        @Override
        public void run() {
            onPostExecute();
        }
    };

    private final Runnable backgroundExecute = new Runnable() {
        @Override
        public void run() {
            doInBackground();
        }
    };

    protected abstract void onPreExecute();

    protected abstract void doInBackground();

    protected abstract void onPostExecute();


    @Override
    public void run() {
        super.run();

        sHandler.post(preExecute);
        try {
            backgroundExecute.run();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sHandler.post(postExecute);
        }
    }
}
