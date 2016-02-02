package ru.euphoriadev.vk.http;


import java.io.IOException;

/**
 * Created by Igor on 18.01.16.
 *
 * Thrown when response return not
 * {@link java.net.HttpURLConnection#HTTP_OK}
 */
public class HttpResponseCodeException extends IOException {
    public static final int NETWORK_ERROR = -100;
    public String responseMessage;
    public int responseCode;

    public HttpResponseCodeException(String responseMessage, int responseCode) {
        super(responseMessage);
        this.responseMessage = responseMessage;
        this.responseCode = responseCode;
    }
}
