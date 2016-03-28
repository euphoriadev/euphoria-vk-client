package ru.euphoriadev.vk.http;

import java.io.Serializable;
import java.net.HttpURLConnection;

/**
 * Created by Igor on 02.02.16.
 * <p/>
 * Class for configuration the HTTP request
 */
public class HttpRequest implements Serializable {
    public static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Android; Mobile; rv:13.0) Gecko/13.0 Firefox/13.0";
    public static final int READ_TIMEOUT = 25_000;
    public static final int CONNECT_TIMEOUT = 25_000;

    /** HTTP methods that the user may select */
    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";

    public String url;
    public String method;
    public String userAgent;
    public HttpParams params;
    public HttpEntity entry;
    public int readTimeout;
    public int connectionTimeout;
    public boolean enableCompression;
    public boolean readAfterConnect;
    public OnResponseListener listener;
    public CachePolicy policy;

    /**
     * Designates the policy to use when dealing with disk cache
     */
    public enum CachePolicy {
        /**
         * Don't use the cache, load only from the network
         */
        NO_CACHE,
        /**
         * Content can be cached
         */
        ENABLED,
        /**
         * Don't use the network, load only from the cache
         */
        ONLY_IF_CACHED
    }

    /**
     * Creates a new HTTP Request with configuration
     *
     * @param url               URL address at which to send request
     * @param method            the HTTP request method (GET,POST,PUT,etc.)
     * @param userAgent         the user agent
     * @param params            the params list of url
     * @param entry             the multipart data
     * @param readTimeout       the read time out in milliseconds
     * @param connectionTimeout the connection time out in milliseconds
     * @param useCompress       enable gzip compression, if available
     * @param readAfterConnect  read fully content after success connection
     * @param listener          for listen successfully execute and progress
     */
    public HttpRequest(String url, HttpEntity entry, HttpParams params, String method, String userAgent, int readTimeout, int connectionTimeout,
                       boolean useCompress, boolean readAfterConnect,
                       OnResponseListener listener, CachePolicy policy) {
        this.url = url;
        this.method = method;
        this.params = params;
        this.userAgent = userAgent;
        this.readTimeout = readTimeout;
        this.connectionTimeout = connectionTimeout;
        this.enableCompression = useCompress;
        this.readAfterConnect = readAfterConnect;
        this.entry = entry;
        this.listener = listener;
        this.policy = policy;
    }


    /**
     * Returns true if method is POST
     */
    public boolean isPost() {
        return method.equalsIgnoreCase(METHOD_POST);
    }

    /**
     * Returns true if method is GET
     */
    public boolean isGet() {
        return method.equalsIgnoreCase(METHOD_GET);
    }

    /**
     * Returns true, if disk cache is enabled
     */
    public boolean usesCache() {
        return policy == CachePolicy.ENABLED
                || policy == CachePolicy.ONLY_IF_CACHED;
    }

    /**
     * Returns a new {@link Builder} to make Request
     */
    public static Builder builder(String url) {
        return new Builder(url);
    }

    /**
     * Builder class for {@link HttpRequest}.
     * Provides a convenient way to set various fields to {@link HttpRequest}
     */
    public static class Builder {
        private String url;
        private String method;
        private String userAgent;
        private HttpParams params;
        private HttpEntity entry;
        private int readTimeout;
        private int connectionTimeout;
        private boolean enableCompression;
        private boolean readAfterConnect;
        private OnResponseListener listener;
        private CachePolicy policy;

        public Builder(String url) {
            url(url);
            readTimeout(READ_TIMEOUT);
            connectTimeout(CONNECT_TIMEOUT);
            readAfterConnect(true);
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder readTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        public Builder connectTimeout(int connectTimeout) {
            this.connectionTimeout = connectTimeout;
            return this;
        }

        public Builder params(HttpParams params) {
            this.params = params;
            return this;
        }

        public Builder entry(HttpEntity entry) {
            this.entry = entry;
            return this;
        }

        public Builder enabpleCompression(boolean compression) {
            this.enableCompression = compression;
            return this;
        }

        @Deprecated
        public Builder enableDiskCache(boolean enable) {
            this.policy = enable ? CachePolicy.ENABLED : CachePolicy.NO_CACHE;
            return this;
        }

        public Builder cachePolicy(CachePolicy policy) {
            this.policy = policy;
            return this;
        }

        public Builder readAfterConnect(boolean enable) {
            this.readAfterConnect = enable;
            return this;
        }

        public Builder listener(OnResponseListener listener) {
            this.listener = listener;
            return this;
        }

        public HttpRequest build() {
            if (url == null) {
                throw new IllegalArgumentException("URL cannot be null");
            }

            if (userAgent == null) {
                userAgent = DEFAULT_USER_AGENT;
            }
            if (method == null) {
                method = METHOD_GET;
            }
            if (policy == null) {
                policy = CachePolicy.NO_CACHE;
            }

            url = params == null ?
                    url :
                    params.join(url);

            return new HttpRequest(url, entry, params, method, userAgent,
                    readTimeout, connectionTimeout, enableCompression, readAfterConnect, listener, policy);
        }
    }

    /**
     * Stub/no-op implementations of all methods of {@link OnResponseListener}
     */
    public static class SimpleOnResponseListener implements OnResponseListener {

        @Override
        public void onResponse(HttpClient client, HttpResponse response) {
        }

        @Override
        public void onProgress(char[] buffer, int progress, long totalSize) {
        }

        @Override
        public void onError(HttpClient client, HttpException exception) {
        }
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
        void onResponse(HttpClient client, HttpResponse response);

        /**
         * Called (ONLY if the Content Length is known!)
         * when progress level has changed
         *
         * @param buffer    intermediate buffer data, which are uploading at moment
         * @param progress  current progress in percent, range [0...100]
         * @param totalSize the total length of uploading data in bytes
         */
        void onProgress(char[] buffer, int progress, long totalSize);

        /**
         * Called when response code not equal 200 (HTT_OK) or Network error
         * e.g. user does not have an Internet connection
         *
         * @param client    the client which execute request
         * @param exception the information about error
         */
        void onError(HttpClient client, HttpException exception);
    }
}
