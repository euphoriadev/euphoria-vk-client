package ru.euphoriadev.vk.util;

import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;

import ru.euphoriadev.vk.BuildConfig;
import ru.euphoriadev.vk.SettingsFragment;

/**
 * Created by Igor on 28.01.16.
 *
 * Keeps track of errors in this application, and writes them to a file on SD
 */
public class CrashManager {
    private static final String TAG = "CrashManager";

    public static final String DIR_NAME = "Logs";
    public static final Thread.UncaughtExceptionHandler EXCEPTION_HANDLER = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            createReportFile(ex);
            if (ex instanceof IndexOutOfBoundsException) {
                // For some users the app crashes with error while loading messages
                PrefManager.putBoolean(SettingsFragment.KEY_USE_ALTERNATIVE_UPDATE_MESSAGES, true);
                Toast.makeText(AppLoader.appContext, "Enabled an alternative method to update the message list", Toast.LENGTH_LONG).show();
            }
            Log.e(TAG, "Create crash report");
            if (sOldHandler != null) {
                sOldHandler.uncaughtException(thread, ex);
            }
        }
    };

    private static final Thread.UncaughtExceptionHandler sOldHandler = Thread.getDefaultUncaughtExceptionHandler();

    private CrashManager() {
        // Empty
    }

    public static void init() {
        Thread.setDefaultUncaughtExceptionHandler(EXCEPTION_HANDLER);
    }

    public static void createReportFile(Throwable ex) {
        try {
            File dir = getLogsDir();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy-HH.mm.ss");
            File crashFile = new File(dir, "log_" + sdf.format(System.currentTimeMillis()) + ".txt");
            if (!crashFile.exists()) {
                crashFile.createNewFile();
            }

            String text = getFormatText(sdf, "Fatal error!", ex.getMessage(), ex);
            FileUtils.write(crashFile, text, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getFormatText(SimpleDateFormat sdf, String tag, String message, Throwable e) {
        StringBuilder buffer = new StringBuilder(32);
        buffer.append("[----- start log ").append(sdf.format(System.currentTimeMillis())).append(" -----]\n");
        buffer.append("----- Device configuration -----\n");
        buffer.append("OS Version: ");
        buffer.append(Build.VERSION.RELEASE).append('\n');
        buffer.append("SDK Version: ");
        buffer.append(Build.VERSION.SDK_INT).append('\n');
        buffer.append("Device: ");
        buffer.append(Build.DEVICE).append('\n');
        buffer.append("Device product: ");
        buffer.append(Build.PRODUCT).append('\n');
        buffer.append("Device manufacturer: ");
        buffer.append(Build.MANUFACTURER).append('\n');
        buffer.append("Device brand: ");
        buffer.append(Build.BRAND).append('\n');
        buffer.append("Device model: ");
        buffer.append(Build.MODEL).append('\n');
        buffer.append("\n");

        buffer.append("----- App Config -----\n");
        buffer.append("Version name: ");
        buffer.append(BuildConfig.VERSION_NAME).append('\n');
        buffer.append("Version code: ");
        buffer.append(BuildConfig.VERSION_CODE).append('\n');
        buffer.append("\n");

        buffer.append("----- Error Stack Trace -----\n");
        buffer.append("Error Tag: ");
        buffer.append(tag);
        buffer.append('\n');
        buffer.append("Error message: ");
        buffer.append(message).append('\n');
        buffer.append("Trace: ");
        buffer.append('\n');

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
        buffer.append("[----- end log ").append(sdf.format(System.currentTimeMillis())).append(" -----]\n");
        return buffer.toString();
    }


    public static File getLogsDir() {
        return new File(AppLoader.getLoader().getAppDir() + "/" + DIR_NAME);
    }

    public static void cleanup() {
        File[] logs = getLogsDir().listFiles();
        for (File log : logs) {
            if (log.getName().startsWith("log_") && log.isFile()) {
                log.delete();
            }
        }
    }

}
