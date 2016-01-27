package ru.euphoriadev.vk.adapter;

import ru.euphoriadev.vk.api.model.VKFullUser;

/**
 * Created by Igor on 30.06.15.
 */
public class ChatMember {
    public VKFullUser user;
    public VKFullUser invitedBy;

    public ChatMember(VKFullUser user, VKFullUser invitedBy) {
        this.user = user;
        this.invitedBy = invitedBy;
    }
}