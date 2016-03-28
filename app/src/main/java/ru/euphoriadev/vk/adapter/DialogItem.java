package ru.euphoriadev.vk.adapter;

import java.util.Comparator;
import java.util.Date;

import ru.euphoriadev.vk.api.model.VKMessage;
import ru.euphoriadev.vk.api.model.VKUser;
import ru.euphoriadev.vk.util.IntegerCompat;

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

    public static final Comparator<DialogItem> COMPARATOR_BY_UNREAD = new Comparator<DialogItem>() {
        @Override
        public int compare(DialogItem lhs, DialogItem rhs) {
            return IntegerCompat.compare(rhs.message.unread, lhs.message.unread);
        }
    };
}