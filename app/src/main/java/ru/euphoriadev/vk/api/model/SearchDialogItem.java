package ru.euphoriadev.vk.api.model;

public class SearchDialogItem {

    public String str_type;
    public SDIType type;
    public String email;
    public VKFullUser user;
    public VKMessage chat;
    public enum SDIType {USER, CHAT, EMAIL}

}
