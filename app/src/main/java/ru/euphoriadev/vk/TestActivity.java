package ru.euphoriadev.vk;

import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import ru.euphoriadev.vk.http.AsyncHttpClient;
import ru.euphoriadev.vk.http.HttpRequest;
import ru.euphoriadev.vk.http.HttpResponse;
import ru.euphoriadev.vk.http.HttpResponseCodeException;
import ru.euphoriadev.vk.util.AndroidUtils;
import ru.euphoriadev.vk.util.Emoji;
import ru.euphoriadev.vk.util.ThemeManager;

/**
 * Created by Igor on 15.07.15.
 */
public class TestActivity extends BaseThemedActivity {
    private AppCompatTextView tvResult;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView rootView = new ScrollView(this);
        setContentView(rootView);

        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        rootLayout.setPadding(0, AndroidUtils.getStatusBarHeight(this), 0, 0);
        rootView.addView(rootLayout);


        tvResult = new AppCompatTextView(this);
        tvResult.setTextColor(ThemeManager.getPrimaryTextColor());
        tvResult.setTextSize(18);
        rootLayout.addView(tvResult);

        // for testing api...
        AppCompatButton buttonCheckConnection = new AppCompatButton(this);
        buttonCheckConnection.setText("Check internet connection");
        buttonCheckConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvResult.append("Network info is available and is connected? \n");
                tvResult.append(String.valueOf(AndroidUtils.isInternetConnection(TestActivity.this)).concat("\n"));
            }
        });
        rootLayout.addView(buttonCheckConnection);

        AppCompatButton buttonClient = new AppCompatButton(this);
        buttonClient.setText("Connect to google.com with AsyncHttpClient");
        buttonClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvResult.append("Start connection to google.com...\n");
                connectToGoogle();
            }
        });
        rootLayout.addView(buttonClient);

        AppCompatButton buttonInterrupted = new AppCompatButton(this);
        buttonInterrupted.setText("Interrupted main thread");
        buttonInterrupted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvResult.append("Stopping main thread...\n");
                try {
                    Thread.currentThread().join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        rootLayout.addView(buttonInterrupted);

        AppCompatButton buttonException = new AppCompatButton(this);
        buttonException.setText("Throws exception");
        buttonException.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvResult.setText("Throws exception!\n");
                throw new RuntimeException("Test exception! Throws DebugActivity");
            }
        });
        rootLayout.addView(buttonException);

        AppCompatButton buttonGC = new AppCompatButton(this);
        buttonGC.setText("Force Garbage Collector");
        buttonGC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvResult.append("Garbage Collector\n");
                System.gc();
            }
        });
        rootLayout.addView(buttonGC);

//        AppCompatButton buttonNative = new AppCompatButton(this);
//        buttonNative.setText("Native method");
//        buttonNative.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                tvResult.append(NativeUtils.helloFromJni());
//                System.gc();
//            }
//        });
//        rootLayout.addView(buttonNative);

        AppCompatButton buttonDownEmoji = new AppCompatButton(this);
        buttonDownEmoji.setText("Download emojis now");
        buttonDownEmoji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvResult.append("Start downloading emojis...");
                Emoji.executeDownloadingTask(TestActivity.this);
            }
        });
        rootLayout.addView(buttonDownEmoji);

        AppCompatButton buttonClear = new AppCompatButton(this);
        buttonClear.setText("Clear text");
        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvResult.setText("");
            }
        });
        rootLayout.addView(buttonClear);

//        FloatingActionsMenu menu = new FloatingActionsMenu(this);
//
//        FloatingActionButton button = new FloatingActionButton(this);
//        button.setIcon(R.drawable.ic_keyboard_arrow_right);
//        button.setColorNormal(ThemeManager.getColorAccent(this));
//        button.setColorPressed(ThemeManager.darkenColor(ThemeManager.getColorAccent(this)));
//        button.setSize(FloatingActionButton.SIZE_MINI);
//
//        menu.addButton(button);
//        rootLayout.addView(menu);

    }

    private void connectToGoogle() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                AsyncHttpClient client = new AsyncHttpClient(TestActivity.this);
                HttpRequest request = new HttpRequest("https://www.google.com/");

                try {
                    final HttpResponse response = client.execute(request);
                    Log.w(AsyncHttpClient.TAG, response.getContentAsString());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvResult.append("Connection successfully!\n");
                            tvResult.append("Response code: " + response.responseCode + "\n");
                        }
                    });
                } catch (final HttpResponseCodeException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvResult.append("Connection failed!\n");
                            tvResult.append("Response code: " + e.responseCode + "\n");
                            tvResult.append("Response message: " + e.responseMessage + "\n");
                        }
                    });
                }
                client.close();
            }
        }).start();
    }

}
