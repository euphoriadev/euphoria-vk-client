package ru.euphoriadev.vk.adapter;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.*;
import android.os.Handler;
import android.os.IBinder;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import ru.euphoriadev.vk.ProfileActivity;
import ru.euphoriadev.vk.R;
import ru.euphoriadev.vk.api.Api;
import ru.euphoriadev.vk.api.model.VKMessage;
import ru.euphoriadev.vk.api.model.VKUser;
import ru.euphoriadev.vk.helper.DBHelper;
import ru.euphoriadev.vk.service.LongPollService;
import ru.euphoriadev.vk.util.AndroidUtils;
import ru.euphoriadev.vk.util.ThemeManagerOld;
import ru.euphoriadev.vk.util.ThemeUtils;
import ru.euphoriadev.vk.util.ThreadExecutor;
import ru.euphoriadev.vk.util.TypefaceManager;
import ru.euphoriadev.vk.util.ViewUtil;
import ru.euphoriadev.vk.view.TextCircleView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 * Created by Igor on 30.03.15.
 */
public class DialogAdapter extends BaseAdapter implements LongPollService.VKOnLongPollListener {

    private Context context;
    private LayoutInflater inflater;
    private ArrayList<DialogItem> dialogItems;
    private ThemeManagerOld tManager;
    private SQLiteDatabase database;
    private Handler handler;
    private ServiceConnection serviceConnection;
    private LongPollService longPollService;
    private boolean mBoundService = false;

    private Spanned spannableString;
    private DBHelper helper;
    private Typeface typeface;
    private Typeface typefaceBold;
    private int fullNameTextColor;
    private int bodyTextColor;
    private Date date;
    private int newMessage = 0;


    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");


