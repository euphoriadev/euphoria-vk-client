package ru.euphoriadev.vk;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;
import pl.droidsonroids.gif.LibraryLoader;
import ru.euphoriadev.vk.api.model.VKPhoto;
import ru.euphoriadev.vk.http.HttpClient;
import ru.euphoriadev.vk.http.HttpRequest;
import ru.euphoriadev.vk.http.HttpResponse;
import ru.euphoriadev.vk.util.AndroidUtils;
import ru.euphoriadev.vk.util.FastBlur;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by Igor on 14.02.16.
 */
public class PhotoViewerActivity extends BaseThemedActivity {
    public static final String PHOTO_URL = "photo_url";
    public static final String IS_GIF = "is_gif";
    public static final String PHOTO_URL_PLACEHOLDER = "photo_url_placeholder";

    private ImageView imageView;
    private ProgressBar progressBar;
    private GifImageView gifImageView;
    private PhotoViewAttacher attacher;

    public static void start(Context context, String url, boolean isGif) {
        start(context, url, null, isGif);
    }

    public static void start(Context context, String url, Bitmap placeholder, boolean isGof) {
        Intent starter = new Intent(context, PhotoViewerActivity.class);
        starter.putExtra(PHOTO_URL, url);
        starter.putExtra(IS_GIF, isGof);
        starter.putExtra(PHOTO_URL_PLACEHOLDER, AndroidUtils.bytesFrom(placeholder));
        context.startActivity(starter);
    }

    public static void start(Context context, VKPhoto photo) {
        String url = photo.src_xxbig;
        if (TextUtils.isEmpty(url)) {
            url = photo.src_xbig;
            if (TextUtils.isEmpty(url)) {
                url = photo.src_big;
                if (TextUtils.isEmpty(url)) {
                    url = photo.src;
                    if (TextUtils.isEmpty(url)) {
                        url = photo.src_small;
                    }
                }
            }
        }
        start(context, url, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_viewwer);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        imageView = (ImageView) findViewById(R.id.ivPhotoView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        final String url = getIntent().getStringExtra(PHOTO_URL);
        boolean isGif = getIntent().getBooleanExtra(IS_GIF, false);
        if (isGif) {
            LibraryLoader.initialize(this);
            imageView.setVisibility(View.GONE);

            displayGif(url);
            return;
        }
        byte[] placeholder = getIntent().getByteArrayExtra(PHOTO_URL_PLACEHOLDER);

        if (!TextUtils.isEmpty(url)) {
            progressBar.setIndeterminate(true);
            Picasso.with(this)
                    .load(url)
                    .noFade()
                    .placeholder(placeholder == null ? new ColorDrawable(Color.BLACK) : new BitmapDrawable(getResources(), FastBlur.doBlur(AndroidUtils.bitmapFrom(placeholder), 10)))
                    .config(Bitmap.Config.ARGB_8888)
                    .priority(Picasso.Priority.HIGH)
                    .into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            ViewCompat.setLayerType(imageView, ViewCompat.LAYER_TYPE_HARDWARE, null);
                            progressBar.setVisibility(View.GONE);

                            attacher = new PhotoViewAttacher(imageView);
                            attacher.setMaximumScale(attacher.getMaximumScale() * 2);

                        }

                        @Override
                        public void onError() {

                        }
                    });
        }
    }

    private void displayGif(String url) {
        gifImageView = (GifImageView) findViewById(R.id.ivGiftPhoto);
        gifImageView.setVisibility(View.VISIBLE);
        ViewCompat.setLayerType(gifImageView, ViewCompat.LAYER_TYPE_HARDWARE, null);

        final HttpRequest request = HttpRequest.builder(url).build();
        HttpClient.execute(request, new HttpRequest.SimpleOnResponseListener() {
            @Override
            public void onResponse(HttpClient client, HttpResponse response) {
                try {
                    progressBar.setVisibility(View.GONE);
                    InputStream stream = response.getContent();
                    if (stream != null) {
                        GifDrawable drawable = new GifDrawable(stream.markSupported() ? stream : new BufferedInputStream(stream));
                        gifImageView.setImageDrawable(drawable);

                        if (attacher == null) {
                            attacher = new PhotoViewAttacher(gifImageView);
                            attacher.setMaximumScale(attacher.getMaximumScale() * 2);
                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onProgress(char[] buffer, int progress, long totalSize) {
                super.onProgress(buffer, progress, totalSize);
                progressBar.setProgress(progress);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        WebView webView = (WebView) findViewById(R.id.webViewGif);
//        if (webView.getVisibility() == View.VISIBLE) {
//            webView.removeAllViews();
//            webView.clearCache(true);
//            webView.destroy();
//            webView = null;
//        }
        if (gifImageView != null && gifImageView.getDrawable() != null) {
            GifDrawable drawable = (GifDrawable) gifImageView.getDrawable();
            drawable.recycle();
        }
        if (attacher != null) {
            attacher.cleanup();
            attacher = null;
        }
    }
}
