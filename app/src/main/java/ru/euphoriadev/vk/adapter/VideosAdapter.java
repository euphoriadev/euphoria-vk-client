package ru.euphoriadev.vk.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import ru.euphoriadev.vk.R;
import ru.euphoriadev.vk.api.model.VKUser;
import ru.euphoriadev.vk.api.model.VKVideo;
import ru.euphoriadev.vk.util.AppLoader;

/**
 * Created by user on 23.11.15.
 */
public class VideosAdapter extends ArrayAdapter<VideosAdapter.VideoItem> {
    private LayoutInflater inflater;
    private Picasso picasso;
    private SimpleDateFormat dateFormat;
    private AppLoader loader;

    public VideosAdapter(Context context, ArrayList<VideoItem> videos) {
        super(context, R.layout.list_item_video, videos);

        inflater = LayoutInflater.from(context);
        picasso = Picasso.with(context);
        dateFormat = new SimpleDateFormat("HH:mm");
        loader = AppLoader.getLoader();

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        final VideoItem item = getItem(position);


        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_video, parent, false);
            holder = new ViewHolder(convertView);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvTitle.setText(item.video.title);
        holder.tvDescription.setText(item.video.description);
        holder.tvDuration.setText(dateFormat.format(TimeUnit.SECONDS.toMillis(item.video.duration)));
        picasso.load(item.video.image_big)
                .into(holder.ivImage);

        holder.ivImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.video.getVideoUrl()));
                intent.setDataAndType(Uri.parse(item.video.getVideoUrl()), "video/mp4");
                getContext().startActivity(intent);
            }
        });

        holder.cardView.setCardBackgroundColor(loader.getApplicationContext().getResources().getColor(loader.isDarkTheme ? R.color.md_dark_background : R.color.md_light_background));

        return convertView;
    }

    public static class VideoItem {
        public VKUser user;
        public VKVideo video;

        public VideoItem(VKUser user, VKVideo video) {
            this.user = user;
            this.video = video;
        }
    }

    private class ViewHolder {
        public CardView cardView;
        public ImageView ivImage;
        public TextView tvDuration;
        public TextView tvTitle;
        public TextView tvDescription;

        public ViewHolder(View v) {
            cardView = (CardView) v.findViewById(R.id.cardVideo);
            ivImage = (ImageView) v.findViewById(R.id.ivVideoImage);
            tvTitle = (TextView) v.findViewById(R.id.tvVideoTitle);
            tvDuration = (TextView) v.findViewById(R.id.tvVideoDuration);
            tvDescription = (TextView) v.findViewById(R.id.tvVideoDescription);
        }
    }

}
