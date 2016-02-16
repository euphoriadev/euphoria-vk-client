package ru.euphoriadev.vk;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import ru.euphoriadev.vk.api.model.VKPhoto;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by Igor on 14.02.16.
 */
public class PhotoViewerActivity extends BaseThemedActivity {
    public static final String PHOTO_URL = "photo_url";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ImageView imageView = new ImageView(this);
        imageView.setBackgroundColor(Color.BLACK);
        setContentView(imageView);

        String url = getIntent().getStringExtra(PHOTO_URL);
        if (!TextUtils.isEmpty(url)) {
            Picasso.with(this).load(url).into(imageView, new Callback() {
                @Override
                public void onSuccess() {
                    new PhotoViewAttacher(imageView);
                }

                @Override
                public void onError() {

                }
            });
        }
    }

    public static void start(Context context, String url) {
        Intent starter = new Intent(context, PhotoViewerActivity.class);
        starter.putExtra(PHOTO_URL, url);
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
        start(context, url);
    }
}
