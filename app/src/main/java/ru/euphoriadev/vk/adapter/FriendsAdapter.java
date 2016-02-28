package ru.euphoriadev.vk.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import ru.euphoriadev.vk.R;
import ru.euphoriadev.vk.UserProfileActivity;
import ru.euphoriadev.vk.api.model.VKUser;
import ru.euphoriadev.vk.common.ResourcesLoader;
import ru.euphoriadev.vk.common.ThemeManager;
import ru.euphoriadev.vk.util.ViewUtil;

/**
 * Created by Igor on 16.07.15.
 */
public class FriendsAdapter extends BaseArrayAdapter<VKUser> {
    final int secondaryTextColor;
    final int tealTextColor;
    final String offline;
    final String online;
    Picasso picasso;
    private ArrayList<VKUser> mCleanFriends;

    public FriendsAdapter(Context context, ArrayList<VKUser> friends) {
        super(context, friends);

        secondaryTextColor = ThemeManager.getSecondaryTextColor();
        tealTextColor = ResourcesLoader.getColor(R.color.md_teal_500);
        picasso = Picasso.with(context);

        offline = getContext().getString(R.string.offline);
        online = getContext().getString(R.string.online);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        View view = convertView;

        if (view == null) {
            view = getInflater().inflate(R.layout.list_item_friends, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        final VKUser user = getItem(position);


        holder.tvName.setTextColor(ThemeManager.getPrimaryTextColor());
        holder.tvName.setText(user.toString());

        holder.tvScreenName.setTextColor(secondaryTextColor);
        holder.tvScreenName.setText("@".concat(user.screen_name));

        ViewUtil.setTypeface(holder.tvName);
        ViewUtil.setTypeface(holder.tvScreenName);
        ViewUtil.setTypeface(holder.tvOnlineStatus);

        if (user.online) {
            holder.tvOnlineStatus.setText(online);
            holder.tvOnlineStatus.setTextColor(tealTextColor);
        } else {
            holder.tvOnlineStatus.setText(offline);
            holder.tvOnlineStatus.setTextColor(secondaryTextColor);
        }

        holder.ivPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), UserProfileActivity.class);
                intent.putExtra("user_id", user.user_id);
                getContext().startActivity(intent);
            }
        });

        picasso.load(user.photo_50)
                .config(Bitmap.Config.RGB_565)
                .into(holder.ivPhoto);

        return view;
    }

    @Override
    public boolean compareTo(String q, VKUser value) {
        return value.toString().toLowerCase().contains(q);
    }

    public void filterByOnline() {
        if (mCleanFriends == null) {
            mCleanFriends = new ArrayList<>(getValues().size());
            mCleanFriends.addAll(getValues());
        }

        getValues().clear();
        final int sizeFriends = mCleanFriends.size();
        for (int i = 0; i < sizeFriends; i++) {
            VKUser user = mCleanFriends.get(i);

            if (user.online) {
                getValues().add(user);
            }
        }
        notifyDataSetChanged();
    }

    public void filterByOffline() {
        if (mCleanFriends == null) {
            mCleanFriends = new ArrayList<>(getValues().size());
            mCleanFriends.addAll(getValues());
        }

        getValues().clear();
        final int sizeFriends = mCleanFriends.size();
        for (int i = 0; i < sizeFriends; i++) {
            VKUser user = mCleanFriends.get(i);

            if (!user.online) {
                getValues().add(user);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public void clear() {
        super.clear();

        if (mCleanFriends != null) {
            mCleanFriends.clear();
            mCleanFriends.trimToSize();
            mCleanFriends = null;
        }
    }

    private static final class ViewHolder {
        public TextView tvName;
        public TextView tvScreenName;
        public TextView tvOnlineStatus;
        public ImageView ivPhoto;

        public ViewHolder(View view) {
            tvName = (TextView) view.findViewById(R.id.tvUserName);
            tvScreenName = (TextView) view.findViewById(R.id.tvUserScreenName);
            tvOnlineStatus = (TextView) view.findViewById(R.id.tvUserOnline);
            ivPhoto = (ImageView) view.findViewById(R.id.ivUserPhoto);
        }
    }
}
