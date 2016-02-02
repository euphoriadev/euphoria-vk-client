package ru.euphoriadev.vk.http;

import java.net.HttpURLConnection;

/**
 * Created by Igor on 02.02.16.
 *
 * Class for configuration the http request
 */
public class HttpRequest {
    public static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Android; Mobile; rv:13.0) Gecko/13.0 Firefox/13.0";

    public String url;
    public String methodName;
    public HttpParams params;
    public boolean enableCompression = true;

    /**
     * Create new HTTP Request with GET method name
     *
     * @param url URL address at which to send request
     */
    public HttpRequest(String url) {
        this(url, "GET", null);
    }

    /**
     * Create a new HTTP Request with params
     *
     * @param url        URL address at which to send request, example: "http://google.com"
     * @param methodName the http method name to be used, e.g. GET, POST
     * @param params     the http params list
     */
    public HttpRequest(String url, String methodName, HttpParams params) {
        this.url = url;
        this.methodName = methodName;
        this.params = params;
    }

    /**
     * Returns true if method equals POST
     */
    public boolean isPost() {
        return methodName.equalsIgnoreCase("POST");
    }

    /**
     * Callback for async successfully execute of {@link HttpURLConnection}
     */
    public interface OnResponseListener {
        /**
         * Called when successfully receiving the response from WEB server
         *
         * @param client   the client which execute request
         * @param response the response object from server
         */
        void onResponse(AsyncHttpClient client, HttpResponse response);

        /**
         * Called when response code not equal 200 (HTT_OK) or Network error
         * e.g. user does not have an Internet connection
         *
         * @param client    the client which execute request
         * @param exception the information about error
         */
        void onError(AsyncHttpClient client, HttpResponseCodeException exception);
    }
}
