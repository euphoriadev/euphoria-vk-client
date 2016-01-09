package ru.euphoriadev.vk.api;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map.Entry;
import java.util.TreeMap;

public class VKParams {
    //TreeMap нужен был чтобы сортировать параметры по имени, сейчас это уже не важно, главно подписывать и передавать параметры в одном и том же порядке
    private TreeMap<String, String> args = new TreeMap<String, String>();
    String method_name;

    public VKParams(String methodName) {
        this.method_name = methodName;
    }

    public boolean contains(String name) {
        return args.containsKey(name);
    }

    public void put(String paramName, String paramValue) {
        if (paramValue == null || paramValue.length() == 0)
            return;
        args.put(paramName, paramValue);
    }

    public void put(String param_name, Long param_value) {
        if (param_value == null) return;
        put(param_name, Long.toString(param_value));
    }

    public void put(String param_name, Integer param_value) {
        if (param_value == null) return;
        put(param_name, Integer.toString(param_value));
    }

    public void put(String params_name, Boolean param_value) {
        if (param_value == null) return;
        put(params_name, param_value ? "1" : "0");
    }

    public void putDouble(String param_name, double param_value) {
        args.put(param_name, Double.toString(param_value));
    }

    public String getMethodName() {
        return method_name;
    }

    public String getParamsString() {
        StringBuilder bufferParams = new StringBuilder();
        try {
            for (Entry<String, String> entry : args.entrySet()) {
                if (bufferParams.length() != 0)
                    bufferParams.append("&");

                bufferParams
                        .append(entry.getKey())
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return bufferParams.toString();
    }

}
