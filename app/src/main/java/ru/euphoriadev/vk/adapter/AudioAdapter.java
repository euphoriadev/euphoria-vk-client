package ru.euphoriadev.vk.adapter;

import android.content.Context;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import ru.euphoriadev.vk.R;
import ru.euphoriadev.vk.api.model.VKAudio;
import ru.euphoriadev.vk.helper.MediaPlayerHelper;
import ru.euphoriadev.vk.util.ThemeUtils;
import ru.euphoriadev.vk.view.ProgressLayout;

/**
 * Created by Igor on 10.06.15.
 */
public class AudioAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<VKAudio> mAudios;
    private LayoutInflater inflater;
    private Typeface typeface;

    private Drawable iconPlay;
    private Drawable iconPause;
    private int themeColor;
    private ColorFilter colorFilter;


    public AudioAdapter(Context context, ArrayList<VKAudio> audios) {
        this.mContext = context;
        this.mAudios = audios;

//        iconPlay = new IconicsDrawable(context, GoogleMaterial.Icon.gmd_play_circle_filled);
//        iconPause = new IconicsDrawable(context, GoogleMaterial.Icon.gmd_pause_circle_filled);
//
//        iconPlay.color(ThemeUtils.getThemeAttrColor(context, R.attr.colorAccent));
//        iconPause.color(ThemeUtils.getThemeAttrColor(context, R.attr.colorAccent));
//
//        iconPause.sizeDp(38);
//        iconPlay.sizeDp(38);
        iconPause = mContext.getResources().getDrawable(R.drawable.ic_pause);
        iconPlay = mContext.getResources().getDrawable(R.drawable.ic_play_arrow);
        themeColor = ThemeUtils.getThemeAttrColor(mContext, R.attr.colorAccent);
        colorFilter = new PorterDuffColorFilter(themeColor, PorterDuff.Mode.MULTIPLY);

        this.inflater = (LayoutInflater)
                mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        typeface = Typeface.createFromAsset(this.mContext.getAssets(), "Roboto-Regular.ttf");
    }

    @Override
    public int getCount() {
        return mAudios.size();
    }

    @Override
    public VKAudio getItem(int position) {
        return mAudios.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.list_item_audio, parent, false);
        }

        final VKAudio item = getItem(position);

        TextView tvTitle = (TextView) view.findViewById(R.id.tvTitle_audio);
        TextView tvArtist = (TextView) view.findViewById(R.id.tvArtist_audio);
        final ProgressLayout progressLayout = (ProgressLayout) view.findViewById(R.id.progressLayout);
        final ImageButton btnPlay = (ImageButton) view.findViewById(R.id.ibPlayAudio);

        tvTitle.setText(mAudios.get(position).title);
        tvTitle.setTypeface(typeface);
        tvArtist.setTypeface(typeface);
        tvArtist.setText(mAudios.get(position).artist);

        btnPlay.setImageDrawable(item.isPlaying ? iconPause : iconPlay);
        btnPlay.setColorFilter(colorFilter);

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (item.isPlaying) {
                    MediaPlayerHelper.stopPlayAudio(mContext);
                    item.isPlaying = false;
                } else {
                    MediaPlayerHelper.playAudio(mContext, item);
                    item.isPlaying = true;
                }

                for (VKAudio a : mAudios) {
                    boolean hash = false;
                    if (a.id != item.id) {
                        a.isPlaying = false;
                        hash = true;
                    }
                    if (hash) notifyDataSetChanged();
                }

            }
        });

        return view;
    }

}
