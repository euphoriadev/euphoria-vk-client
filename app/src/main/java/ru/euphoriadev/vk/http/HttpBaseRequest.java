package ru.euphoriadev.vk.http;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

/**
 * Created by Igor on 11.12.15.
 * <p/>
 * Base class for all http requests
 *
 * @see HttpGetRequest
 * @see HttpPostRequest
 * @see HttpPostFileRequest
 * @see HttpBitmapRequest
 */
public abstract class HttpBaseRequest<E> {

    protected HttpParams mParams;
    private String mUrl;
    private OnErrorResponseListener errorListener;

    /**
     * Create new HTTP Request
     *
     * @param url URL address at which to send request
     */
    public HttpBaseRequest(String url) {
        this.mUrl = url;
    }

    /**
     * Create a new HTTP Request with params
     *
     * @param url    URL address at which to send request, example: "http://google.com"
     * @param params
     */
    public HttpBaseRequest(String url, HttpParams params) {
        this.mUrl = url;
        this.mParams = params;
    }

    public void setParams(HttpParams params) {
        this.mParams = params;
    }

    /**
     * Return this url of Request
     */
    public String getUrl() {
        return mParams == null ? mUrl : mParams.join(mUrl);
    }

    public OnErrorResponseListener getErrorListener() {
        return errorListener;
    }

    public void setErrorListener(OnErrorResponseListener listener) {
        this.errorListener = listener;
    }

    /**
     * Add params to Request
     */
    public void addParam(String key, String value) {
        if (mParams == null) {
            mParams = new HttpParams();
        }
        mParams.addParam(key, value);
    }

    /**
     * Return body in bytes if method is POST
     * Will be written to {@link java.io.OutputStream} after execute
     * <p/>
     * It is not recommended to return big amount of data,
     * for it may be a {@link java.lang.OutOfMemoryError}
     * <p/>
     * To do this, use {@link #writeLargeTo(java.io.OutputStream)}
     */
    public abstract byte[] getBody();

    /**
     * Write large data in {@link java.net.HttpURLConnection}
     * it is recommended to use method only when a large volume of data.
     * This method performed before getting response of {@link HttpURLConnection#getInputStream()}
     * <p/>
     * For example, you can write file to server
     *
     * @param os OutputStream of this {@link java.net.HttpURLConnection}
     * @see HttpPostFileRequest#writeLargeTo(OutputStream)
     */
    protected void writeLargeTo(OutputStream os) {

    }

    /**
     * Parsed result from Stream
     * Subclasses must implement this to parse the raw network response.
     * <p/>
     * The stream closed after using this method,
     * therefore you should not store a reference to it/use in the future
     */
    protected E createResult(InputStream is) {
        return null;
    }

    /**
     * Return the method for this request
     */
    public abstract String getRequestMethod();

    /**
     * You should return true, if method is POST
     *
     * @see #getRequestMethod
     */
    public abstract boolean isPost();

    /**
     * Callback for unsuccessful execute of {@link HttpURLConnection}
     */
    public interface OnErrorResponseListener {
        /**
         * Called when response code not equal 200 (OK) {@link HttpURLConnection#HTTP_OK}
         * @param responseCode response code of connection
         * @param responseMessage response message of connection
         */
        void onError(int responseCode, String responseMessage);
    }


}
