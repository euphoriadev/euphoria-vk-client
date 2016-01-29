package ru.euphoriadev.vk.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.SparseArray;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ru.euphoriadev.vk.PrefsFragment;

/**
 * Created by Igor on 27.01.16.
 *
 * Emoji symbols contained in text messages in form of special characters.
 * If the system where to display the symbols, not their support,
 * it is necessary to display the characters using any available set of emoji icons.
 * In this case, all messages containing the characters you want to replace,
 * contain a special box emoji;
 * if this field does not come, the message can not be processed
 *
 * http://vk.com/dev//emoji
 */
public class Emoji {
    /** URL link for load emoji images */
    public static final String BASE_EMOJI_URL = "http://vk.com/images/emoji/";
    public static final String EMOJI_DIR_NAME = "Emojis";

    private static final HashMap<String, String> sEmojiUnicodes = new HashMap<>();
    static {
        sEmojiUnicodes.put("\uD83D\uDE0A", "D83DDE0A");
        sEmojiUnicodes.put("\uD83D\uDE03", "D83DDE03");
        sEmojiUnicodes.put("\uD83D\uDE09", "D83DDE09");
        sEmojiUnicodes.put("\uD83D\uDE06", "D83DDE06");
        sEmojiUnicodes.put("\uD83D\uDE1C", "D83DDE1C");
        sEmojiUnicodes.put("\uD83D\uDE0B", "D83DDE0B");
        sEmojiUnicodes.put("\uD83D\uDE0D", "D83DDE0D");
        sEmojiUnicodes.put("\uD83D\uDE0E", "D83DDE0E");
        sEmojiUnicodes.put("\uD83D\uDE12", "D83DDE12");

        sEmojiUnicodes.put("\uD83D\uDE0F", "D83DDE0F");
        sEmojiUnicodes.put("\uD83D\uDE14", "D83DDE14");
        sEmojiUnicodes.put("\uD83D\uDE22", "D83DDE22");
        sEmojiUnicodes.put("\uD83D\uDE2D", "D83DDE2D");
        sEmojiUnicodes.put("\uD83D\uDE29", "D83DDE29");
        sEmojiUnicodes.put("\uD83D\uDE28", "D83DDE28");
        sEmojiUnicodes.put("\uD83D\uDE10", "D83DDE10");
        sEmojiUnicodes.put("\uD83D\uDE0C", "D83DDE0C");
        sEmojiUnicodes.put("\uD83D\uDE20", "D83DDE20");

        sEmojiUnicodes.put("\uD83D\uDE21", "D83DDE21");
        sEmojiUnicodes.put("\uD83D\uDE07", "D83DDE07");
        sEmojiUnicodes.put("\uD83D\uDE30", "D83DDE30");
        sEmojiUnicodes.put("\uD83D\uDE32", "D83DDE32");
        sEmojiUnicodes.put("\uD83D\uDE33", "D83DDE33");
        sEmojiUnicodes.put("\uD83D\uDE37", "D83DDE37");
        sEmojiUnicodes.put("\uD83D\uDE1A", "D83DDE1A");
        sEmojiUnicodes.put("\uD83D\uDE08", "D83DDE08");
        sEmojiUnicodes.put("\u2764", "2764");

        sEmojiUnicodes.put("\uD83D\uDC4D", "D83DDC4D");
        sEmojiUnicodes.put("\uD83D\uDC4E", "D83DDC4E");
        sEmojiUnicodes.put("\u261D", "261D");
        sEmojiUnicodes.put("\u270C", "270C");
        sEmojiUnicodes.put("\uD83D\uDC4C", "D83DDC4C");
        sEmojiUnicodes.put("\u26BD", "26BD");
        sEmojiUnicodes.put("\u26C5", "26C5");
        sEmojiUnicodes.put("\uD83C\uDF1F", "D83CDF1F");
        sEmojiUnicodes.put("\uD83C\uDF4C", "D83CDF4C");
        sEmojiUnicodes.put("\uD83C\uDF7A", "D83CDF7A");
        sEmojiUnicodes.put("\uD83C\uDF7B", "D83CDF7B");

        sEmojiUnicodes.put("\uD83C\uDF39", "D83CDF39");
        sEmojiUnicodes.put("\uD83C\uDF45", "D83CDF45");
        sEmojiUnicodes.put("\uD83C\uDF52", "D83CDF52");
        sEmojiUnicodes.put("\uD83C\uDF81", "D83CDF81");
        sEmojiUnicodes.put("\uD83C\uDF82", "D83CDF82");
        sEmojiUnicodes.put("\uD83C\uDF84", "D83CDF84");
        sEmojiUnicodes.put("\uD83C\uDFC1", "D83CDFC1");
        sEmojiUnicodes.put("\uD83C\uDFC6", "D83CDFC6");
        sEmojiUnicodes.put("\uD83D\uDC0E", "D83DDC0E");

        sEmojiUnicodes.put("\uD83D\uDC0F", "D83DDC0F");
        sEmojiUnicodes.put("\uD83D\uDC1C", "D83DDC1C");
        sEmojiUnicodes.put("\uD83D\uDC2B", "D83DDC2B");
        sEmojiUnicodes.put("\uD83D\uDC2E", "D83DDC2E");
        sEmojiUnicodes.put("\uD83D\uDC03", "D83DDC03");
        sEmojiUnicodes.put("\uD83D\uDC3B", "D83DDC3B");
        sEmojiUnicodes.put("\uD83D\uDC3C", "D83DDC3C");
        sEmojiUnicodes.put("\uD83D\uDC05", "D83DDC05");
        sEmojiUnicodes.put("\uD83D\uDC13", "D83DDC13");

        sEmojiUnicodes.put("\uD83D\uDC18", "D83DDC18");
        sEmojiUnicodes.put("\uD83D\uDC94", "D83DDC94");
        sEmojiUnicodes.put("\uD83D\uDCAD", "D83DDCAD");
        sEmojiUnicodes.put("\uD83D\uDC36", "D83DDC36");
        sEmojiUnicodes.put("\uD83D\uDC31", "D83DDC31");
        sEmojiUnicodes.put("\uD83D\uDC37", "D83DDC37");
        sEmojiUnicodes.put("\uD83D\uDC11", "D83DDC11");
        sEmojiUnicodes.put("\u23F3", "23F3");
        sEmojiUnicodes.put("\u26BE", "26BE");
        sEmojiUnicodes.put("\u26C4", "26C4");
        sEmojiUnicodes.put("\u2600", "2600");

        sEmojiUnicodes.put("\uD83C\uDF3A", "D83CDF3A");
        sEmojiUnicodes.put("\uD83C\uDF3B", "D83CDF3B");
        sEmojiUnicodes.put("\uD83C\uDF3C", "D83CDF3C");
        sEmojiUnicodes.put("\uD83C\uDF3D", "D83CDF3D");
        sEmojiUnicodes.put("\uD83C\uDF4A", "D83CDF4A");
        sEmojiUnicodes.put("\uD83C\uDF4B", "D83CDF4B");
        sEmojiUnicodes.put("\uD83C\uDF4D", "D83CDF4D");
        sEmojiUnicodes.put("\uD83C\uDF4E", "D83CDF4E");
        sEmojiUnicodes.put("\uD83C\uDF4F", "D83CDF4F");

        sEmojiUnicodes.put("\uD83C\uDF6D", "D83CDF6D");
        sEmojiUnicodes.put("\uD83C\uDF37", "D83CDF37");
        sEmojiUnicodes.put("\uD83C\uDF38", "D83CDF38");
        sEmojiUnicodes.put("\uD83C\uDF46", "D83CDF46");
        sEmojiUnicodes.put("\uD83C\uDF49", "D83CDF49");
        sEmojiUnicodes.put("\uD83C\uDF50", "D83CDF50");
        sEmojiUnicodes.put("\uD83C\uDF51", "D83CDF51");
        sEmojiUnicodes.put("\uD83C\uDF53", "D83CDF53");
        sEmojiUnicodes.put("\uD83C\uDF54", "D83CDF54");

        sEmojiUnicodes.put("\uD83C\uDF55", "D83CDF55");
        sEmojiUnicodes.put("\uD83C\uDF56", "D83CDF56");
        sEmojiUnicodes.put("\uD83C\uDF57", "D83CDF57");
        sEmojiUnicodes.put("\uD83C\uDF69", "D83CDF69");
        sEmojiUnicodes.put("\uD83C\uDF83", "D83CDF83");
        sEmojiUnicodes.put("\uD83C\uDFAA", "D83CDFAA");
        sEmojiUnicodes.put("\uD83C\uDFB1", "D83CDFB1");
        sEmojiUnicodes.put("\uD83C\uDFB2", "D83CDFB2");
        sEmojiUnicodes.put("\uD83C\uDFB7", "D83CDFB7");

        sEmojiUnicodes.put("\uD83C\uDFB8", "D83CDFB8");
        sEmojiUnicodes.put("\uD83C\uDFBE", "D83CDFBE");
        sEmojiUnicodes.put("\uD83C\uDFC0", "D83CDFC0");
        sEmojiUnicodes.put("\uD83C\uDFE6", "D83CDFE6");
        sEmojiUnicodes.put("\uD83D\uDC00", "D83DDC00");
        sEmojiUnicodes.put("\uD83D\uDC0C", "D83DDC0C");
        sEmojiUnicodes.put("\uD83D\uDC1B", "D83DDC1B");
        sEmojiUnicodes.put("\uD83D\uDC1D", "D83DDC1D");
        sEmojiUnicodes.put("\uD83D\uDC1F", "D83DDC1F");

        sEmojiUnicodes.put("\uD83D\uDC2A", "D83DDC2A");
        sEmojiUnicodes.put("\uD83D\uDC2C", "D83DDC2C");
        sEmojiUnicodes.put("\uD83D\uDC2D", "D83DDC2D");
        sEmojiUnicodes.put("\uD83D\uDC3A", "D83DDC3A");
        sEmojiUnicodes.put("\uD83D\uDC3D", "D83DDC3D");
        sEmojiUnicodes.put("\uD83D\uDC2F", "D83DDC2F");
        sEmojiUnicodes.put("\uD83D\uDC5C", "D83DDC5C");
        sEmojiUnicodes.put("\uD83D\uDC7B", "D83DDC7B");
        sEmojiUnicodes.put("\uD83D\uDC14", "D83DDC14");

        sEmojiUnicodes.put("\uD83D\uDC23", "D83DDC23");
        sEmojiUnicodes.put("\uD83D\uDC24", "D83DDC24");
        sEmojiUnicodes.put("\uD83D\uDC40", "D83DDC40");
        sEmojiUnicodes.put("\uD83D\uDC42", "D83DDC42");
        sEmojiUnicodes.put("\uD83D\uDC43", "D83DDC43");
        sEmojiUnicodes.put("\uD83D\uDC46", "D83DDC46");
        sEmojiUnicodes.put("\uD83D\uDC47", "D83DDC47");
        sEmojiUnicodes.put("\uD83D\uDC48", "D83DDC48");
        sEmojiUnicodes.put("\uD83D\uDC51", "D83DDC51");

        sEmojiUnicodes.put("\uD83D\uDC60", "D83DDC60");
        sEmojiUnicodes.put("\uD83D\uDCA1", "D83DDCA1");
        sEmojiUnicodes.put("\uD83D\uDCA3", "D83DDCA3");
        sEmojiUnicodes.put("\uD83D\uDCAA", "D83DDCAA");
        sEmojiUnicodes.put("\uD83D\uDCAC", "D83DDCAC");
        sEmojiUnicodes.put("\uD83D\uDD14", "D83DDD14");
        sEmojiUnicodes.put("\uD83D\uDD25", "D83DDD25");

        // Some smilies are not specify on the website
        sEmojiUnicodes.put("\uD83D\uDE04", "D83DDE04");
        sEmojiUnicodes.put("\uD83D\uDE02", "D83DDE02");

        sEmojiUnicodes.put("\uD83D\uDE18", "D83DDE18");
        sEmojiUnicodes.put("\uD83D\uDE19", "D83DDE19");
        sEmojiUnicodes.put("\uD83D\uDE17", "D83DDE17");

    }


