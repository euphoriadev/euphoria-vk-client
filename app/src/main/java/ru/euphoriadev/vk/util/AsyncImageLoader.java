package ru.euphoriadev.vk.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Looper;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.euphoriadev.vk.http.AsyncHttpClient;


/**
 * Created by Igor on 27.07.15.
 *
 * Асинхронный загрузчик ихображений,
 * кэширование изображений в память приложения, а так же на SD карту.
 * ВНИМАНИЕ! Данные загрузчик не предназначен для списков! {@link android.widget.ListView}
 *
 * @see DiskCache
 * @see MemoryCache
 */
public class AsyncImageLoader {

    private static final String TAG = "AsyncImageLoader";
    private static volatile AsyncImageLoader instance;
    private Context context;
    private ExecutorService executor;
    private BitmapDecoder decoder;
    private DiskCache diskCache;
    private MemoryCache memoryCache;
    private BitmapDisplayer displayer;
    private AsyncHttpClient httpClient;
    private OnCompleteListener listener;

    private AsyncImageLoader(Context context) {
        this.context = context;
        this.decoder = new BitmapDecoder();
        this.displayer = new BitmapDisplayer();
        this.memoryCache = new MemoryCache((int) (Runtime.getRuntime().maxMemory() / 4));
        this.diskCache = new DiskCache();
        this.executor = Executors.newFixedThreadPool(2);
        this.httpClient = new AsyncHttpClient(null);

    }

    /**
     * Получение экземпляра. SINGLETON.
     *
     * @param context
     * @return
     */
    public static AsyncImageLoader get(Context context) {
        if (instance == null) {
            synchronized (AsyncImageLoader.class) {
                if (instance == null) {
                    instance = new AsyncImageLoader(context);
                }
            }
        }
        return instance;
    }

    public void setListener(OnCompleteListener listener) {
        this.listener = listener;
    }

    /**
     * Загрузка изображения из сети и изменение его к требуемым нам размерам
     * TODO: указать 0,0 если нужно получить оригинальное ихображение без изменения размера
     * @param width Попытка привести ширину изображения к width
     * @param height Попытка привести высоту изображения к height
     */
    public void displayImage(final ImageView iv, final String path, final int width, final int height) {
        Bitmap memoryBitmap = getBitmapFromMemoryCache(path);
        if (memoryBitmap != null) {
         //   Log.i(TAG, "bitmap from MEMORY cache via path: " + path);
            iv.setImageBitmap(memoryBitmap);

            if (listener != null) listener.onComplete(iv, memoryBitmap, path);
            return;
        } else {
            iv.setImageDrawable(null);
        }

        final WeakReference<ImageView> imageViewReference = new WeakReference<ImageView>(iv);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Bitmap dickBitmap = getBitmapFromDiskCache(path);
                    if (dickBitmap != null) {
                        //   Log.i(TAG, "bitmap from DISK cache via path: " + path);
                        addBitmapToMemoryCache(path, dickBitmap);
                        displayer.display(iv, dickBitmap);

                        if (listener != null) listener.onComplete(iv, dickBitmap, path);
                        return;
                    }

                    HttpURLConnection connection = (HttpURLConnection) new URL(path).openConnection();
                    connection.setUseCaches(false);
                    connection.setDoInput(true);
                    connection.connect();
                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        connection.disconnect();
                        return;
                    }

                    Bitmap decodeBitmap;
                    if (height != 0 && width != 0) {
                        decodeBitmap = decoder.decodeWithSampleSize(connection.getInputStream(), width, height);
                    } else {
                        decodeBitmap = decoder.decodeStream(connection.getInputStream());
                    }

                    if (imageViewReference != null && decodeBitmap != null) {
                        if (listener != null) {
                            listener.onComplete(iv, decodeBitmap, path);
                        }


                        final ImageView imageView = imageViewReference.get();
                        if (imageView != null) {
                            displayer.display(imageView, decodeBitmap);
                        }

                        addBitmapToDiskCache(path, decodeBitmap);
                        addBitmapToMemoryCache(path, decodeBitmap);
                    }
                    connection.disconnect();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void displayImage(ImageView iv, String path) {
        displayImage(iv, path, 0, 0);
    }

