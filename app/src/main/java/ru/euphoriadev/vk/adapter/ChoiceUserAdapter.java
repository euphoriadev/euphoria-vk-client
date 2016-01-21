package ru.euphoriadev.vk.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import ru.euphoriadev.vk.R;
import ru.euphoriadev.vk.api.model.VKUser;
import ru.euphoriadev.vk.util.ThemeManagerOld;

/**
 * Created by Igor on 01.11.15.
 */
public class ChoiceUserAdapter extends BaseAdapter implements View.OnClickListener {
    public ArrayList<Integer> checkedUsers;
    int primaryTextColor;
    int secondaryTextColor;
    private LayoutInflater inflater;
    private Context context;
    private Picasso picasso;
    private ThemeManagerOld manager;
    private Typeface typeface;
    private ArrayList<VKUser> users;
    private boolean isSystemFont;

    public ChoiceUserAdapter(Context context, ArrayList<VKUser> users) {
        this.context = context;
        this.users = users;
        this.manager = ThemeManagerOld.get(context);
        this.picasso = Picasso.with(context);
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        this.isSystemFont = manager.isSystemFont();
        this.checkedUsers = new ArrayList<>();
        if (isSystemFont)
        this.typeface = Typeface.createFromAsset(context.getAssets(), manager.getFont());

        primaryTextColor = manager.getPrimaryTextColor();
        secondaryTextColor = manager.getSecondaryTextColor();

    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public VKUser getItem(int position) {
        return users.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.easy_user_list_item, parent, false);

            holder = new ViewHolder(convertView);
            convertView.setTag(holder);

            if (isSystemFont) {
                holder.tvFullName.setTypeface(typeface);
                holder.tvOnlineStatus.setTypeface(typeface);
            }
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
        picasso.load(user.photo_50).into(holder.ivPhoto);
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
