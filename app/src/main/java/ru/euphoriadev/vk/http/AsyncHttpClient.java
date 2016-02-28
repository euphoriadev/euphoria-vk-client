package ru.euphoriadev.vk.http;


import android.app.Activity;
import android.content.Context;
import android.net.http.HttpResponseCache;
import android.os.Build;
import android.util.Log;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;

import ru.euphoriadev.vk.BuildConfig;
import ru.euphoriadev.vk.util.AndroidUtils;
import ru.euphoriadev.vk.common.AppLoader;

/**
 * Created by Igor on 18.01.16.
 * <p/>
 * A Async HTTP client
 * It can be used to send requests to server with {@link HttpURLConnection}
 * Supports all standard protocols (HTTP, HTTPS, other...).
 */
public class AsyncHttpClient implements Closeable {
    public static final String TAG = "AsyncHttpClient";
    public static final String CACHE_DIR = "HttpCache";
    public static final boolean DEBUG = BuildConfig.DEBUG;
    public static final int DEFAULT_THREADS_SIZE = 3;
    private int mThreadsSize;
    private ExecutorService mExecutor;
    private Context mContext;

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
        this.mContext = context;
        this.mThreadsSize = threadsCount;

        // Work around pre-Froyo bugs in HTTP connection reuse
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
        enableCache();
    }

    private void enableCache() {
        // Response cache available only on ICS+
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return;
        }

        File cacheDir = new File(AppLoader.getLoader().getAppDir(), CACHE_DIR);
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        try {
            HttpResponseCache.install(cacheDir, 15 * 1024 * 1024); // 10 MB
        } catch (IOException e) {
            if (DEBUG) e.printStackTrace();
            Log.e(TAG, "HTTP response cache installation failed.");
        }
    }

    private void disableCache() {
        // Response cache available only on ICS+
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return;
        }
        HttpResponseCache cache = HttpResponseCache.getInstalled();
        if (cache != null) {
            cache.flush();
        }
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
            connection.setUseCaches(request.useCaches);
            connection.setDoOutput(request.isPost());
            connection.setDoInput(true);
            connection.setRequestMethod(request.methodName);
            if (request.enableCompression) {
                connection.setRequestProperty("Accept-Encoding", "gzip");
            }

            connection.setRequestProperty("User-Agent", HttpRequest.DEFAULT_USER_AGENT);
            if (request.isPost()) {
                if (request.params != null) {
                    OutputStream outputStream = connection.getOutputStream();
                    outputStream.write(request.params.toString().getBytes("UTF-8"));
                }
                HttpEntity entity = request.entry;
                if (entity != null) {
                    connection.addRequestProperty("Content-Type", entity.getContentType());
                    long length = entity.getContentLength();
                    if (length != -1) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            if (DEBUG) Log.w(TAG, "fixed length: " + length);
                            connection.setFixedLengthStreamingMode(length);
                        } else {
                            connection.addRequestProperty("Content-Length", String.valueOf(length));
                        }

                    } else {
                        connection.setChunkedStreamingMode(8384);
                    }
                    OutputStream outputStream = connection.getOutputStream();

                    if (DEBUG) Log.w(TAG, "write bytes in stream...");
                    entity.writeTo(outputStream);
                    if (DEBUG) Log.w(TAG, "done write bytes");
                }
            }

            int responseCode = connection.getResponseCode();
            String responseMessage = connection.getResponseMessage();

            if (DEBUG) Log.i(TAG, "code: " + responseCode);
            if (responseCode != HttpURLConnection.HTTP_OK) {
                // error...
                if (DEBUG) Log.e(TAG, "Server returned response code: " + responseCode);
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
            if (DEBUG) e.printStackTrace();

            if (e instanceof HttpResponseCodeException) {
                throw (HttpResponseCodeException) e;
            } else
                throw new HttpResponseCodeException(e.getMessage(), HttpResponseCodeException.NO_CONNECTION_ERROR);
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
                    final Runnable responseRunnable = new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null && httpResponse != null) {
                                listener.onResponse(AsyncHttpClient.this, httpResponse);
                            }
                        }
                    };
                    if (mContext != null && mContext instanceof Activity) {
                        ((Activity) mContext).runOnUiThread(responseRunnable);
                    } else {
                        AndroidUtils.runOnUi(responseRunnable);
                    }
                } catch (HttpResponseCodeException e) {
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

        disableCache();
    }

}
