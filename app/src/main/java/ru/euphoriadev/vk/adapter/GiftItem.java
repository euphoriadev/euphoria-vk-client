package ru.euphoriadev.vk.adapter;

import ru.euphoriadev.vk.api.model.VKGift;
import ru.euphoriadev.vk.api.model.VKUser;

/**
 * Created by user on 24.07.15.
 */
public class GiftItem {

    public VKUser fromUser;
    public VKGift gift;

    public GiftItem(VKUser fromUser, VKGift gift) {
        this.fromUser = fromUser;
        this.gift = gift;
    }
}
