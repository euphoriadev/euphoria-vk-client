package ru.euphoriadev.vk.adapter;

import java.util.Date;

import ru.euphoriadev.vk.api.model.VKMessage;
import ru.euphoriadev.vk.api.model.VKUser;

/**
 * Created by user on 09.05.15.
 */

public class DialogItem implements Comparable<DialogItem> {


    public VKMessage message;
    public VKUser user;
    public Date date;

    public boolean isTyping;
    public int userIdTyping;
    public int chatIdTyping;


    public DialogItem(VKMessage message, VKUser user) {
        this.message = message;
        this.user = user;
        date = new Date(message.date * 1000);
    }


    @Override
    public int compareTo(DialogItem another) {

        return (this.message.date > another.message.date) ? -1 : ((this.message.date == another.message.date) ? 1 : 0);
    }
}