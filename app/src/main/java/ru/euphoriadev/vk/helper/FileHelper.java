package ru.euphoriadev.vk.helper;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import ru.euphoriadev.vk.util.AppLoader;


/**
 * Created by Igor on 12.07.15.
 * <p/>
 * File helper Allows you to quickly perform any operations
 */
public class FileHelper {

    private BufferedWriter mWriter;

    /**
     * Reading a text file, this method is optimized,
     * however it is not recommended to read a large text file,
     * because it may be {@link OutOfMemoryError error}
     *
     * @param file File to read
     * @return Text of Reading file
     */
    public static String readText(File file) {
        if (file == null) {
            return null;
        }
        if (!file.exists()) {
            return null;
        }
        BufferedReader reader = null;
        StringBuffer buffer = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            buffer = new StringBuffer(128);
//            String readLine;
//            while ((readLine = reader.readLine()) != null) {
//                buffer.append(readLine);
//                buffer.append("\n");
//            }

            char[] cBuff = new char[8192];
            int read = 0;
            while ((read = reader.read(cBuff)) != -1) {
                for (int i = 0; i < read; i++) {
                    buffer.append(cBuff[i]);
                }
            }
            cBuff = null;
            return buffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (buffer != null) {
                buffer.setLength(0);
            }
            if (reader != null) {
                try {
                    reader.close();
                    reader = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return "";
    }

    public static void writeText(File file, String text) {
        FileHelper.writeText(file, text, false);
    }

    /**
     * Write byte array in file
     *
     * @param file  the file to write bytes
     * @param bytes the byte array for write
     */
    public static void writeBytes(File file, byte[] bytes) {
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(file));
            bos.write(bytes);
            bos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Inverse operation - Write text to File
     *
     * @param file   that we need to write text
     * @param text   that you want to write
     * @param append indicates whether or not to append to an existing file
     */
    public static void writeText(File file, String text, boolean append) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file, append));
            writer.write(text);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.flush();
                    writer.close();
                    writer = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Write all text in one file from other files
     *
     * @param target the non-null File to write text to
     * @param files  the file from which to get the text to write to {@code target}
     */
    public static void joinFiles(File target, File[] files) {
        for (int i = 0; i < files.length; i++) {
            File f = files[i];

            final String textOfFile = readText(f);
            if (textOfFile == null) {
                continue;
            }

            writeText(target, textOfFile);
        }

    }

    /**
     * Convert {@link InputStream} to {@link String}
     *
     * @param inputStream the Stream that want to convert to a string
     */
    public static String streamToString(InputStream inputStream) {
        StringBuilder buffer = new StringBuilder(128);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
                buffer.append('\n');

            }
            return buffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                buffer.setLength(0);
                reader.close();

