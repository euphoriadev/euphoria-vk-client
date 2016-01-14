package ru.euphoriadev.vk.http;


import android.util.Log;

import java.io.InputStream;

/**
 * Created by Igor on 11.12.15.
 */
public class HttpResponse {
    public HttpBaseRequest request;
    public int responseCode;
    public String responseMessage;

    Object result;

    public HttpResponse(HttpBaseRequest request, InputStream is, int responseCode, String responseMessage) {
        this.result = request.createResult(is);
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.request = request;
    }


    @Override
    public String toString() {
        Log.w("HttpTextClient", result.toString());
        return result.toString();
    }


    public Object getResult() {
        return request;
    }

}
