package ru.euphoriadev.vk.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by user on 13.12.15.
 */
public class HttpPostFileRequest extends HttpPostRequest {
    private File mFile;

    public HttpPostFileRequest(String url, String body) {
        super(url, body);
    }

    public HttpPostFileRequest(String url, File file) {
        super(url);
        this.mFile = file;
    }

    /**
     * Write file data in {@link OutputStream}
     * Trying is not bad coding, so close stream after use :D
     *
     * @param os OutputStream of this {@link java.net.HttpURLConnection}
     */
    @Override
    protected void writeLargeTo(OutputStream os) {
        super.writeLargeTo(os);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(mFile);
            int read;
            byte[] buffer = new byte[4056];

            while ((read = fis.read(buffer)) != -1) {
                os.write(buffer, 0, read);
            }
            buffer = null;
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
