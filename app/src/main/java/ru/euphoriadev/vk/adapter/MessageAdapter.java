package ru.euphoriadev.vk.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import ru.euphoriadev.vk.ForwardMessagesActivity;
import ru.euphoriadev.vk.PhotoViewerActivity;
import ru.euphoriadev.vk.R;
import ru.euphoriadev.vk.SettingsFragment;
import ru.euphoriadev.vk.api.Api;
import ru.euphoriadev.vk.api.model.VKAttachment;
import ru.euphoriadev.vk.api.model.VKMessage;
import ru.euphoriadev.vk.api.model.VKUser;
import ru.euphoriadev.vk.async.ThreadExecutor;
import ru.euphoriadev.vk.common.PrefManager;
import ru.euphoriadev.vk.common.ResourcesLoader;
import ru.euphoriadev.vk.common.ThemeManager;
import ru.euphoriadev.vk.common.TypefaceManager;
import ru.euphoriadev.vk.helper.DBHelper;
import ru.euphoriadev.vk.helper.MediaPlayerHelper;
import ru.euphoriadev.vk.sqlite.VKSqliteHelper;
import ru.euphoriadev.vk.util.AndroidUtils;
import ru.euphoriadev.vk.util.Emoji;
import ru.euphoriadev.vk.util.VKUpdateController;
import ru.euphoriadev.vk.util.ViewUtil;
import ru.euphoriadev.vk.view.BoundedLinearLayout;
import ru.euphoriadev.vk.view.CircleImageView;

/**
 * Created by Igor on 07.09.15.
 */
public class MessageAdapter extends BaseArrayAdapter<MessageItem> implements VKUpdateController.MessageListener {

    public static final int DEFAULT_COLOR = ResourcesLoader.getColor(R.color.md_grey_800);
    public static final int DEFAULT_LIGHT_COLOR = ResourcesLoader.getColor(R.color.md_grey_200);

    public static final int DEFAULT_DARK_COLOR = ThemeManager.darkenColor(DEFAULT_COLOR);
    private final Object mLock = new Object();
    public boolean isScrolling;
    public boolean mShowTime;
    int secondaryTextColorDark;
    int secondaryTextColorLight;
    int widthDisplay;
    private Context mContext;
    private DBHelper mHelper;
    private SimpleDateFormat sdf;
    private Date date;
    private CacheMessages cacheMessages;
    private long chat_id;
    private long uid;
    private int colorInMessages;
    private int colorOutMessages;
    private boolean isColorInMessages;
    private boolean isColorOutMessages;
    private int colorNotReadingInMessages;
    private int colorNotReadingOutMessages;
    private boolean isNightTheme;
    private boolean isBlackTheme;
    private int primaryDarkColorLight;
    private int primaryDarkColorDark;
    private boolean systemEmoji;
    private Typeface typeface;


