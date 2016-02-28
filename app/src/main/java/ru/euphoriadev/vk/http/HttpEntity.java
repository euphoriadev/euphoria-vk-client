package ru.euphoriadev.vk.http;

import java.io.File;
import java.io.OutputStream;

/**
 * Created by Igor on 21.02.16.
 * <p/>
 * Used for build upload multipart data for servers
 */
public abstract class HttpEntity {
    /**
     * file to upload
     */
    public File mFile;

    /**
     * Creates a new HttpEntity with file
     *
     * @param file the file for upload to server
     */
    public HttpEntity(File file) {
        this.mFile = file;
    }

    /**
     * Returns the file to upload
     */
    public File getFile() {
        return mFile;
    }

    /**
     * Used when method is "POST", you must implement to write to {@link OutputStream}
     *
     * @param os the stream to write it
     */
    protected abstract void writeTo(OutputStream os);

    /**
     * Returns a content type
     */
    public String getContentType() {
        return null;
    }

    /**
     * Returns total length in bytes, including the child headers
     */
    public long getContentLength() {
        return mFile.length();
    }
}
