package ru.euphoriadev.vk.util;

import android.content.Context;
import android.net.http.HttpResponseCache;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;

/**
 * Created by Igor on 18.01.16.
 * <p/>
 * A Async HTTP client
 * It can be used to send requests to server with {@link HttpURLConnection}
 * Supports all standard protocols (HTTP, HTTPS, other...).
 */
public class AsyncHttpClient implements Closeable {
    public static final String TAG = "AsyncHttpClient";
    public static final int DEFAULT_THREADS_SIZE = 1;
    private Context mContent;
    private int mThreadsSize;
    private ExecutorService mExecutor;

    /**
     * Create a new AsyncHttpClient with default threads size of pool executor
     *
     * @param context the context for access on resources
     */
    public AsyncHttpClient(Context context) {
        this(context, DEFAULT_THREADS_SIZE);
    }

    /**
     * Create a new AsyncHttpClient with thread pool executor
     *
     * @param context      the context for access on resources
     * @param threadsCount a fixed number of threads pool executor
     */
    public AsyncHttpClient(Context context, int threadsCount) {
        if (threadsCount <= 0) {
            threadsCount = DEFAULT_THREADS_SIZE;
        }
        this.mContent = context;
        this.mThreadsSize = threadsCount;
    }

    /**
     * Execute HTTP request sync
     *
     * @param request the request that should be sent to http server
     * @return response
     * @throws HttpResponseCodeException when responseCode return not HTTP_OK
     */
    public HttpResponse execute(HttpRequest request) throws HttpResponseCodeException {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(request.params == null ? request.url : request.params.join(request.url)).openConnection();
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);
            connection.setUseCaches(false);
            connection.setDoOutput(request.isPost());
            connection.setDoInput(true);
            connection.setRequestMethod(request.methodName);
            if (request.enableCompression) {
                connection.setRequestProperty("Accept-Encoding", "gzip");
            }
            connection.setRequestProperty("User-Agent", HttpRequest.DEFAULT_USER_AGENT);
            if (request.isPost()) {
                connection.getOutputStream().write(request.params.toString().getBytes("UTF-8"));
            }

            int responseCode = connection.getResponseCode();
            String responseMessage = connection.getResponseMessage();

            Log.i(TAG, "response code: " + responseCode);
            if (responseCode != HttpURLConnection.HTTP_OK) {
                // error...
                Log.e(TAG, "Server returned response code: " + responseCode);
                throw new HttpResponseCodeException(responseMessage, responseCode);
            }

            InputStream is = connection.getInputStream();
            String encoding = connection.getHeaderField("Content-Encoding");
            if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
                is = new GZIPInputStream(is);
            }
            return new HttpResponse(request, is, responseMessage, responseCode);
            // MalformedURLException | ConnectException | UnsupportedEncodingException | ProtocolException | HttpResponseCodeException
        } catch (IOException e) {
            e.printStackTrace();

            if (e instanceof HttpResponseCodeException) {
                throw (HttpResponseCodeException) e;
            } else throw new HttpResponseCodeException(e.getMessage(), HttpResponseCodeException.NETWORK_ERROR);
        }
    }


    /**
     * Execute ASYNC HTTP request,
     * listener must be called from the main (UI) Thread.
     *
     * @param request  the request that should be sent to http server
     * @param listener callback for async successfully execute
     */
    public void execute(final HttpRequest request, final HttpRequest.OnResponseListener listener) {
        if (mExecutor == null) {
            mExecutor = Executors.newFixedThreadPool(mThreadsSize);
        }
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final HttpResponse httpResponse = execute(request);
                    AndroidUtils.runOnUi(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null && httpResponse != null) {
                                listener.onResponse(AsyncHttpClient.this, httpResponse);
                            }
                        }
                    });
                } catch (HttpResponseCodeException e) {
                    e.printStackTrace();
                    if (listener != null) {
                        listener.onError(AsyncHttpClient.this, e);
                    }
                }
            }
        });
    }

    /**
     * Close this http client and release resources
     */
    @Override
    public void close() {
        if (mExecutor != null) {
            mExecutor.shutdownNow();
            mExecutor = null;
        }
    }

    /**
     * Class for configuration the http request
     */
    public static class HttpRequest {
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

    public static class HttpParams {
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

                buffer.append(entry.getKey());
                buffer.append("=");

                try {
                    buffer.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                buffer.append("&");
            }

            return buffer.toString();
        }
    }

    public static class HttpResponse {
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

    /**
     * Thrown when response return not
     * {@link java.net.HttpURLConnection#HTTP_OK}
     */
    public static class HttpResponseCodeException extends IOException {
        public static final int NETWORK_ERROR = -100;
        public String responseMessage;
        public int responseCode;

        public HttpResponseCodeException(String responseMessage, int responseCode) {
            super(responseMessage);
            this.responseMessage = responseMessage;
            this.responseCode = responseCode;
        }
    }
}
