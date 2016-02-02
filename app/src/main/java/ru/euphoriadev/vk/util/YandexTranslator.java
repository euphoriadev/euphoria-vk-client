package ru.euphoriadev.vk.util;

import android.content.Context;

import java.io.Closeable;

import ru.euphoriadev.vk.http.AsyncHttpClient;
import ru.euphoriadev.vk.http.HttpParams;
import ru.euphoriadev.vk.http.HttpRequest;
import ru.euphoriadev.vk.http.HttpResponse;
import ru.euphoriadev.vk.http.HttpResponseCodeException;


/**
 * Created by Igor on 11.11.15.
 */
public class YandexTranslator implements Closeable {
    public static final String TAG = "YandexTranslator";
    /**
     * Бесплатный ключ, необходимый осуществления переводов
     */
    private final String apiKey = "trnsl.1.1.20151111T152603Z.2776f2b27e3ca850.db06c544a222c5278777f8c8968629dcf4836182";
    AsyncHttpClient client;
    private Context context;

    public YandexTranslator(Context context) {
        this.context = context;
        this.client = new AsyncHttpClient(context);
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

        HttpParams params = new HttpParams();
        params.addParam("key", apiKey);
        params.addParam("text", message);
        params.addParam("lang", (languageFrom.equals(Language.AUTO_DETECT.toString()) ? languageTo : languageFrom + "-" + languageTo));

        HttpRequest request = new HttpRequest("https://translate.yandex.net/api/v1.5/tr.json/translate", "GET", params);
        try {
            HttpResponse response = client.execute(request);
            return response.getContentAsJson().optJSONArray("text").optString(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "[error]";
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
        HttpParams params = new HttpParams();
        params.addParam("key", apiKey);
        params.addParam("text", message);
        params.addParam("lang", (languageFrom.equals(Language.AUTO_DETECT.toString()) ? languageTo : languageFrom + "-" + languageTo));

        final HttpRequest request = new HttpRequest("https://translate.yandex.net/api/v1.5/tr.json/translate", "GET", params);
        client.execute(request, new HttpRequest.OnResponseListener() {
            @Override
            public void onResponse(AsyncHttpClient client, HttpResponse response) {
                if (listener != null) {
                    listener.onCompleteTranslate(YandexTranslator.this, response.getContentAsJson().optJSONArray("text").optString(0));
                }
            }

            @Override
            public void onError(AsyncHttpClient client, HttpResponseCodeException exception) {
            }
        });
    }

    @Override
    public void close() {
        if (client != null) {
            client.close();
            client = null;
        }
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



    public interface OnCompleteListener {
        /**
         * Callback при ассинхронном вызове
         * @param translator
         * @param message
         */
        void onCompleteTranslate(YandexTranslator translator, String message);
    }
}
