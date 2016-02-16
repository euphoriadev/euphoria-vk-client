package ru.euphoriadev.vk.http;


import android.content.Context;
import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;

import ru.euphoriadev.vk.util.AndroidUtils;

/**
 * Created by Igor on 18.01.16.
 * <p/>
 * A Async HTTP client
 * It can be used to send requests to server with {@link HttpURLConnection}
 * Supports all standard protocols (HTTP, HTTPS, other...).
 */
public class AsyncHttpClient implements Closeable {
    public static final String TAG = "AsyncHttpClient";
    public static final int DEFAULT_THREADS_SIZE = 3;
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
//        this.mContent = context;
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

            Log.i(TAG, "code: " + responseCode);
            if (responseCode != HttpURLConnection.HTTP_OK) {
                // error...
                Log.e(TAG, "Server returned response code: " + responseCode);
                throw new HttpResponseCodeException(responseMessage, responseCode);
            }
            InputStream is = AndroidUtils.clone(connection.getInputStream());
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
        } finally {
            if (connection != null) {
                connection.disconnect();
                connection = null;
            }
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

}
