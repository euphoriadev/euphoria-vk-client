package ru.euphoriadev.vk;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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

import ru.euphoriadev.vk.api.Api;
import ru.euphoriadev.vk.service.EternallOnlineService;
import ru.euphoriadev.vk.util.Account;
import ru.euphoriadev.vk.util.AndroidUtils;
import ru.euphoriadev.vk.util.CrashManager;
import ru.euphoriadev.vk.util.PrefManager;
import ru.euphoriadev.vk.util.ThemeManager;
import ru.euphoriadev.vk.util.TypefaceManager;
import ru.euphoriadev.vk.util.ViewUtil;
import ru.euphoriadev.vk.view.colorpicker.ColorPickerDialog;
import ru.euphoriadev.vk.view.colorpicker.ColorPickerSwatch;
import ru.euphoriadev.vk.view.pref.MaterialCheckBoxPreference;
import ru.euphoriadev.vk.view.pref.MaterialListPreference;
import ru.euphoriadev.vk.view.pref.MaterialPreference;
import ru.euphoriadev.vk.view.pref.MaterialPreferenceCategory;
import ru.euphoriadev.vk.view.pref.MaterialSwitchPreference;
import ru.euphoriadev.vk.view.pref.ProgressBarPreference;

/**
 * Created by Igor on 28.02.15.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    /** Preference keys. Making */
    public static final String KEY_IS_NIGHT_MODE = "is_night_theme";
    public static final String KEY_COLOR_IN_MESSAGES = "color_in_messages";
    public static final String KEY_COLOR_OUT_MESSAGES = "color_out_messages";
    public static final String KEY_MAKING_DRAWER_HEADER = "drawer_header";
    public static final String KEY_BLUR_RADIUS = "blur_radius";
    public static final String KEY_SHOW_DIVIDER = "show_divider";
    public static final String KEY_USE_TWO_PROFILE = "use_two_profile";
    public static final String KEY_USE_CAT_ICON_SEND = "use_cat_icon_send";
    public static final String KEY_FORCED_LOCALE = "forced_locale";
    public static final String KEY_USE_SYSTEM_EMOJI = "use_system_emoji";

    /** Font keys */
    public static final String KEY_FONT_FAMILY = "font_family";
    public static final String KEY_TEXT_WEIGHT = "text_weight";

    /** Online status keys */
    public static final String KEY_ONLINE_STATUS = "online_status";
    public static final String KEY_HIDE_TYPING = "hide_typing";

    /** Notifications keys */
    public static final String KEY_ENABLE_NOTIFY = "enable_notify";
    public static final String KEY_ENABLE_NOTIFY_VIBRATE = "enable_notify_vibrate";
    public static final String KEY_ENABLE_NOTIFY_LED = "enable_notify_led";

    /** Other keys */
    public static final String KEY_WRITE_LOG = "write_log";
    public static final String KEY_RESEND_FAILED_MESSAGES = "resend_failed_msg";
    public static final String KEY_ENCRYPT_MESSAGES = "encrypt_messages";
    public static final String KEY_CHECK_UPDATE = "auto_update";
    public static final String KEY_USE_ALTERNATIVE_UPDATE_MESSAGES = "use_alternative_update_messages";

    /** Web url for check updates this app */
    public static final String UPDATE_URL = "http://timeteam.3dn.ru/timevk_up.txt";
    public static final String LAST_UPDATE_TIME = "last_update_time";

    boolean isSetListView;
    PreferenceScreen rootScreen;

    public SettingsFragment() {
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
        boxNightTheme.setKey(KEY_IS_NIGHT_MODE);
        boxNightTheme.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                ThemeManager.setDarkTheme((Boolean) o);
                ThemeManager.updateThemeValues();
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
        boxColorBubble.setKey(KEY_COLOR_IN_MESSAGES);
        boxColorBubble.setSummary(getActivity().getString(R.string.prefs_color_in_msg_description));
        boxColorBubble.setDefaultValue(true);

        categoryUI.addPreference(boxColorBubble);


        CheckBoxPreference boxColorBubble2 = new MaterialCheckBoxPreference(getActivity());
        boxColorBubble2.setTitle(getActivity().getString(R.string.prefs_color_out_msg));
        boxColorBubble2.setKey(KEY_COLOR_OUT_MESSAGES);
        boxColorBubble2.setSummary(getActivity().getString(R.string.prefs_color_out_msg_description));
        boxColorBubble2.setDefaultValue(false);

        categoryUI.addPreference(boxColorBubble2);


        ListPreference listHeaderDrawer = new MaterialListPreference(getActivity());
        listHeaderDrawer.setTitle(getResources().getString(R.string.prefs_drawer));
        listHeaderDrawer.setSummary(getResources().getString(R.string.prefs_drawer_description));
        listHeaderDrawer.setKey(KEY_MAKING_DRAWER_HEADER);
        listHeaderDrawer.setEntries(R.array.prefs_drawer_header_array);
        listHeaderDrawer.setEntryValues(new CharSequence[]{"0", "1", "2"});
        listHeaderDrawer.setDefaultValue("0");

        categoryUI.addPreference(listHeaderDrawer);

        ProgressBarPreference blurRadiusPreference = new ProgressBarPreference(getActivity());
        blurRadiusPreference.setTitle(R.string.pref_blur_radius);
        blurRadiusPreference.setSummary(R.string.pref_blur_radius_description);
        blurRadiusPreference.setEnabled(PrefManager.getString(KEY_MAKING_DRAWER_HEADER).equalsIgnoreCase("2"));
        blurRadiusPreference.setKey(KEY_BLUR_RADIUS);
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
        boxDivider.setDefaultValue(true);
        boxDivider.setKey(KEY_SHOW_DIVIDER);


        categoryUI.addPreference(boxDivider);

        CheckBoxPreference boxTwoProfile = new MaterialCheckBoxPreference(getActivity());
        boxTwoProfile.setTitle(getActivity().getString(R.string.prefs_use_second_profile));
        boxTwoProfile.setKey(KEY_USE_TWO_PROFILE);
        boxTwoProfile.setEnabled(false);
        boxTwoProfile.setSummary(getActivity().getString(R.string.prefs_use_second_profile_description));
        boxTwoProfile.setDefaultValue(false);

        categoryUI.addPreference(boxTwoProfile);

        CheckBoxPreference boxUseCatIcon = new MaterialCheckBoxPreference(getActivity());
        boxUseCatIcon.setTitle(getActivity().getString(R.string.prefs_use_cat_icon_send));
        boxUseCatIcon.setSummary(getActivity().getString(R.string.prefs_use_cat_icon_send_description));
        boxUseCatIcon.setKey(KEY_USE_CAT_ICON_SEND);
        boxUseCatIcon.setDefaultValue(false);

        categoryUI.addPreference(boxUseCatIcon);

        CheckBoxPreference boxSystemEmoji = new MaterialCheckBoxPreference(getActivity());
        boxSystemEmoji.setTitle(R.string.prefs_use_system_emoji);
        boxSystemEmoji.setSummary(R.string.prefs_use_system_emoji_description);
        boxSystemEmoji.setKey(KEY_USE_SYSTEM_EMOJI);
        boxSystemEmoji.setDefaultValue(true);

        categoryUI.addPreference(boxSystemEmoji);


        ListPreference listSelectLocale = new MaterialListPreference(getActivity());
        listSelectLocale.setTitle(getActivity().getString(R.string.prefs_select_locale));
        listSelectLocale.setSummary(getActivity().getString(R.string.prefs_select_locale_description));
        listSelectLocale.setEntries(new String[]{"Русский", "English", "Дореволюцiонный", "Українська"});
        listSelectLocale.setEntryValues(new CharSequence[]{"ru", "en", "cu", "uk"});
        listSelectLocale.setKey(KEY_FORCED_LOCALE);

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
        listFontFamily.setKey(KEY_FONT_FAMILY);
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
        listTextWeight.setKey(KEY_TEXT_WEIGHT);
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


        MaterialListPreference listOnlineStatus = new MaterialListPreference(getActivity());
        listOnlineStatus.setTitle(getActivity().getString(R.string.prefs_online_status));
        listOnlineStatus.setKey(KEY_ONLINE_STATUS);
        listOnlineStatus.setSummary(getActivity().getString(R.string.prefs_online_statu_description));
        listOnlineStatus.setEntries(R.array.online_status_array);
        listOnlineStatus.setEntryValues(new CharSequence[]{"off", "eternal", "phantom"});
        listOnlineStatus.setDefaultValue("off");
        listOnlineStatus.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                String s = (String) o;
                Log.w(KEY_ONLINE_STATUS, s);
                getActivity().startService(new Intent(getActivity(), EternallOnlineService.class).putExtra("online_status", (String) o));
                return true;
            }
        });
        categoryOnline.addPreference(listOnlineStatus);


        CheckBoxPreference boxTyping = new MaterialCheckBoxPreference(getActivity());
        boxTyping.setTitle(getActivity().getString(R.string.prefs_hide_typing));
        boxTyping.setDefaultValue(false);
        boxTyping.setKey(KEY_HIDE_TYPING);
        boxTyping.setSummary(getActivity().getString(R.string.prefs_hide_typing_description));

        categoryOnline.addPreference(boxTyping);

        PreferenceCategory categoryNotification = new MaterialPreferenceCategory(getActivity());
        categoryNotification.setTitle(getActivity().getString(R.string.prefs_notification));

        rootScreen.addPreference(categoryNotification);

        CheckBoxPreference boxEnableNotification = new MaterialCheckBoxPreference(getActivity());
        boxEnableNotification.setTitle(getActivity().getString(R.string.prefs_notification_enable));
        boxEnableNotification.setSummary(getActivity().getString(R.string.prefs_notification_enable_description));
        boxEnableNotification.setDefaultValue(true);
        boxEnableNotification.setKey(KEY_ENABLE_NOTIFY);

        categoryNotification.addPreference(boxEnableNotification);


        final CheckBoxPreference boxNotificationVibrate = new MaterialCheckBoxPreference(getActivity());
        boxNotificationVibrate.setTitle(getActivity().getString(R.string.prefs_vibration));
        boxNotificationVibrate.setDefaultValue(true);
        boxNotificationVibrate.setEnabled(boxEnableNotification.getSharedPreferences().getBoolean("enable_notify", true));
        boxNotificationVibrate.setKey(KEY_ENABLE_NOTIFY_VIBRATE);

        categoryNotification.addPreference(boxNotificationVibrate);


        final CheckBoxPreference boxNotificationLED = new MaterialCheckBoxPreference(getActivity());
        boxNotificationLED.setTitle(getActivity().getString(R.string.prefs_led_indicator));
        boxNotificationLED.setDefaultValue(true);
        boxNotificationLED.setEnabled(boxEnableNotification.getSharedPreferences().getBoolean("enable_notify", true));
        boxNotificationLED.setKey(KEY_ENABLE_NOTIFY_LED);

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
        boxEnableLog.setKey(KEY_WRITE_LOG);
        boxEnableLog.setDefaultValue(true);

        categoryLog.addPreference(boxEnableLog);


        Preference preferenceCleanUpLog = new MaterialPreference(getActivity());
        preferenceCleanUpLog.setTitle(R.string.prefs_clear_log);
        preferenceCleanUpLog.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                CrashManager.cleanup();
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
        boxSendFailedMsg.setKey(KEY_RESEND_FAILED_MESSAGES);
        boxSendFailedMsg.setDefaultValue(true);

        categoryOther.addPreference(boxSendFailedMsg);


        MaterialListPreference listEncrypt = new MaterialListPreference(getActivity());
        listEncrypt.setTitle(R.string.prefs_message_encryption);
        listEncrypt.setSummary(R.string.prefs_message_encryption_description);
        listEncrypt.setEntries(new CharSequence[]{"Base64", "HEX", "MD5", "Text to Binary", "3DES", "String.hashCode"});
        listEncrypt.setEntryValues(new CharSequence[]{"base", "hex", "md5", "binary", "3des", "hashCode"});
        listEncrypt.setKey(KEY_ENCRYPT_MESSAGES);
        listEncrypt.setDefaultValue("hex");
        //  listEncrypt.setValueIndex(1);

        categoryOther.addPreference(listEncrypt);


        CheckBoxPreference autoUpdatePreference = new MaterialCheckBoxPreference(getActivity());
        autoUpdatePreference.setTitle(R.string.prefs_check_auto_update);
        autoUpdatePreference.setSummary(R.string.prefs_check_auto_update_description);
        autoUpdatePreference.setKey(KEY_CHECK_UPDATE);
        autoUpdatePreference.setDefaultValue(true);

        categoryOther.addPreference(autoUpdatePreference);

        PreferenceCategory categoryAbout = new MaterialPreferenceCategory(getActivity());
        categoryAbout.setTitle(getActivity().getString(R.string.prefs_about));

        rootScreen.addPreference(categoryAbout);


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

        categoryAbout.addPreference(groupScreen);

        if (BuildConfig.DEBUG) {
            PreferenceCategory debugCategory = new MaterialPreferenceCategory(getActivity());
            debugCategory.setTitle("For developers");

            rootScreen.addPreference(debugCategory);


            Preference debugPreference = new MaterialPreference(getActivity());
            debugPreference.setTitle("Debug Activity");
            debugPreference.setSummary("Open a TestActivity.class");
            debugPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getActivity(), TestActivity.class));
                    return true;
                }
            });
            debugCategory.addPreference(debugPreference);
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if (isSetListView) return;
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



    private void checkUpdate() {
        AndroidUtils.checkUpdate(getActivity(), true);
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
