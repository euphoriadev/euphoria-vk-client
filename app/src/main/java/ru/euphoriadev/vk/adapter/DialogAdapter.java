package ru.euphoriadev.vk.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import ru.euphoriadev.vk.R;
import ru.euphoriadev.vk.api.Api;
import ru.euphoriadev.vk.api.model.VKMessage;
import ru.euphoriadev.vk.api.model.VKUser;
import ru.euphoriadev.vk.async.ThreadExecutor;
import ru.euphoriadev.vk.common.ThemeManager;
import ru.euphoriadev.vk.common.TypefaceManager;
import ru.euphoriadev.vk.helper.DBHelper;
import ru.euphoriadev.vk.util.AndroidUtils;
import ru.euphoriadev.vk.util.ThemeUtils;
import ru.euphoriadev.vk.util.VKUpdateController;
import ru.euphoriadev.vk.util.ViewUtil;
import ru.euphoriadev.vk.view.CircleView;

/**
 * Created by Igor on 30.03.15.
 */
public class DialogAdapter extends BaseAdapter implements VKUpdateController.MessageListener, VKUpdateController.UserListener {

    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
    private Context context;
    private LayoutInflater inflater;
    private ArrayList<DialogItem> dialogItems;
    private Handler handler;
    private DBHelper helper;
    private Typeface typefaceBold;
    private int fullNameTextColor;
    private int bodyTextColor;
    private Date date;


    public DialogAdapter(final Context context, ArrayList<DialogItem> listMessages) {
        this.context = context;

        dialogItems = listMessages;
        inflater = (LayoutInflater)
                this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        fullNameTextColor = ThemeManager.getPrimaryTextColor();
        bodyTextColor = ThemeManager.getSecondaryTextColor();
        date = new Date(System.currentTimeMillis());

        helper = DBHelper.get(context);
        helper.open();
        handler = new Handler(Looper.getMainLooper());

        VKUpdateController.getInstance().addUserListener(this);
        VKUpdateController.getInstance().addMessageListener(this);
    }

    @Override
    public void onNewMessage(VKMessage message) {
        update(message); // заменяем элемент
        Collections.sort(dialogItems); // сортируем по дате
        notifyDataSetChanged(); // обновляем


    }


    @Override
    public void onDeleteMessage(int message_id) {

    }


    @Override
    public void onReadMessage(int message_id) {
        for (int i = 0; i < dialogItems.size(); i++) {
            DialogItem item = dialogItems.get(i);
            if (item.message.mid == message_id) {
                item.message.read_state = true;
                item.message.unread = 0;
                notifyDataSetChanged();
                break;
            }
        }
    }


    public void disconnectLongPoll() {
        VKUpdateController.getInstance().removeUserListener(this);
        VKUpdateController.getInstance().removeMessageListener(this);
    }

    public DialogItem search(long uid, long chat_id) {
        for (DialogItem dialog : dialogItems) {
            // ищем сначала chat
            if (chat_id == dialog.message.chat_id && chat_id != 0) {
                return dialog;
            }

            // теперь ищем user-а
            if (uid == dialog.message.uid && chat_id == dialog.message.chat_id) {
                return dialog;
            }
        }

        return null;
    }

    public ArrayList<DialogItem> getDialogs() {
        return dialogItems;
    }

    @Override
    public int getCount() {
        return dialogItems.size();
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        date.setTime(System.currentTimeMillis());
    }

    public Object getItem(int position) {
        return dialogItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.list_item_dialog, parent, false);

            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        final DialogItem item = (DialogItem) getItem(position);
        if (item == null) {
            return view;
        }

        final VKUser user = item.user;
        final VKMessage message = item.message;
        holder.tvFullName.setTextColor(fullNameTextColor);
        holder.tvFullName.setText(message.isChat() ? message.title : user.toString());

        final int fontFamily = TypefaceManager.getFontFamily();
        final int textWeight = TypefaceManager.getTextWeight();
        typefaceBold = TypefaceManager.getTypeface(context, fontFamily, textWeight != TypefaceManager.TextWeight.BOLD && textWeight != 0 ? textWeight + 1 : textWeight);

