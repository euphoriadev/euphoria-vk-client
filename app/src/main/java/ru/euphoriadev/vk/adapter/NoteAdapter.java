package ru.euphoriadev.vk.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import ru.euphoriadev.vk.R;
import ru.euphoriadev.vk.api.model.VKNote;
import ru.euphoriadev.vk.util.AppLoader;
import ru.euphoriadev.vk.util.TypefaceManager;
import ru.euphoriadev.vk.util.ViewUtil;

/**
 * Created by Igor on 09.12.15.
 */
public class NoteAdapter extends BaseArrayAdapter<VKNote> {
    AppLoader loader;
    SimpleDateFormat sdf;
    Date date;
    XmlPullParser xmlParser;

    public NoteAdapter(Context context, ArrayList<VKNote> values) {
        super(context, values);

        loader = AppLoader.getLoader();
        sdf = new SimpleDateFormat("d MMM, yyyy");
        date = new Date(System.currentTimeMillis());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder holder;

        if (view == null) {
            view = getInflater().inflate(R.layout.note_list_item, parent, false);
            holder = new ViewHolder(view);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        final VKNote item = getItem(position);

        holder.tvTitle.setTypeface(TypefaceManager.getBoldTypeface(getContext()));
        ViewUtil.setTypeface(holder.tvText);
        ViewUtil.setTypeface(holder.tvDate);

        holder.cardView.setCardBackgroundColor(loader.getApplicationContext().getResources().getColor(loader.isDarkTheme ?
                R.color.md_dark_background : R.color.white));
        holder.tvTitle.setText(item.title);
        holder.tvText.setText(Html.fromHtml(item.text));
        holder.tvDate.setText(sdf.format(item.date * 1000));

        return view;
    }

    public String parseForText(VKNote note) throws XmlPullParserException {
//        if (xmlParser == null) {
//            xmlParser = Xml.newPullParser();
//        }
//
//        while (xmlParser.getEventType() != XmlPullParser.END_DOCUMENT) {
//        }
//        String xmlText = note.text;
        return null;
    }

    private class ViewHolder {

        public CardView cardView;
        public TextView tvTitle;
        public TextView tvText;
        public TextView tvDate;

        public ViewHolder(View v) {
            cardView = (CardView) v.findViewById(R.id.cardNote);
            tvTitle = (TextView) v.findViewById(R.id.tvNoteTitle);
            tvText = (TextView) v.findViewById(R.id.tvNoteText);
            tvDate = (TextView) v.findViewById(R.id.tvNoteDate);
        }
    }
}
