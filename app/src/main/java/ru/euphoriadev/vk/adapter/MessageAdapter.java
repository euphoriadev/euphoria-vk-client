package ru.euphoriadev.vk.adapter;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
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
import android.widget.*;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import ru.euphoriadev.vk.ForwardMessagesActivity;
import ru.euphoriadev.vk.R;
import ru.euphoriadev.vk.api.Api;
import ru.euphoriadev.vk.api.model.VKAttachment;
import ru.euphoriadev.vk.api.model.VKMessage;
import ru.euphoriadev.vk.api.model.VKUser;
import ru.euphoriadev.vk.helper.DBHelper;
import ru.euphoriadev.vk.helper.MediaPlayerHelper;
import ru.euphoriadev.vk.service.LongPollService;
import ru.euphoriadev.vk.util.AndroidUtils;
import ru.euphoriadev.vk.util.ThreadExecutor;
import ru.euphoriadev.vk.util.ViewUtil;
import ru.euphoriadev.vk.view.CircleImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Igor on 07.09.15.
 */
public class MessageAdapter extends BaseArrayAdapter<MessageItem> implements LongPollService.VKOnDialogListener {

    private final Object mLock = new Object();

    private Context mContext;
    private DBHelper mHelper;
    private SimpleDateFormat sdf;
    private Date date;
    private CacheMessages cacheMessages;
    private ServiceConnection serviceConnection;
    private LongPollService longPollService;
    private boolean isBoundService;
    private long chat_id;
    private long uid;

    public boolean isScrolling;

    private int colorInMessages;
    private int colorOutMessages;

    private boolean isColorInMessages;
    private boolean isColorOutMessages;

    private int colorNotReadingInMessages;
    private int colorNotReadingOutMessages;

    private boolean isNightTheme;
    private boolean isBlackTheme;

    int secondaryTextColorDark;
    int secondaryTextColorLight;

    private static final int DEFAULT_COLOR = Color.parseColor("#424242");
    private static final int DEFAULT_DARK_COLOR = Color.parseColor("#303030");

    private int primaryDarkColorLight;
    private int primaryDarkColorDark;

    int widthDisplay;

    Drawable dBubbleOutgoing;
    Drawable dBubbleIncoming;

    public MessageAdapter(Context context, ArrayList<MessageItem> values) {
        super(context, values);
    }

    public MessageAdapter(Context context, ArrayList<MessageItem> items, long uid, long chat_id) {
        super(context, items);

        this.mContext = getContext();
        this.chat_id = chat_id;
        this.uid = uid;

        this.sdf = new SimpleDateFormat("HH:mm");

        isColorInMessages = PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("color_in_messages", true);
        isColorOutMessages = PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("color_out_messages", false);
        isNightTheme = getThemeManager().isNightTheme();
        isBlackTheme = getThemeManager().isBlackTheme();

        dBubbleOutgoing = mContext.getResources().getDrawable(R.drawable.msg_bubble_outgoing);
        dBubbleIncoming = mContext.getResources().getDrawable(R.drawable.msg_bubble_incoming);

        if (isColorInMessages) {
            colorInMessages = getThemeManager().getBasicColorOfTheme();
            colorNotReadingInMessages = getThemeManager().getDarkBasicColorOfTheme();
        } else {
            if (isNightTheme) {
                colorInMessages = DEFAULT_COLOR;
                colorNotReadingInMessages = DEFAULT_DARK_COLOR;
            } else {
                colorNotReadingInMessages = mContext.getResources().getColor(R.color.secondary_text_default_material_dark);
                colorInMessages = mContext.getResources().getColor(R.color.white);
            }
        }

        if (isColorOutMessages) {
            colorOutMessages = getThemeManager().getBasicColorOfTheme();
            colorNotReadingOutMessages = getThemeManager().getDarkBasicColorOfTheme();
        } else {
            if (isNightTheme) {
                colorOutMessages = DEFAULT_COLOR;
                colorNotReadingOutMessages = DEFAULT_DARK_COLOR;
            } else {
                colorNotReadingOutMessages = mContext.getResources().getColor(R.color.secondary_text_default_material_dark);
                colorOutMessages = mContext.getResources().getColor(R.color.white);
            }
        }
        widthDisplay = context.getResources().getDisplayMetrics().widthPixels;

        primaryDarkColorDark = mContext.getResources().getColor(R.color.primary_text_default_material_light);
        primaryDarkColorLight = mContext.getResources().getColor(R.color.primary_text_default_material_dark);

        secondaryTextColorDark = mContext.getResources().getColor(R.color.secondary_text_default_material_light);
        secondaryTextColorLight = mContext.getResources().getColor(R.color.secondary_text_default_material_dark);


        this.date = new Date(System.currentTimeMillis());
        mHelper = DBHelper.get(mContext);
        cacheMessages = new CacheMessages(CacheMessages.DEFAULT_SIZE);

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                isBoundService = true;

                longPollService = ((LongPollService.LocalBinder) binder).getService();
                longPollService.setDialogListener(MessageAdapter.this);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                isBoundService = false;

            }
        };
    }
