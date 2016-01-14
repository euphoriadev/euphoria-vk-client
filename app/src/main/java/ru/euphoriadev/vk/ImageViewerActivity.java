package ru.euphoriadev.vk;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.util.ArrayList;

import ru.euphoriadev.vk.api.Api;
import ru.euphoriadev.vk.api.model.VKFullUser;
import ru.euphoriadev.vk.util.Account;

/**
 * Created by user on 25.04.15.
 */
public class ImageViewerActivity extends Activity {

    Api api;
    Account account;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final WebView webView = new WebView(this);
        webView.setBackgroundColor(Color.BLACK);

        // Поддержка масштабирования
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        // Включаем кеш
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
       // полосы прокрутки – внутри изображения, увеличение места для просмотра
        webView.setScrollbarFadingEnabled(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        setContentView(webView);

        api = Api.get();

        final long user_id = getIntent().getExtras().getLong("user_id");

        final ArrayList<Long> uids = new ArrayList<>();
        uids.add(user_id);

//        ImageView view = new ImageView(this);
//        view.setLayoutParams(new LinearLayout.LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.MATCH_PARENT));
//
//        view.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher));
//
//        webView.addView(view);

        new AsyncTask<Void, Void, Void>() {

            String photo;

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    ArrayList<VKFullUser> profiles = api.getProfilesFull(uids, null, "photo_400_orig", null, null, null);

                    for (VKFullUser user : profiles) {
                        photo = user.photo_400_orig;
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            webView.loadUrl(photo);
                        }
                    });

                } catch (Throwable e) {
                    e.printStackTrace();
                }
                return null;
            }

        }.execute();

    }
}