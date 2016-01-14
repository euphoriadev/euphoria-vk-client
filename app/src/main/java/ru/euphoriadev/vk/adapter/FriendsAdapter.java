package ru.euphoriadev.vk.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import ru.euphoriadev.vk.R;
import ru.euphoriadev.vk.UserProfileActivity;
import ru.euphoriadev.vk.api.model.VKUser;
import ru.euphoriadev.vk.util.ThemeManagerOld;
import ru.euphoriadev.vk.util.ViewUtil;

/**
 * Created by Igor on 16.07.15.
 */
public class FriendsAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<VKUser> mFriends;
    private ArrayList<VKUser> mCleanFriends;
    private ThemeManagerOld mtm;
    private Typeface mTypeface;

    public FriendsAdapter(Context context, ArrayList<VKUser> friends) {
        this.mContext = context;
        this.mFriends = friends;
        this.mtm = new ThemeManagerOld(this.mContext);
        this.mInflater = (LayoutInflater)
                this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (!mtm.isSystemFont())
            mTypeface = Typeface.createFromAsset(this.mContext.getAssets(), mtm.getFont());

    }

    @Override
    public int getCount() {
        return mFriends.size();
    }

    @Override
    public Object getItem(int position) {
        return mFriends.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = mInflater.inflate(R.layout.friends_list_item, parent, false);
        }

        final VKUser user = mFriends.get(position);

        TextView tvName = (TextView) view.findViewById(R.id.tvUserName);
        TextView tvScreenName = (TextView) view.findViewById(R.id.tvUserScreenName);
        TextView tvOnlineStatus = (TextView) view.findViewById(R.id.tvUserOnline);
        ImageView ivPhoto = (ImageView) view.findViewById(R.id.ivUserPhoto);

        tvName.setTextColor(mtm.getPrimaryTextColor());
        tvName.setText(user.toString());

        tvScreenName.setTextColor(mtm.getSecondaryTextColor());
        tvScreenName.setText("@".concat(user.screen_name));

        ViewUtil.setTypeface(tvName);
        ViewUtil.setTypeface(tvScreenName);
        ViewUtil.setTypeface(tvOnlineStatus);

        if (user.online) {
            tvOnlineStatus.setText(mContext.getString(R.string.online));
            tvOnlineStatus.setTextColor(mContext.getResources().getColor(R.color.md_teal_500));
        } else {
            tvOnlineStatus.setText(mContext.getString(R.string.offline));
            tvOnlineStatus.setTextColor(mtm.getSecondaryTextColor());
        }

        ivPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, UserProfileActivity.class);
                intent.putExtra("user_id", user.user_id);
                mContext.startActivity(intent);
            }
        });

        Picasso.with(mContext)
                .load(user.photo_50)
                .into(ivPhoto);

        return view;
    }

    public void filter(String q) {
        q = q.toLowerCase();
        if (mCleanFriends == null) {
            mCleanFriends = new ArrayList<>(mFriends.size());
            mCleanFriends.addAll(mFriends);
        }

        mFriends.clear();
        if (TextUtils.isEmpty(q)) {
            mFriends.addAll(mCleanFriends);

            mCleanFriends.clear();
            mCleanFriends.trimToSize();
            mCleanFriends = null;
            notifyDataSetChanged();
            return;
        }

        final int sizeFriends = mCleanFriends.size();
        for (int i = 0; i < sizeFriends; i++) {
            VKUser user = mCleanFriends.get(i);

            if (user.toString().toLowerCase().contains(q)) {
                mFriends.add(user);
            }
        }
        notifyDataSetChanged();
    }

    public void filterByOnline() {
        if (mCleanFriends == null) {
            mCleanFriends = new ArrayList<>(mFriends.size());
            mCleanFriends.addAll(mFriends);
        }

        mFriends.clear();
        final int sizeFriends = mCleanFriends.size();
        for (int i = 0; i < sizeFriends; i++) {
            VKUser user = mCleanFriends.get(i);

            if (user.online) {
                mFriends.add(user);
            }
        }
        notifyDataSetChanged();
    }

    public void filterByOffline() {
        if (mCleanFriends == null) {
            mCleanFriends = new ArrayList<>(mFriends.size());
            mCleanFriends.addAll(mFriends);
        }

        mFriends.clear();
        final int sizeFriends = mCleanFriends.size();
        for (int i = 0; i < sizeFriends; i++) {
            VKUser user = mCleanFriends.get(i);

            if (!user.online) {
                mFriends.add(user);
            }
        }
        notifyDataSetChanged();
    }

    public void clear() {
        if (mCleanFriends != null) {
            mCleanFriends.clear();
            mCleanFriends.trimToSize();
            mCleanFriends = null;
        }

        if (mFriends != null) {
            mFriends.clear();
            mFriends.trimToSize();
            mFriends = null;
        }
        mTypeface = null;
    }


}
