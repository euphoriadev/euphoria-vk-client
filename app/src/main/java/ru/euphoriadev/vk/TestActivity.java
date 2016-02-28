package ru.euphoriadev.vk;

import android.animation.LayoutTransition;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import ru.euphoriadev.vk.api.Api;
import ru.euphoriadev.vk.api.KException;
import ru.euphoriadev.vk.api.model.VKPhoto;
import ru.euphoriadev.vk.api.model.VKUser;
import ru.euphoriadev.vk.http.AsyncHttpClient;
import ru.euphoriadev.vk.http.HttpRequest;
import ru.euphoriadev.vk.http.HttpResponse;
import ru.euphoriadev.vk.http.HttpResponseCodeException;
import ru.euphoriadev.vk.interfaces.OnTwiceClickListener;
import ru.euphoriadev.vk.napi.VKApi;
import ru.euphoriadev.vk.util.AndroidUtils;
import ru.euphoriadev.vk.common.AppLoader;
import ru.euphoriadev.vk.util.Emoji;
import ru.euphoriadev.vk.common.ThemeManager;
import ru.euphoriadev.vk.async.ThreadExecutor;
import ru.euphoriadev.vk.async.ThreadTask;

import static ru.euphoriadev.vk.napi.VKApi.TAG;
import static ru.euphoriadev.vk.napi.VKApi.VKMessageUploader;

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
                VKApi.init(VKApi.VKUserConfig.from(Api.get().getAccount()));
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

        AppCompatButton buttonSendGift = new AppCompatButton(this);
        buttonSendGift.setText("Send gift");
        buttonSendGift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ThreadExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // Victor
//                            List<Integer> integers = Collections.singletonList(17791724);
                            // Roman
//                            List<Integer> integers = Collections.singletonList(185957061);
                            // Lev
//                            List<Integer> integers = Collections.singletonList(138479323);
                            // Denis
                            List<Integer> integers = Collections.singletonList(214453200);

//                            Api.get().sendGift(integers, String.valueOf(System.currentTimeMillis()), true, 692, new Random().nextInt());
                            Api.get().sendGift(integers, "С прошедшим тебя тоже!", false, 750, new Random().nextInt());
                        } catch (Exception e) {
                            if (e instanceof KException) {
//                                KException ke = (KException) e;
//                                if (ke.error_code == VKApi.VKErrorCodes.VALIDATION_REQUIRED) {
//                                    AndroidUtils.openUrlInBrowser(TestActivity.this, ke.redirect_uri);
//                                }
                            }
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        rootLayout.addView(buttonSendGift);

        AppCompatButton buttonAuthorization = new AppCompatButton(this);
        buttonAuthorization.setText("Direct Authorization");
        buttonAuthorization.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VKApi.authorization("login", "pass", new VKApi.VKOnAuthorizationListener() {
                    @Override
                    public void onSuccess(VKApi.VKUserConfig newConfig) {
                        Log.w(TAG, "new config: " + newConfig.toString());
                        Log.w(TAG, "apply to vk api");
                        VKApi.setUserConfig(newConfig);
                        Api.get().setAccessToken(newConfig.accessToken);
                    }

                    @Override
                    public void onError(VKApi.VKException e) {

                    }
                });
            }
        });
        rootLayout.addView(buttonAuthorization);

        AppCompatButton buttonSend = new AppCompatButton(this);
        buttonSend.setText("VKApi: Send message to me");
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VKApi.init(VKApi.VKUserConfig.from(Api.get().getAccount()));
                tvResult.append("Sending message...\n");
                final long startTime = System.currentTimeMillis();

                VKApi.messages()
                        .send()
                        .message("This is text message from Euphoria")
                        .guid(new Random().nextInt())
                        .userId(VKApi.getUserConfig().userId)
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

        AppCompatButton buttonUpload = new AppCompatButton(this);
        buttonUpload.setText("Upload file");
        buttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvResult.append("Starting upload file...");
                uploadFile();
            }
        });
        rootLayout.addView(buttonUpload);

        AppCompatButton memoryButton = new AppCompatButton(this);
        memoryButton.setText("Fill memory");
        memoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ThreadExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ArrayList<Integer> list = new ArrayList<>();
                            list.add(1024);

                            for (int i = 0; i < list.size(); i++) {
                                list.add(list.get(i));
                            }
                        } catch (OutOfMemoryError e) {
                            System.gc();
                            e.printStackTrace();
                        }

                    }
                });
            }
        });
        rootLayout.addView(memoryButton);

        AppCompatButton buttonClear = new AppCompatButton(this);
        buttonClear.setText("Clear text");
        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvResult.setText("");
            }
        });
        rootLayout.addView(buttonClear);

        ThreadTask task = new ThreadTask() {
            @Override
            public void onPreExecute() {

            }

            @Override
            public void doInBackground() {

            }

            @Override
            public void onPostExecute() {

            }
        };
        task.start();

        final Button button = new Button(this);
        button.setText("Test scale with animation");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float scaleY = button.getScaleY();
                float scaleX = button.getScaleX();

                ViewCompat.animate(v)
                        .setDuration(100)
                        .scaleX(scaleY * 2.0f)
                        .scaleY(scaleX * 2.0f)
                        .withLayer()
                        .start();


            }
        });

        rootLayout.addView(button);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void uploadFile() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    File file = new File(AppLoader.getLoader().getAppDir(), "test.jpg");
                    VKMessageUploader uploader = VKApi.getMessageUploader();
                    VKPhoto photo = uploader.uploadFile(file, new VKApi.VKOnProgressListener() {
                        @Override
                        public void onStart(File file) {
                            Toast.makeText(TestActivity.this, "Start loading file: " + file, Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onProgress(byte[] buffer, int progress, long totalSize) {
                            Log.w(TAG, "progress: " + progress);
                        }

                        @Override
                        public void onSuccess() {
                            Toast.makeText(TestActivity.this, "Success!", Toast.LENGTH_LONG).show();
                        }
                    });

                    VKApi.messages().send()
                            .attachment(photo.toAttachmentString())
                            .message("Message with files!")
                            .userId(Api.get().getUserId())
                            .execute();

//                    Api.get().sendMessage(185957061, 0, "Test message with file",
//                            null, null, Collections.singleton("photo" + photo.pid),
//                            null, null, null, null, null);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
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
