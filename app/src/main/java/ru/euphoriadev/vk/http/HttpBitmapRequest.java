package ru.euphoriadev.vk.http;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.InputStream;

/**
 * Created by Igor on 13.12.15.
 *
 * A Bitmap Request
 */
public class HttpBitmapRequest extends HttpBaseRequest<Bitmap> {


    public HttpBitmapRequest(String url) {
        super(url);
    }

    public HttpBitmapRequest(String url, HttpParams params) {
        super(url, params);
    }

    @Override
    public byte[] getBody() {
        return null;
    }

    @Override
    public String getRequestMethod() {
        return "GET";
    }

    @Override
    public boolean isPost() {
        return false;
    }

    @Override
    protected Bitmap createResult(InputStream is) {
        return BitmapFactory.decodeStream(is);
    }
}
