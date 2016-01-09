package ru.euphoriadev.vk.http;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ru.euphoriadev.vk.api.Api;
import ru.euphoriadev.vk.api.VKParams;

/**
 * Created by Igor on 13.12.15.
 *
 * {@link HttpClient} for VK Api
 */
public class VKHttpClient extends DefaultHttpClient {

    public JSONObject execute(VKParams params) {
        HttpGetRequest request = new HttpGetRequest(Api.BASE_URL + "/" + params.getMethodName() + "?" + params.getParamsString());
        HttpResponse response = execute(request);
        try {
            return new JSONObject((String) response.getResult());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void execute(VKParams params, final VKOnResponseListener listener) {
        HttpGetRequest request = new HttpGetRequest(Api.BASE_URL + "/" + params.getMethodName() + "?" + params.getParamsString());
        execute(request, new OnResponseListener() {
            @Override
            public void onResponse(HttpClient client, HttpResponse response) {
                try {
                String json = (String) response.getResult();
                    listener.onResponse(new JSONObject(json));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public interface VKOnResponseListener {
        void onResponse(JSONObject response);
    }
}
