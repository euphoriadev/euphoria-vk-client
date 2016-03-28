package ru.euphoriadev.vk.http;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.HttpURLConnection;

import ru.euphoriadev.vk.common.Cache;

/**
 * Created by Igor on 21.03.16.
 */
public class HttpCache implements Cache<HttpRequest, HttpResponse> {
    public static final int DEFAULT_SIZE = 30 * 1024 * 1024; // 30 MB

    private File mCacheDir;
    private int mMaxSize;

    public HttpCache(File cacheDir, int maxSize) {
        this.mCacheDir = cacheDir;
        this.mMaxSize = maxSize;
    }

    public HttpCache(File cacheDir) {
        this(cacheDir, DEFAULT_SIZE);
    }

    @Override
    public boolean put(HttpRequest key, HttpResponse value) {
        String fileName = String.valueOf(key.url.hashCode());
        File file = new File(mCacheDir, fileName);
        if (file.exists()) {
            return false;
        }

        return value.receive(file);
    }

    @Override
    public HttpResponse get(HttpRequest key) {
        String fileName = String.valueOf(key.url.hashCode());
        File file = new File(mCacheDir, fileName);
        if (file.exists()) {
            try {
                String string = FileUtils.readFileToString(file);
                HttpResponse response = HttpResponse.create(key);
                response.setCode(HttpURLConnection.HTTP_OK);
                response.setMessage("OK");
                response.setFromCache(true);
                response.inputStreamAsString = string;
                return response;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public boolean remove(HttpRequest key) {
        String fileName = String.valueOf(key.url.hashCode());
        File file = new File(mCacheDir, fileName);
        return file.delete();
    }

    @Override
    public int size() {
        return mMaxSize;
    }

    @Override
    public void clear() {
        File[] files = mCacheDir.listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
    }
}
