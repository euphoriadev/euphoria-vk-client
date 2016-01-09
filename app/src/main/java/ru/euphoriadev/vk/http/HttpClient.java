package ru.euphoriadev.vk.http;


/**
 * Created by Igor on 11.12.15.
 * <p/>
 * Base class for all HttpClients
 *
 * in general, this class is likely to be deprecated,
 * for why procreating classes? You can transfer startup code in {@link HttpBaseRequest}
 *
 *
 * @see DefaultHttpClient
 */
public interface HttpClient {

    /**
     * Execute HTTP request sync
     *
     * @param request the request that should be sent to http server
     * @return response
     */
    HttpResponse execute(HttpBaseRequest request);

    /**
     * Execute HTTP request for URL
     *
     * @param url a link to server on which it is necessary to send request
     * @return response
     */
    HttpResponse execute(String url);

    /**
     * Execute ASYNC HTTP request,
     * listener must be called from the main (UI) Thread.
     * You can use {@link java.lang.Thread} or {@link android.os.AsyncTask}
     * for async execute
     *
     * @param request
     * @param listener
     */
    void execute(HttpBaseRequest request, OnResponseListener listener);

    /**
     * Callback for Async execute
     */
    interface OnResponseListener {
        /**
         * Called when successfully receiving the response from WEB
         *
         * @param client   a Client who is execute request
         * @param response response from web
         */
        void onResponse(HttpClient client, HttpResponse response);
    }
}
