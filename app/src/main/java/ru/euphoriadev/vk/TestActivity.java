package ru.euphoriadev.vk;

import android.animation.LayoutTransition;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Random;

import ru.euphoriadev.vk.api.Api;
import ru.euphoriadev.vk.api.model.VKUser;
import ru.euphoriadev.vk.http.AsyncHttpClient;
import ru.euphoriadev.vk.http.HttpRequest;
import ru.euphoriadev.vk.http.HttpResponse;
import ru.euphoriadev.vk.http.HttpResponseCodeException;
import ru.euphoriadev.vk.interfaces.OnTwiceClickListener;
import ru.euphoriadev.vk.util.AndroidUtils;
import ru.euphoriadev.vk.util.Emoji;
import ru.euphoriadev.vk.util.ThemeManager;
import ru.euphoriadev.vk.util.ThreadExecutor;
import ru.euphoriadev.vk.vkapi.VKApi;

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            LayoutTransition transition = new LayoutTransition();
            transition.enableTransitionType(LayoutTransition.CHANGING);
            rootLayout.setLayoutTransition(transition);
        }
        rootView.addView(rootLayout);

        tvResult = new AppCompatTextView(this);
        tvResult.setTextColor(ThemeManager.getPrimaryTextColor());
        tvResult.setTextSize(18);
        rootLayout.addView(tvResult);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm.s", Locale.US);
        String date = sdf.format(System.currentTimeMillis());

        final AppCompatEditText editText = new AppCompatEditText(this);
        editText.setHint("Execute code. Tap twice for run");
        editText.setOnClickListener(new OnTwiceClickListener() {
            @Override
            public void onTwiceClick(View v) {
                VKApi.execute(editText.getText().toString().trim(), new VKApi.VKOnResponseListener() {
                    @Override
                    public void onResponse(VKApi.VKRequest request, JSONObject responseJson) {
                        tvResult.append("\nResponse:\n " + responseJson.toString());
                    }

                    @Override
                    public void onError(VKApi.VKException exception) {
                        tvResult.append("\nError:\n " + exception.errorMessage);
                    }
                });
            }
        });
        rootLayout.addView(editText);

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

        AppCompatButton buttonVkApi = new AppCompatButton(this);
        buttonVkApi.setText("VKApi: setActivity");
        buttonVkApi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VKApi.init(VKApi.VKUserAccount.from(Api.get().getAccount()));
                VKApi.messages().setActivity().userId(Api.get().getUserId()).execute(VKApi.DEFAULT_RESPONSE_LISTENER);
            }
        });
        rootLayout.addView(buttonVkApi);

        AppCompatButton buttonUsers = new AppCompatButton(this);
        buttonUsers.setText("VKApi: users.get");
        buttonUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VKApi.users()
                        .get()
                        .userIds(205387401)
                        .fields(VKUser.FIELDS_DEFAULT)
                        .asModel(new VKUser())
                        .execute(new VKApi.VKResponseHandler<VKUser>() {
                            @Override
                            public void onResponse(VKUser result) {
                                tvResult.append("Yes! Response: " + result.toString());
                            }
                        });
            }
        });
        rootLayout.addView(buttonUsers);

        AppCompatButton buttonKateApi = new AppCompatButton(this);
        buttonKateApi.setText("Kate Api: setActivity");
        buttonKateApi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ThreadExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Api.get().setMessageActivity(Api.get().getUserId(), null, true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        rootLayout.addView(buttonKateApi);

        AppCompatButton buttonSend = new AppCompatButton(this);
        buttonSend.setText("VKApi: Send message to me");
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VKApi.init(VKApi.VKUserAccount.from(Api.get().getAccount()));
                tvResult.append("Sending message...\n");
                final long startTime = System.currentTimeMillis();

                VKApi.messages()
                        .send()
                        .message("This is text message from Euphoria")
                        .guid(new Random().nextInt())
                        .userId(VKApi.getAccount().userId)
                        .execute(new VKApi.VKOnResponseListener() {
                            @Override
                            public void onResponse(VKApi.VKRequest r, JSONObject responseJson) {
                                long endTime = System.currentTimeMillis() - startTime;
                                tvResult.append("Message is sent. ");
                                tvResult.append("Time has passed: ");
                                tvResult.append(String.valueOf(endTime));
                                tvResult.append(" ms\n");
                            }

                            @Override
                            public void onError(VKApi.VKException exception) {
                                tvResult.append("Error sending message!\n");
                                tvResult.append("Error code: " + exception.errorCode + "\n");
                                tvResult.append("Error message: " + exception.errorMessage + "\n");
                            }
                        });
            }
        });
        rootLayout.addView(buttonSend);


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


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

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