    public static void parseEmoji(TextView view) {
        if (PrefManager.getBoolean(PrefsFragment.KEY_USE_SYSTEM_EMOJI, true)) {
            // enables "use system emoji"
            return;
        }
        String text = view.getText().toString();

        view.setText(getEmojiText(view.getContext(), text, (int) view.getTextSize()));

    }

    private static Spannable getEmojiText(Context context, String text, int size) {
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        for (int index = 0; index < builder.length(); index++) {
            for (Map.Entry<String, String> entry : sEmojiUnicodes.entrySet()) {
                int length = entry.getKey().length();
                if (index + length > builder.length()) continue;

                if (builder.subSequence(index, index + length).toString().equals(entry.getKey())) {
                    ImageSpan imageSpan = new ImageSpan(context, EmojiLoader.get(entry.getValue()));
                    imageSpan.getDrawable().setBounds(size, size, size, size);
                    builder.setSpan(imageSpan, index, index + length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    index += length - 1;
                    break;
                }
            }
        }
        return builder;

    }

    public static HashMap<String, String> getAllEmojes() {
        return sEmojiUnicodes;
    }

    public static void executeDownloadingTask(Context context) {
        new LoadEmojiTask(context).execute();
    }

    private static class LoadEmojiTask extends AsyncTask<Void, String, Void> {
        private Context mContext;
        ProgressDialog dialog;

        public LoadEmojiTask(Context context) {
            this.mContext = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            dialog = new ProgressDialog(mContext);
            dialog.setTitle("Emoji");
            dialog.setMessage("Загрузка смайликов, подождите");
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            AsyncHttpClient client = new AsyncHttpClient(mContext);
            File dir = new File(AppLoader.getLoader().getExternalFilesDir().getAbsolutePath() + "/" + AppLoader.APP_DIR + "/" + EMOJI_DIR_NAME);
            dir.mkdirs();

            for (Map.Entry<String, String> entry : sEmojiUnicodes.entrySet()) {
                String url = BASE_EMOJI_URL + entry.getValue() + ".png";
                Log.i("Emoji", url);

                try {
                    AsyncHttpClient.HttpResponse response = client.execute(new AsyncHttpClient.HttpRequest(url));
                    File file = new File(dir, String.valueOf(entry.getValue().hashCode()));
                    file.createNewFile();

                    FileOutputStream fos = new FileOutputStream(file);
                    IOUtils.copy(response.getContent(), fos);
                    fos.flush();
                    fos.close();

                    response.release();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            dialog.cancel();
            Toast.makeText(mContext, "Смайлики загруженны", Toast.LENGTH_SHORT).show();
        }
    }

    public static class EmojiLoader {
        private static final SparseArray<Bitmap> sImageCache = new SparseArray<>();

        public static Bitmap get(String emojiUnicode) {
            Bitmap bitmap = sImageCache.get(emojiUnicode.hashCode());
            if (bitmap == null) {
                bitmap = BitmapFactory.decodeFile(new File(AppLoader.getLoader().getExternalFilesDir().getAbsolutePath() + "/" + AppLoader.APP_DIR + "/" + EMOJI_DIR_NAME + "/" + emojiUnicode.hashCode()).getAbsolutePath());

                sImageCache.append(emojiUnicode.hashCode(), bitmap);
            }
            return bitmap;
        }

        public static void clear() {
            for (int i = 0; i < sImageCache.size(); i++) {
                Bitmap bitmap = sImageCache.valueAt(i);
                if (bitmap != null) {
                    bitmap.recycle();
                    bitmap = null;
                }
            }
            System.gc();;
        }
    }

    public static class EmojiSpan extends DynamicDrawableSpan {
        private Drawable mDrawable;
        private int mSize;

        public EmojiSpan(Drawable emoji, int size) {
            this.mDrawable = emoji;
            this.mSize = size;
        }

        @Override
        public Drawable getDrawable() {
            if (mDrawable != null) {
                mDrawable.setBounds(0, 0, mSize, mSize);
            }
            return null;
        }
    }
}
