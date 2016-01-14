package ru.euphoriadev.vk;


import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;

import ru.euphoriadev.vk.api.Api;
import ru.euphoriadev.vk.service.EternallOnlineService;
import ru.euphoriadev.vk.util.Account;
import ru.euphoriadev.vk.util.AppLoader;
import ru.euphoriadev.vk.util.FileLogger;
import ru.euphoriadev.vk.util.PrefManager;
import ru.euphoriadev.vk.util.ThemeManager;
import ru.euphoriadev.vk.util.ThreadExecutor;
import ru.euphoriadev.vk.util.TypefaceManager;
import ru.euphoriadev.vk.util.AndroidUtils;
import ru.euphoriadev.vk.util.ViewUtil;
import ru.euphoriadev.vk.view.pref.MaterialCheckBoxPreference;
import ru.euphoriadev.vk.view.pref.MaterialListPreference;
import ru.euphoriadev.vk.view.pref.MaterialPreference;
import ru.euphoriadev.vk.view.pref.MaterialPreferenceCategory;
import ru.euphoriadev.vk.view.pref.MaterialSwitchPreference;
import ru.euphoriadev.vk.view.colorpicker.ColorPickerDialog;
import ru.euphoriadev.vk.view.colorpicker.ColorPickerSwatch;
import ru.euphoriadev.vk.view.pref.ProgressBarPreference;

/**
 * Created by Igor on 28.02.15.
 */
public class PrefsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{

    boolean isSetListView;
    PreferenceScreen rootScreen;

    public PrefsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //    addPreferencesFromResource(R.xml.prefs);

        //      FastPrefs prefs = new FastPrefs(getActivity())

        rootScreen = getPreferenceManager().createPreferenceScreen(getActivity());
        // говорим, что rootScreen - корневой экран
        setPreferenceScreen(rootScreen);
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        PreferenceCategory categoryUI = new MaterialPreferenceCategory(getActivity());
        categoryUI.setTitle(getActivity().getString(R.string.prefs_title_ui));

        rootScreen.addPreference(categoryUI);

        final MaterialSwitchPreference boxNightTheme = new MaterialSwitchPreference(getActivity());
        boxNightTheme.setTitle(getActivity().getString(R.string.prefs_night_theme));
        boxNightTheme.setDefaultValue(true);
        boxNightTheme.setKey("is_night_theme");
        boxNightTheme.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                ThemeManager.setDarkTheme((Boolean) o);
                TaskStackBuilder.create(getActivity())
                        .addNextIntent(new Intent(getActivity(), BasicActivity.class))
                        .addNextIntent(getActivity().getIntent())
                        .startActivities();
                getActivity().overridePendingTransition(R.anim.alpha_out, R.anim.alpha_in);
                return true;
            }
        });


        categoryUI.addPreference(boxNightTheme);


        Preference colourThemeScreen = new MaterialPreference(getActivity());
        colourThemeScreen.setTitle(getActivity().getString(R.string.prefs_app_color));
        colourThemeScreen.setSummary(getActivity().getString(R.string.prefs_app_color_description));
        colourThemeScreen.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ColorPickerDialog pickerDialog = new ColorPickerDialog();
//                final ThemeManagerOld manager = ThemeManagerOld.get(getActivity());
//                int[] colors = manager.getThemeColors();
                pickerDialog.initialize(R.string.pick_color, ThemeManager.PALETTE, ThemeManager.getPaletteColor(), 4, ThemeManager.PALETTE.length);
                pickerDialog.show(((BaseThemedActivity) getActivity()).getSupportFragmentManager(), "colorpicker");

                pickerDialog.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int color) {
//                        manager.putColor(color);
                        ThemeManager.updateColourTheme(color);
                        ThemeManager.updateThemeValues();

                        if (getActivity().getResources().getColor(R.color.md_grey_900) == color) {
                            boxNightTheme.setChecked(true);
                            boxNightTheme.setEnabled(false);
                        } else {
                            boxNightTheme.setEnabled(true);
                        }

                    }
                });
                pickerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
