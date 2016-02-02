package ru.euphoriadev.vk.http;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import ru.euphoriadev.vk.util.AndroidUtils;

/**
 * Created by Igor on 18.01.16.
 */
public class HttpResponse {
    private InputStream inputStream;
    private String inputStreamAsString;

    public HttpRequest request;
    public String responseMessage;

    public int responseCode;

    public HttpResponse(HttpRequest request, InputStream inputStream, String responseMessage, int responseCode) {
        this.request = request;
        this.inputStream = inputStream;
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
    }

    /**
     * Return inputStream of this HttpResponse
     */
    public InputStream getContent() {
        return inputStream;
    }

    /**
     * Return converted this {@link InputStream} to {@link String} and release
     */
    public String getContentAsString() {
        if (inputStreamAsString == null) {
            inputStreamAsString = AndroidUtils.convertStreamToString(inputStream);
            release();
        }
        return inputStreamAsString;
    }

    /**
     * Return converted this {@link InputStream} to {@link JSONObject} and release
     */
    public JSONObject getContentAsJson() {
        try {
            return new JSONObject(getContentAsString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Closes this stream, release resources
     */
    public void release() {
        if (inputStream != null) {
            try {
                inputStream.close();
                inputStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String toString() {
        return getContentAsString();
    }
}
