package ru.euphoriadev.vk.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import ru.euphoriadev.vk.R;
import ru.euphoriadev.vk.api.model.VKUser;
import ru.euphoriadev.vk.util.ThemeManager;
import ru.euphoriadev.vk.util.ViewUtil;

/**
 * Created by Igor on 01.11.15.
 */
public class ChoiceUserAdapter extends BaseArrayAdapter<VKUser> implements View.OnClickListener {
    public ArrayList<Integer> checkedUsers;
    int primaryTextColor;
    int secondaryTextColor;

    public ChoiceUserAdapter(Context context, ArrayList<VKUser> users) {
        super(context, users);
        this.checkedUsers = new ArrayList<>();

        this.primaryTextColor = ThemeManager.getPrimaryTextColor();
        this.secondaryTextColor = ThemeManager.getSecondaryTextColor();

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = getInflater().inflate(R.layout.easy_user_list_item, parent, false);

            holder = new ViewHolder(convertView);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        convertView.setOnClickListener(this);

        final VKUser user = getItem(position);

        holder.tvFullName.setTextColor(primaryTextColor);
        holder.tvOnlineStatus.setTextColor(secondaryTextColor);

        holder.tvFullName.setText(user.toString());
        holder.tvOnlineStatus.setText(user.online ? "Online" : "Offline");
        holder.checkBox.setChecked(checkedUsers.contains(user.user_id));
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.checkBox.isChecked()) {
                    if (!checkedUsers.contains(user.user_id)) checkedUsers.add(user.user_id);
                } else {
                    checkedUsers.remove(user.user_id);
                }
            }
        });

        ViewUtil.setTypeface(holder.tvFullName);
        ViewUtil.setTypeface(holder.tvOnlineStatus);

        Picasso.with(getContext()).load(user.photo_50).into(holder.ivPhoto);
        return convertView;
    }

    @Override
    public void onClick(View v) {
        ViewHolder holder = (ViewHolder) v.getTag();
        holder.checkBox.setChecked(!holder.checkBox.isChecked());
    }

    static class ViewHolder {
        public TextView tvFullName;
        public TextView tvOnlineStatus;
        public ImageView ivPhoto;
        public CheckBox checkBox;

        public ViewHolder(View v) {
            tvFullName = (TextView) v.findViewById(R.id.tvUserName);
            tvOnlineStatus = (TextView) v.findViewById(R.id.tvUserOnline);
            checkBox = (CheckBox) v.findViewById(R.id.cbUser);
            ivPhoto = (ImageView) v.findViewById(R.id.ivUserPhoto);
        }
    }
}