//                        AlphaAnimation animation = new AlphaAnimation(0.2f, 1.0f);
//                        animation.setDuration(300);


                        // почему-то на 4.0,3 вылет с ошибкой
                        TaskStackBuilder.create(getActivity())
                                .addNextIntent(new Intent(getActivity(), BasicActivity.class))
                                .addNextIntent(getActivity().getIntent())
                                .startActivities();
                        getActivity().overridePendingTransition(R.anim.alpha_out, R.anim.alpha_in);

                    }
                });

                return true;
            }
        });

        categoryUI.addPreference(colourThemeScreen);

        CheckBoxPreference boxColorBubble = new MaterialCheckBoxPreference(getActivity());
        boxColorBubble.setTitle(getActivity().getString(R.string.prefs_color_in_msg));
        boxColorBubble.setKey("color_in_messages");
        boxColorBubble.setSummary(getActivity().getString(R.string.prefs_color_in_msg_description));
        boxColorBubble.setDefaultValue(true);

        categoryUI.addPreference(boxColorBubble);


        CheckBoxPreference boxColorBubble2 = new MaterialCheckBoxPreference(getActivity());
        boxColorBubble2.setTitle(getActivity().getString(R.string.prefs_color_out_msg));
        boxColorBubble2.setKey("color_out_messages");
        boxColorBubble2.setSummary(getActivity().getString(R.string.prefs_color_out_msg_description));
        boxColorBubble2.setDefaultValue(false);

        categoryUI.addPreference(boxColorBubble2);


        ListPreference listHeaderDrawer = new MaterialListPreference(getActivity());
        listHeaderDrawer.setTitle(getResources().getString(R.string.prefs_drawer));
        listHeaderDrawer.setSummary(getResources().getString(R.string.prefs_drawer_description));
        listHeaderDrawer.setKey("making_drawer_header");
        listHeaderDrawer.setEntries(R.array.prefs_drawer_header_array);
        listHeaderDrawer.setEntryValues(new CharSequence[]{"0", "1", "2"});
        listHeaderDrawer.setDefaultValue("0");

        categoryUI.addPreference(listHeaderDrawer);

        ProgressBarPreference blurRadiusPreference = new ProgressBarPreference(getActivity());
        blurRadiusPreference.setTitle(R.string.pref_blur_radius);
        blurRadiusPreference.setSummary(R.string.pref_blur_radius_description);
        blurRadiusPreference.setEnabled(PrefManager.getString("making_drawer_header").equalsIgnoreCase("2"));
        blurRadiusPreference.setKey("blur_radius");
        blurRadiusPreference.setDefaultValue(20);
        blurRadiusPreference.getSeekBar().setMax(50);
        blurRadiusPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Toast.makeText(getActivity(), "Please, restart app", Toast.LENGTH_LONG).show();
                return true;
            }
        });

        categoryUI.addPreference(blurRadiusPreference);

        CheckBoxPreference boxDivider = new MaterialCheckBoxPreference(getActivity());
        boxDivider.setTitle(getActivity().getString(R.string.prefs_show_divider));
        boxDivider.setSummary(getActivity().getString(R.string.prefs_show_divider_description));
        boxDivider.setDefaultValue(false);
        boxDivider.setKey("show_divider");


        categoryUI.addPreference(boxDivider);

        CheckBoxPreference boxTwoProfile = new MaterialCheckBoxPreference(getActivity());
        boxTwoProfile.setTitle(getActivity().getString(R.string.prefs_use_second_profile));
        boxTwoProfile.setKey("use_two_profile");
        boxTwoProfile.setEnabled(false);
        boxTwoProfile.setSummary(getActivity().getString(R.string.prefs_use_second_profile_description));
        boxTwoProfile.setDefaultValue(false);

        categoryUI.addPreference(boxTwoProfile);


        ListPreference listSelectLocale = new MaterialListPreference(getActivity());
        listSelectLocale.setTitle(getActivity().getString(R.string.prefs_select_locale));
        listSelectLocale.setSummary(getActivity().getString(R.string.prefs_select_locale_description));
        listSelectLocale.setEntries(new String[]{"Русский", "English", "Дореволюцiонный", "Українська"});
        listSelectLocale.setEntryValues(new CharSequence[]{"ru", "en", "cu", "uk"});
        listSelectLocale.setKey("forced_locale");

        categoryUI.addPreference(listSelectLocale);

        MaterialPreferenceCategory categoryFont = new MaterialPreferenceCategory(getActivity());
        categoryFont.setTitle(R.string.prefs_font);

        rootScreen.addPreference(categoryFont);


        final ListPreference listFontFamily = new MaterialListPreference(getActivity());
        listFontFamily.setTitle(R.string.prefs_font_family);
        listFontFamily.setDefaultValue(String.valueOf(TypefaceManager.FontFamily.ROBOTO));
        listFontFamily.setSummary(getResources().getStringArray(R.array.font_family_array)[TypefaceManager.getFontFamily()]);
        listFontFamily.setEntries(getResources().getStringArray(R.array.font_family_array));
        listFontFamily.setEntryValues(new CharSequence[]{"0", "1", "2", "3"});
        listFontFamily.setKey(TypefaceManager.PREF_KEY_FONT_FAMILY);
        listFontFamily.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                listFontFamily.setSummary(getResources().getStringArray(R.array.font_family_array)[Integer.parseInt(newValue.toString())]);
                return true;
            }
        });

        categoryFont.addPreference(listFontFamily);


        final ListPreference listTextWeight = new MaterialListPreference(getActivity());
        listTextWeight.setTitle(R.string.prefs_font_weight);
        listTextWeight.setDefaultValue(String.valueOf(TypefaceManager.TextWeight.NORMAL));
        listTextWeight.setSummary(getResources().getStringArray(R.array.text_weight_array)[TypefaceManager.getTextWeight()]);
        listTextWeight.setEntries(getResources().getStringArray(R.array.text_weight_array));
        listTextWeight.setEntryValues(new CharSequence[]{"0", "1", "2", "3", "4"});
        listTextWeight.setKey(TypefaceManager.PREF_KEY_TEXT_WEIGHT);
        listTextWeight.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                listTextWeight.setSummary(getResources().getStringArray(R.array.text_weight_array)[Integer.parseInt(newValue.toString())]);
                return true;
            }
        });

        categoryFont.addPreference(listTextWeight);



        PreferenceCategory categoryOnline = new MaterialPreferenceCategory(getActivity());
        categoryOnline.setTitle(getActivity().getString(R.string.prefs_visiblity));

        rootScreen.addPreference(categoryOnline);


