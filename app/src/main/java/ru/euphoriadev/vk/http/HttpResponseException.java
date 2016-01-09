package ru.euphoriadev.vk.http;

import java.io.IOException;

/**
 * Created by Igor on 11.12.15.
 * <p/>
 * Thrown when response return not
 * {@link java.net.HttpURLConnection#HTTP_OK}
 *
 * @see DefaultHttpClient
 * @see HttpResponse
 */
public class HttpResponseException extends IOException {
    private static final long serialVersionUID = 1L;

    public HttpResponseException(String message) {
        super(message);
    }
}
