package ru.euphoriadev.vk.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import ru.euphoriadev.vk.PhotoViewerActivity;
import ru.euphoriadev.vk.R;
import ru.euphoriadev.vk.api.model.VKPhoto;
import ru.euphoriadev.vk.util.AndroidUtils;

/**
 * Created by Igor on 15.02.16.
 */
public class PhotosRecyclerAdapter extends RecyclerView.Adapter<PhotosRecyclerAdapter.ViewHolder> {
    private Activity activity;
    private ArrayList<VKPhoto> photos;
    private LayoutInflater inflater;


    public PhotosRecyclerAdapter(Activity activity, ArrayList<VKPhoto> photos) {
        this.activity = activity;
        this.photos = photos;
        this.inflater = (LayoutInflater)
                activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(AndroidUtils.getDisplayWidth(activity) / 3, AndroidUtils.getDisplayWidth(activity) / 3);
        params.setMargins(AndroidUtils.pxFromDp(activity, 6),
                AndroidUtils.pxFromDp(activity, 2),
                AndroidUtils.pxFromDp(activity, 2),
                AndroidUtils.pxFromDp(activity, 2));

        ImageView imageView = new ImageView(activity);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(params);
        return new ViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhotoViewerActivity.start(holder.imageView.getContext(), photos.get(position));
            }
        });
        Picasso.with(activity).load(photos.get(position).src_big).placeholder(R.drawable.camera_b).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);

            imageView = (ImageView) itemView;
        }

    }
}
