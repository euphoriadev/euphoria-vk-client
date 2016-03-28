package ru.euphoriadev.vk.http;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.Writer;

import ru.euphoriadev.vk.util.AndroidUtils;

/**
 * Created by Igor on 18.01.16.
 * An HTTP response.
 */
public class HttpResponse implements Serializable {
    private HttpRequest mRequest;
    private boolean mFromCache;
    private String mMessage;
    private int mCode;
    private boolean mCancel;

    String inputStreamAsString;
    InputStream inputStream;
    HttpException exception;
    int contentLength = -1;

    public HttpResponse(HttpRequest request, InputStream inputStream, String responseMessage, int responseCode) {
        this.mRequest = request;
        this.inputStream = inputStream;
        this.mCode = responseCode;
        this.mMessage = responseMessage;
    }

    public static HttpResponse create(HttpRequest request) {
        return new HttpResponse(request, null, null, 0);
    }

    /**
     * Returns request that initiated this HTTP response
     */
    public HttpRequest request() {
        return mRequest;
    }

    /**
     * Returns the response code, or -1 if code is unknown
     * @see #isSuccess()
     */
    public int code() {
        return mCode;
    }

    /**
     * Returns the response status code (200, 404,),
     * or null if it is unknown.
     */
    public String message() {
        return mMessage;
    }

    /**
     * Returns a string that contains code and response message,
     * separated by space
     */
    public String statusLine() {
        return code() + " " + message();
    }

    /**
     * Returns true, if response status code is success (2xx), false otherwise
     */
    public boolean isSuccess() {
        return code() >= 200 && code() < 300;
    }

    /**
     * Returns true if the response was loaded from disk.
     * For enable cache use {@link HttpRequest.Builder#enableDiskCache(boolean)}
     */
    public boolean fromCache() {
        return mFromCache;
    }

    /**
     * Return inputStream of this HttpResponse
     */
    public InputStream getContent() {
        return inputStream;
    }

    /**
     * Obtains information about an exception, if it exists
     */
    public HttpException getCause() {
        return exception;
    }

    /**
     * Return converted this {@link InputStream} to {@link String}
     * and release connection
     */
    public String asString() {
        if (inputStreamAsString == null) {
            inputStreamAsString = request().listener == null ?
                    AndroidUtils.convertStreamToString(inputStream)
                    : readStreamAsString(request().listener);
            release();
        }
        return inputStreamAsString;
    }

    /**
     * Return converted this {@link InputStream} to {@link JSONObject} and release.
     * or null if the json parse fails
     */
    public JSONObject asJson() {
        try {
            return new JSONObject(asString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    public boolean receive(Writer writer) {
        try {
            writer.write(asString());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean receive(File file) {
        Writer writer = null;
        try {
            writer = new FileWriter(file);
            return receive(writer);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * Closes this stream, release resources.
     * This operation cannot be reversed, so it should only be
     * called if you are sure there are no further uses for the response.
     * When using {@link #asString()} or {@link #asJson()} don't
     * need to worry about it, because it calls this method,
     * and caches the contents to string
     */
    public void release() {
        request().listener = null;
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                if (HttpClient.DEBUG) e.printStackTrace();
            }
            inputStream = null;
        }
    }

    /**
     * Returns true, if content is closed
     */
    public boolean isReleased() {
        return inputStream == null;
    }

    public BufferedInputStream buffer() {
        return inputStream instanceof BufferedInputStream ?
                (BufferedInputStream) inputStream
                : new BufferedInputStream(inputStream);
    }


    @Override
    public String toString() {
        return asString();
    }

    private String readStreamAsString(HttpRequest.OnResponseListener listener) {
        InputStreamReader reader = new InputStreamReader(getContent());
        StringBuilder builder = new StringBuilder(128);
        char[] buffer = new char[4096];
        try {
            int n;
            int progress = 0;
            while ((n = reader.read(buffer)) != -1) {
                if (mCancel) {
                    break;
                }
                builder.append(buffer, 0, n);
                if (contentLength != -1) {
                    progress += n;
                    onProgress(listener, buffer, (progress * 100 / contentLength), contentLength);
                }
            }
            buffer = null;
            return builder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                builder.setLength(0);
                reader.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        return null;
    }

    private void onProgress(final HttpRequest.OnResponseListener listener, final char[] buffer, final int progress, final long totalSize) {
        if (listener == null) {
            return;
        }
        AndroidUtils.runOnUi(new Runnable() {
            @Override
            public void run() {

                listener.onProgress(buffer, progress, totalSize);
            }
        });
    }

    /* package */ void readFully() {
        InputStream cloned = cloneStream(inputStream);
        if (cloned != null) {
            release(); // prevent memory leak
            inputStream = cloned;
        }
    }

    private InputStream cloneStream(InputStream input) {
        if (input == null) {
            return null;
        }
        if (!input.markSupported()) {
            input = new BufferedInputStream(input);
        }
        ByteArrayOutputStream output = null;
        try {
            output = new ByteArrayOutputStream(8192);

            byte[] buffer = new byte[8192];
            int read;
            int progress = 0;
            while ((read = input.read(buffer)) != -1) {
                if (mCancel) {
                    break;
                }
                output.write(buffer, 0, read);
                if (contentLength != -1) {
                    progress += read;
                    onProgress(request().listener, null, (progress * 100 / contentLength), contentLength);
                }
            }

            return new ByteArrayInputStream(output.toByteArray());
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.flush();
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                output = null;
            }
        }
        return null;
    }

    /* package */ void setCode(int code) {
        this.mCode = code;
    }

    /* package */ void setMessage(String message) {
        this.mMessage = message;
    }

    /* package */ void setFromCache(boolean fromCache) {
        this.mFromCache = fromCache;
    }

}