//    IconicsDrawable iconDoneAll;


    @Override
    public void clear() {
        super.clear();

        if (cacheMessages != null) {
            cacheMessages.clear();
            cacheMessages = null;
        }
    }

    public void connectToLongPoll() {
        if (!isBoundService) {
            mContext.bindService(new Intent(mContext, LongPollService.class), serviceConnection, 0);
        }
    }

    public void unregisterLongPoll() {
        if (isBoundService) {
            longPollService.setDialogListener(null);
            mContext.unbindService(serviceConnection);
            isBoundService = false;
        }
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //    long startTime = System.currentTimeMillis();
        final ViewHolder holder;
        View view = convertView;

        if (view == null) {
            view = getInflater().inflate(R.layout.message_list_item, parent, false);

            holder = new ViewHolder(view);
            view.setTag(holder);

        } else {
            holder = (ViewHolder) view.getTag();
        }

        final MessageItem item = getItem(position);
        if (item == null) return view;

        ViewUtil.setTypeface(holder.tvBody);
        ViewUtil.setTypeface(holder.tvDate);

        if (isInMultiSelectMode()) {
            holder.ivSelected.setVisibility(View.VISIBLE);
            if (isSelectedItem(item)) {
                holder.ivSelected.setImageResource(R.drawable.ic_selected);
                holder.ivSelected.setColorFilter(getThemeManager().getFabColor());
                holder.ivSelected.setAlpha(1f);
            } else {
                holder.ivSelected.setImageResource(R.drawable.ic_vector_unselected);
                holder.ivSelected.setColorFilter(getThemeManager().getSecondaryTextColor());
                holder.ivSelected.setAlpha(0.5f);
            }
        } else {
            holder.ivSelected.setVisibility(View.GONE);
        }
        if (item.message.is_out) {
            holder.llMessageContainer.setGravity(Gravity.END);

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

            holder.llAttachContainer.setBackgroundDrawable(getDrawable(R.drawable.msg_bubble_outgoing));
            if (item.message.read_state) {
                if (isBlackTheme) {
                    holder.llAttachContainer.getBackground().setColorFilter(mContext.getResources().getColor(R.color.md_grey_900), PorterDuff.Mode.MULTIPLY);
                } else {
                    holder.llAttachContainer.getBackground().setColorFilter(colorOutMessages, PorterDuff.Mode.MULTIPLY);
                }
            } else {
                if (isBlackTheme) {
                    holder.llAttachContainer.getBackground().setColorFilter(Color.parseColor("#FF0B0B0B"), PorterDuff.Mode.MULTIPLY);
                } else {
                    holder.llAttachContainer.getBackground().setColorFilter(colorNotReadingOutMessages, PorterDuff.Mode.MULTIPLY);
                }
            }


        } else {
            holder.llMessageContainer.setGravity(Gravity.START);
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
            ;
            holder.llAttachContainer.setBackgroundDrawable(getDrawable(R.drawable.msg_bubble_incoming));
            if (item.message.read_state) {
                if (isBlackTheme) {
                    holder.llAttachContainer.getBackground().setColorFilter(mContext.getResources().getColor(R.color.md_grey_900), PorterDuff.Mode.MULTIPLY);
                } else
                    holder.llAttachContainer.getBackground().setColorFilter(colorInMessages, PorterDuff.Mode.MULTIPLY);
            } else {
                if (isBlackTheme) {
                    holder.llAttachContainer.getBackground().setColorFilter(Color.parseColor("#FF0B0B0B"), PorterDuff.Mode.MULTIPLY);
                } else
                    holder.llAttachContainer.getBackground().setColorFilter(colorNotReadingInMessages, PorterDuff.Mode.MULTIPLY);
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

        holder.tvBody.setMaxWidth(widthDisplay - (widthDisplay / 4));
//        holder.tvBody.setTextColor(isNightTheme ? primaryDarkColorLight : primaryDarkColorDark);
        holder.tvBody.setTextColor(primaryTextColor);
        holder.tvDate.setTextColor(secondaryTextColor);

//        holder.tvDate.setTextColor(isNightTheme ? secondaryTextColorDark : secondaryTextColorLight);
//        holder.tvDateOneLine.setTextColor(isNightTheme ? secondaryTextColorDark : secondaryTextColorLight);

//        holder.tvDate.setText(sdf.format(item.message.date * 1000));
//        Date currentDate = new Date(System.currentTimeMillis());
//        if ((currentDate.getDate() - item.date.getDate() == 1)) {
//            holder.tvDate.setText("Вчера в " + sdf.format(item.message.date * 1000));
//        }

        if (TextUtils.isEmpty(item.message.body) && !item.message.emoji && !item.message.attachments.isEmpty()) {
            holder.tvBody.setVisibility(View.GONE);
        } else {
            holder.tvBody.setVisibility(View.VISIBLE);
            holder.tvBody.setText(item.message.body);
        }

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
//        holder.tvDateOneLine.setText(sdf.format(item.date));
//        holder.tvDate.setText(sdf.format(item.date));
//
//        holder.tvBody.post(new Runnable() {
//            @Override
//            public void run() {
//                // если в тексте больше одной строки
//                if (holder.tvBody.getLineCount() > 1) {
        holder.tvDate.setVisibility(View.VISIBLE);
////                    holder.tvDateOneLine.setVisibility(View.GONE);
//                } else {
//                    holder.tvDate.setVisibility(View.GONE);
////                    holder.tvDateOneLine.setVisibility(View.VISIBLE);
//                }
//            }
//        });


        switch (item.status) {
            case SENT:
//                if (item.message.isChat()) {
//                    //     tvDate.setText(item.user.toString() + ", ");
//                    //     tvDate.append(sdf.format(item.message.date * 1000));
//                } else {
                holder.tvDate.setText(sdf.format(item.date));
//                holder.tvDateOneLine.setText(sdf.format(item.date));
                break;

            case SENDING:
                holder.tvDate.setText(mContext.getString(R.string.sending));
//                holder.tvDateOneLine.setText(mContext.getString(R.string.sending));
                break;

            case ERROR:
//                holder.tvDateOneLine.setText(mContext.getString(R.string.error));
                holder.tvDate.setText(mContext.getString(R.string.error));

//                holder.tvDateOneLine.setTextColor(mContext.getResources().getColor(R.color.md_red_500));
                holder.tvDate.setTextColor(mContext.getResources().getColor(R.color.md_red_500));
                break;
        }


        if (holder.llImageContainer.getChildCount() != 0) {
            holder.llImageContainer.removeAllViewsInLayout();
        }
        if (holder.llImageContainer.getVisibility() == View.VISIBLE) {
            holder.llImageContainer.setVisibility(View.GONE);
        }
        if (holder.llAudioContainer.getChildCount() != 0) {
            holder.llAudioContainer.removeAllViewsInLayout();
        }
        if (holder.llFwdMessagesContainer.getChildCount() != 0) {
            holder.llFwdMessagesContainer.removeAllViewsInLayout();
        }

        if (holder.llAudioContainer.getVisibility() == View.VISIBLE) {
            holder.llAudioContainer.setVisibility(View.GONE);
        }
        if (holder.llDocContainer.getChildCount() != 0) {
            holder.llDocContainer.removeAllViewsInLayout();
        }
        if (holder.llDocContainer.getVisibility() == View.VISIBLE) {
            holder.llDocContainer.setVisibility(View.GONE);
        }
        if (holder.llFwdMessagesContainer.getVisibility() == View.VISIBLE) {
            holder.llFwdMessagesContainer.setVisibility(View.GONE);
        }


        if (!item.message.attachments.isEmpty()) {
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
                        if (holder.llFwdMessagesContainer.getVisibility() == View.GONE)
                            holder.llFwdMessagesContainer.setVisibility(View.VISIBLE);

                        if (holder.tvBody.getVisibility() == View.GONE) {
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
                                holder.llFwdMessagesContainer.removeView(fwdMessageView);
                                cacheMessages.remove(item.message.hashCode() + att.message.hashCode());
                            }
                            holder.llFwdMessagesContainer.addView(fwdMessageView);
                            break;
                        }

                        // если нет - создаем view.
                        fwdMessageView = getInflater().inflate(R.layout.forward_message_layout, holder.llFwdMessagesContainer, false);


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

                        holder.llFwdMessagesContainer.addView(fwdMessageView);
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
                        holder.llImageContainer.addView(iv);
//                        iv.setClipToOutline(true);
                        if (holder.llImageContainer.getVisibility() == View.GONE) {
                            holder.llImageContainer.setVisibility(View.VISIBLE);
                        }

                        Picasso.with(mContext)
                                .load(att.photo.src_big)
                                .fit()
                                .transform(new RoundedTransformation(6))
                                .placeholder(new ColorDrawable(Color.GRAY))
                                .config(Bitmap.Config.RGB_565)
                                .into(iv);

                        break;

                    case VKAttachment.TYPE_AUDIO:

                        if (holder.llAudioContainer.getVisibility() == View.GONE)
                            holder.llAudioContainer.setVisibility(View.VISIBLE);

                        View audioitem = View.inflate(mContext, R.layout.attachment_audio_item, null);
//                        audioitem.setPadding(6, 0, 6, 0);
//                        IconicsDrawable iconPlay = new IconicsDrawable(mContext, GoogleMaterial.Icon.gmd_play_circle_outline);
//                        iconPlay.color(tm.getSecondaryTextColor());
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

                        holder.llAudioContainer.addView(audioitem);

//                        btnPlay.setImageDrawable(iconPlay);

                        btnPlay.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                MediaPlayerHelper.playAudio(mContext, att.audio);
                            }
                        });

                        tvTitle.setText(att.audio.artist);
                        tvDes.setText(att.audio.title);
