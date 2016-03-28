package ru.euphoriadev.vk;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import ru.euphoriadev.vk.api.Auth;
import ru.euphoriadev.vk.util.Account;

public class LoginActivity extends Activity {
    private static final String TAG = "Kate.LoginActivity";
    WebView webview;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_webview);

        webview = (WebView) findViewById(R.id.vkontakteview);
        webview.getSettings().setJavaScriptEnabled(true);
        ViewCompat.setLayerType(webview, ViewCompat.LAYER_TYPE_HARDWARE, null);

        // Чтобы получать уведомления об окончании загрузки страницы
        webview.setWebViewClient(new VkontakteWebViewClient());

        // otherwise CookieManager will fall with java.lang.IllegalStateException: CookieSyncManager::createInstance() needs to be called before CookieSyncManager::getInstance()
        //    CookieSyncManager.createInstance(this);

        //    CookieManager cookieManager = CookieManager.getInstance();
        //    cookieManager.removeAllCookie();
        String url = Auth.getUrl(Account.API_ID, Auth.getSettings());
        webview.loadUrl(url);

    }

    private void parseUrl(String url) {
        try {
            if (url == null)
                return;
            Log.i(TAG, "url =" + url);
            if (url.startsWith(Auth.redirect_url)) {
                if (!url.contains("error=")) {
                    String[] auth = Auth.parseRedirectUrl(url);
                    Intent intent = new Intent();
                    intent.putExtra("token", auth[0]);
                    intent.putExtra("user_id", Integer.parseInt(auth[1]));
                    setResult(Activity.RESULT_OK, intent);

                }
                finish();
                //  webview.clearCache(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (webview != null) {
            webview.removeAllViews();
            webview.clearCache(true);
            webview.destroy();
            webview = null;
        }

    }

    class VkontakteWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            parseUrl(url);
        }
    }
}