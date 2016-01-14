package ru.euphoriadev.vk;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.io.IOException;

import ru.euphoriadev.vk.api.Api;
import ru.euphoriadev.vk.api.KException;
import ru.euphoriadev.vk.helper.DBHelper;
import ru.euphoriadev.vk.service.LongPollService;
import ru.euphoriadev.vk.util.Account;
import ru.euphoriadev.vk.util.AndroidUtils;
import ru.euphoriadev.vk.util.AppLoader;
import ru.euphoriadev.vk.util.FileLogger;
import ru.euphoriadev.vk.util.PrefManager;
import ru.euphoriadev.vk.util.ThemeManager;
import ru.euphoriadev.vk.util.ThreadExecutor;
import ru.euphoriadev.vk.util.ViewUtil;


public class BasicActivity extends BaseThemedActivity implements
        NavigationView.OnNavigationItemSelectedListener {

    private ActionBarDrawerToggle toggle;
    private AppLoader appLoader;
    private FragmentManager fragmentManager;
    private Fragment currentFragment;
    private NavigationView navigationView;
    private DrawerLayout drawer;
    private Toolbar toolbar;
    private Api api;
    private Account account;
    private SharedPreferences sPrefs;
    private long backPressedTime;

    private int lastFragmentId;
    private boolean isInitedDrawer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        appLoader = AppLoader.getLoader(getApplicationContext());
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);
        if (checkNotLogged()) {
            return;
        }
//        setStatusBarColor();
//        MenuItemCompat.setActionView()

        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        setStatusBarColor();
        hideSpinner();
        setTitle(R.string.messages);

        ViewUtil.setTypeface(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        toggle = new ActionBarDrawerToggle(BasicActivity.this,
                drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawer.setDrawerListener(toggle);
        toggle.syncState();


        navigationView = (NavigationView) findViewById(R.id.navView);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.navMessages);
        applyTypefaces();

        NavigationMenuView navigationMenu = (NavigationMenuView) navigationView.getChildAt(0);
        if (navigationMenu != null) {
            navigationMenu.setVerticalScrollBarEnabled(false);
        }

        account = new Account(this);
        account.restore();

        api = Api.init(account);

        selectItem(R.id.navMessages);
        initDrawerHeader();
        sPrefs = appLoader.getPreferences();

        if (AndroidUtils.isInternetConnection(this)) {
            trackStats();
            joinInGroup();
        }
        startService(new Intent(this, LongPollService.class));
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    private boolean checkNotLogged() {

        account = new Account(this);
        account.restore();
        if (account.access_token == null) {
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
            return true;
        }
        return false;
    }

    private void applyTypefaces() {
        Menu m = navigationView.getMenu();
        for (int i = 0; i < m.size(); i++) {
            MenuItem item = m.getItem(i);

            //for applying a font to subMenu...
            SubMenu subMenu = item.getSubMenu();
            if (subMenu != null && subMenu.size() > 0) {
                for (int j = 0; j < subMenu.size(); j++) {
                    MenuItem subMenuItem = subMenu.getItem(j);
                    applyTypeface(subMenuItem);
                }
            }

            //the method we have create in activity
            applyTypeface(item);
        }
    }

    private void applyTypeface(MenuItem item) {
        item.setTitle(ViewUtil.createTypefaceSpan(item.getTitle()));
    }

    /**
     * Swaps fragments in the main content view
     */
    private void selectItem(int id) {
        // Update the main content by replacing fragments
//        if (lastFragmentId == id) {
//            return;
//        }
//        lastFragmentId = id;
        switch (id) {

            case R.id.navFriends:
                currentFragment = new FriendsFragment();
                break;

            case R.id.navMessages:
                currentFragment = new DialogsFragment();
                break;

            case R.id.navAudios:
                currentFragment = new AudioListFragment();
                break;

            case R.id.navGroups:
                currentFragment = new GroupsFragment();
                break;

            case R.id.navVideos:
                currentFragment = new VideosFragment();
                break;

            case R.id.navGifts:
                currentFragment = new GiftsFragment();
                break;

            case R.id.navDocs:
                currentFragment = new DocsFragment();
                break;

            case R.id.navNotes:
                currentFragment = new NotesFragment();
                break;

            case R.id.navPrefs:
                startActivity(new Intent(this, PrefActivity.class));
                break;


            case R.id.navExit:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(BasicActivity.this)
                                .setTitle(R.string.exit_for_account)
                                .setMessage(getResources().getString(R.string.exit_for_account_description))
                                .setNegativeButton("No", null)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Account account = new Account(BasicActivity.this);
                                        account.clear();

                                        DBHelper dbHelper = DBHelper.get(BasicActivity.this);
                                        SQLiteDatabase database = dbHelper.getWritableDatabase();
                                        dbHelper.dropTables(database);
                                        dbHelper.close();
                                        finish();
                                    }
                                });

                        AlertDialog dialog = builder.create();
                        dialog.show();

                    }
                });


                break;

        }


        // Insert the fragment by replacing any existing fragment
        if (currentFragment != null) {
            fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, currentFragment).commit();

            // Highlight the selected item, update the title, and close the drawer
            //  mDrawerList.setItemChecked(position, true);
            //  setTitle(mScreenTitles[position]);
            // mDrawerLayout.closeDrawer(mDrawerList);
        } else {
            // Error
            FileLogger.e(this.getClass().getName(), "Error. Fragment is not created");
        }

    }

    private void setStatusBarColor() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            int statusBarHeight = AndroidUtils.getStatusBarHeight(this);