//                        tvArtist.setTextColor(tm.getPrimaryTextColor());

//                        tvTitle.setText(att.audio.title);
//                        tvTitle.setTypeface(mTypeface);
//                        tvTitle.setTextColor(tm.getSecondaryTextColor());

//                        tvTime.setText(new SimpleDateFormat("MM:ss").format(att.audio.duration));
//                        tvTime.setTypeface(mTypeface);
//                        tvTime.setTextColor(tm.getSecondaryTextColor());


                        break;

                    case VKAttachment.TYPE_STICKER:
                        final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                att.sticker.width,
                                att.sticker.height);
                        layoutParams.setMargins(6, 6, 6, 5);

                        final ImageView ivSticker = new ImageView(mContext);
                        ivSticker.setLayoutParams(layoutParams);
                        holder.llImageContainer.addView(ivSticker);

                        if (holder.llImageContainer.getVisibility() == View.GONE)
                            holder.llImageContainer.setVisibility(View.VISIBLE);

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
                        holder.llImageContainer.addView(ivGift);

                        if (holder.llImageContainer.getVisibility() == View.GONE)
                            holder.llImageContainer.setVisibility(View.VISIBLE);


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

                        if (holder.llDocContainer.getVisibility() == View.GONE) {
                            holder.llDocContainer.setVisibility(View.VISIBLE);
                        }

                        View docItem = View.inflate(mContext, R.layout.doc_list_item, null);

                        TextView tvDocTitle = (TextView) docItem.findViewById(R.id.tvDocTitle);
                        TextView tvDocSize = (TextView) docItem.findViewById(R.id.tvDocSize);
