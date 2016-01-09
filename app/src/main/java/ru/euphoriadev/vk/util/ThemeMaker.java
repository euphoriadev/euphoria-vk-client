package ru.euphoriadev.vk.util;

import android.content.Context;
import android.content.res.Resources;

import org.xmlpull.v1.XmlPullParser;

import java.io.File;

import ru.euphoriadev.vk.R;

/**
 * Created by Igor on 20.11.15.
 */
public class ThemeMaker {
    private Context context;


    public ThemeMaker(Context c) {
        this.context = c;
    }

    public String parseTheme(File fileTheme) {
        XmlPullParser parser = context.getResources().getXml(R.xml.prefs);

        //... wait for updates
        return "";
    }
}
