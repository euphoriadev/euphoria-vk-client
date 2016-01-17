package ru.euphoriadev.vk.http;


import android.os.AsyncTask;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

/**
 * Created by Igor on 11.12.15.
 * <p/>
 * A Default HTTP client
 * It can be used to send requests to server with {@link HttpURLConnection}
 * Supports all standard protocols (HTTP, HTTPS, other...).
 * <p/>
 * For sending request you need to use method {@link HttpClient#execute(HttpBaseRequest)}_
 * {@link HttpGetRequest} for GET method and {@link HttpPostRequest} for POST Method.
 */
public class DefaultHttpClient implements HttpClient {

    /**
     * Empty Constructor
     */
    public DefaultHttpClient() {

    }

    /**
     * Execute HTTP request for send to web server
     * Method by default use {@link HttpURLConnection}
     *
     * @return HTTP Response of request.
     * @throws HttpResponseException if response code not equals {@link HttpURLConnection#HTTP_OK}
     *                               or equals -1 (probably a Network Error)
     */
    @Override
    public HttpResponse execute(HttpBaseRequest request) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(request.getUrl()).openConnection();
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);
            connection.setUseCaches(false);
            connection.setDoOutput(request.isPost());
            connection.setDoInput(true);
            connection.setRequestMethod(request.getRequestMethod());
            connection.setRequestProperty("Accept-Encoding", "gzip");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:13.0) Gecko/13.0 Firefox/13.0");

            if (request.isPost() && request.getBody() != null) {
                connection.getOutputStream().write(request.getBody());
                request.writeLargeTo(connection.getOutputStream());
            }

            final int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                if (request.getErrorListener() != null) {
                    request.getErrorListener().onError(connection.getResponseCode(), connection.getResponseMessage());
                }
                if (responseCode == -1) {
                    throw new HttpResponseException("Network Error");
                } else throw new HttpResponseException("Server returned response code: "
                        + responseCode +
                        " for URL " + connection.getURL());
            }

            InputStream is = connection.getInputStream();
            String encoding = connection.getHeaderField("Content-Encoding");
            if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
                is = new GZIPInputStream(is);
            }
            return new HttpResponse(request, is, responseCode, connection.getResponseMessage());
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }

    /**
     * Execute HTTP request by URL.
     * In case of an error return null
     *
     * @param url URL of server
     * @return HTTP Response
     */
    @Override
    public HttpResponse execute(String url) {
        try {
            return execute(new HttpGetRequest(url));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Execute ASYNC with {@link AsyncTask}. If successful,
     * listener will be called in UI Thread
     *
     * @param request
     * @param listener
     */
    @Override
    public void execute(HttpBaseRequest request, OnResponseListener listener) {
        new AsyncHttpTask(listener).execute(request);
    }

    /**
     * Async task for execute HTTP Requests
     */
    private class AsyncHttpTask extends AsyncTask<HttpBaseRequest, Void, HttpResponse> {
        private OnResponseListener listener;

        public AsyncHttpTask(OnResponseListener listener) {
            this.listener = listener;
        }

        @Override
        protected HttpResponse doInBackground(HttpBaseRequest... params) {
            try {
                return DefaultHttpClient.this.execute(params[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(HttpResponse response) {
            super.onPostExecute(response);
            if (listener != null && response != null) {
                listener.onResponse(DefaultHttpClient.this, response);
            }
        }

    }
}