    public Bitmap loadBitmapSync(String url) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.connect();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                connection.disconnect();
                return null;
            }

            return decoder.decodeStream(connection.getInputStream());
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
     * Получание изображение из memory кеша
     * TODO: вернут null если изображение нет в кеше
     * @param path URL путь картинки
     * @return Bitmap
     */
    private Bitmap getBitmapFromMemoryCache(String path) {
        return memoryCache.get(path);
    }

    /**
     * Добавление изображение в memory кеш приложения
     * @param path URL путь к изображению
     * @param bitmap текущее изображение, которое надо поместить в кеш
     */
    private void addBitmapToMemoryCache(String path, Bitmap bitmap) {
        if (getBitmapFromMemoryCache(path) == null) {
            memoryCache.add(path, bitmap);
        }
    }

    /**
     * Думаю, тут тоже уже понятно, получаение ихображеиня с SD карты
     * Вернет null, если изображения нет на SD
     * @param path ссылка к изображению
     * @return
     */
    private synchronized Bitmap getBitmapFromDiskCache(String path) {
        Bitmap bitmap = null;
        try {
            File file = diskCache.get(path);
            if (file.exists()) {
                bitmap = decoder.decodeFile(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * Добавление ихображение на SD карту изображение
     * @param path ссылка на изображение
     * @param bitmap само изображение
     */
    private synchronized void addBitmapToDiskCache(String path, Bitmap bitmap) {
        File file = diskCache.get(path);
        if (file.exists()) {
            return;
        }
        diskCache.save(path, bitmap);
    }

    /**
     * Очистка всех ихображений из memory кеша приложения
     */
    public void clearMemoryCache() {
        memoryCache.clear();
    }

    /**
     * Очистка всех сохраненных ихображений с SD карты
     */
    public void clearDickCache() {
        diskCache.clear();
    }

    /**
     * Очистка и memory кеша, и disk кеша
     */
    public void clearCache() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                memoryCache.clear();
                diskCache.clear();
            }
        }).start();
    }

    public void stop() {
        memoryCache.clear();
        executor.shutdown();
    }
    /**
     * Получение оптимизированного декодера изображения
     * @return
     */
    public BitmapDecoder getDecoder() {
        return decoder;
    }

    public interface OnCompleteListener {

        /**
         * Вызывается при удачной загрузке изображение
         *
         * @param view   View, куда было загруженно изображение
         * @param bitmap собственно само изображение
         * @param path   ссылка на изображение
         */
        void onComplete(ImageView view, Bitmap bitmap, String path);
    }

    private static class Utils {
        public static int calculateInSampleSize(
                BitmapFactory.Options options, int reqWidth, int reqHeight) {
            // Raw height and width of image
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {

                final int halfHeight = height / 2;
                final int halfWidth = width / 2;

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while ((halfHeight / inSampleSize) > reqHeight
                        && (halfWidth / inSampleSize) > reqWidth) {
                    inSampleSize *= 2;
                }

            }

            return inSampleSize;
        }
    }

    public class BitmapDecoder {

        /**
         * Докодирований потока с options. Тут все и так понятно
         * @param is
         * @param options
         * @return
         */
        public Bitmap decodeStream(InputStream is, BitmapFactory.Options options) {
            Bitmap bitmap = null;
            try {
                if (!is.markSupported()) {
                    is = new BufferedInputStream(is, 8192);
                }
                bitmap = BitmapFactory.decodeStream(is, null, options);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return bitmap;
        }

        public Bitmap decodeStream(InputStream is) {
            return decodeStream(is, null);
        }


        public Bitmap decodeResource(int resId) {
            return decodeStream(context.getResources().openRawResource(resId, new TypedValue()));
        }

        /**
         * Докидирования файла,
         * TODO: работает на порядок быстрее обычного, засчет BufferedInputStream
         * @param file
         * @param options
         * @return
         */
        public Bitmap decodeFile(File file, BitmapFactory.Options options) {
            try {
                return decodeStream(new FileInputStream(file), options);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }

        public Bitmap decodeFile(File file) {
            return decodeFile(file, null);
        }

        /**
         * Попытка привести размер Bitmap в width и height.
         * Докидирования размеров изображение, вычисление насколько можно сжать
         * @param is
         * @param width
         * @param height
         * @return
         */
        public Bitmap decodeWithSampleSize(InputStream is, int width, int height) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            BufferedInputStream bis = new BufferedInputStream(is);
            BitmapFactory.decodeStream(bis, null, options);
            try {
                bis.reset();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.i(TAG, "outHeight " + options.outHeight);
            Log.i(TAG, "outWidth  " + options.outWidth);

            options.inSampleSize = Utils.calculateInSampleSize(options, width, height);

            options.inJustDecodeBounds = false;
            options.inMutable = true;
//          options.inDither = true;
//          options.inPreferQualityOverSpeed = true;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            return decodeStream(bis, options);
        }

    }

    private class BitmapDisplayer {
        public void display(final ImageView iv, final Bitmap bitmap) {
            if (isMainThread()) {
                iv.setImageBitmap(bitmap);
            } else {
                iv.post(new Runnable() {
                    @Override
                    public void run() {
                        iv.setImageBitmap(bitmap);
                    }
                });
            }
        }

        private boolean isMainThread() {
            return Looper.getMainLooper().getThread() == Thread.currentThread();
        }
    }

    /**
     * Собственно сам класс по управление кешом на SD карте
     */
    private class DiskCache {
        public static final String CACHE_DIR = ".EuphoriaCache";
        public final Object mLock = new Object();
        private File cacheDir;

        public DiskCache() {
            cacheDir = new File(AppLoader.getLoader().getAppDir() + "/" + CACHE_DIR);
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
        }

        public File getCacheDir() {
            return cacheDir;
        }

        public void save(String path, Bitmap bitmap) {
            File file = new File(cacheDir, String.valueOf(path.hashCode()));
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fos != null) {
                        fos.flush();
                        fos.close();
                        fos = null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public boolean delete(String path) {
            File file = new File(cacheDir, String.valueOf(path.hashCode()));
            return file.delete();
        }

        public File get(String path) {
            return new File(cacheDir, String.valueOf(path.hashCode()));
        }

        public void clear() {
            File[] files = cacheDir.listFiles();
            for (File file : files) {
                file.delete();
            }
        }
    }

    /**
     * RAM кеш изображений
     */
    private class MemoryCache {
        private long mMaxSize;
        private long mTotalSize;
        private SparseArray<Bitmap> mCache;


        public MemoryCache(long maxSize) {
            this.mMaxSize = maxSize;
            this.mCache = new SparseArray<>();
        }

        /**
         * Текущий размер изображений в байтах
         * @param bitmap изображение
         * @return
         */
        private int sizeOf(Bitmap bitmap) {
            return bitmap.getRowBytes() * bitmap.getHeight();
        }

        /**
         * Добавление изображение в RAM кеш
         * TODO: если размер кеша превзойдет максимальный - он автоматически очиститься
         * @param path URL или пусть к файлу
         * @param bitmap само изображение
         */
        public void add(String path, Bitmap bitmap) {
            if (mTotalSize >= mMaxSize) {
                Log.w(TAG, "WARNING! Memory cache is full!");
                clear();
                return;
            }

            if (mCache.get(path.hashCode()) == null) {
                mCache.append(path.hashCode(), bitmap);

                mTotalSize = mTotalSize + sizeOf(bitmap);
            }
        }

        /**
         * Получение изображение из RAM кеша по ключу
         * TODO: Вернет null, если изображения в кеше нет
         * @param path сам ключ, путь
         * @return
         */
        public Bitmap get(String path) {
            return mCache.get(path.hashCode());
        }

        /**
         * Удаление изображения из RAM кеша
         * @param path путь к изображению, которое надо удалить
         */
        public void remove(String path) {
            Bitmap b = mCache.get(path.hashCode());
            if (b != null && !b.isRecycled()) {
                b.recycle();
                b = null;
            }
            mCache.remove(path.hashCode());

        }

        /**
         * Очистка всех изображений
         */
        public void clear() {
            for (int i = 0; i < mCache.size(); i++) {
                Bitmap b = mCache.valueAt(i);
                if (b != null && !b.isRecycled()) {
                    b.recycle();
                    b = null;
                }
            }

            mCache.clear();
            mTotalSize = 0;
            System.gc();
        }
    }

}