                buffer = null;
                reader = null;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return "";
    }

    /**
     * Copy data from one file to another
     *
     * @param source the from file
     * @param toFile the to file
     */
    public static void copyFile(File source, File toFile) {
        FileOutputStream outputStream = null;
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(source);
            outputStream = new FileOutputStream(toFile);

            byte[] buffer = new byte[8192];
            int read;

            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            buffer = null;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Getting the number of lines from File
     *
     * @param file File to get line count
     * @return line count
     * @throws IOException if {@code file} does not exist
     */
    public static int countLines(File file) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(file), 8192);
        try {
            byte[] cBuffer = new byte[8192];
            int count = 0;
            int readChars = 0;
            boolean empty = true;
            while ((readChars = is.read(cBuffer)) != -1) {
                empty = false;
                for (int i = 0; i < readChars; ++i) {
                    if (cBuffer[i] == '\n') {
                        ++count;
                    }
                }
            }
            cBuffer = null;
            return (count == 0 && !empty) ? 1 : count;
        } finally {
            is.close();
        }
    }

    /**
     * Getting the number of words from File
     *
     * @param file File to get words count
     * @return words count
     * @throws IOException if {@code file} does not exist
     */
    public static int countWords(File file) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(file), 8192);
        try {
            byte[] cBuffer = new byte[8192];
            int countWords = 1;
            int readChars = 0;
            while ((readChars = is.read(cBuffer)) != -1) {
                for (int i = 0; i < readChars; ++i) {
                    if (cBuffer[i] == ' ') {
                        ++countWords;
                    }
                }
            }
            cBuffer = null;
            return countWords;
        } finally {
            is.close();
        }
    }

    /**
     * Getting the number of chars from File
     *
     * @param file File to get chars count
     * @return chars count
     * @throws IOException if {@code file} does not exist
     */
    public static int countChars(File file) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(file), 8192);
        try {
            byte[] cBuffer = new byte[8192];
            int countWords = 0;
            int readChars = 0;
            while ((readChars = is.read(cBuffer)) != -1) {
                for (int i = 0; i < readChars; ++i) {
                    ++countWords;
                }
            }
            cBuffer = null;
            return countWords;
        } finally {
            is.close();
        }
    }

    /**
     * Download file from network via URL
     *
     * @param url  the URL to download data
     * @param file the file to save data
     * @throws IOException
     */
    public static void downloadFile(String url, File file) throws IOException {
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        try {
            bis = new BufferedInputStream(new URL(url).openStream());
            fos = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int count;

            while ((count = bis.read(buffer)) != -1) {
                fos.write(buffer, 0, count);
                System.out.println("while...");
            }
            System.err.println("DOBE");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bis != null) {
                bis.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }

    /**
     * Download file via URL with {@link DownloadManager}
     * Only Android
     *
     * @param url   the URL to download data
     * @param title the file name
     */
    public static void downloadFileWithDefaultManager(String url, String title) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        //  request.setDescription("Some descrition");
        request.setTitle(title);
//        request.setMimeType("application/vnd.android.package-archive");
        // in order for this if to run, you must use the android 3.2 to compile your app
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        }
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title);
        // get download service and enqueue file
        DownloadManager manager = (DownloadManager) AppLoader.appContext.getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }

    /**
     * Download file ASYNC. On new {@link Thread}
     *
     * @param url  the URL to download data
     * @param file the file to save data
     */
    public static void downloadFileAsync(final String url, final File file) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    downloadFile(url, file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void downloadFileWithProgress(String url, File file) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);
            connection.setUseCaches(false);
            connection.setDoOutput(false);
            connection.setDoInput(true);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:13.0) Gecko/13.0 Firefox/13.0");
            connection.connect();

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                System.out.println("Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage());
                return;
            }

            // this will be useful to display download percentage
            // might be -1: server did not report the length
            int fileLength = connection.getContentLength();
            System.out.println(fileLength == -1 ? "length of content is not known" : ("Length of content if " + fileLength));

            // download the file
            System.out.println("start download file via url:" + url);
            input = connection.getInputStream();
            output = new FileOutputStream(file);

            byte data[] = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                // allow canceling with back button
//                if (isCancelled()) {
//                    input.close();
//                    return null;
//                }
                total += count;
                // publishing the progress....
                if (fileLength > 0) // only if total length is known
                    System.out.println("Downloading file... " + (total * 100 / fileLength + "%"));
                output.write(data, 0, count);
            }
            System.out.println("File downloaded!");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }

            if (connection != null)
                connection.disconnect();
        }
    }

    /**
     * Open file for writing text
     *
     * @param file The file to open
     * @return true if file is opened successfully, false if an error occurred
     */
    public boolean openFileForWrite(File file) {
        try {
            mWriter = new BufferedWriter(new FileWriter(file, true));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Write text to opened file.
     *
     * @param text The text to write in file
     */
    public void writeText(String text) {
        try {
            mWriter.write(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes the character to opened file
     *
     * @param c the character to write
     */
    public void writeText(char c) {
        try {
            mWriter.write(c);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Flushes this writer. The contents of the buffer are committed to the
     * target writer and it is then flushed
     */
    public void flushBuffer() {
        try {
            mWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * To close the stream to release resources.
     * The text is immediately saved the file after calling this method
     */
    public void endWrite() {
        try {
            mWriter.flush();
            mWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