    public MessageAdapter(Context context, ArrayList<MessageItem> items, long uid, long chat_id) {
        super(context, items);

        this.mContext = getContext();
        this.chat_id = chat_id;
        this.uid = uid;

        this.sdf = new SimpleDateFormat("HH:mm");

        isColorInMessages = PrefManager.getBoolean(SettingsFragment.KEY_COLOR_IN_MESSAGES, true);
        isColorOutMessages = PrefManager.getBoolean(SettingsFragment.KEY_COLOR_OUT_MESSAGES, false);
        isNightTheme = ThemeManager.isDarkTheme();
        isBlackTheme = ThemeManager.getThemeColor(getContext()) == ThemeManager.PALETTE[20];

        if (isColorInMessages) {
            colorInMessages = ThemeManager.getThemeColor(getContext());
            colorNotReadingInMessages = ThemeManager.darkenColor(ThemeManager.getThemeColor(getContext()));
        } else {
            if (isNightTheme) {
                colorInMessages = DEFAULT_COLOR;
                colorNotReadingInMessages = DEFAULT_DARK_COLOR;
            } else {
                colorNotReadingInMessages = AndroidUtils.getColor(mContext, R.color.secondary_text_default_material_dark);
                colorInMessages = AndroidUtils.getColor(mContext, R.color.md_grey_200);
            }
        }

        if (isColorOutMessages) {
            colorOutMessages = ThemeManager.getThemeColor(getContext());
            colorNotReadingOutMessages = ThemeManager.darkenColor(ThemeManager.getThemeColor(getContext()));
        } else {
            if (isNightTheme) {
                colorOutMessages = DEFAULT_COLOR;
                colorNotReadingOutMessages = DEFAULT_DARK_COLOR;
            } else {
                colorNotReadingOutMessages = AndroidUtils.getColor(mContext, R.color.secondary_text_default_material_dark);
                colorOutMessages = AndroidUtils.getColor(mContext, R.color.md_grey_200);
            }
        }
        boolean isWallpaperBackground = !TextUtils.isEmpty(ThemeManager.getWallpaperPath(mContext));
        if (isWallpaperBackground) {
            colorOutMessages = ThemeManager.alphaColor(colorOutMessages);
            colorInMessages = ThemeManager.alphaColor(colorInMessages);

            colorNotReadingOutMessages = ThemeManager.alphaColor(colorNotReadingOutMessages);
            colorNotReadingInMessages = ThemeManager.alphaColor(colorNotReadingInMessages);
        }
        widthDisplay = context.getResources().getDisplayMetrics().widthPixels;

        primaryDarkColorDark = AndroidUtils.getColor(mContext, R.color.primary_text_default_material_light);
        primaryDarkColorLight = AndroidUtils.getColor(mContext, R.color.primary_text_default_material_dark);

        secondaryTextColorDark = AndroidUtils.getColor(mContext, R.color.secondary_text_default_material_light);
        secondaryTextColorLight = AndroidUtils.getColor(mContext, R.color.secondary_text_default_material_dark);


        this.date = new Date(System.currentTimeMillis());
        mHelper = DBHelper.get(mContext);
        cacheMessages = new CacheMessages(CacheMessages.DEFAULT_SIZE);

        systemEmoji = PrefManager.getBoolean(SettingsFragment.KEY_USE_SYSTEM_EMOJI);
        typeface = TypefaceManager.getTypeface(context);
        VKUpdateController.getInstance().addMessageListener(this);

    }

    @Override
    public void clear() {
        super.clear();

        if (cacheMessages != null) {
            cacheMessages.clear();
            cacheMessages = null;
        }
    }

