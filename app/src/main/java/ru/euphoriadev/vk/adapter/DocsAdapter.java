package ru.euphoriadev.vk.adapter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.PermissionChecker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import ru.euphoriadev.vk.R;
import ru.euphoriadev.vk.api.model.VKDocument;
import ru.euphoriadev.vk.common.AppLoader;
import ru.euphoriadev.vk.common.ThemeManager;
import ru.euphoriadev.vk.helper.FileHelper;
import ru.euphoriadev.vk.util.AndroidUtils;
import ru.euphoriadev.vk.util.PermissionAllower;
import ru.euphoriadev.vk.view.CircleImageView;

/**
 * Created by user on 13.12.15.
 */
public class DocsAdapter extends BaseArrayAdapter<VKDocument> {
    private static ColorFilter colorFilter;

    private static final Drawable colorDrawable = new ColorDrawable(ThemeManager.getColorAccent(AppLoader.appContext));
    private static final Drawable fileIconDrawable = decodeBitmap(R.drawable.ic_insert_drive_file);
    private static final Drawable textIconDrawable = decodeBitmap(R.drawable.ic_text_fields);
//    private static final Drawable gifIconDrawable = decodeBitmap(R.drawable.ic_gif);
    private static final Drawable audioIconDrawable = decodeBitmap(R.drawable.ic_audiotrack);
    private static final Drawable videoIconDrawable = decodeBitmap(R.drawable.ic_movie);
    private static final Drawable codeIconDrawable =  decodeBitmap(R.drawable.ic_code);
    private static final Drawable androidIconDrawable = decodeBitmap(R.drawable.ic_android);
    private static final Drawable bookIconDrawable = decodeBitmap(R.drawable.ic_book);
    private static final Drawable archiveIconDrawable = decodeBitmap(R.drawable.ic_doc_compressed);

    public DocsAdapter(Context context, ArrayList<VKDocument> values) {
        super(context, values);
        colorFilter = new PorterDuffColorFilter(ThemeManager.getSecondaryTextColor(), PorterDuff.Mode.MULTIPLY);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder holder;
        if (view == null) {
            view = getInflater().inflate(R.layout.list_item_doc, parent, false);

            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        getView(view, getItem(position), holder);
        return view;
    }


    /**
     * Magic, don't to touch
     */
    public static String convertBytes(long sizeInBytes) {
        long unit = 1024;
        if (sizeInBytes < unit) return sizeInBytes + " B";
        int exp = (int) (Math.log(sizeInBytes) / Math.log(unit));
        String pre = ("KMGTPE").charAt(exp - 1) + ("i");
        return String.format("%.1f %sB", sizeInBytes / Math.pow(unit, exp), pre);
    }


    @Override
    public boolean compareTo(String q, VKDocument value) {
        return value.title.toLowerCase().contains(q);
    }

    private static BitmapDrawable decodeBitmap(int resId) {
        return new BitmapDrawable(AppLoader.appContext.getResources(), BitmapFactory.decodeResource(AppLoader.appContext.getResources(), resId));
    }

    public static void getView(final View view, final VKDocument doc, ViewHolder holder) {
        holder.tvTitle.setText(doc.title);
        holder.tvSize.setText(convertBytes(doc.size));

        holder.ivDownload.setColorFilter(colorFilter);
        holder.ivDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PermissionChecker.checkSelfPermission(view.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PermissionChecker.PERMISSION_GRANTED) {
                    PermissionAllower.allowPermission((Activity) view.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    return;
                }
                String type = "";
                if (doc.isAndroidApp()) {
                    type = "application/vnd.android.package-archive";
                    String title = doc.title;
                    if (title.contains(".apk")) {
                        String[] split = AndroidUtils.split(title, '.');
                        split[split.length - 1] = "apk";
                        title = AndroidUtils.join(split, '.');

                    }
                    FileHelper.downloadFileWithDefaultManager(doc.url, title, type);
                    return;
                }
                FileHelper.downloadFileWithDefaultManager(doc.url, doc.title, type);
            }
        });
        holder.ivDownload.setFocusable(false);
        holder.ivDownload.setFocusableInTouchMode(false);

        if (doc.isImage() || doc.isGif()) {
            holder.ivIcon.setVisibility(View.GONE);
        } else {
            Picasso.with(view.getContext()).cancelRequest(holder.ivCircleIcon);
            holder.ivIcon.setVisibility(View.VISIBLE);
            holder.ivCircleIcon.setImageDrawable(colorDrawable);
        }

        boolean isCode = doc.isCode();
        switch (doc.type) {
            case VKDocument.TYPE_GIF:
            case VKDocument.TYPE_IMAGE:
                Picasso.with(view.getContext())
                        .load(doc.photo_100)
                        .config(Bitmap.Config.RGB_565)
                        .placeholder(colorDrawable)
                        .into(holder.ivCircleIcon);
                break;

            case VKDocument.TYPE_TEXT:
                holder.ivIcon.setImageDrawable(isCode ? codeIconDrawable : textIconDrawable);
                break;

//            case VKDocument.TYPE_GIF:
//                holder.ivIcon.setImageDrawable(gifIconDrawable);
//                break;

            case VKDocument.TYPE_VIDEO:
                holder.ivIcon.setImageDrawable(videoIconDrawable);
                break;

            case VKDocument.TYPE_AUDIO:
                holder.ivIcon.setImageDrawable(audioIconDrawable);
                break;

            case VKDocument.TYPE_BOOK:
                holder.ivIcon.setImageDrawable(bookIconDrawable);
                break;

            case VKDocument.TYPE_ARCHIVE:
                holder.ivIcon.setImageDrawable(archiveIconDrawable);
                break;

            default:
                if (isCode) {
                    holder.ivIcon.setImageDrawable(codeIconDrawable);
                } else if (doc.isJar()) {
                    holder.ivIcon.setImageDrawable(archiveIconDrawable);
                } else {
                    holder.ivIcon.setImageDrawable(doc.isAndroidApp() ? androidIconDrawable : fileIconDrawable);
                }
        }
    }

    public static class ViewHolder {
        public ImageView ivIcon;
        public CircleImageView ivCircleIcon;
        public ImageView ivDownload;
        public TextView tvTitle;
        public TextView tvSize;

        public ViewHolder(View v) {
            ivIcon = (ImageView) v.findViewById(R.id.ivDocIcon);
            ivDownload = (ImageView) v.findViewById(R.id.ivDocDownload);
            ivCircleIcon = (CircleImageView) v.findViewById(R.id.ivOocCircle);

            tvTitle = (TextView) v.findViewById(R.id.tvDocTitle);
            tvSize = (TextView) v.findViewById(R.id.tvDocSize);
        }
    }

}
