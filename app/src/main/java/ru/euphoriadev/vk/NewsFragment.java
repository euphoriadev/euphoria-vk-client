package ru.euphoriadev.vk;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.io.IOException;
import java.text.SimpleDateFormat;

import ru.euphoriadev.vk.api.Api;
import ru.euphoriadev.vk.api.KException;
import ru.euphoriadev.vk.api.model.NewsItem;
import ru.euphoriadev.vk.api.model.Newsfeed;
import ru.euphoriadev.vk.api.model.VKGroup;
import ru.euphoriadev.vk.util.Account;

public class NewsFragment extends Fragment {

    Account account;
    Api api;
    ListView lvNews;

    public NewsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_news, container, false);

        api = Api.get();

        lvNews = (ListView) rootView.findViewById(R.id.lvNews);


        getNews(account.user_id);

        return rootView;
    }

    public void getNews(long user_id) {


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Newsfeed apiNews = api.getNews(null, 30, null, 0, "", "groups, pages", "post", 5, null, null);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            lvNews.setAdapter(new NewsAdapter(getActivity(), apiNews));

                        }
                    });
                } catch (IOException | JSONException | KException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    private class NewsAdapter extends BaseAdapter {

        Context mContext;
        LayoutInflater inflater;
        Newsfeed newsfeed;

        public NewsAdapter(Context context, Newsfeed newsfeed) {
            this.mContext = context;
            this.newsfeed = newsfeed;
            this.inflater = (LayoutInflater)
                    mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        }

        @Override
        public int getCount() {
            return this.newsfeed.items.size();
        }

        @Override
        public Object getItem(int position) {
            return this.newsfeed.items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = convertView;
            if (view == null) {
                view = inflater.inflate(R.layout.news_list_item, parent, false);
            }

            NewsItem item = newsfeed.items.get(position);
            VKGroup group = newsfeed.groups.get(position);

            TextView tvPageName = (TextView) view.findViewById(R.id.tvGroupName_news);
            TextView tvText = (TextView) view.findViewById(R.id.tvTextOfPost_news);
            TextView tvDate = (TextView) view.findViewById(R.id.tvTime_news);
            ImageView ivPhoto = (ImageView) view.findViewById(R.id.ivPhoto_news);



            tvText.setText(item.text);

            tvDate.setText(new SimpleDateFormat("HH:mm").format(item.date * 1000));

            tvPageName.setText(group.name);

            Picasso.with(mContext)
                    .load(group.photo_50)
                    .into(ivPhoto);



            return view;
        }
    }



}