//                        ImageView ivDocIcon = (ImageView) docItem.findViewById(R.id.ivDocIcon);
                        holder.llDocContainer.addView(docItem);

//                        IconicsDrawable iconDoc = new IconicsDrawable(mContext, GoogleMaterial.Icon.gmd_description);
//                        iconDoc.color(tm.getPrimaryTextColor());

                        tvDocTitle.setTextColor(getThemeManager().getPrimaryTextColor());
                        tvDocSize.setTextColor(getThemeManager().getSecondaryTextColor());
//                        ivDocIcon.setImageDrawable(iconDoc);

                        tvDocTitle.setText(att.document.title);
                        tvDocSize.setText((att.document.size / 1048576) + "MB");

                        ViewUtil.setTypeface(tvDocTitle);
                        ViewUtil.setTypeface(tvDocSize);

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
        }
        return view;
    }

    private Drawable getDrawable(int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return mContext.getResources().getDrawable(id, mContext.getTheme());
        } else {
            return mContext.getResources().getDrawable(id);
        }
    }

    private void addUserToDatabase(final long uid, final OnVKUserLoaded l) {
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
                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mHelper.addUserToDB(finalProfile);
                            l.onLoaded(finalProfile);
                        }
                    });
                }
            }
        });
    }

    private interface OnVKUserLoaded {
        void onLoaded(VKUser user);
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
                    ArrayList<Long> mids = new ArrayList<>(1);
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


    private static class ViewHolder {

        public LinearLayout llAttachContainer;
        public LinearLayout llBaseContainer;
        public LinearLayout llMessageContainer;
        public LinearLayout llImageContainer;
        public LinearLayout llAudioContainer;
        public LinearLayout llDocContainer;
        public LinearLayout llFwdMessagesContainer;
        public TextView tvBody;
        public TextView tvDate;
        //        public TextView tvDateOneLine;
        public CircleImageView ivPhoto;
        public ImageView ivSelected;

        public ViewHolder(View v) {
            llAttachContainer = (LinearLayout) v.findViewById(R.id.container_message);
            llBaseContainer = (LinearLayout) v.findViewById(R.id.base_container_message);
            llMessageContainer = (LinearLayout) v.findViewById(R.id.llMessageContainer);
            llImageContainer = (LinearLayout) v.findViewById(R.id.imageContainer_message);
            llAudioContainer = (LinearLayout) v.findViewById(R.id.audioContainer_message);
            llDocContainer = (LinearLayout) v.findViewById(R.id.docContainer_message);
            llFwdMessagesContainer = (LinearLayout) v.findViewById(R.id.fwdMessagesContainer_message);

            tvBody = (TextView) v.findViewById(R.id.tvBody_message);
            tvDate = (TextView) v.findViewById(R.id.tvDate_message);
            ivSelected = (ImageView) v.findViewById(R.id.ivMessageSelected);
//            tvDateOneLine = (TextView) v.findViewById(R.id.tvDate_message_one_line);

            ivPhoto = (CircleImageView) v.findViewById(R.id.ivPhoto_message);
        }
    }

    public static class RoundedTransformation implements Transformation {
        int pixels;

        public RoundedTransformation(int pixels) {
            this.pixels = pixels;
        }

        @Override
        public Bitmap transform(Bitmap source) {
            Bitmap output = Bitmap.createBitmap(source.getWidth(), source
                    .getHeight(), source.getConfig());
            Canvas canvas = new Canvas(output);

            final int color = 0xff424242;
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, source.getWidth(), source.getHeight());
            final RectF rectF = new RectF(rect);
            final float roundPx = pixels;

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(source, rect, rect, paint);

            if (source != output) {
                source.recycle();
                source = null;
            }

            return output;
        }

        @Override
        public String key() {
            return "round";
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
                // Размер переполнен
                // Пробуем переиспользовать старые View
//                removeView(baseCache.valueAt(0));
//                baseCache.removeAt(0);

//                baseCache.setValueAt(baseCache.indexOfKey(key), fwdMessageView);

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
