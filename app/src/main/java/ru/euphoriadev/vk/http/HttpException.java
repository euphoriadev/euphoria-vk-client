package ru.euphoriadev.vk.http;


import java.io.IOException;

/**
 * Created by Igor on 18.01.16.
 * <p/>
 * Thrown when response return not
 * {@link java.net.HttpURLConnection#HTTP_OK}
 */
public class HttpException extends IOException {
    public static final int NO_CONNECTION_ERROR = -100;
    public String responseMessage;
    public int responseCode;

    public HttpException(String responseMessage, int responseCode) {
        super(responseMessage);
        this.responseMessage = responseMessage;
        this.responseCode = responseCode;
    }
}
