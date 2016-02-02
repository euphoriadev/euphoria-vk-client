package ru.euphoriadev.vk.adapter;

import android.content.Context;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import ru.euphoriadev.vk.R;
import ru.euphoriadev.vk.api.model.VKDocument;
import ru.euphoriadev.vk.helper.FileHelper;
import ru.euphoriadev.vk.util.ThemeUtils;
import ru.euphoriadev.vk.view.TextCircleView;

/**
 * Created by user on 13.12.15.
 */
public class DocsAdapter extends BaseArrayAdapter<VKDocument> {
    private ColorFilter colorFilter;
    private Drawable colorDrawable;

    public DocsAdapter(Context context, ArrayList<VKDocument> values) {
        super(context, values);

        colorFilter = new PorterDuffColorFilter(getThemeManager().getSecondaryTextColor(), PorterDuff.Mode.MULTIPLY);
        colorDrawable = new ColorDrawable(ThemeUtils.getThemeAttrColor(getContext(), R.attr.colorAccent));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = getInflater().inflate(R.layout.list_item_doc, parent, false);
        }
        final VKDocument doc = getItem(position);
        TextCircleView iv = (TextCircleView) view.findViewById(R.id.ivDoc);
        TextView tvTitle = (TextView) view.findViewById(R.id.tvDocTitle);
        TextView tvSize = (TextView) view.findViewById(R.id.tvDocSize);

        tvTitle.setText(doc.title);
        tvSize.setText(convertBytes(doc.size));


        final ImageButton button = (ImageButton) view.findViewById(R.id.ibDocMenu);
        button.setColorFilter(colorFilter);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                PopupMenu menu = new PopupMenu(getContext(), button);
//                menu.getMenuInflater().inflate(R.menu.oopup_menu_doc, menu.getMenu());
//                menu.show();

                FileHelper.downloadFileWithDefaultManager(doc.url, doc.title, null);
            }
        });
        if (doc.isImage()) {
            iv.setText("");
            Picasso.with(getContext())
                    .load(doc.photo_130)
                    .placeholder(colorDrawable)
                    .into(iv);
        } else {
            iv.setText(doc.ext);
            iv.setImageDrawable(colorDrawable);

        }

//        final Bitmap bitmap = AndroidUtils.drawableToBitmap(iv.getDrawable());
//        AndroidUtils.drawText("Text", bitmap);
//        iv.setImageBitmap(bitmap);


        return view;
    }

    private String convertBytes(long sizeInBytes) {
        long unit = 1024;
        if (sizeInBytes < unit) return sizeInBytes + " B";
        int exp = (int) (Math.log(sizeInBytes) / Math.log(unit));
        String pre = ("KMGTPE").charAt(exp-1) + ("i");
        return String.format("%.1f %sB", sizeInBytes / Math.pow(unit, exp), pre);
    }


    @Override
    public boolean compareTo(String q, VKDocument value) {
        return value.title.toLowerCase().contains(q);
    }


}