//        CheckBoxPreference boxOnline = new CheckBoxPreference(getActivity());
//        boxOnline.setTitle(getActivity().getString(R.string.prefs_offline));
//        boxOnline.setDefaultValue(true);
//        boxOnline.setKey("offline");
//        boxOnline.setSummary(getActivity().getString(R.string.prefs_offline_description));

      //  categoryOnline.addPreference(boxOnline);

        MaterialListPreference listOnlineStatus = new MaterialListPreference(getActivity());
        listOnlineStatus.setTitle(getActivity().getString(R.string.prefs_online_status));
        listOnlineStatus.setKey("online_status");
        listOnlineStatus.setSummary(getActivity().getString(R.string.prefs_online_statu_description));
        listOnlineStatus.setEntries(R.array.online_status_array);
        listOnlineStatus.setEntryValues(new CharSequence[]{"off", "eternal", "phantom"});
        listOnlineStatus.setDefaultValue("off");
        listOnlineStatus.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                String s = (String) o;
                FileLogger.w("online_status", s);
                getActivity().startService(new Intent(getActivity(), EternallOnlineService.class).putExtra("online_status", (String) o));
                return true;
            }
        });
        categoryOnline.addPreference(listOnlineStatus);


        CheckBoxPreference boxTyping = new MaterialCheckBoxPreference(getActivity());
        boxTyping.setTitle(getActivity().getString(R.string.prefs_hide_typing));
        boxTyping.setDefaultValue(false);
        boxTyping.setKey("hide_typing");
        boxTyping.setSummary(getActivity().getString(R.string.prefs_hide_typing_description));

        categoryOnline.addPreference(boxTyping);

        PreferenceCategory categoryNotification = new MaterialPreferenceCategory(getActivity());
        categoryNotification.setTitle(getActivity().getString(R.string.prefs_notification));

        rootScreen.addPreference(categoryNotification);

        CheckBoxPreference boxEnableNotification = new MaterialCheckBoxPreference(getActivity());
        boxEnableNotification.setTitle(getActivity().getString(R.string.prefs_notification_enable));
        boxEnableNotification.setSummary(getActivity().getString(R.string.prefs_notification_enable_description));
        boxEnableNotification.setDefaultValue(true);
        boxEnableNotification.setKey("enable_notify");

        categoryNotification.addPreference(boxEnableNotification);



        final CheckBoxPreference boxNotificationVibrate = new MaterialCheckBoxPreference(getActivity());
        boxNotificationVibrate.setTitle(getActivity().getString(R.string.prefs_vibration));
        boxNotificationVibrate.setDefaultValue(true);
        boxNotificationVibrate.setEnabled(boxEnableNotification.getSharedPreferences().getBoolean("enable_notify", true));
        boxNotificationVibrate.setKey("enable_notify_vibrate");

        categoryNotification.addPreference(boxNotificationVibrate);


        final CheckBoxPreference boxNotificationLED = new MaterialCheckBoxPreference(getActivity());
        boxNotificationLED.setTitle(getActivity().getString(R.string.prefs_led_indicator));
        boxNotificationLED.setDefaultValue(true);
        boxNotificationLED.setEnabled(boxEnableNotification.getSharedPreferences().getBoolean("enable_notify", true));
        boxNotificationLED.setKey("enable_notify_led");

        categoryNotification.addPreference(boxNotificationLED);

        boxEnableNotification.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                boxNotificationLED.setEnabled((Boolean) newValue);
                boxNotificationVibrate.setEnabled((Boolean) newValue);
                return true;
            }
        });

        PreferenceCategory categoryLog = new MaterialPreferenceCategory(getActivity());
        categoryLog.setTitle(R.string.prefs_bug_report);

        rootScreen.addPreference(categoryLog);


        CheckBoxPreference boxEnableLog = new MaterialCheckBoxPreference(getActivity());
        boxEnableLog.setTitle(R.string.prefs_enable_log);
        boxEnableLog.setSummary(R.string.prefs_enable_log_description);
        boxEnableLog.setKey(AppLoader.KEY_WRITE_LOG);
        boxEnableLog.setDefaultValue(true);

        categoryLog.addPreference(boxEnableLog);

