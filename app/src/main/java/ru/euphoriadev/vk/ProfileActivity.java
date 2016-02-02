package ru.euphoriadev.vk;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import ru.euphoriadev.vk.api.Api;
import ru.euphoriadev.vk.api.model.VKUser;
import ru.euphoriadev.vk.util.AppLoader;
import ru.euphoriadev.vk.util.FastBlur;
import ru.euphoriadev.vk.util.ThreadExecutor;
import ru.euphoriadev.vk.view.CircleImageView;

/**
 * Created by Igor on 18.11.15.
 */
public class ProfileActivity extends BaseThemedActivity {
    private int uid;
    private VKUser profile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        AppLoader.getLoader().applyTheme(this);
//        ThemeManagerOld.get(this).setBasicTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        uid = getIntent().getIntExtra("uid", 0);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarProfile);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final ImageView ivPlaceholder = (ImageView) findViewById(R.id.ivProfilePlaceholder);
        final ImageView ivPhoto = (ImageView) findViewById(R.id.ivProfilePhoto);
        final TextView tvTitle = (TextView) findViewById(R.id.tvProfileTitle);
        final TextView tvStatus = (TextView) findViewById(R.id.tvProfileStatus);

        final View onlineIndicator = findViewById(R.id.profileOnlineIndicator);

        getProfile(new OnCompleteListener() {
            @Override
            public void omComplete(VKUser user) {
                profile = user;
                Picasso.with(ProfileActivity.this)
                        .load(user.photo_50)
                        .config(Bitmap.Config.RGB_565)
                        .transform(new BlurTransformation(30))
                        .fit()
                        .into(ivPlaceholder);

                Picasso.with(ProfileActivity.this)
                        .load(user.photo_200)
                        .fit()
                        .into(ivPhoto);

                tvTitle.setText(user.toString());
                tvStatus.setText(user.status);

//                toolbar.setTitle(user.online ? R.string.online : R.string.offline);

                onlineIndicator.setVisibility(View.GONE);
//                onlineIndicator.getBackground().setColorFilter(ThemeUtils.getThemeAttrColor(ProfileActivity.this, R.attr.colorAccent), PorterDuff.Mode.MULTIPLY);
            }
        });


    }

    private void getProfile(final OnCompleteListener listener) {
        ThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final VKUser profile = Api.get().getProfile(uid);
                    if (listener != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listener.omComplete(profile);
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Send").setIcon(R.drawable.ic_pets_white).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private interface OnCompleteListener {
        void omComplete(VKUser user);
    }

    private class BlurTransformation implements Transformation {
        private int radius;

        public BlurTransformation(int radius) {
            this.radius = radius;
        }

        @Override
        public Bitmap transform(Bitmap source) {
            return FastBlur.doBlur(source, radius);
        }

        @Override
        public String key() {
            return "blur";
        }
    }

    public class AvatarImageBehavior extends CoordinatorLayout.Behavior<CircleImageView> {

        private final static float MIN_AVATAR_PERCENTAGE_SIZE = 0.3f;
        private final static int EXTRA_FINAL_AVATAR_PADDING = 80;

        private final static String TAG = "behavior";
        private final Context mContext;
        private float mAvatarMaxSize;

        private float mFinalLeftAvatarPadding;
        private float mStartPosition;
        private int mStartXPosition;
        private float mStartToolbarPosition;
        private int mStartYPosition;
        private int mFinalYPosition;
        private int finalHeight;
        private int mStartHeight;
        private int mFinalXPosition;

        public AvatarImageBehavior(Context context, AttributeSet attrs) {
            mContext = context;
            init();

            mFinalLeftAvatarPadding = 16;
        }

        private void init() {
            bindDimensions();
        }

        private void bindDimensions() {
            mAvatarMaxSize = 120;
        }

        @Override
        public boolean layoutDependsOn(CoordinatorLayout parent, CircleImageView child, View dependency) {
            return dependency instanceof Toolbar;
        }

        @Override
        public boolean onDependentViewChanged(CoordinatorLayout parent, CircleImageView child, View dependency) {
            shouldInitProperties(child, dependency);

            final int maxScrollDistance = (int) (mStartToolbarPosition - getStatusBarHeight());
            float expandedPercentageFactor = dependency.getY() / maxScrollDistance;

            float distanceYToSubtract = ((mStartYPosition - mFinalYPosition)
                    * (1f - expandedPercentageFactor)) + (child.getHeight() / 2);

            float distanceXToSubtract = ((mStartXPosition - mFinalXPosition)
                    * (1f - expandedPercentageFactor)) + (child.getWidth() / 2);

            float heightToSubtract = ((mStartHeight - finalHeight) * (1f - expandedPercentageFactor));

            child.setY(mStartYPosition - distanceYToSubtract);
            child.setX(mStartXPosition - distanceXToSubtract);

            int proportionalAvatarSize = (int) (mAvatarMaxSize * (expandedPercentageFactor));

            CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
            lp.width = (int) (mStartHeight - heightToSubtract);
            lp.height = (int) (mStartHeight - heightToSubtract);
            child.setLayoutParams(lp);
            return true;
        }

        private void shouldInitProperties(CircleImageView child, View dependency) {
            if (mStartYPosition == 0)
                mStartYPosition = (int) (child.getY() + (child.getHeight() / 2));

            if (mFinalYPosition == 0)
                mFinalYPosition = (dependency.getHeight() / 2);

            if (mStartHeight == 0)
                mStartHeight = child.getHeight();

            if (finalHeight == 0)
                finalHeight = 32;

            if (mStartXPosition == 0)
                mStartXPosition = (int) (child.getX() + (child.getWidth() / 2));

            if (mFinalXPosition == 0)
                mFinalXPosition = mContext.getResources().getDimensionPixelOffset(R.dimen.abc_action_bar_content_inset_material) + (finalHeight / 2);

            if (mStartToolbarPosition == 0)
                mStartToolbarPosition = dependency.getY() + (dependency.getHeight() / 2);
        }

        public int getStatusBarHeight() {
            int result = 0;
            int resourceId = mContext.getResources().getIdentifier("status_bar_height", "dimen", "android");

            if (resourceId > 0) {
                result = mContext.getResources().getDimensionPixelSize(resourceId);
            }
            return result;
        }
    }

}
