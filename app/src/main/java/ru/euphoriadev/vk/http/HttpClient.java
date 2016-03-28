package ru.euphoriadev.vk.http;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import ru.euphoriadev.vk.BuildConfig;
import ru.euphoriadev.vk.async.ThreadExecutor;
import ru.euphoriadev.vk.common.AppLoader;

/**
 * Created by Igor on 20.03.16.
 *
 * A Simple HTTP library based on {@link HttpURLConnection}
 * to make HTTP requests and access to response.
 * Supports all standard protocols (HTTP, HTTPS, other...).
 *
 * Are requests asynchronous? Yes,
 * use {@link #execute(HttpRequest, HttpRequest.OnResponseListener)}.
 *
 * Support Android 2.2 (Froyo) and older? Yes.
 * Prior to Android 2.2 (Froyo), {@link HttpURLConnection} class
 * had some frustrating bugs. Works around this by disabling connection pooling
 *
 * Can I get access to the not verified site? Yes.
 * Connection allowed to all hosts.
 *
 * Support caching to disk by url? Yes.
 * use {@link HttpRequest.Builder#enableDiskCache(boolean)}
 */
public class HttpClient {
    public static final String TAG = "HttpClient";
    public static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String CACHE_DIR = "HttpCache";

    private static HttpCache cache;

