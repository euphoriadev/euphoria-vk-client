package ru.euphoriadev.vk.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import ru.euphoriadev.vk.R;
import ru.euphoriadev.vk.util.ThemeManagerOld;

/**
 * Created by user on 24.07.15.
 */
public class GiftsAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<GiftItem> gifts;
    private LayoutInflater inflater;
    private ThemeManagerOld tm;

    private int colorPrimary;
    private int colorSecondary;
    private boolean isNightTheme;
    private Typeface typeface;

    public GiftsAdapter(Context context, ArrayList<GiftItem> gifts) {
        this.context = context;
        this.gifts = gifts;
        this.tm = new ThemeManagerOld(this.context);
        this.inflater = (LayoutInflater)
                this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        this.colorPrimary = tm.getPrimaryTextColor();
        this.colorSecondary = tm.getSecondaryTextColor();
        this.isNightTheme = tm.isNightTheme();

        if (!tm.isSystemFont()) {
            typeface = Typeface.createFromAsset(context.getAssets(), tm.getFont());
        }
    }

    @Override
    public int getCount() {
        return gifts.size();
    }

    @Override
    public Object getItem(int position) {
        return gifts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = this.inflater.inflate(R.layout.gift_list_item, parent, false);
        }
        GiftItem item = gifts.get(position);

        ImageView ivPhoto = (ImageView) view.findViewById(R.id.ivGiftPhoto);
        ImageView ivSrc = (ImageView) view.findViewById(R.id.ivGiftSrc);
        TextView tvTitle = (TextView) view.findViewById(R.id.tvGiftTitle);
        TextView tvDate = (TextView) view.findViewById(R.id.tvGiftDate);
        CardView cardView = (CardView) view.findViewById(R.id.cvGift);

        if (!tm.isSystemFont()) {
            tvTitle.setTypeface(typeface);
            tvDate.setTypeface(typeface);
        }
        if (isNightTheme) {
            cardView.setCardBackgroundColor(Color.parseColor("#424242"));
        } else {
            cardView.setCardBackgroundColor(Color.WHITE);
        }
        tvTitle.setTextColor(colorPrimary);
        tvDate.setTextColor(colorSecondary);

        tvTitle.setText(String.valueOf(item.fromUser.toString()));
        tvDate.setText(new SimpleDateFormat("dd MMM. HH:mm").format(item.gift.date * 1000));

        Picasso.with(context)
                .load(item.gift.thumb_256)
//                .transform(new MessageAdapter.RoundedTransformation(8))
                .into(ivSrc);

        Picasso.with(context)
                .load(item.fromUser.photo_50)
                .placeholder(R.drawable.camera_b)
                .into(ivPhoto);

        return view;
    }

    /**
     * Так как разработчики подарков вк - отсталые люди
     * эти говнюки сделали их в JPEG, из-за чего приходится применять костыль
     * для конвертации белого фона
     * Однако он не корректно работает
     */
    @Deprecated
    private class ConvertWhiteColor implements Transformation {

        @Override
        public Bitmap transform(Bitmap source) {
            Bitmap bitmap = null;
            if (!source.isMutable()) {
                bitmap = source.copy(source.getConfig(), true);
            } else {
                bitmap = source;
            }
            if (source != bitmap) {
                source.recycle();
                source = null;
            }

            for (int i = 0; i < bitmap.getHeight(); i++) {
                for (int j = 0; j < bitmap.getWidth(); j++) {
                    if (bitmap.getPixel(j, i) == Color.WHITE) {
                        bitmap.setPixel(j, i, Color.RED);
                    }
                }
            }
            return bitmap;
        }

        @Override
        public String key() {
            return "to_png";
        }
    }
}
