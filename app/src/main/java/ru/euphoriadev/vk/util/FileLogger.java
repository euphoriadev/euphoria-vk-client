package ru.euphoriadev.vk.util;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;

import ru.euphoriadev.vk.BuildConfig;
import ru.euphoriadev.vk.api.Api;
import ru.euphoriadev.vk.api.model.Newsfeed;
import ru.euphoriadev.vk.helper.FileHelper;

/**
 * Created by Igor on 20.11.15.
 *
 * Класс помощник, который записывает логи приложения на SD карту,
 * с помощью оптимизированного {@link FileHelper}
 *
 * С каждым запуском создается новый файлик с датой,
 * который можно найти в Euphoria/Logs/log_...
 *
 * Так же {@link FileLogger} предоставляет возможность отлавливать исключения
 * посредством переопределения слушателя {@link java.lang.Thread.UncaughtExceptionHandler}
 * При возникновении ошибка {@link Throwable} сначала записывается в файлик,
 * потом уже происходит "вылет" приложения
 *
 * Для очистки всех логов можно воспользоваться методом {@link #cleanup()}
 */
public class FileLogger {
    public static final String TAG = "FileLogger";
    public static final String LOGS_DIR = "Logs";
    private static FileLogger instance;

    private AppLoader appLoader;
    private SimpleDateFormat sdf;
    private File currentFile;
    private static Thread.UncaughtExceptionHandler oldHandler = Thread.getDefaultUncaughtExceptionHandler();

    public static final Thread.UncaughtExceptionHandler DEFAULT_ERROR_HANDLER = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            e("FileLogger", "Unchecked ERROR!", ex);

            if (oldHandler != null) {
                oldHandler.uncaughtException(thread, ex);
            }
        }
    };

    public static synchronized FileLogger get() {
        if (instance == null) {
            instance = new FileLogger();
        }
        return instance;
    }

    private FileLogger() {
        appLoader = AppLoader.getLoader();
        sdf = new SimpleDateFormat("dd.MM.yyyy.HH:mm:ss");
        if (!appLoader.writeLog) {
            return;
        }

        File dir = new File(appLoader.getExternalFilesDir().getAbsolutePath() + "/" + AppLoader.APP_DIR + "/" + LOGS_DIR);
        dir.mkdirs();

        currentFile = new File(dir, "log_".concat(sdf.format(System.currentTimeMillis())).concat(".txt"));
        try {
            currentFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (BuildConfig.DEBUG) {
            Log.w(TAG, "onCreate");
        }
        try {
            FileHelper.writeText(currentFile, "[----- start log ".concat(sdf.format(System.currentTimeMillis()).concat(" -----]".concat("\n"))), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void i(String tag, String message) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, message);
        }

        FileLogger logger = get();
        if (!logger.appLoader.writeLog) {
            return;
        }

        FileHelper.writeText(logger.currentFile, logger.getFormatedText(tag, message, "I", null), true);

    }

    public static void w(String tag, String message) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, message);
        }

        FileLogger logger = get();
        if (!logger.appLoader.writeLog) {
            return;
        }

        FileHelper.writeText(logger.currentFile, logger.getFormatedText(tag, message, "W", null), true);
    }

    public static void e(String tag, String message, Throwable e) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, message);
        }

        FileLogger logger = get();
        if (!logger.appLoader.writeLog) {
            return;
        }

        FileHelper.writeText(logger.currentFile, logger.getFormatedText(tag, message, "E", e), true);
    }

    public static void e(String tag, String message) {
        FileLogger.e(tag, message, null);
    }

    private String getFormatedText(String tag, String message, String state, Throwable e) {
        StringBuilder buffer = new StringBuilder(32);
        buffer.append(sdf.format(System.currentTimeMillis()));
        buffer.append(" ");
        buffer.append(state);
        buffer.append("/");
        buffer.append(tag);
        buffer.append(": ");
        buffer.append(message);
        buffer.append("\n");

        if (e != null) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));

            buffer.append(errors.toString());
            buffer.append("\n");


            try {
                errors.flush();
                errors.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            errors = null;
        }
        buffer.append("\n");
        return buffer.toString();
    }

    /**
     * Очистка всех логов приложения,
     * TODO: Удаляет файлы, только если они начинаются с "log_"
     */
    public static void cleanup() {
        FileLogger logger = get();
        File dir = new File(AppLoader.getLoader().getExternalFilesDir().getAbsolutePath() + "/" + AppLoader.APP_DIR + "/" + LOGS_DIR);
        if (!dir.exists()) {
            return;
        }
        File[] filesLog = dir.listFiles();
        for (File file : filesLog) {
            if (file.getName().startsWith("log_")) {
                file.delete();
            }
        }
        
    }

}
