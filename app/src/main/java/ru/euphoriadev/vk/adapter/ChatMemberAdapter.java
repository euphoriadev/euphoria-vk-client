package ru.euphoriadev.vk.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import ru.euphoriadev.vk.R;
import ru.euphoriadev.vk.util.ViewUtil;

/**
 * Created by user on 30.06.15.
 */
public class ChatMemberAdapter extends BaseArrayAdapter<ChatMember> {

    public ChatMemberAdapter(Context context, ArrayList<ChatMember> members) {
        super(context, members);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = getInflater().inflate(R.layout.list_item_chat_member, parent, false);
        }

        ChatMember member = getItem(position);

        TextView tvTitle = (TextView) view.findViewById(R.id.tvChatMemberTitle);
        TextView tvInvitedBy = (TextView) view.findViewById(R.id.tvChatMemberInvitedBy);
        ImageView ivPhoto = (ImageView) view.findViewById(R.id.ivChatMember);

        ViewUtil.setTypeface(tvTitle);
        ViewUtil.setTypeface(tvInvitedBy);

        tvTitle.setText(member.user.toString());
        if (member.user.uid == member.invitedBy.uid) {
            tvInvitedBy.setText(getContext().getString(R.string.owner_of_chat));
        } else {
            tvInvitedBy.setText(getContext().getString(R.string.invited_by) + member.invitedBy.toString());
        }

        Picasso.with(getContext()).load(member.user.photo).into(ivPhoto);

        return view;
    }


}