    public DialogAdapter(final Context context, ArrayList<DialogItem> listMessages) {
        this.context = context;

        tManager = new ThemeManagerOld(this.context);
        dialogItems = listMessages;
        inflater = (LayoutInflater)
                this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        fullNameTextColor = tManager.getPrimaryTextColor();
        bodyTextColor = tManager.getSecondaryTextColor();
        String you = context.getString(R.string.you);
        spannableString = Html.fromHtml("<b>" + you + ":&emsp;</b>");
        date = new Date(System.currentTimeMillis());
        if (!tManager.isSystemFont())
            typeface = Typeface.createFromAsset(this.context.getAssets(), tManager.getFont());
        typefaceBold = Typeface.createFromAsset(this.context.getAssets(), "Roboto-Bold.ttf");

        helper = DBHelper.get(context);
        helper.open();
        database = helper.getWritableDatabase();
        handler = new Handler(Looper.getMainLooper());
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                mBoundService = true;

                longPollService = ((LongPollService.LocalBinder) binder).getService();
                longPollService.register("DialogsAdapter", DialogAdapter.this);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mBoundService = false;
                longPollService = null;
            }
        };
    }

    @Override
    public void onNewMessage(VKMessage message) {
        update(message); // заменяем элемент
        Collections.sort(dialogItems); // сортируем по дате
        notifyDataSetChanged(); // обновляем


    }

    @Override
    public void onUserTyping(final long chat_id, final long uid) {
        final DialogItem dialog = search(uid, chat_id);
        if (dialog != null) {
            dialog.isTyping = true;
            notifyDataSetChanged();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    dialog.isTyping = false;
                    notifyDataSetChanged();
                }
            }, 5000);
        }

    }

    @Override
    public void onReadMessage(long message_id) {
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

//    public View getViewByPosition(int pos, ListView listView) {
//        final int firstListItemPosition = listView.getFirstVisiblePosition();
//        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;
//
//        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
//            return listView.getAdapter().getView(pos, null, listView);
//        } else {
//            final int childIndex = pos - firstListItemPosition;
//            return listView.getChildAt(childIndex);
//        }
//    }

    public void connectToLongPollService() {
        try {
            if (!mBoundService || longPollService == null) {
                context.bindService(new Intent(context, LongPollService.class), serviceConnection, 0);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return mBoundService;
    }

    public void disconnectLongPoll() {
        if (mBoundService || longPollService != null) {
            longPollService.unregister("DialogsAdapter");
            context.unbindService(serviceConnection);
            mBoundService = false;
        }
    }

    public DialogItem search(long uid, long cha_id) {
        for (DialogItem dialog : dialogItems) {
            // ищем сначала chat
            if (cha_id == dialog.message.chat_id && cha_id != 0) {
                return dialog;
            }

            // теперь ищем user-а
            if (uid == dialog.message.uid && cha_id == dialog.message.chat_id) {
                return dialog;
            }
        }

        return null;
    }

    public ArrayList<DialogItem> getDialogs() {
        return dialogItems;
    }

    static class ViewHolder {

        TextView tvFullName;
        TextView tvBody;
        TextView tvDate;
        View indicator;
        TextView tvUnreadCount;
        TextCircleView ivPhoto;
        View onlineIndicator;
        //     LinearLayout lLayout;

        public ViewHolder(View v) {
            tvFullName = (TextView) v.findViewById(R.id.tvDialogTitle);
            tvBody = (TextView) v.findViewById(R.id.tvDialogBody);
            tvDate = (TextView) v.findViewById(R.id.tvDialogDate);
            ivPhoto = (TextCircleView) v.findViewById(R.id.ivDialogPhoto);
            tvUnreadCount = (TextView) v.findViewById(R.id.tvUnreadCount);
            indicator = v.findViewById(R.id.vDialogUnreadIndicator);
            onlineIndicator = v.findViewById(R.id.viewDialogOnlineIndicator);

        }

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
            view = inflater.inflate(R.layout.dialog_list_item, parent, false);

            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        final DialogItem item = (DialogItem) getItem(position);
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
        ViewUtil.setTypeface(holder.tvUnreadCount);

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


        if (tManager.isTheme("DARK") || tManager.isTheme("BLACK")) {
            holder.tvUnreadCount.setTextColor(Color.BLACK);
        } else
            holder.tvUnreadCount.setTextColor(Color.WHITE);

        if (message.isChat()) {
//            IconicsDrawable iconChat = new IconicsDrawable(context, GoogleMaterial.Icon.gmd_people);
//            iconChat.sizeDp(20);
//            iconChat.color(tManager.getPrimaryTextColor());
            // holder.tvFullName.setCompoundDrawables(iconChat, null, null, null);
//            holder.tvFullName.setCompoundDrawablesWithIntrinsicBounds(iconChat, null, null, null);

            holder.tvFullName.setText(holder.tvFullName.getText());
        } else {
            holder.tvFullName.setCompoundDrawables(null, null, null, null);
        }


//        // если отправил я
//        if (item.isOut) holder.tvBody.setText(spannableString + item.body);
//        else holder.tvBody.setText(item.body);

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
            holder.indicator.getBackground().setColorFilter(ThemeManagerOld.get(context).getIndicatorColor(), PorterDuff.Mode.MULTIPLY);
            holder.tvDate.setTextColor(ThemeManagerOld.get(context).getIndicatorColor());

            holder.tvBody.setTextColor(fullNameTextColor);
            if (item.message.unread != 0)
                holder.tvUnreadCount.setText(item.message.unread > 1000 ? item.message.unread / 1000 + "к" : item.message.unread + "");
            //      holder.lLayout.setBackgroundColor(context.getResources().getColor(R.color.translucent_white));
        } else {
            //      holder.lLayout.setBackgroundColor(Color.TRANSPARENT);
            holder.indicator.setVisibility(View.GONE);
            holder.tvUnreadCount.setText("");
            holder.tvBody.setTextColor(bodyTextColor);
            //       holder.tvDate.setTextColor(fullNameTextColor);

        }

        if (user.online && !item.message.isChat()) {
            holder.onlineIndicator.setVisibility(View.VISIBLE);
            if (holder.onlineIndicator.getBackground() == null) {
                holder.onlineIndicator.setBackgroundDrawable(user.online_mobile ?
                        context.getResources().getDrawable(R.drawable.ic_android) :
                        context.getResources().getDrawable(R.drawable.shape_circle));
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

        String you = context.getResources().getString(R.string.you);

        // если набирает текст
        if (item.isTyping) {
            if (item.message.isChat())
                holder.tvBody.setText(user.first_name + ": " + (context.getResources().getString(R.string.is_typing)));
            else holder.tvBody.setText((context.getResources().getString(R.string.is_typing)));
        } else
            // если написал я
            if (message.is_out) {
                holder.tvBody.setText(Html.fromHtml("<b>" + you + ":  </b>" + message.body));
            } else if (message.isChat())
                holder.tvBody.setText(Html.fromHtml("<b>" + user.first_name + ":  </b>" + message.body));
            else holder.tvBody.setText(message.body);


//      Загрузка изображений
        try {
            Picasso.with(context)
                    .load(message.isChat() ? message.photo_50 == null ? user.photo_50 : message.photo_50 : user.photo_50) // загрузка
                    .placeholder(R.drawable.camera_b) // заглушка
                    .config(Bitmap.Config.RGB_565)
                    .into(holder.ivPhoto);
        } catch (Exception e) {
            e.printStackTrace();
        }

//        ImageLoader.get(context).
//                displayImage(item.photo, holder.ivPhoto);

        holder.ivPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra("uid", message.uid);
                context.startActivity(intent);
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
                    messageActionText = "Вас добавили в чат";
                    break;

                case "chat_kick_user":
                    messageActionText = "Вас исключили из чата";
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
                // если id совпадают, и они не 0 - значит мы нащли
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

    private void addUserToDB(final long uid, final VKMessage message) {
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

}