    @Deprecated
    public void unregisterLongPoll() {
//        if (isBoundService) {
//            longPollService.setDialogListener(null);
//            mContext.unbindService(serviceConnection);
//            isBoundService = false;
//        }
        VKUpdateController.getInstance().removeListener(this);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        View view = convertView;

        if (view == null) {
            view = getInflater().inflate(R.layout.list_item_message, parent, false);

            holder = new ViewHolder(view);
            view.setTag(holder);

        } else {
            holder = (ViewHolder) view.getTag();
        }

        final MessageItem item = getItem(position);

        if (!TextUtils.isEmpty(item.message.action)) {
            View serviceView = getInflater().inflate(R.layout.list_item_service_message, parent, false);
            TextView textServiceView = (TextView) serviceView.findViewById(R.id.tvServiceText);
            switch (item.message.action) {
                case VKMessage.ACTION_CHAT_CREATE:
                    textServiceView.setText(Html.fromHtml(String.format("<b><i>%s</i></b> created chat \"%s\"", item.user.toString(), item.message.action_text)));
                    break;

                case VKMessage.ACTION_CHAT_PHOTO_UPDATE:
                    textServiceView.setText(Html.fromHtml(String.format("<b><i>%s</i></b> has updated chat photo ", item.user.toString())));
                    break;

                case VKMessage.ACTION_CHAT_PHOTO_REMOVE:
                    textServiceView.setText(Html.fromHtml(String.format("<b><i>%s</i></b> has removed chat photo ", item.user.toString())));
                    break;

                case VKMessage.ACTION_CHAT_TITLE_UPDATE:
                    textServiceView.setText(Html.fromHtml(String.format("<b><i>%s</i></b> changed title on <b><i>«%s»</i></b>", item.user.toString(), item.message.action_text)));
                    break;

                case VKMessage.ACTION_CHAT_KICK_USER:
                    if (item.user.user_id == item.message.action_mid) {
                        textServiceView.setText(Html.fromHtml(String.format("<b><i>%s</i></b> leaved from chat ", item.user.toString())));
                    } else {
                        textServiceView.setText(Html.fromHtml(String.format("<b><i>%s</i></b> kick <b><i>«%s»</i></b>", item.user.toString(), VKSqliteHelper.getUser(DBHelper.getDatabase(getContext()), item.message.action_mid))));
                    }
                    break;

                case VKMessage.ACTION_CHAT_INVITE_USER:
                    if (item.user.user_id == item.message.action_mid) {
                        textServiceView.setText(Html.fromHtml(String.format("<b><i>%s</i></b> returned to chat ", item.user.toString())));
                    } else {
                        textServiceView.setText(Html.fromHtml(String.format("<b><i>%s</i></b> invite <b><i>«%s»</i></b>", item.user.toString(), VKSqliteHelper.getUser(DBHelper.getDatabase(getContext()), item.message.action_mid))));
                    }
                    break;



            }

            return serviceView;
        }

//        ViewUtil.setTypeface(holder.tvBody);
//        ViewUtil.setTypeface(holder.tvDate);

        if (holder == null) {
            Log.e("MessageAdapter", "inflate item layout again");
            // inflate again, because last was is service layout
            view = getInflater().inflate(R.layout.list_item_message, parent, false);

            holder = new ViewHolder(view);
            view.setTag(holder);
        }

        holder.tvBody.setTypeface(typeface);
        if (mShowTime) {
            holder.tvDate.setTypeface(typeface);
        }

        holder.spaceSelected.setVisibility(item.message.is_out ? View.VISIBLE : View.GONE);
        if (isInMultiSelectMode()) {
            holder.ivSelected.setVisibility(View.VISIBLE);
            if (isSelectedItem(item)) {
                holder.ivSelected.setImageResource(R.drawable.ic_selected);
                holder.ivSelected.setColorFilter(ThemeManager.getColorAccent(getContext()));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    holder.ivSelected.setAlpha(1f);
                }
            } else {
                holder.ivSelected.setImageResource(R.drawable.ic_vector_unselected);
                holder.ivSelected.setColorFilter(ThemeManager.getSecondaryTextColor());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    holder.ivSelected.setAlpha(0.5f);
                }
            }
        } else {
            holder.ivSelected.setVisibility(View.GONE);
        }
        if (item.message.is_out) {
            holder.llMainContainer.setGravity(Gravity.END);

            holder.ivPhoto.setVisibility(View.GONE);
            if (isColorOutMessages) {
                holder.tvBody.setTextColor(Color.WHITE);
            } else {
                if (isNightTheme || isBlackTheme) {
                    holder.tvBody.setTextColor(primaryDarkColorLight);
                } else {
                    holder.tvBody.setTextColor(primaryDarkColorDark);
                }
            }

//            holder.llContainer.setBackgroundDrawable(getDrawable(R.drawable.msg_bubble_outgoing));
            holder.llContainer.setBackgroundDrawable(getDrawable(R.drawable.message_sent));
            if (item.message.read_state) {
                if (isBlackTheme) {
                    holder.llContainer.getBackground().setColorFilter(mContext.getResources().getColor(R.color.md_grey_900), PorterDuff.Mode.MULTIPLY);
                } else {
                    holder.llContainer.getBackground().setColorFilter(colorOutMessages, PorterDuff.Mode.MULTIPLY);
                }
            } else {
                if (isBlackTheme) {
                    holder.llContainer.getBackground().setColorFilter(Color.parseColor("#FF0B0B0B"), PorterDuff.Mode.MULTIPLY);
                } else {
                    holder.llContainer.getBackground().setColorFilter(colorNotReadingOutMessages, PorterDuff.Mode.MULTIPLY);
                }
            }


        } else {
            holder.llMainContainer.setGravity(Gravity.START);
            holder.ivPhoto.setVisibility(View.VISIBLE);
            if (isColorInMessages) {
                holder.tvBody.setTextColor(Color.WHITE);
            } else {
                if (isNightTheme || isBlackTheme) {
                    holder.tvBody.setTextColor(primaryDarkColorLight);
                } else {
                    holder.tvBody.setTextColor(primaryDarkColorDark);
                }
            }
//            holder.llContainer.setBackgroundDrawable(getDrawable(R.drawable.msg_bubble_incoming));
            holder.llContainer.setBackgroundDrawable(getDrawable(R.drawable.message_received));
            if (item.message.read_state) {
                if (isBlackTheme) {
                    holder.llContainer.getBackground().setColorFilter(mContext.getResources().getColor(R.color.md_grey_900), PorterDuff.Mode.MULTIPLY);
                } else
                    holder.llContainer.getBackground().setColorFilter(colorInMessages, PorterDuff.Mode.MULTIPLY);
            } else {
                if (isBlackTheme) {
                    holder.llContainer.getBackground().setColorFilter(Color.parseColor("#FF0B0B0B"), PorterDuff.Mode.MULTIPLY);
                } else
                    holder.llContainer.getBackground().setColorFilter(colorNotReadingInMessages, PorterDuff.Mode.MULTIPLY);
            }
        }

        if (item.message.isChat() && !item.message.is_out) {
            holder.ivPhoto.setVisibility(View.VISIBLE);
            holder.ivPhoto.setBorderColor(Color.WHITE);
            holder.ivPhoto.setBorderWidth(1);
            Picasso.with(mContext).load(item.user.photo_50).
                    placeholder(R.drawable.camera_b).into(holder.ivPhoto);

        } else {
            holder.ivPhoto.setVisibility(View.GONE);
            holder.ivPhoto.setImageDrawable(null);
        }

        int primaryTextColor = 0;
        int secondaryTextColor;
        if (isNightTheme) {
            primaryTextColor = primaryDarkColorLight;
            secondaryTextColor = secondaryTextColorLight;

        } else {
            secondaryTextColor = secondaryTextColorDark;
            primaryTextColor = primaryDarkColorDark;
            if (item.message.is_out && isColorOutMessages) {
                primaryTextColor = primaryDarkColorLight;
                secondaryTextColor = secondaryTextColorLight;
            }
            if (!item.message.is_out && isColorInMessages) {
                primaryTextColor = primaryDarkColorLight;
                secondaryTextColor = secondaryTextColorLight;
            }
        }