        // если сообщение не прочитанно мной
        if (!message.read_state && !message.is_out) {
            // ставим жирный шрифт, для наглядности
            holder.tvFullName.setTypeface(typefaceBold);
        } else {
            ViewUtil.setTypeface(holder.tvFullName);
        }
        ViewUtil.setTypeface(holder.tvBody);
//        ViewUtil.setTypeface(holder.tvUnreadCount);

        holder.tvBody.setTextColor(bodyTextColor);
        holder.tvDate.setTextColor(fullNameTextColor);
//        holder.tvDate.setText(sdf.format(message.date * 1000));
        if (this.date.getYear() > item.date.getYear()) {
            // если отправили больше года назад
            sdf.applyPattern("d MMM, yyyy"); // 23 Окт, 2015
        } else if (this.date.getMonth() > item.date.getMonth()) {
            // если отправили больше месяца назад
            sdf.applyPattern("d MMM"); // 23 Окт
        } else if (this.date.getDate() > item.date.getDate()) {
            // Следовательно этот месяц, но несколько дней назад
            // так же
            sdf.applyPattern("d MMM"); // 23 Окт
        } else {
            // если сегодня
            sdf.applyPattern("HH:mm"); // 15:57

        }
        holder.tvDate.setText(sdf.format(item.date.getTime()));

        holder.indicator.setTextSize(10);
        holder.indicator.setTextColor(ThemeManager.getPrimaryTextColorOnAccent(context));
        if (message.isChat()) {
            holder.tvFullName.setText(holder.tvFullName.getText());
        } else {
            holder.tvFullName.setCompoundDrawables(null, null, null, null);
        }

        // Если я отправил, но пользователь еще не прочитал
        if (message.is_out && !message.read_state) {
            //  holder.tvBody.setBackgroundColor(context.getResources().getColor(R.color.translucent_white));
            holder.tvFullName.setTextColor(bodyTextColor);
            holder.tvDate.setTextColor(bodyTextColor);
        } else {
            // holder.tvBody.setBackgroundColor(Color.TRANSPARENT);
            holder.tvFullName.setTextColor(fullNameTextColor);
            holder.tvDate.setTextColor(fullNameTextColor);

        }

        // Если мне отправили, и я не прочитал
        if (!message.is_out && !message.read_state) {
            holder.indicator.setVisibility(View.VISIBLE);
//            holder.indicator.getBackground().setColorFilter(ThemeManager.getColorAccent(context), PorterDuff.Mode.MULTIPLY);
            holder.tvDate.setTextColor(ThemeManager.getColorAccent(context));

            holder.tvBody.setTextColor(fullNameTextColor);
            if (item.message.unread != 0)
                holder.indicator.setText(item.message.unread > 1000 ? item.message.unread / 1000 + "к" : item.message.unread + "");
            //      holder.lLayout.setBackgroundColor(context.getResources().getColor(R.color.translucent_white));
        } else {
            //      holder.lLayout.setBackgroundColor(Color.TRANSPARENT);
            holder.indicator.setVisibility(View.GONE);
            holder.indicator.setText("");
            holder.tvBody.setTextColor(bodyTextColor);
            //       holder.tvDate.setTextColor(fullNameTextColor);

        }

        if (user.online && !item.message.isChat()) {
            holder.onlineIndicator.setVisibility(View.VISIBLE);
            if (holder.onlineIndicator.getBackground() == null) {
                holder.onlineIndicator.setBackgroundDrawable(user.online_mobile ?
                        AndroidUtils.getDrawable(context, R.drawable.ic_android) :
                        AndroidUtils.getDrawable(context, R.drawable.shape_circle));
            }

            holder.onlineIndicator.setLayoutParams(new LinearLayout.LayoutParams(
                    AndroidUtils.pxFromDp(context, user.online_mobile ? 15 : 10),
                    AndroidUtils.pxFromDp(context, user.online_mobile ? 15 : 10)
            ));
            holder.onlineIndicator.getBackground().setColorFilter(ThemeUtils.getThemeAttrColor(context, R.attr.colorAccent), PorterDuff.Mode.MULTIPLY);
        } else {
            holder.onlineIndicator.setBackgroundDrawable(null);
            holder.onlineIndicator.setVisibility(View.GONE);
        }


