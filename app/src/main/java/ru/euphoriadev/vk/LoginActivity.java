package ru.euphoriadev.vk;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import ru.euphoriadev.vk.api.Auth;
import ru.euphoriadev.vk.util.Account;
import ru.euphoriadev.vk.util.FileLogger;

public class LoginActivity extends Activity {
    private static final String TAG = "Kate.LoginActivity";
    WebView webview;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_webview);

        webview = (WebView) findViewById(R.id.vkontakteview);
        webview.getSettings().setJavaScriptEnabled(true);

        // Чтобы получать уведомления об окончании загрузки страницы
        webview.setWebViewClient(new VkontakteWebViewClient());

        // otherwise CookieManager will fall with java.lang.IllegalStateException: CookieSyncManager::createInstance() needs to be called before CookieSyncManager::getInstance()
        //    CookieSyncManager.createInstance(this);

        //    CookieManager cookieManager = CookieManager.getInstance();
        //    cookieManager.removeAllCookie();
        String url = Auth.getUrl(Account.API_ID, Auth.getSettings());
        webview.loadUrl(url);
        webview.clearCache(false);

        webview.destroy();
    }

    private void parseUrl(String url) {
        try {
            if (url == null)
                return;
            FileLogger.i(TAG, "url =" + url);
            if (url.startsWith(Auth.redirect_url)) {
                if (!url.contains("error=")) {
                    String[] auth = Auth.parseRedirectUrl(url);
                    Intent intent = new Intent();
                    intent.putExtra("token", auth[0]);
                    intent.putExtra("user_id", Long.parseLong(auth[1]));
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