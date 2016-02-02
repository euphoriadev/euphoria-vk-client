package ru.euphoriadev.vk.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import ru.euphoriadev.vk.R;
import ru.euphoriadev.vk.UserProfileActivity;
import ru.euphoriadev.vk.api.model.VKUser;
import ru.euphoriadev.vk.util.ThemeManager;
import ru.euphoriadev.vk.util.ViewUtil;

/**
 * Created by Igor on 16.07.15.
 */
public class FriendsAdapter extends BaseArrayAdapter<VKUser> {
    private ArrayList<VKUser> mCleanFriends;

    public FriendsAdapter(Context context, ArrayList<VKUser> friends) {
        super(context, friends);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = getInflater().inflate(R.layout.list_item_friends, parent, false);
        }

        final VKUser user = getItem(position);

        TextView tvName = (TextView) view.findViewById(R.id.tvUserName);
        TextView tvScreenName = (TextView) view.findViewById(R.id.tvUserScreenName);
        TextView tvOnlineStatus = (TextView) view.findViewById(R.id.tvUserOnline);
        ImageView ivPhoto = (ImageView) view.findViewById(R.id.ivUserPhoto);

        tvName.setTextColor(ThemeManager.getPrimaryTextColor());
        tvName.setText(user.toString());

        tvScreenName.setTextColor(ThemeManager.getSecondaryTextColor());
        tvScreenName.setText("@".concat(user.screen_name));

        ViewUtil.setTypeface(tvName);
        ViewUtil.setTypeface(tvScreenName);
        ViewUtil.setTypeface(tvOnlineStatus);

        if (user.online) {
            tvOnlineStatus.setText(getContext().getString(R.string.online));
            tvOnlineStatus.setTextColor(getContext().getResources().getColor(R.color.md_teal_500));
        } else {
            tvOnlineStatus.setText(getContext().getString(R.string.offline));
            tvOnlineStatus.setTextColor(ThemeManager.getSecondaryTextColor());
        }

        ivPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), UserProfileActivity.class);
                intent.putExtra("user_id", user.user_id);
                getContext().startActivity(intent);
            }
        });

        Picasso.with(getContext())
                .load(user.photo_50)
                .into(ivPhoto);

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
}