    /** Default hostname verifier */
    private static final HostnameVerifier VERIFIER = new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true; // Just allow them all
        }
    };

    private final static TrustManager[] TRUST_ALL_CERTS = new TrustManager[] {
            new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null; // Not relevant.
                }
                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    // Do nothing. Just allow them all.
                }
                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    // Do nothing. Just allow them all.
                }
            }
    };


    static {
        // Work around pre-Froyo bugs in HTTP connection reuse
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
        disableCheckVerification();
        enableCache();
    }

    private static void disableCheckVerification() {
        System.setProperty("jsse.enableSNIExtension", "false");
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, TRUST_ALL_CERTS, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // allow connection to all hosts
        HttpsURLConnection.setDefaultHostnameVerifier(VERIFIER);
    }

    private static void enableCache() {
        File cacheDir = new File(AppLoader.getLoader().getAppDir(), CACHE_DIR);
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        cache = new HttpCache(cacheDir);

        // Response cache available only on ICS+
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
//            try {
//                HttpResponseCache.install(cacheDir, 15 * 1024 * 1024); // 10 MB
//            } catch (IOException e) {
//                if (DEBUG) e.printStackTrace();
//                Log.e(TAG, "HTTP response cache installation failed.");
//            }
//        }

    }

    /**
     * Executes the HTTP Request.
     * Before sending a request it is advisable to check your Internet connection,
     * use method {@link ru.euphoriadev.vk.util.AndroidUtils#hasConnection(Context)}
     */
    public static HttpResponse execute(HttpRequest request) {
        if (request.usesCache() && request.isGet()) {
            // get the response from disk cache
            HttpResponse cacheResponse = getResponseFromCache(request);
            if (cacheResponse != null) {
                // there are in the cache
                return cacheResponse;
            }
        }
        if (request.policy == HttpRequest.CachePolicy.ONLY_IF_CACHED) {
            // if not in cache, don't loads data from the network
            return null;
        }

        HttpResponse response = HttpResponse.create(request);
        HttpURLConnection connection = null;
        try {
            connection = createConnection(request);
            response.setMessage(connection.getResponseMessage());
            response.setCode(connection.getResponseCode());
            InputStream content = connection.getInputStream();

            // if server returned content in gzip
            String encoding = connection.getHeaderField("Content-Encoding");
            if (encoding != null && "gzip".equalsIgnoreCase(encoding)) {
                content = new GZIPInputStream(content);
            }

            response.contentLength = connection.getContentLength();
            log("Content Length:" + response.contentLength);
            response.inputStream = content;
            if (request.readAfterConnect) {
                // read fully content and disconnect
                response.readFully();
                connection.disconnect();
            }
            if (request.usesCache() && request.isGet()) {
                // save response to disk cache
                putResponseToCache(request, response);
            }
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof HttpException) {
                response.exception = (HttpException) e;
            } else {
                response.exception = new HttpException(e.getMessage(), response.code());
            }
            if (connection != null) {
                InputStream errorStream = connection.getErrorStream();
                if (errorStream != null) {
                    response.inputStream = errorStream;
                }
            }
        }

        return response;
    }


    /**
     * Executes the GET Request
     * @param url URL address at which to send request
     */
    public static HttpResponse executeGet(String url) {
        return execute(HttpRequest.builder(url)
                .method(HttpRequest.METHOD_GET)
                .build());
    }

    /**
     * Executes the POST Request
     * @param url URL address at which to send request
     */
    public static HttpResponse executePost(String url) {
        return execute(HttpRequest.builder(url)
                .method(HttpRequest.METHOD_POST)
                .build());
    }


    /**
     * Executes ASYNC HTTP request,
     * listener must be called from the main (UI) Thread.
     *
     * @param request  the request that should be sent to http server
     * @param listener callback for async successfully execute
     */
    public static void execute(final HttpRequest request, final HttpRequest.OnResponseListener listener) {
        ThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                request.listener = listener;
                HttpResponse response = execute(request);
                runOnUi(new SuccessWorker(listener, response));
            }
        });
    }


    private static void runOnUi(Runnable runnable) {
        AppLoader.getLoader().getHandler().post(runnable);
    }

    /**
     * Creates a new connection from request
     */
    private static HttpURLConnection createConnection(HttpRequest request) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(request.url).openConnection();
        connection.setReadTimeout(request.readTimeout);
        connection.setConnectTimeout(request.connectionTimeout);
        connection.setDoInput(true);
        connection.setDoOutput(request.isPost());
        connection.setRequestMethod(request.method);
        connection.setRequestProperty(HttpHeaders.USER_AGENT, request.userAgent);

        // enable gzip compression
        if (request.enableCompression) {
            connection.setRequestProperty("Accept-Encoding", "gzip");
        }

        // Upload body data if method is POST
        boolean post = request.isPost();
        if (post && request.params != null) {
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(request.params.toString().getBytes("UTF-8"));
            outputStream.flush();
        }

        // Upload file to server if method is POST
        HttpEntity entry = request.entry;
        if (post && entry != null) {
            connection.addRequestProperty(HttpHeaders.CONTENT_TYPE, entry.getContentType());
            long length = entry.getContentLength();
            if (length != -1) {
                // fixed length for performance
               setFixedLength(connection, length);
            } else {
                connection.setChunkedStreamingMode(8384);
            }
            OutputStream outputStream = connection.getOutputStream();
            // write data to connection stream

            entry.writeTo(outputStream);
            outputStream.flush();
            outputStream.close();
        }
        return connection;
    }

    private static void setFixedLength(HttpURLConnection connection, long length) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // Fixed length available only on ICS+
            connection.setFixedLengthStreamingMode(length);
        } else {
            connection.addRequestProperty(HttpHeaders.CONTENT_LENGTH, String.valueOf(length));
        }
        if (DEBUG) Log.i(TAG, "fixed length: " + length);
    }

    private static HttpResponse getResponseFromCache(HttpRequest request) {
        if (cache == null) {
            return null;
        }

        HttpResponse response = cache.get(request);
        if (response != null) {
            return response;
        }
        return null;
    }

    private static void putResponseToCache(HttpRequest request, HttpResponse response) {
        cache.put(request, response);
    }

    private static void log(String message) {
        if (DEBUG) {
            Log.w(TAG, message);
        }
    }

    private static class SuccessWorker implements Runnable {
        private HttpRequest.OnResponseListener listener;
        private HttpResponse response;

        public SuccessWorker(HttpRequest.OnResponseListener listener, HttpResponse response) {
            this.listener = listener;
            this.response = response;
        }

        @Override
        public void run() {
            if (listener != null) {
                if (response.isSuccess()) {
                    listener.onResponse(null, response);
                } else {
                    listener.onError(null, response.getCause());
                }
            }
        }
    }

}
