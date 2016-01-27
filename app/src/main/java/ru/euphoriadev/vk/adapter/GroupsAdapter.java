package ru.euphoriadev.vk.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import ru.euphoriadev.vk.R;
import ru.euphoriadev.vk.api.model.VKGroup;
import ru.euphoriadev.vk.util.ViewUtil;

/**
 * This is a fork of the LazyList proyect.
 * The aim of this fork is to contribute and create an Android Library without any reference to Views,
 * and to delay the "imageView.setImageBitmap" in order to let the implementation do it,
 * do what the final project needs.
 * For example: before set the Image to an ImageView reduce it size, put rounded corners or…
 */
public class GroupsAdapter extends BaseArrayAdapter<VKGroup> {


    public GroupsAdapter(Context context, ArrayList<VKGroup> values) {
        super(context, values);
    }

    @Override
    public boolean compareTo(String q, VKGroup value) {
        return value.name.toLowerCase().contains(q);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder holder;
        if (view == null) {
            view = getInflater().inflate(R.layout.group_list_item, parent, false);

            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        VKGroup group = getItem(position);

        holder.tvTitle.setTextColor(getThemeManager().getPrimaryTextColor());
        holder.tvStatus.setTextColor(getThemeManager().getSecondaryTextColor());


        ViewUtil.setTypeface(holder.tvTitle);
        ViewUtil.setTypeface(holder.tvStatus);

        if (isInMultiSelectMode()) {
            holder.ivSelected.setVisibility(View.VISIBLE);
            if (isSelectedItem(group)) {
                holder.ivSelected.setImageResource(R.drawable.ic_selected);
                holder.ivSelected.setColorFilter(getThemeManager().getFabColor());
                holder.ivSelected.setAlpha(1f);
            } else {
                holder.ivSelected.setImageResource(R.drawable.ic_vector_unselected);
                holder.ivSelected.setColorFilter(getThemeManager().getSecondaryTextColor());
                holder.ivSelected.setAlpha(0.5f);
            }
        } else {
            holder.ivSelected.setVisibility(View.GONE);
        }

        String type;
        switch (group.type) {
            case VKGroup.Type.GROUP: type = getContext().getString(R.string.type_group); break;
            case VKGroup.Type.PAGE:  type = getContext().getString(R.string.type_page); break;
            case VKGroup.Type.EVENT: type = getContext().getString(R.string.type_event); break;

            default: type = getContext().getString(R.string.type_group);
        }

        holder.tvTitle.setText(group.name);
        holder.tvStatus.setText(type.concat(" | ")
                .concat(String.valueOf(group.members_count))
                .concat(" ")
                .concat(getContext().getResources().getString(R.string.members)));

        Picasso.with(getContext())
                .load(group.photo_50)
                .config(Bitmap.Config.RGB_565)
                .into(holder.ivPhoto);

//        AsyncImageLoader imageLoader = AsyncImageLoader.get(mContext);
//        imageLoader.displayImage(holder.ivPhoto, group.photo_50);


        return view;
    }

    private static class ViewHolder {
        TextView tvTitle;
        TextView tvStatus;
        ImageView ivPhoto;
        ImageView ivSelected;

        public ViewHolder(View view) {
            tvTitle = (TextView) view.findViewById(R.id.tvGroupTitle);
            tvStatus = (TextView) view.findViewById(R.id.tvGroupStatus);
            ivPhoto = (ImageView) view.findViewById(R.id.ivGroupPhoto);
            ivSelected = (ImageView) view.findViewById(R.id.ivGroupSelected);
        }
    }
}
