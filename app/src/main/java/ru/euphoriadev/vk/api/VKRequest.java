package ru.euphoriadev.vk.api;

import org.json.JSONObject;

/**
 * Created by user on 05.07.15.
 */
public class VKRequest {

    static Api api;
    VKParams params;
    VKRequestListener listener;

    static String access_token;
    static String api_id;

    public VKRequest(String methodName) {
        if (api == null) {
            api = Api.init(access_token, api_id);
        }
        params = new VKParams(methodName);
    }

    public static void init(String appId, String token) {
        access_token = token;
        api_id = appId;
    }

    public VKRequest put(String name, String value) {
        params.put(name, value);
        return this;
    }

    public VKRequest put(String name, long value) {
        params.put(name, value);
        return this;
    }

    public void executeWithListener(VKRequestListener listener) {
        this.listener = listener;
        startAsync();
    }

    public void start() {
        try {
            JSONObject root = api.sendRequest(params);

            if (listener != null) {
                listener.onComplete(root);
            }
        } catch (Exception e) {
            if (listener != null) {
                listener.onError(e);
            }
        }
    }

    public void startAsync() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                start();
            }
        }).start();
    }


    public interface VKRequestListener {

        void onComplete(JSONObject response);

        void onError(Exception e);
    }
}
