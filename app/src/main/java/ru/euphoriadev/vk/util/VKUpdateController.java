package ru.euphoriadev.vk.util;

import android.util.Log;

import java.util.ArrayList;

import ru.euphoriadev.vk.api.model.VKMessage;

/**
 * Created by Igor on 17.02.16.
 * <p/>
 * Helper, for control receipt of messages
 */
public class VKUpdateController {
    private static VKUpdateController instance;
    private static final String TAG = "VKUpdateController";

    private ArrayList<MessageListener> messageListeners = new ArrayList<>(6);
    private ArrayList<UserListener> userListeners = new ArrayList<>(6);

    public static VKUpdateController getInstance() {
        VKUpdateController localInstance = instance;
        if (localInstance == null) {
            synchronized (VKUpdateController.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new VKUpdateController();
                }
            }
        }
        return localInstance;
    }

    public void addListener(MessageListener listener) {
        Log.w(TAG, "register message listener: " + listener);
        messageListeners.add(listener);
    }

    public void addListener(UserListener listener) {
        Log.w(TAG, "register user listener: " + listener);
        userListeners.add(listener);
    }

    public void addUserListener(UserListener listener) {
        addListener(listener);
    }

    public void addMessageListenr(MessageListener listener) {
        addListener(listener);
    }

    public void removeListener(MessageListener listener) {
        messageListeners.remove(listener);
    }

    public void removeUserListener(UserListener listener) {
        removeListener(listener);
    }

    public void removeMessageListener(MessageListener listener) {
        removeListener(listener);
    }

    public void removeListener(UserListener listener) {
        userListeners.remove(listener);
    }

    public void updateMessageListenersForNew(VKMessage message) {
        if (!ArrayUtil.isEmpty(messageListeners)) {
            for (int i = 0; i < messageListeners.size(); i++) {
                messageListeners.get(i).onNewMessage(message);
            }
        }
    }

    public void updateMessageListenersForRead(int id) {
        if (!ArrayUtil.isEmpty(messageListeners)) {
            for (int i = 0; i < messageListeners.size(); i++) {
                messageListeners.get(i).onReadMessage(id);
            }
        }
    }

    public void updateMessageListenersForDelete(int id) {
        if (!ArrayUtil.isEmpty(messageListeners)) {
            for (int i = 0; i < messageListeners.size(); i++) {
                messageListeners.get(i).onDeleteMessage(id);
            }
        }
    }

    public void updateUserListenersForOffline(int user_id) {
        if (!ArrayUtil.isEmpty(userListeners)) {
            for (int i = 0; i < userListeners.size(); i++) {
                userListeners.get(i).onOffline(user_id);
            }
        }
    }

    public void updateUserListenersForOnline(int user_id) {
        if (!ArrayUtil.isEmpty(userListeners)) {
            for (int i = 0; i < userListeners.size(); i++) {
                userListeners.get(i).onOnline(user_id);
            }
        }
    }

    public void updateUserListenersForTyping(int user_id, int chat_id) {
        if (!ArrayUtil.isEmpty(userListeners)) {
            for (int i = 0; i < userListeners.size(); i++) {
                userListeners.get(i).onTyping(user_id, chat_id);
            }
        }
    }

    public void cleanup() {
        messageListeners.clear();
        messageListeners.trimToSize();

        userListeners.clear();
        userListeners.trimToSize();
    }

    /**
     * Interface definition for a callback to be invoked when
     * user gets new messages, or other action associated with the message.
     * e.g: read/delete message
     */
    public interface MessageListener {
        /**
         * Called when user gets new message from long poll server
         *
         * @param message the new message
         */

        void onNewMessage(VKMessage message);

        /**
         * Called when read all outgoing messages
         *
         * @param message_id the id of read message
         */
        void onReadMessage(int message_id);

        /**
         * Called when message is deleted
         *
         * @param message_id the id of deleted message
         */
        void onDeleteMessage(int message_id);
    }

    /**
     * Interface definition for a callback to be invoked when user did the action.
     * e.g.: user became offline/start typing on chat
     */
    public interface UserListener {
        /**
         * Called when a friend became offline. e.g. clicked "Log Out"
         * or offline upon timeout
         *
         * @param user_id the id of user which has become offline
         */
        void onOffline(int user_id);

        /**
         * Called when a friend became online,
         *
         * @param user_id the id of user which has become online
         */
        void onOnline(int user_id);

        /**
         * Called when user started typing text in a dialog.
         * NOTE: The event is sent once in ~5 sec if constantly typing
         *
         * @param chat_id the id of chat, which started to typing text
         * @param user_id the id of typing user
         */
        void onTyping(int user_id, int chat_id);
    }
}
