package ru.euphoriadev.vk;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import ru.euphoriadev.vk.api.model.VKAudio;
import ru.euphoriadev.vk.common.AppLoader;

/**
 * Created by Igor on 03.12.15.
 * <p/>
 * Музыкальный плеер
 */
public class MusicPlayerActivity extends AppCompatActivity {
    public static final String TAG = "MusicPlayerActivity";

    private VKAudio mAudio;

    private ImageButton buttonPrev;
    private ImageButton buttonPlay;
    private ImageButton buttonNext;

    private TextView tvTitle;
    private TextView tvArtist;

    private ProgressBar progressBar;

    private LinearLayout llBackgroundPhoto;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppLoader.getLoader().applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_activity);

        buttonPrev = (ImageButton) findViewById(R.id.ibPlayerSkipPrev);
        buttonPlay = (ImageButton) findViewById(R.id.ibPlayerPlay);
        buttonNext = (ImageButton) findViewById(R.id.ibPlayerSkipNext);

        tvTitle = (TextView) findViewById(R.id.tvPlayerTitle);
        tvArtist = (TextView) findViewById(R.id.tvPlayerArtist);

        progressBar = (SeekBar) findViewById(R.id.pbPlayer);


        mAudio = (VKAudio) getIntent().getSerializableExtra("audio");

        setupViews();
    }

    private void setupViews() {
        tvTitle.setText(mAudio.title);
        tvArtist.setText(mAudio.artist);

        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (mAudio.isPlaying) {
////                    MediaPlayerHelper.
//                }
            }
        });
    }
}