//        holder.llContainer.getLayoutParams().width = widthDisplay - (widthDisplay / 4);
        holder.llContainer.setMaxWidth(widthDisplay - (widthDisplay / 4));
//        holder.tvBody.setMaxWidth(widthDisplay - (widthDisplay / 4));
        holder.tvBody.setTextColor(primaryTextColor);
        if (mShowTime) {
            holder.tvDate.setTextColor(ThemeManager.getSecondaryTextColor());
        }

        if (TextUtils.isEmpty(item.message.body) && !item.message.emoji && !item.message.attachments.isEmpty()) {
            holder.tvBody.setVisibility(View.GONE);
        } else {
            holder.tvBody.setVisibility(View.VISIBLE);
            holder.tvBody.setText(item.message.body);
        }

        if (mShowTime)
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

        holder.tvDate.setVisibility(mShowTime ? View.VISIBLE : View.GONE);

        switch (item.status) {
            case SENT:
                if (mShowTime) {
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.tvDate.getLayoutParams();
                    params.gravity = item.message.is_out ? Gravity.END : Gravity.START;
                    holder.tvDate.setLayoutParams(params);
                    holder.tvDate.setText("");
                    if (item.message.isChat()) {
                        if (!item.message.is_out) {
                            holder.tvDate.setText(Html.fromHtml("<b>" + item.user.first_name + "</b>"));
                            holder.tvDate.append(",  ");
                        }
                        holder.tvDate.append(sdf.format(item.date));
                    } else {
                        holder.tvDate.setText(sdf.format(item.date));
                    }
                }
                break;

            case SENDING:
                if (mShowTime) holder.tvDate.setText(mContext.getString(R.string.sending));
                break;

            case ERROR:
                if (mShowTime) {
                    holder.tvDate.setText(mContext.getString(R.string.error));
                    holder.tvDate.setTextColor(mContext.getResources().getColor(R.color.md_red_500));
                }
                break;
        }


        if (holder.llAttachContainer.getChildCount() != 0) {
            holder.llAttachContainer.removeAllViewsInLayout();
        }

        if (item.message.emoji && !systemEmoji) {
            Emoji.parseEmoji(holder.tvBody);
        }
        if (!item.message.attachments.isEmpty()) {
            holder.llAttachContainer.setVisibility(View.GONE);
            int fwdMessageCount = 0;
            boolean hasAttach = false;
            // если пересланных сообщений не слишком много
            for (int i = 0; i < item.message.attachments.size(); i++) {
                final VKAttachment vkAttachment = item.message.attachments.get(i);
                if (vkAttachment.type.equals(VKAttachment.TYPE_MESSAGE)) {
                    fwdMessageCount++;

                    // Вложение в пересланных сообщений
                    if (!vkAttachment.message.attachments.isEmpty()) {
                        hasAttach = true;
                    }
                }
            }
            boolean isForwardedMessage = false;

            for (final VKAttachment att : item.message.attachments) {
                switch (att.type) {
                    case VKAttachment.TYPE_MESSAGE:
                        if (holder.llAttachContainer.getVisibility() == View.GONE)
                            holder.llAttachContainer.setVisibility(View.VISIBLE);

                        if (TextUtils.isEmpty(item.message.body) && !item.message.emoji) {
                            holder.tvBody.setVisibility(View.GONE);
                        } else {
                            holder.tvBody.setVisibility(View.VISIBLE);
                        }

                        // если сообщений больше 30, или они содержат вложения, то скрываем
                        if (fwdMessageCount != 0 && fwdMessageCount > 30 || hasAttach) {
                            if (isForwardedMessage) break;

                            final String fwdNessageString = mContext.getResources().getString(R.string.forward_messages);
                            Spannable fwd = new SpannableString(fwdNessageString);
                            fwd.setSpan(new StyleSpan(Typeface.BOLD), 0, fwd.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            fwd.setSpan(new ForegroundColorSpan(holder.tvBody.getLinkTextColors().getDefaultColor()), 0, fwd.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);


                            if (item.message.body.length() > 0) {
                                holder.tvBody.setText(item.message.body);
                                holder.tvBody.append("\n");
                                holder.tvBody.append(fwd);

                            } else holder.tvBody.setText(fwd);

                            holder.tvBody.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ArrayList<VKMessage> msgs = new ArrayList<>();
                                    for (VKAttachment a : item.message.attachments) {
                                        if (a.message != null) {
                                            msgs.add(a.message);
                                        }
                                    }
                                    Intent intent = new Intent(mContext, ForwardMessagesActivity.class);
                                    intent.putExtra("messages", msgs);
                                    mContext.startActivity(intent);

                                }
                            });
                            isForwardedMessage = true;
                            break;
                        } else {
                            holder.tvBody.setOnClickListener(null);
                        }

                        // если есть в кеше - отображаем
                        // TODO: Почему-то по неизвестным мне причинам - вылетаем иногда
                        View fwdMessageView = cacheMessages.get(item.message.hashCode() + att.message.hashCode());
                        if (fwdMessageView != null) {
                            if (fwdMessageView.getParent() != null) {
                                holder.llAttachContainer.removeView(fwdMessageView);
                                cacheMessages.remove(item.message.hashCode() + att.message.hashCode());
                            }
                            holder.llAttachContainer.addView(fwdMessageView);
                            break;
                        }

                        // если нет - создаем view.
                        fwdMessageView = getInflater().inflate(R.layout.forward_message_layout, holder.llAttachContainer, false);


                        final TextView tvNameFwd = (TextView) fwdMessageView.findViewById(R.id.tvNameFwdMessage);
                        final TextView tvBodyFwd = (TextView) fwdMessageView.findViewById(R.id.tvBodyFwdMessage);
                        final ImageView ivPhotoFwd = (ImageView) fwdMessageView.findViewById(R.id.ivPhotoFwdMessage);

                        tvNameFwd.setTextColor(secondaryTextColor);
                        tvBodyFwd.setTextColor(primaryTextColor);

                        ViewUtil.setTypeface(tvNameFwd);
                        ViewUtil.setTypeface(tvBodyFwd);

                        VKUser user = mHelper.getUserFromDB(att.message.uid);
                        if (user != null) {
                            tvNameFwd.setText(user.toString());
                            Picasso.with(mContext).load(user.photo_50).into(ivPhotoFwd);
                        } else {
                            addUserToDatabase(att.message.uid, new OnVKUserLoaded() {
                                @Override
                                public void onLoaded(VKUser user) {
                                    tvNameFwd.setText(user.toString());
                                    Picasso.with(mContext).load(user.photo_50).into(ivPhotoFwd);
                                }
                            });
                        }
                        tvBodyFwd.setText(att.message.body);

                        holder.llAttachContainer.addView(fwdMessageView);
                        cacheMessages.put(item.message.hashCode() + att.message.hashCode(), fwdMessageView);
//                        cacheForwardedMessages.append(String.valueOf(att.message.uid).concat(att.message.body).hashCode(), fwdMessageView);

                        break;

                    case VKAttachment.TYPE_PHOTO:
                        if (TextUtils.isEmpty(item.message.body) && !item.message.emoji) {
                            holder.tvBody.setVisibility(View.GONE);
                        }

                        int inSampleSize = AndroidUtils.calculateInSampleSize(att.photo.height, att.photo.width, 604, 604);

//                        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//                                att.photo.width / inSampleSize,
//                                att.photo.height / inSampleSize);

                        int width;
                        int height;
                        if (att.photo.width > 604 || att.photo.height > 604) {
                            int coefficient = 1;
                            // если высота больше ширины, то делим высоту
                            // на требуемый нам размер
                            if (att.photo.height > att.photo.width) {
                                coefficient = att.photo.height / 604;
                                // если наоборот, ширина больше высоты
                            } else if (att.photo.width > att.photo.height) {
                                coefficient = att.photo.width / 604;
                            } else {
                                // если квадратная фото, хотя это редкость
                                // тут без разници
                                coefficient = att.photo.height;
                            }
                            // получаем высоту и ширину, поделив ее на коофицент
                            width = att.photo.width / coefficient;
                            height = att.photo.height / coefficient;
                        } else {
                            width = att.photo.width;
                            height = att.photo.height;
                        }


                        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                width,
                                height);
                        params.setMargins(4, 4, 4, 4);

                        final ImageView iv = new ImageView(mContext);
                        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        iv.setLayoutParams(params);
                        iv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                PhotoViewerActivity.start(getContext(), att.photo);
                            }
                        });
                        holder.llAttachContainer.addView(iv);
