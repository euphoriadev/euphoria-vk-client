package ru.euphoriadev.vk.util;


import android.animation.Animator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.ClipboardManager;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.EdgeEffect;
import android.widget.ListView;
import android.widget.Toast;

import com.squareup.picasso.Transformation;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashSet;

import ru.euphoriadev.vk.BuildConfig;
import ru.euphoriadev.vk.R;
import ru.euphoriadev.vk.SettingsFragment;
import ru.euphoriadev.vk.adapter.DocsAdapter;
import ru.euphoriadev.vk.common.AppLoader;
import ru.euphoriadev.vk.common.PrefManager;
import ru.euphoriadev.vk.common.ThemeManager;
import ru.euphoriadev.vk.helper.DBHelper;
import ru.euphoriadev.vk.helper.FileHelper;
import ru.euphoriadev.vk.http.HttpClient;
import ru.euphoriadev.vk.http.HttpException;
import ru.euphoriadev.vk.http.HttpRequest;
import ru.euphoriadev.vk.http.HttpResponse;

public class AndroidUtils {

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
    public static boolean hasConnection(Context c) {
        if (c == null) {
            return false;
        }
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

    public static int getDisplayWidth(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels;
    }


    public static int getDisplayHeight(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.heightPixels;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void circularReveal(View rootLayout) {
        int cx = rootLayout.getWidth() / 2;
        int cy = rootLayout.getHeight() / 2;

        float finalRadius = Math.max(rootLayout.getWidth(), rootLayout.getHeight());

        // create the animator for this view (the start radius is zero)
        Animator circularReveal = ViewAnimationUtils.createCircularReveal(rootLayout, cx, cy, 0, finalRadius);
        circularReveal.setDuration(1000);

        // make the view visible and start the animation
        rootLayout.setVisibility(View.VISIBLE);
        circularReveal.start();
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

    public static Drawable setFilter(Drawable drawable, int color) {
        if (drawable == null) {
            return null;
        }
        ColorFilter colorFilter = new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY);
        drawable.setColorFilter(colorFilter);
        return drawable;
    }

    public static byte[] bytesFrom(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    public static View getViewByPosition(int position, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (position < firstListItemPosition || position > lastListItemPosition ) {
            return listView.getAdapter().getView(position, null, listView);
        } else {
            final int childIndex = position - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

//    public static ColorStateList createColorStateList(int defaultColor, int selectedColor) {
//        final int[][] states = new int[2][];
//        final int[] colors = new int[2];
//        int i = 0;
//
//        states[i] = View.SELECTED_STATE_SET;
//        colors[i] = selectedColor;
//        i++;
//
//        // Default enabled state
//        states[i] = View.EMPTY_STATE_SET;
//        colors[i] = defaultColor;
//
//        return new ColorStateList(states, colors);
//    }

    public static ColorStateList createColorStateList(int selectedColor, int defaultColor) {
        final int[][] states = new int[2][];
        final int[] colors = new int[2];
        int i = 0;

        states[i] = new int[] { android.R.attr.state_selected };
        colors[i] = selectedColor;
        i++;

        // Default enabled state
        states[i] = new int[] { android.R.attr.state_empty };
        colors[i] = defaultColor;

        return new ColorStateList(states, colors);
    }

    public static File rename(File file, String newName) {
        return new File(file.getParent(), newName);
    }

    public static Bitmap bitmapFrom(byte[] bytes) {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }


    public static int dpFromPx(final Context context, final int px) {
        return (int) (px / context.getResources().getDisplayMetrics().density);
    }

    public static int pxFromDp(final Context context, final int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

    public static long getCurrentSizeBy(int progress, long totalSize) {
//      long result = (progress / 100) * totalSize;
        return (totalSize * progress) / 100;
    }

    public static String convertBytes(long sizeBytes) {
        return DocsAdapter.convertBytes(sizeBytes);
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
        if (!input.markSupported()) {
            input = new BufferedInputStream(input);
        }
        ByteArrayOutputStream output = null;
        try {
            output = new ByteArrayOutputStream(8192);
            IOUtils.copy(input, output);
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
        post(runnable);
    }

    public static void checkDatabase(Context context, SQLiteDatabase database) {
        if (database == null || !database.isOpen()) {
            database = DBHelper.get(context).getWritableDatabase();
        }
    }

    public static HashSet<Integer> keySet(SparseArray array) {
        HashSet<Integer> set = new HashSet<>(array.size());
        for (int i = 0; i < array.size(); i++) {
            set.add(array.keyAt(i));
        }
        return set;
    }

    public static String[] split(String string, char delimiter) {
        int n = 1;
        int i = 0;
        while (true) {
            i = string.indexOf(delimiter, i);
            if (i == -1) break;
            n++;
            i++;
        }
        if (n == 1) return new String[]{string};

        String[] result = new String[n];
        n = 0;
        i = 0;
        int start = 0;
        while (true) {
            i = string.indexOf(delimiter, start);
            if (i == -1) break;
            result[n++] = string.substring(start, i);
            start = i + 1;
        }
        result[n] = string.substring(start);
        return result;
    }

    public static String join(String[] strings, char separator) {
        StringBuilder buffer = new StringBuilder();
        boolean first = true;
        for (String string : strings) {
            if (!first) {
                buffer.append(separator);
            } else {
                first = false;
            }

            buffer.append(string);
        }
        return buffer.toString();
    }


    public static void openUrlInBrowser(Context context, String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(browserIntent);
    }

    public static void checkUpdate(final Context context, final boolean forceCheck) {
        boolean isCheckUpdate = PrefManager.getBoolean(SettingsFragment.KEY_CHECK_UPDATE, true);
        if (!isCheckUpdate && !forceCheck) {
            return;
        }
        long lastUpdateTime = PrefManager.getLong(SettingsFragment.LAST_UPDATE_TIME);
        if (!forceCheck && (System.currentTimeMillis() - lastUpdateTime) <= 24 * 60 * 60 * 1000) {
            return;
        }

        HttpRequest request = HttpRequest.builder(SettingsFragment.UPDATE_URL).build();
        HttpClient.execute(request, new HttpRequest.SimpleOnResponseListener() {
            @Override
            public void onResponse(HttpClient client, HttpResponse response) {
                PrefManager.putLong(SettingsFragment.LAST_UPDATE_TIME, System.currentTimeMillis());

                JSONObject json = response.asJson();
                if (BuildConfig.VERSION_CODE >= json.optInt("version_code")) {
                    if (!forceCheck) {
                        return;
                    }
                    AndroidUtils.runOnUi(new Runnable() {
                        @Override
                        public void run() {
                            showToast(context, "Update not found", true);
                        }
                    });
                    return;
                }

                createUpdateDialog(context, json);
            }

            @Override
            public void onError(HttpClient client, final HttpException exception) {
                runOnUi(new Runnable() {
                    @Override
                    public void run() {
                        showToast(context, "Error! " + exception.getMessage(), true);
                    }
                });
            }
        });
    }

    private static void createUpdateDialog(Context context, final JSONObject json) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.update))
                .setMessage(context.getString(R.string.found_new_version) + json.optString("version") + "\n" + context.getString(R.string.download_ask))
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FileHelper.downloadFileWithDefaultManager(json.optString("url"), "Euphoria.apk", "application/vnd.android.package-archive");
                    }
                });

        builder.create().show();
    }

    public static void post(Runnable runnable) {
        AppLoader.getLoader().getHandler().post(runnable);
    }

    /**
     * Return a drawable object associated with a particular resource ID.
     * <p/>
     * Starting in {@link android.os.Build.VERSION_CODES#LOLLIPOP}, the returned
     * drawable will be styled for the specified Context's theme.
     *
     * @param drawableRed the desired resource identifier, as generated by the aapt tool.
     *                    This integer encodes the package, type, and resource entry.
     *                    The value 0 is an invalid identifier.
     * @return drawable An object that can be used to draw this resource.
     */
    public static Drawable getDrawable(Context context, int drawableRed) {
        return ContextCompat.getDrawable(context, drawableRed);
    }

    /**
     * Returns a color associated with a particular resource ID
     * <p/>
     * Starting in {@link android.os.Build.VERSION_CODES#M}, the returned
     * color will be styled for the specified Context's theme.
     *
     * @param colorRed the desired resource identifier, as generated by the aapt
     *                 tool. This integer encodes the package, type, and resource
     *                 entry. The value 0 is an invalid identifier.
     * @return a single color value in the form 0xAARRGGBB.
     * @throws android.content.res.Resources.NotFoundException if the given ID
     *                                                         does not exist.
     */
    public static int getColor(Context context, int colorRed) {
        return ContextCompat.getColor(context, colorRed);
    }

    public static void setEdgeGlowColor(AbsListView listView, int color) {
        try {
            Class<?> clazz = AbsListView.class;
            Field fEdgeGlowTop = clazz.getDeclaredField("mEdgeGlowTop");
            Field fEdgeGlowBottom = clazz.getDeclaredField("mEdgeGlowBottom");
            fEdgeGlowTop.setAccessible(true);
            fEdgeGlowBottom.setAccessible(true);
            setEdgeEffectColor((EdgeEffect) fEdgeGlowTop.get(listView), color);
            setEdgeEffectColor((EdgeEffect) fEdgeGlowBottom.get(listView), color);
        } catch (Throwable ignored) {
        }
    }

    public static void setEdgeEffectColor(EdgeEffect edgeEffect, int color) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                edgeEffect.setColor(color);
                return;
            }
            Field edgeField = EdgeEffect.class.getDeclaredField("mEdge");
            Field glowField = EdgeEffect.class.getDeclaredField("mGlow");
            edgeField.setAccessible(true);
            glowField.setAccessible(true);
            Drawable mEdge = (Drawable) edgeField.get(edgeEffect);
            Drawable mGlow = (Drawable) glowField.get(edgeEffect);
            mEdge.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            mGlow.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            mEdge.setCallback(null); // free up any references
            mGlow.setCallback(null); // free up any references
        } catch (Exception ignored) {

        }
    }

    public void roundBitmap(Bitmap source) {
        BitmapShader shader;
        shader = new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(shader);

        final int width = source.getWidth();
        final int height = source.getHeight();
        RectF rect = new RectF(0.0f, 0.0f, width, height);

        // rect contains the bounds of the shape
        // radius is the radius in pixels of the rounded corners
        // paint contains the shader that will texture the shape
        final int radius = Math.abs(Math.min(width, height)) / 2;

        Canvas canvas = new Canvas(source);
        canvas.drawRoundRect(rect, radius, radius, paint);
    }

    public static class PicassoBlurTransform implements Transformation {
        public int radius;

        public PicassoBlurTransform(int radius) {
            this.radius = radius;
        }

        @Override
        public Bitmap transform(Bitmap source) {
            Bitmap blurred = FastBlur.doBlur(source, radius);
            if (blurred != source) {
                source.recycle();
                source = null;
            }
            return blurred;
        }

        @Override
        public String key() {
            return "blur_photo";
        }
    }

    public static class RoundedTransformation implements Transformation {
        int pixels;

        public RoundedTransformation(int pixels) {
            this.pixels = pixels;
        }

        @Override
        public Bitmap transform(Bitmap source) {
            Bitmap output = Bitmap.createBitmap(source.getWidth(), source
                    .getHeight(), source.getConfig());
            Canvas canvas = new Canvas(output);

            final int color = 0xff424242;
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, source.getWidth(), source.getHeight());
            final RectF rectF = new RectF(rect);
            final float roundPx = pixels;

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(source, rect, rect, paint);

            if (source != output) {
                source.recycle();
                source = null;
            }

            return output;
        }

        @Override
        public String key() {
            return "round";
        }
    }

}

	 
