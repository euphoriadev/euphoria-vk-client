package ru.euphoriadev.vk.http;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Igor on 11.12.15.
 */
public class HttpParams {
    private TreeMap<String, String> params;

    public HttpParams() {
        params = new TreeMap<>();
    }

    public void addParam(String key, String value) {
        params.put(key, value);
    }

    public void addParam(String key, int value) {
        addParam(key, String.valueOf(value));
    }

    public void addParam(String key, long value) {
        addParam(key, String.valueOf(value));
    }

    public void addParam(String key, boolean value) {
        addParam(key, value ? "1" : "0");
    }

    public void addParam(String key, Object value) {
        addParam(key, String.valueOf(value));
    }

    public String join(String url) {
        return url.concat("?").concat(toString());
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            buffer.append("&");

            buffer.append(entry.getKey());
            buffer.append("=");
            try {
                buffer.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return buffer.toString();
    }
}
