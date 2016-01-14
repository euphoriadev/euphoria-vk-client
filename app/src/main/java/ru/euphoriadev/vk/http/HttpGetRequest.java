package ru.euphoriadev.vk.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Igor on 11.12.15.
 * <p/>
 * HTTP Request with GET method
 */
public class HttpGetRequest extends HttpBaseRequest<String> {

    public HttpGetRequest(String url) {
        super(url);
    }

    public HttpGetRequest(String url, HttpParams params) {
        super(url, params);
    }

    @Override
    public byte[] getBody() {
        return null;
    }

//    @Override
//    protected String parseResult(InputStream is) {
//        try {
//            return AndroidUtils.convertStreamToString(is);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    public String getRequestMethod() {
        return "GET";
    }

    @Override
    public boolean isPost() {
        return false;
    }

    /**
     * Convert {@link InputStream} to {@link String}
     * using {@link StringBuilder}
     *
     * Conversion happens very fast,
     * so you can call this method in the UI Thread, I think so
     *
     * @param is input stream of the current connection {@link java.net.HttpURLConnection}
     * @return
     */
    @Override
    protected String createResult(InputStream is) {
        StringBuffer buffer = null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(is));
            buffer = new StringBuffer();

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
                buffer.append("\n");
            }
            return buffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (buffer != null) {
                    buffer.setLength(0);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
