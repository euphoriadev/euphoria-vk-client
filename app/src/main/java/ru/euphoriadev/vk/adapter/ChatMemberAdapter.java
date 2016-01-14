package ru.euphoriadev.vk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import ru.euphoriadev.vk.R;
import ru.euphoriadev.vk.util.ViewUtil;

/**
 * Created by user on 30.06.15.
 */
public class ChatMemberAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater inflater;
    private ArrayList<ChatMember> members;

    public ChatMemberAdapter(Context context, ArrayList<ChatMember> members) {
        this.mContext = context;
        this.members = members;
        this.inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return members.size();
    }

    @Override
    public Object getItem(int position) {
        return members.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.chat_member_list_item, parent, false);
        }

        ChatMember member = (ChatMember) getItem(position);

        TextView tvTitle = (TextView) view.findViewById(R.id.tvChatMemberTitle);
        TextView tvInvitedBy = (TextView) view.findViewById(R.id.tvChatMemberInvitedBy);
        ImageView ivPhoto = (ImageView) view.findViewById(R.id.ivChatMember);

        ViewUtil.setTypeface(tvTitle);
        ViewUtil.setTypeface(tvInvitedBy);

        tvTitle.setText(member.user.toString());
        if (member.user.uid == member.invitedBy.uid) {
            tvInvitedBy.setText(mContext.getString(R.string.owner_of_chat));
        } else {
            tvInvitedBy.setText(mContext.getString(R.string.invited_by) + member.invitedBy.toString());
        }

        Picasso.with(mContext).load(member.user.photo).into(ivPhoto);

        return view;
    }


}