        // если набирает текст
        if (item.isTyping) {
            holder.tvBody.setText((context.getResources().getString(R.string.is_typing)));
        } else {
            holder.tvBody.setText(message.body);
        }

        if ((item.isTyping ? item.userIdTyping == Api.get().getUserId() : item.message.is_out) || item.message.isChat()) {
            holder.ivLastPhotoUser.setVisibility(View.VISIBLE);

            try {
                Picasso.with(context)
                        .load(item.message.is_out ? Api.get().getAccount().photo : user.photo_50)
                        .placeholder(R.drawable.camera_b)
                        .config(Bitmap.Config.RGB_565)
                        .into(holder.ivLastPhotoUser);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            holder.ivLastPhotoUser.setVisibility(View.GONE);
            holder.ivLastPhotoUser.setImageDrawable(null);
        }


//      Загрузка изображений
        try {
            Picasso.with(context)
                    .load(TextUtils.isEmpty(message.photo_50) ? user.photo_50 : message.photo_50) // загрузка
                    .placeholder(R.drawable.camera_b) // заглушка
                    .config(Bitmap.Config.RGB_565)
                    .into(holder.ivPhoto);
        } catch (Exception e) {
            e.printStackTrace();
        }


        holder.ivPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(context, ProfileActivity.class);
//                intent.putExtra("uid", message.uid);
//                context.startActivity(intent);
            }
        });

        if (message.attachments != null && !message.attachments.isEmpty()) {
            Spannable attachmentString = new SpannableString(message.attachments.size() > 1 ? "[Вложения]" : "[Вложение]");
            attachmentString.setSpan(new StyleSpan(Typeface.BOLD), 0, attachmentString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            attachmentString.setSpan(new ForegroundColorSpan(holder.tvBody.getLinkTextColors().getDefaultColor()), 0, attachmentString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            if (message.body.length() > 0) {
                holder.tvBody.setSingleLine(false);
                holder.tvBody.setMaxLines(2);
                holder.tvBody.append("\n");
                holder.tvBody.append(attachmentString);
            } else holder.tvBody.setText(attachmentString);
        }

        // если это служобное сообщение
        if (!TextUtils.isEmpty(message.action)) {
            String messageActionText = "";
            switch (message.action) {
                case "chat_photo_update":
                    messageActionText = user.toString() + " обновил(а) фото беседы";
                    break;

                case "chat_photo_remove":
                    messageActionText = user.toString() + " удалил(а) фото беседы";
                    break;

                case "chat_create":
                    messageActionText = user.toString() + " создал(а) беседу";
                    break;

                case "chat_title_update":
                    messageActionText = user.toString() + " обновил(а) нвазние беседы";
                    break;

                case "chat_invite_user":
                    messageActionText = user.toString() + " добавлен(а) в чат";
                    break;

                case "chat_kick_user":
                    messageActionText = message.action_mid == Api.get().getUserId() ? "Вас исключили из чата" : DBHelper.get(context).getUserFromDB(message.action_mid).toString() + " исключили из чата";
                    break;

            }
            holder.tvBody.setText(Html.fromHtml("<b>" + messageActionText + "</b>"));

        }
        return view;
    }

    public synchronized void update(final VKMessage updateMessage) {
        DialogItem item = search(updateMessage.uid, updateMessage.chat_id);
        if (item != null) {
            // если чат с одним пользователем
            if (!item.message.isChat()) {
                item.message.mid = updateMessage.mid;
                item.message.body = updateMessage.body;
                item.message.is_out = updateMessage.is_out;
                item.message.date = updateMessage.date;
                item.message.read_state = updateMessage.read_state;
                item.message.date = updateMessage.date;
                item.isTyping = false;

                item.message.unread++;
                if (item.message.is_out) item.message.unread = 0;
            } else {
                // если это беседа
                // если id совпадают, и они не 0 - значит мы нашли
                if (updateMessage.chat_id == item.message.chat_id) {

                    if (updateMessage.uid != item.message.uid) {
                        // если написал другой человек, то загружаем его из базы
                        VKUser user = helper.getUserFromDB(updateMessage.uid);
                        if (user != null) {
                            // Если он есть в бд
                            item.user = user;
                        } else {
                            // заносим его в бд, и заного получаем + обновляем адаптер
                            addUserToDB(updateMessage.uid, updateMessage);
                            return;
                        }
                    }

                    item.message.mid = updateMessage.mid;
                    item.message.uid = updateMessage.uid;
                    //  item.message.chat_id = updateMessage.chat_id;
                    item.message.title = updateMessage.title;
                    item.message.photo_50 = updateMessage.photo_50;
                    item.message.photo_100 = updateMessage.photo_100;
                    item.message.body = updateMessage.body;
                    item.message.is_out = updateMessage.is_out;
                    item.message.read_state = updateMessage.read_state;
                    item.message.date = updateMessage.date;
                    item.isTyping = false;

                    item.message.unread++;
                    if (item.message.is_out) item.message.unread = 0;
                }
            }
            notifyDataSetChanged();
            return;
        }

        // если пришло новое сообщение, либо которое не отображается в списке
        ThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final VKUser profile = Api.get().getProfile(updateMessage.uid);
                if (profile == null) {
                    return;
                }
                helper.addUserToDB(profile);
                dialogItems.add(0, new DialogItem(updateMessage, profile));
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        update(updateMessage);
                    }
                });
            }
        });
    }

    private void addUserToDB(final int uid, final VKMessage message) {
        ThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                VKUser profile = Api.get().getProfile(uid);
                if (profile == null) {
                    return;
                }
                helper.addUserToDB(profile);

                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // что бы не было удвоение счетчика
                        message.unread--;
                        update(message);
                    }
                });
            }
        });
    }

    public void clear() {
        if (dialogItems != null) {
            dialogItems.clear();
            dialogItems.trimToSize();
            dialogItems = null;
        }
    }

    @Override
    public void onOffline(int user_id) {

    }

    @Override
    public void onOnline(int user_id) {

    }

    @Override
    public void onTyping(int user_id, int chat_id) {
        final DialogItem dialog = search(user_id, chat_id);
        if (dialog != null) {
            dialog.isTyping = true;
            dialog.userIdTyping = user_id;
            dialog.chatIdTyping = chat_id;
            notifyDataSetChanged();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    dialog.isTyping = false;
                    dialog.userIdTyping = -1;
                    dialog.chatIdTyping = -1;
                    notifyDataSetChanged();
                }
            }, 6000);
        }
    }

    static class ViewHolder {
        TextView tvFullName;
        TextView tvBody;
        TextView tvDate;
        CircleView indicator;
        //        TextView tvUnreadCount;
        ImageView ivPhoto;
        ImageView ivLastPhotoUser;
        View onlineIndicator;
        //     LinearLayout lLayout;

        public ViewHolder(View v) {
            tvFullName = (TextView) v.findViewById(R.id.tvDialogTitle);
            tvBody = (TextView) v.findViewById(R.id.tvDialogBody);
            tvDate = (TextView) v.findViewById(R.id.tvDialogDate);
            ivPhoto = (ImageView) v.findViewById(R.id.ivDialogPhoto);
            ivLastPhotoUser = (ImageView) v.findViewById(R.id.ivDialogLastPhotoUser);
//            tvUnreadCount = (TextView) v.findViewById(R.id.tvUnreadCount);
            indicator = (CircleView) v.findViewById(R.id.vDialogUnreadIndicator);
            onlineIndicator = v.findViewById(R.id.viewDialogOnlineIndicator);

        }

    }

}