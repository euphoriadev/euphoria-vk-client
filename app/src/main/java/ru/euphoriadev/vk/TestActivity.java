package ru.euphoriadev.vk;

import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import ru.euphoriadev.vk.util.AndroidUtils;
import ru.euphoriadev.vk.util.AsyncHttpClient;
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



        AppCompatButton buttonClear = new AppCompatButton(this);
        buttonClear.setText("Clear text");
        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvResult.setText("");
            }
        });
        rootLayout.addView(buttonClear);
    }

    private void connectToGoogle() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                AsyncHttpClient client = new AsyncHttpClient(TestActivity.this);
                AsyncHttpClient.HttpRequest request = new AsyncHttpClient.HttpRequest("https://www.google.com/test/1");

                try {
                    final AsyncHttpClient.HttpResponse response = client.execute(request);
                    response.release();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvResult.append("Connection successfully!\n");
                            tvResult.append("Response code: " + response.responseCode + "\n");
                        }
                    });
                } catch (final AsyncHttpClient.HttpResponseCodeException e) {
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