//                        iv.setClipToOutline(true);
                        if (holder.llAttachContainer.getVisibility() == View.GONE) {
                            holder.llAttachContainer.setVisibility(View.VISIBLE);
                        }

                        Picasso.with(mContext)
                                .load(att.photo.src_big)
                                .fit()
                                .transform(new AndroidUtils.RoundedTransformation(6))
                                .placeholder(new ColorDrawable(Color.GRAY))
                                .config(Bitmap.Config.RGB_565)
                                .into(iv);

                        break;

                    case VKAttachment.TYPE_AUDIO:

                        if (holder.llAttachContainer.getVisibility() == View.GONE)
                            holder.llAttachContainer.setVisibility(View.VISIBLE);

                        View audioitem = View.inflate(mContext, R.layout.attachment_audio_item, null);

                        audioitem.setLayoutParams(new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        ));

                        final ImageButton btnPlay = (ImageButton) audioitem.findViewById(R.id.btnAudioPlay);
                        TextView tvTitle = (TextView) audioitem.findViewById(R.id.tvAudioTitle);
                        TextView tvDes = (TextView) audioitem.findViewById(R.id.tvAudioDes);

                        tvTitle.setTextColor(primaryTextColor);
                        tvDes.setTextColor(secondaryTextColor);

                        ViewUtil.setTypeface(tvTitle);
                        ViewUtil.setTypeface(tvDes);

                        holder.llAttachContainer.addView(audioitem);