//        Preference prefSendLotToDev = new MaterialPreference(getActivity());
//        prefSendLotToDev.setTitle("Отправить логи разработчикам");
//        prefSendLotToDev.setSummary("В случае ошибки вы можете отправить нам логи, для дальнейшего исправления");
//
//        categoryLog.addPreference(prefSendLotToDev);


        Preference preferenceCleanUpLog = new MaterialPreference(getActivity());
        preferenceCleanUpLog.setTitle(R.string.prefs_clear_log);
        preferenceCleanUpLog.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                FileLogger.cleanup();
                return true;
            }
        });

        categoryLog.addPreference(preferenceCleanUpLog);



        PreferenceCategory categoryOther = new MaterialPreferenceCategory(getActivity());
        categoryOther.setTitle(getActivity().getString(R.string.prefs_other));

        rootScreen.addPreference(categoryOther);



        CheckBoxPreference boxSendFailedMsg = new MaterialCheckBoxPreference(getActivity());
        boxSendFailedMsg.setTitle(getActivity().getString(R.string.prefa_unstable_connection));
        boxSendFailedMsg.setSummary(getActivity().getString(R.string.prefa_unstable_connection_description));
        boxSendFailedMsg.setKey("resend_failed_msg");
        boxSendFailedMsg.setDefaultValue(true);

        categoryOther.addPreference(boxSendFailedMsg);


        MaterialListPreference listEncrypt = new MaterialListPreference(getActivity());
        listEncrypt.setTitle(R.string.prefs_message_encryption);
        listEncrypt.setSummary(R.string.prefs_message_encryption_description);
        listEncrypt.setEntries(new CharSequence[]{"Base64", "HEX", "MD5", "Text to Binary", "3DES", "String.hashCode"});
        listEncrypt.setEntryValues(new CharSequence[]{"base", "hex", "md5", "binary", "3des", "hashCode"});
        listEncrypt.setKey("encrypt_messages");
        listEncrypt.setDefaultValue("hex");
      //  listEncrypt.setValueIndex(1);

        categoryOther.addPreference(listEncrypt);


        PreferenceCategory categoryAbout = new MaterialPreferenceCategory(getActivity());
        categoryAbout.setTitle(getActivity().getString(R.string.prefs_about));

        rootScreen.addPreference(categoryAbout);

        Preference gitHubPreference = new MaterialPreference(getActivity());
        gitHubPreference.setTitle("Open Source");
        gitHubPreference.setSummary("Посмотреть исходный код проекта на GitHub");
        gitHubPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/EuphoriaDev/Euphoria-VK-Client"));
                startActivity(browserIntent);
                return true;
            }
        });
        categoryAbout.addPreference(gitHubPreference);


        Preference versionScreen = new MaterialPreference(getActivity());
        versionScreen.setTitle(getActivity().getString(R.string.prefs_version) + ": " + BuildConfig.VERSION_NAME);
        versionScreen.setSummary(getActivity().getString(R.string.click_to_update));
        versionScreen.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                checkUpdate();
                return true;
            }
        });
        categoryAbout.addPreference(versionScreen);

        Preference changelogSceen = new MaterialPreference(getActivity());
        changelogSceen.setTitle(getActivity().getString(R.string.prefs_changelog));
        changelogSceen.setSummary(getActivity().getString(R.string.prefs_changelog_description));
        changelogSceen.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final String title = getActivity().getResources().getString(R.string.change_log);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                        .setTitle(title)
                        .setPositiveButton("OK", null)
                        .setMessage(getActivity().getResources().getString(R.string.changelog));
                final AlertDialog alertDialog = builder.create();
                alertDialog.show();

                ViewUtil.setTypeface(alertDialog, title);
                // FileHelper.downloadFileAsync("http://cs612426.vk.me/u157582555/docs/da1604b960ce/MaterialDrawer-develop-1.zip1?extra=Gh1hZtTUZNqFzUvGkF79SV3b6Dyn-AaGRJaUluSXj6ateHzyRIHhwJL0I-5Gxm8GBbtFC4zpvv6QZgWcVk1Rmt2z5DyJh9g&dl=1");
                return false;
            }
        });

        categoryAbout.addPreference(changelogSceen);



        Preference groupScreen = new MaterialPreference(getActivity());
        groupScreen.setTitle("TimeVK/Euphoria for Android");
        groupScreen.setSummary(getActivity().getString(R.string.prefs_group_description));
        groupScreen.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Api api = Api.get();
                            Account account = new Account(getActivity());
                            account.restore();
                            if (account.access_token != null) {

                                final Boolean isMemberGroup = api.isGroupMember(59383198, api.getUserId());
                                if (!isMemberGroup) api.joinGroup(59383198, null, null);

                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String text = isMemberGroup ? getActivity().getString(R.string.already_in_group) : getActivity().getString(R.string.thank_you);
                                        AndroidUtils.showToast(getActivity(), text, true);
                                    }
                                });
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                    }
                }).start();
                return true;
            }
        });

        categoryAbout.addItemFromInflater(groupScreen);

    }

    @Override
    public void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if (isSetListView) return;;
            View rootView = getView();
            ListView list = (ListView) rootView.findViewById(android.R.id.list);
            if (list != null) {
//                list.setPadding(0,  0, 0, 0);

//                float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());
//                if (ThemeManagerOld.get(getActivity()).isLight()) {
//                    list.setDivider(new ColorDrawable(getActivity().getResources().getColor(R.color.material_drawer_divider)));
//                } else {
//                    list.setDivider(new ColorDrawable(getActivity().getResources().getColor(R.color.material_drawer_dark_divider)));
//                }
//                list.setDividerHeight((int) px);
            }
            isSetListView = true;
        }
    }

    private JSONObject loadJsonFromSite(String url) {
        BufferedReader reader;
        HttpURLConnection connection;
        StringBuilder builder;
        JSONObject result = null;

        try {
            connection = (HttpURLConnection) new java.net.URL(url).openConnection();
            connection.setUseCaches(false);
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), Charset.defaultCharset()));
            builder = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            connection.disconnect();
            reader.close();

            result = new JSONObject(builder.toString().trim());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }



    private void checkUpdate() {
        ThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final JSONObject json = loadJsonFromSite("http://timeteam.3dn.ru/timevk_up.txt");
                    if (BuildConfig.VERSION_CODE < json.optInt("version_code")) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle(getActivity().getString(R.string.update))
                                        .setMessage(getActivity().getString(R.string.found_new_version) + json.optString("version") + "\n" + getActivity().getString(R.string.download_ask))
                                        .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                String url = json.optString("url");
                                                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                                                //  request.setDescription("Some descrition");
                                                request.setTitle("Euphoria.apk");
                                                request.setMimeType("application/vnd.android.package-archive");
                                                // in order for this if to run, you must use the android 3.2 to compile your app
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                                    request.allowScanningByMediaScanner();
                                                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                                }
                                                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Euphoria.apk");
                                                // get download service and enqueue file
                                                DownloadManager manager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
                                                manager.enqueue(request);
                                            }
                                        }).create().show();
                            }
                        });
                    } else {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.no_updates), Toast.LENGTH_LONG).show();
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
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        if (key.equals(TypefaceManager.PREF_KEY_FONT_FAMILY) || key.equals(TypefaceManager.PREF_KEY_TEXT_WEIGHT)) {

            // he should update views for typeface
            ViewUtil.refreshViewsForTypeface();
        }
        Log.w("onSharedPreference", key);
    }

}