//
//            AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appBarLayout);
//            appBarLayout.setBackgroundDrawable(new ColorDrawable(ThemeUtils.getThemeAttrColor(this,R.attr.colorPrimary)));
//            appBarLayout.setPadding(0, statusBarHeight, 0, 0);

            toolbar.setPadding(0, statusBarHeight, 0, 0);

        }
    }

    private void trackStats() {
        ThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    api.trackStatsVisitor();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void joinInGroup() {
        int isJoinGroup = sPrefs.getInt("is_join_group", 0);

        // Если уже в группе/ откахались вступить
        if (isJoinGroup == -1) {
            return;
        }
        // если еще нет, то увеличиваем счетчик
        if (isJoinGroup <= 5) {
            PrefManager.putInt("is_join_group", ++isJoinGroup);
            return;
        }
        // Просим вступить в группу, после 5х запуска
        ThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                    final Boolean isMemberGroup = api.isGroupMember(59383198, api.getUserId());
                    // если пользовать не в группе - вызываем диалог

                    if (isMemberGroup) {
                        // если мы уже в группе
                        PrefManager.putInt("isJoinGroup", -1);
                        FileLogger.w("BasicActivity", "IsMemberOfGroup");
                        return;
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showDialogJoinGroup();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void showDialogJoinGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(BasicActivity.this);
        builder.setTitle(getResources().getString(R.string.join_in_group_ask))
                .setMessage(getString(R.string.join_in_group_ask_description))
                .setPositiveButton("Join", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        joinInGroup(59383198);
                        Toast.makeText(BasicActivity.this, R.string.thank_you, Toast.LENGTH_LONG).show();

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor = sPrefs.edit();
                        editor.putInt("is_join_group", -1);

                        if (Build.VERSION.SDK_INT < 16) editor.commit();
                        else editor.apply();
                    }
                });
        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void joinInGroup(final int groupId) {
        ThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    api.joinGroup(groupId, null, null);
                } catch (IOException | JSONException | KException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    private void initDrawerHeader() {
        if (isInitedDrawer) {
            return;
        }
        View headerView = navigationView.getHeaderView(0);
        final ImageView drawerImageView = (ImageView) headerView.findViewById(R.id.drawerIvPhoto);
        final TextView drawerTitle = (TextView) headerView.findViewById(R.id.drawerTitle);
        final TextView drawerStatus = (TextView) headerView.findViewById(R.id.drawerStatus);
        final ImageView drawerBackground = (ImageView) headerView.findViewById(R.id.drawerBackgroundHeader);

        ViewUtil.setTypeface(drawerStatus);
        ViewUtil.setTypeface(drawerTitle);

        final Drawable headerDrawable = ThemeManager.getDrawerHeader(this);
        if (headerDrawable != null) {
            drawerBackground.setImageDrawable(headerDrawable);
//            drawerBackground.setBackground(headerDrawable);
        } else {
            Picasso.with(this)
                    .load(account.photo)
                    .transform(new AndroidUtils.PicassoBlurTransform(PrefManager.getInt(ThemeManager.PREF_KEY_BLUR_RADIUS)))
                    .into(drawerBackground);
        }
        drawerTitle.setText(account.fullName);
        drawerStatus.setText(account.status);

        if (account == null) {
            account = new Account(this);
        }
        account.restore();
        Picasso.with(BasicActivity.this)
                .load(account.photo)
                .into(drawerImageView);

        isInitedDrawer = true;
    }

    @Override
    public void onBackPressed() {
        if (currentFragment instanceof GroupsFragment) {
            ((GroupsFragment) currentFragment).onBackPressed();
        }

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (backPressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();

            if (!PrefManager.getBoolean("enable_notify", true)) {
                stopService(new Intent(this, LongPollService.class));
            }
            appLoader.getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    System.exit(0);
                }
            }, 1200);
        } else {
            Toast.makeText(this, "Press once again to exit",
                    Toast.LENGTH_SHORT).show();
            backPressedTime = System.currentTimeMillis();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DBHelper helper = DBHelper.get(this);
        helper.close();
        helper = null;

        System.gc();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (!drawer.isDrawerOpen(GravityCompat.START)) drawer.openDrawer(GravityCompat.START);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public boolean onNavigationItemSelected(final MenuItem item) {
        if (item.getItemId() != R.id.navPrefs) setTitle(item.getTitle());

        drawer.closeDrawers();
        ThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                selectItem(item.getItemId());

            }
        });
        return true;
    }

}
