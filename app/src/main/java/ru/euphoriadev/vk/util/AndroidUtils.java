package ru.euphoriadev.vk.util;


import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Environment;
import android.text.ClipboardManager;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.squareup.picasso.Transformation;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.TeeInputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class AndroidUtils {

    SharedPreferences sPrefs;

    // Укороченный вид Toast"а
    public static void showToast(Context c, String text, boolean longLength) {
        int duration = longLength ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
        Toast.makeText(c, text, duration).show();
    }

    public static void showToast(Context c, int redId, boolean longLength) {
        int duration = longLength ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
        Toast.makeText(c, redId, duration).show();
    }



    // Нужно добавить строчку в manifest:
    // <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
    public static boolean isInternetConnection(Context c) {
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isAvailable() &&
                cm.getActiveNetworkInfo().isConnected());

    }

    public static int calculateInSampleSize(int realHeight, int realWidth,
                                            int reqWidth, int reqHeight) {
        // Реальные размеры изображения
        int inSampleSize = 1;

        if (realHeight > reqHeight || realWidth > reqWidth) {

            final int halfHeight = realHeight / 2;
            final int halfWidth = realWidth / 2;

            // Вычисляем наибольший inSampleSize, который будет кратным двум
            // и оставит полученные размеры больше, чем требуемые
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
            inSampleSize *= 2;
        }
        Log.i("Util", String.format("inSampleSize = %s, real = %sx%s, req = %sx%s", inSampleSize, realHeight, realWidth, realHeight / inSampleSize, reqWidth / inSampleSize));
        return inSampleSize;
    }


    public static Bitmap processingBitmap(Bitmap src) {
        Bitmap dest = Bitmap.createBitmap(
                src.getWidth(), src.getHeight(), src.getConfig());

        for (int x = 0; x < src.getWidth(); x++) {
            for (int y = 0; y < src.getHeight(); y++) {
                // получим каждый пиксель
                int pixelColor = src.getPixel(x, y);
                // получим информацию о прозрачности
                int pixelAlpha = Color.alpha(pixelColor);
                // получим цвет каждого пикселя
                int pixelRed = Color.red(pixelColor);
                int pixelGreen = Color.green(pixelColor);
                int pixelBlue = Color.blue(pixelColor);
                // перемешаем цвета
                int newPixel = Color.argb(
                        pixelAlpha, pixelBlue, pixelRed, pixelGreen);

                // полученный результат вернём в Bitmap
                dest.setPixel(x, y, newPixel);
            }
        }
        return dest;
    }


    public static int dpFromPx(final Context context, final int px) {
        return (int) (px / context.getResources().getDisplayMetrics().density);
    }

    public static int pxFromDp(final Context context, final int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }


    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * (metrics.densityDpi / 160f);
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px      A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return px / (metrics.densityDpi / 160f);
    }

    /**
     * Копирование текста в Clipboard. Поддерживаются все версии
     *
     * @param context
     * @param text    текст, который необходимо скопировать
     */
    public static void copyTextToClipboard(Context context, String text) {
        if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
                    context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Message", text);
            clipboard.setPrimaryClip(clip);

        }
    }

    public static void setStatusBarColor(Activity activity, View statusBarView) {
        if (statusBarView == null) {
            return;
        }
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
//            AndroidBug5497Workaround.assistActivity(activity);
//            KeyboardUtil keyboardUtil = new KeyboardUtil(activity, activity.findViewById(android.R.id.content));
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//
//            View statusBarView = new View(this);
//            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, getStatusBarHeight());
//            params.gravity = Gravity.TOP;
//            LinearLayout rootLayout = new LinearLayout(this);
//            rootLayout.setLayoutParams(params);
//            rootLayout.addView(statusBarView);
//
//            statusBarView.setLayoutParams(params);
//            statusBarView.setVisibility(View.VISIBLE);
//            ((ViewGroup) getWindow().getDecorView()).addView(rootLayout);
//            //status bar height
//         //   statusBarView.getLayoutParams().height = getStatusBarHeight();
//            statusBarView.setBackgroundColor(color);


            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
//            getWindow().addFlags(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);


            //status bar height
            statusBarView.getLayoutParams().height = getStatusBarHeight(activity);
            statusBarView.setBackgroundColor(ThemeManager.getThemeColorDark(activity));
        } else {
            statusBarView.setVisibility(View.GONE);
        }
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static String convertStreamToString(InputStream inputStream) {
        try {
            return ru.euphoriadev.vk.api.Utils.convertStreamToString(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static InputStream clone(InputStream input) {
        if (input == null) {
            return null;
        }
        ByteArrayOutputStream output = null;
        try {
            output = new ByteArrayOutputStream(1024);
            IOUtils.copy(input, output);
            return new ByteArrayInputStream(output.toByteArray());
        } catch (Exception e) {
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

    public static void drawText(String text, Bitmap bitmap) {
        Canvas canvas = new Canvas(bitmap);
        TextPaint textPaint = new TextPaint();
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(Color.WHITE);
        textPaint.setAntiAlias(true);
        textPaint.setTypeface(Typeface.SERIF);
        textPaint.setTextSize(pxFromDp(AppLoader.appContext, 12));

        canvas.drawText(text, bitmap.getHeight() / 2, bitmap.getWidth() / 2, textPaint);
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }


    /**
     * Checks if external storage is available for read and write
     */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * Checks if external storage is available to at least read
     */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    public static long getWordsCount(String from) {
        if (TextUtils.isEmpty(from)) {
            return 0;
        }
        long count = 1;
        for (int i = 0; i < from.length(); i++) {
            char c = from.charAt(i);
            if (c == ' ') {
                ++count;
            }
        }
        return count;
    }

    public static void runOnUi(Runnable runnable) {
        AppLoader.getLoader().getHandler().post(runnable);
    }


    public static class PicassoBlurTransform implements Transformation {
        public int radius;

        public PicassoBlurTransform(int radius) {
            this.radius = radius;
        }

        @Override
        public Bitmap transform(Bitmap source) {
            return FastBlur.doBlur(source, radius);
        }

        @Override
        public String key() {
            return "blur_photo";
        }
    }
}

	 