//                        btnPlay.setImageDrawable(iconPlay);

                        btnPlay.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                MediaPlayerHelper.playAudio(mContext, att.audio);
                            }
                        });

                        tvTitle.setText(att.audio.artist);
                        tvDes.setText(att.audio.title);

                        if (!isNightTheme && (!item.message.is_out && !isColorInMessages) ||
                                (item.message.is_out && !isColorOutMessages)) {
                            ViewUtil.setFilter(btnPlay, ThemeManager.getThemeColor(getContext()));
                        }
                        break;

                    case VKAttachment.TYPE_STICKER:
                        final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                att.sticker.width,
                                att.sticker.height);
                        layoutParams.setMargins(6, 6, 6, 5);

                        final ImageView ivSticker = new ImageView(mContext);
                        ivSticker.setLayoutParams(layoutParams);
                        holder.llAttachContainer.addView(ivSticker);

                        if (holder.llAttachContainer.getVisibility() == View.GONE)
                            holder.llAttachContainer.setVisibility(View.VISIBLE);

                        holder.llAttachContainer.setBackgroundColor(Color.TRANSPARENT);

                        Picasso.with(mContext).load(att.sticker.photo_352)
                                .resize(att.sticker.width, att.sticker.height)
                                .centerInside()
                                .placeholder(new ColorDrawable(Color.GRAY))
                                .into(ivSticker);
                        break;

                    case VKAttachment.TYPE_GIFT:
                        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                        params1.setMargins(6, 6, 6, 5);

                        final ImageView ivGift = new ImageView(mContext);
                        ivGift.setLayoutParams(params1);
                        holder.llAttachContainer.addView(ivGift);

                        if (holder.llAttachContainer.getVisibility() == View.GONE)
                            holder.llAttachContainer.setVisibility(View.VISIBLE);


                        Picasso.with(mContext)
                                .load(att.gift.thumb_256)
                                .placeholder(new ColorDrawable(Color.GRAY))
                                .into(ivGift);

                        Spannable giftText = new SpannableString("\n" + (mContext.getString(R.string.gift)));
                        giftText.setSpan(new StyleSpan(Typeface.BOLD), 0, giftText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        giftText.setSpan(new ForegroundColorSpan(holder.tvBody.getLinkTextColors().getDefaultColor()), 0, giftText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                        holder.tvBody.setText(giftText);

                        break;

                    case VKAttachment.TYPE_DOC:

                        if (holder.llAttachContainer.getVisibility() == View.GONE) {
                            holder.llAttachContainer.setVisibility(View.VISIBLE);
                        }

                        View docItem = View.inflate(mContext, R.layout.list_item_doc, null);
                        DocsAdapter.getView(docItem, att.document, new DocsAdapter.ViewHolder(docItem));
                        holder.llAttachContainer.addView(docItem);

                        ImageView ivDoc = (ImageView) docItem.findViewById(R.id.ivDocDownload);
                        if (!isNightTheme && (!item.message.is_out && !isColorInMessages) ||
                                (item.message.is_out && !isColorOutMessages)) {
                            ViewUtil.setFilter(ivDoc, ThemeManager.getThemeColor(getContext()));
                        }
                        break;

                    case VKAttachment.TYPE_LINK:
                        if (holder.tvBody.getVisibility() == View.GONE) {
                            holder.tvBody.setVisibility(View.VISIBLE);
                        }

                        if (holder.tvBody.getText().toString().contains(att.link.title)) {
                            break;
                        }

                        Spannable lingSpan = new SpannableString("Link: ".concat(att.link.title));
                        lingSpan.setSpan(new StyleSpan(Typeface.BOLD), 0, lingSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        lingSpan.setSpan(new ForegroundColorSpan(holder.tvBody.getLinkTextColors().getDefaultColor()), 0, lingSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                        if (holder.tvBody.getText().toString().length() > 0) {
                            holder.tvBody.append("\n");
                            holder.tvBody.append(lingSpan);
                        } else holder.tvBody.setText(lingSpan);


                        holder.tvBody.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(att.link.url));
                                mContext.startActivity(intent);
                            }
                        });
                        break;

                }


            }
        } else {
            holder.llAttachContainer.setVisibility(View.GONE);
        }
        return view;
    }

    private Drawable getDrawable(int id) {
        return AndroidUtils.getDrawable(mContext, id);
    }

    private void addUserToDatabase(final int uid, final OnVKUserLoaded l) {
        ThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (mLock) {
                    VKUser profile = mHelper.getUserFromDB(uid);
                    if (profile == null) {
                        profile = Api.get().getProfile(uid);
                    }
                    if (profile == null) {
                        return;
                    }
                    final VKUser finalProfile = profile;
                    mHelper.addUserToDB(finalProfile);
                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            l.onLoaded(finalProfile);
                        }
                    });
                }
            }
        });
    }

    public ArrayList<MessageItem> getMessages() {
        return getValues();
    }

    @Override
    public void onNewMessage(final VKMessage message) {
        Log.w("MessageAdapter", "onNewMessage");
        // если это мы написали, то будет дублированние сообщений
        if (message.is_out) return;

        // при обновление истории возникли трудности, поэтому приходится применять костыль
        // загружаем новое сообщение сомостоятельно
        ThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // если это не тот диалог, в котором мы сейчас находимся
                    if (chat_id != message.chat_id && chat_id != 0) {
                        return;
                    } else if (message.uid != uid && chat_id == 0) {
                        return;
                    }
                    VKUser user = null;
//                    final VKMessage newMessage = Api.get().getMessagesHistory(uid, chat_id, 0, 1).get(0);
//                    if (message.chat_id != newMessage.chat_id && message.uid != newMessage.uid) {
//                        // может случиться такое, что наплыв сообщений будет настолько быстрым
//                        // что загрузится просто последнее смс не успеет, как не его место поступит новое
//                        // TODO: однако у меня такого не было, но это вполне может случится
//                        return;
//                    }
                    ArrayList<Integer> mids = new ArrayList<>(1);
                    mids.add(message.mid);

                    final VKMessage newMessage = Api.get().getMessagesById(mids).get(0);
                    mids.clear();
                    mids = null;

                    // если это чат - загружаем пользователя
                    if (chat_id != 0) {
                        user = mHelper.getUserFromDB(message.uid);
                        // если его нет в бд, то загружаем с вк
                        if (user == null) {
                            user = Api.get().getProfile(message.uid);
                            mHelper.addUserToDB(user);
                        }
                    }
                    if (user == null) user = new VKUser();

                    final VKUser finalUser = user;
                    mHelper.addMessageToDB(newMessage);
                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getMessages().add(new MessageItem(newMessage, finalUser));
                            notifyDataSetChanged();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onReadMessage(int message_id) {
        for (int i = 0; i < getMessages().size(); i++) {
            MessageItem item = getMessages().get(i);
            if (message_id == item.message.mid) {
                item.message.read_state = true;
                notifyDataSetChanged();
                break;
            }
        }
    }

    @Override
    public void onDeleteMessage(int message_id) {

    }

    public void toggleStateTime() {
        mShowTime = !mShowTime;
        notifyDataSetChanged();
    }

    private interface OnVKUserLoaded {
        void onLoaded(VKUser user);
    }

    private static class ViewHolder {
        public BoundedLinearLayout llContainer;
        public LinearLayout llMainContainer;
        public LinearLayout llAttachContainer;
        public TextView tvBody;
        public TextView tvDate;
        public CircleImageView ivPhoto;
        public ImageView ivSelected;
        public android.support.v4.widget.Space spaceSelected;

        public ViewHolder(View v) {
            llContainer = (BoundedLinearLayout) v.findViewById(R.id.llMessageContainer);
            llMainContainer = (LinearLayout) v.findViewById(R.id.llMessageMainContainer);
            llAttachContainer = (LinearLayout) v.findViewById(R.id.llMessageAttachments);
            tvBody = (TextView) v.findViewById(R.id.tvMessageText);
            tvDate = (TextView) v.findViewById(R.id.tvMessageDate);
            ivPhoto = (CircleImageView) v.findViewById(R.id.ivMessagePhoto);
            ivSelected = (ImageView) v.findViewById(R.id.ivMessageSelected);
            spaceSelected = (android.support.v4.widget.Space) v.findViewById(R.id.spaceSelected);
        }
    }


    /**
     * Кеш пересланных сообщений
     * Позволяет хранить View - более быстрый доступ, чем создавать его (inflate)
     */
    private class CacheMessages {
        public static final int DEFAULT_SIZE = 60;
        private static final String TAG = "CacheMessages";

        private SparseArray<View> baseCache;
        private int maxSize;
        private int totalSize;

        public CacheMessages(int maxSize) {
            this.maxSize = maxSize;
            this.baseCache = new SparseArray<>(maxSize);
        }

        public int sizeOf(View v) {
            return 1;
        }

        public boolean put(int key, View fwdMessageView) {
            if (totalSize >= maxSize) {
                Log.w(TAG, "Size overflow ".concat(String.valueOf(totalSize)));

                clear();
                return false;
            }
            if (baseCache.get(key) != null) {
                return false;
            }

            baseCache.put(key, fwdMessageView);
            totalSize = totalSize + sizeOf(fwdMessageView);
            return true;
        }

        public View get(int key) {
            return baseCache.get(key);
        }

        public void remove(int key) {
            removeView(baseCache.get(key));
            baseCache.remove(key);
        }

        public void clear() {
            if (totalSize <= 0) {
                return;
            }
            for (int i = 0; i < baseCache.size(); i++) {
                View valueView = baseCache.valueAt(i);
                removeView(valueView);
            }
            baseCache.clear();
            totalSize = 0;
        }


        private void removeView(View view) {
            if (view != null && view.getBackground() != null) {
                try {
                    view.getBackground().setCallback(null);
                    Bitmap bitmap = ((BitmapDrawable) view.getBackground()).getBitmap();
                    if (bitmap != null && !bitmap.isRecycled()) {
                        bitmap.recycle();
                        bitmap = null;
                    }
                    view.destroyDrawingCache();
                    view.notifyAll();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
                parent = null;
            }
            view = null;
        }

    }
}
