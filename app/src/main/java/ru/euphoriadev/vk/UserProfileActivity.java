package ru.euphoriadev.vk;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import ru.euphoriadev.vk.api.Api;
import ru.euphoriadev.vk.api.model.Counters;
import ru.euphoriadev.vk.api.model.VKFullUser;
import ru.euphoriadev.vk.util.Account;
import ru.euphoriadev.vk.common.ThemeManager;
import ru.euphoriadev.vk.async.ThreadExecutor;

/**
 * Created by Igor on 30.07.15.
 */
public class UserProfileActivity extends BaseThemedActivity {

    Api api;
    Account account;

    TextView tvFullName;
    TextView tvStatus;
    ImageView ivPhoto;

    TextView tvFriends;
    TextView tvAudios;
    TextView tvVideos;


    long user_id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean useTwoProfile = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("use_two_profile", false);
        if (useTwoProfile) {
            setContentView(R.layout.profile_two);
        } else {
            setContentView(R.layout.profile_new);
        }

        user_id = getIntent().getExtras().getLong("user_id");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_profile);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        }

        tvFullName = (TextView) findViewById(R.id.tvProfileName);
        tvStatus = (TextView) findViewById(R.id.tvProfileStatus);
        ivPhoto = (ImageView) findViewById(R.id.ivProfilePhoto);

        tvFriends = (TextView) findViewById(R.id.tvProfileFriends);
        tvAudios = (TextView) findViewById(R.id.tvProfileAudios);
        tvVideos = (TextView) findViewById(R.id.tvProfileVideos);

        int colorSecondary = ThemeManager.getSecondaryTextColor();
        int colorPrimary = ThemeManager.getPrimaryTextColor();

        tvStatus.setTextColor(colorSecondary);
        tvFullName.setTextColor(colorPrimary);


        LinearLayout ll = (LinearLayout) findViewById(R.id.llProfileBackground);
        ll.setBackgroundColor(ThemeManager.getThemeColor(this));

        api = Api.get();
        getProfile();

    }

    private void getProfile() {
        ThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final VKFullUser user = api.getProfileFull(user_id, "online,last_seen, city, counters, status, photo_200, photo_50, photo_400_orig");
//                    if (user.uid == user_id) {
//                     //   account.photo = user.photo;
//                        account.update("photo", user.photo);
//                    }
                    final Counters counters = user.counters;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvFullName.setText(user.toString());
                            tvStatus.setText(user.status);
                            getSupportActionBar().setTitle(getString(R.string.profile));


                            tvFriends.setText(getString(R.string.friends));
                            tvVideos.setText(getString(R.string.videos));
                            tvAudios.setText(getString(R.string.audios));

                            tvFriends.append("\n" + counters.friends);
                            tvVideos.append("\n" + counters.videos);
                            tvAudios.append("\n" + counters.audios);

                            Picasso.with(UserProfileActivity.this)
                                    .load(user.photo_400_orig)
                                    .into(ivPhoto);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
