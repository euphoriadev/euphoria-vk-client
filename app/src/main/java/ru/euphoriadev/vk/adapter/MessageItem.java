package ru.euphoriadev.vk.adapter;

import ru.euphoriadev.vk.api.model.VKMessage;
import ru.euphoriadev.vk.api.model.VKUser;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by user on 09.05.15.
 */
public class MessageItem implements Serializable {
    public VKMessage message;
    public VKUser user;
    public Status status;
    public Date date;

    public enum Status {
        SENDING,
        SENT,
        ERROR
    }

    public MessageItem(VKMessage message) {
        new MessageItem(message, new VKUser());
    }

    public MessageItem(VKMessage message, VKUser user) {
        this.message = message;
        this.user = user;
        status = Status.SENT;
        date = new Date(message.date * 1000);
    }

    public MessageItem(VKMessage message, VKUser user, Status status) {
        this.message = message;
        this.user = user;
        this.status = status;
        date = new Date(message.date * 1000);
    }

    public MessageItem init(VKMessage message, VKUser user) {
        this.message = message;
        this.user = user;
        this.date = new Date(message.date * 1000);
        return this;
    }


    public void setStatus(Status status) {
        this.status = status;
    }
}
