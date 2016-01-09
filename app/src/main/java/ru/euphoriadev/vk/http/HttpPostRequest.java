package ru.euphoriadev.vk.http;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * Created by user on 11.12.15.
 */
public class HttpPostRequest extends HttpGetRequest {

    private String body;

    public HttpPostRequest(String url, String body) {
        super(url);
        this.body = body;
    }

    public HttpPostRequest(String url) {
        this(url, null);
    }

    @Override
    public byte[] getBody() {
        try {
            return mParams == null ? body.getBytes() : mParams.toString().getBytes();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getRequestMethod() {
        return "POST";
    }

    @Override
    public boolean isPost() {
        return true;
    }

}
