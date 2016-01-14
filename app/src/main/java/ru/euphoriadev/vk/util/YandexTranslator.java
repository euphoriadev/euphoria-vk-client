package ru.euphoriadev.vk.util;

import android.app.Activity;
import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import ru.euphoriadev.vk.http.DefaultHttpClient;
import ru.euphoriadev.vk.http.HttpClient;
import ru.euphoriadev.vk.http.HttpGetRequest;
import ru.euphoriadev.vk.http.HttpParams;
import ru.euphoriadev.vk.http.HttpResponse;


/**
 * Created by Igor on 11.11.15.
 */
public class YandexTranslator {
    public static final String TAG = "YandexTranslator";
    /**
     * Бесплатный ключ, необходимый осуществления переводов
     */
    private final String apiKey = "trnsl.1.1.20151111T152603Z.2776f2b27e3ca850.db06c544a222c5278777f8c8968629dcf4836182";
    HttpClient client;
    private Context context;

    public YandexTranslator(Context context) {
        this.context = context;
        this.client = new DefaultHttpClient();
    }

    /**
     * Перевод текста с помощью Yandex Translate Api
     * https://tech.yandex.ru/translate/
     *
     * @param message      Переводимый текст
     * @param languageFrom исходный язык текстового сообщения
     * @param languageTo   Язык, на который нужно перевести сообщение
     * @return Переведенный текст
     */
    public String translate(String message, String languageFrom, String languageTo) {
//        String s = "https://translate.yandex.net/api/v1.5/tr.json/translate?" +
//                "key=" + apiKey +
//                "&text=" + message +
//                "&lang=" +
//                (languageFrom.equals(Language.AUTO_DETECT.toString()) ?
//                        languageTo : languageFrom + "-" + languageTo);
//        JSONObject request = sendRequest(s);
//        if (request != null) {
//            return request.optJSONArray("text").optString(0);
//        }

        HttpParams params = new HttpParams();
        params.addParam("key", apiKey);
        params.addParam("text", message);
        params.addParam("lang", (languageFrom.equals(Language.AUTO_DETECT.toString()) ? languageTo : languageFrom + "-" + languageTo));

        HttpGetRequest request = new HttpGetRequest("https://translate.yandex.net/api/v1.5/tr.json/translate", params);
        HttpResponse response = client.execute(request);

        try {
            return new JSONObject(response.toString()).optJSONArray("text").optString(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Ассинхронный запрос на перевод текста, т.е не нужно создавать новый поток
     *
     * @param message
     * @param languageFrom
     * @param languageTo
     * @param listener
     */
    public void translateAsync(final String message, final String languageFrom, final String languageTo, final OnCompleteListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String translateMessage = translate(message, languageFrom, languageTo);
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (listener == null) {
                            return;
                        }
                        listener.onCompleteTranslate(YandexTranslator.this, translateMessage);
                    }
                });
            }
        }).start();
    }

    public enum Language {
        AUTO_DETECT   (""),
        ALBANIAN      ("sq"),
        ENGLISH       ("en"),
        ARAB          ("ar"),
        ARMENIAN      ("hy"),
        AZERBAJIAN    ("az"),
        AFRIKAAMS     ("af"),
        BASQUE        ("eu"),
        BELARUSIAN    ("be"),
        BUGARIAN      ("bg"),
        BOSNIAN       ("bs"),
        WELSH         ("cy"),
        VIETNAMESE    ("vi"),
        HUNGRATION    ("hu"),
        HAITIAN       ("ht"),
        GALICIAN      ("gl"),
        DUTCH         ("gl"),
        GREEK         ("el"),
        GEORGIAN      ("ka"),
        DANISH        ("da"),
        HEBREW        ("da"),
        INDONESIAN    ("id"),
        IRISH         ("ga"),
        ITALIAN       ("it"),
        ICELANDIC     ("is"),
        SPANISH       ("es"),
        KAZAKH        ("kk"),
        CATALAN       ("ca"),
        KYRGYA        ("ky"),
        CHINESE       ("zh"),
        KOREAN        ("ko"),
        LATIN         ("la"),
        LATVIAN       ("lv"),
        LITHUANIAN    ("lt"),
        MALAGSKY      ("mg"),
        MALAY         ("ms"),
        MALTA         ("mt"),
        MACEDONIAN    ("mk"),
        MONGOLIAN     ("mn"),
        GERMAN        ("de"),
        NORWEGIAN     ("no"),
        PERSIAN       ("fa"),
        POLISH        ("pl"),
        PORTUGUESE    ("pt"),
        ROMANIAN      ("ro"),
        RUSSIAN       ("ru"),
        SERBIAN       ("sr"),
        SLOVAK        ("sk"),
        SLOVENAAN     ("sl"),
        SWAHILI       ("sw"),
        TAJIK         ("tg"),
        THAI          ("th"),
        TAGALOG       ("tl"),
        TATAR         ("tt"),
        TURKISH       ("tr"),
        UZBEK         ("uz"),
        UKRAINIAN     ("uk"),
        FINNISH       ("fi"),
        FRENCH        ("fr"),
        CROATIAN      ("hr"),
        CZECH         ("cs"),
        SWEDISH       ("sv"),
        ESTONIAN      ("et"),
        JAPANESE      ("ja");

        private String mLanguage;

        Language(String lang) {
            this.mLanguage = lang;
        }

        @Override
        public String toString() {
            return mLanguage;
        }
    }


//    private JSONObject sendRequest(String url) {
//        BufferedInputStream is = null;
//        HttpURLConnection connection = null;
//        try {
//            connection = (HttpURLConnection) new URL(url).openConnection();
////            connection.connect();
//            connection.setConnectTimeout(30000);
//            connection.setReadTimeout(30000);
//            connection.setRequestMethod("GET");
//            connection.setUseCaches(false);
//            connection.setDoInput(true);
//
//            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
//                // error
//                return null;
//            }
//
//            is = new BufferedInputStream(connection.getInputStream());
//            String stream = AndroidUtils.convertStreamToString(is);
//            if (stream != null) {
//                Log.i(TAG, stream);
//                return new JSONObject(stream);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (is != null) {
//                try {
//                    is.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            if (connection != null) {
//                connection.disconnect();
//                connection = null;
//            }
//        }
//        return null;
//    }


    public interface OnCompleteListener {
        /**
         * Callback при ассинхронном вызове
         * @param translator
         * @param message
         */
        void onCompleteTranslate(YandexTranslator translator, String message);
    }
}
