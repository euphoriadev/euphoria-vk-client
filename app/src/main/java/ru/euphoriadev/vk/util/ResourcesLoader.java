package ru.euphoriadev.vk.util;

import android.content.Context;

import ru.euphoriadev.vk.R;

import static ru.euphoriadev.vk.R.color.*;

/**
 * Created by Igor on 05.02.16.
 *
 * Simple class for load resources
 */
public class ResourcesLoader {
    public static final SimpleSparseArray sColors = new SimpleSparseArray(200);
    private static boolean isLoadedColors;

    public static void loadColors(Context context) {
        if (isLoadedColors) {
            return;
        }
        isLoadedColors = true;

        // Red
        sColors.put(md_red_50,  getColor(context, R.color.md_red_50));
        sColors.put(md_red_100, getColor(context, R.color.md_red_100));
        sColors.put(md_red_200, getColor(context, R.color.md_red_200));
        sColors.put(md_red_300, getColor(context, R.color.md_red_300));
        sColors.put(md_red_400, getColor(context, R.color.md_red_400));
        sColors.put(md_red_500, getColor(context, R.color.md_red_500));
        sColors.put(md_red_600, getColor(context, R.color.md_red_600));
        sColors.put(md_red_700, getColor(context, R.color.md_red_700));
        sColors.put(md_red_800, getColor(context, R.color.md_red_800));
        sColors.put(md_red_900, getColor(context, R.color.md_red_900));

        // Pink
        sColors.put(md_pink_50,  getColor(context, R.color.md_pink_50));
        sColors.put(md_pink_100, getColor(context, R.color.md_pink_100));
        sColors.put(md_pink_200, getColor(context, R.color.md_pink_200));
        sColors.put(md_pink_300, getColor(context, R.color.md_pink_300));
        sColors.put(md_pink_400, getColor(context, R.color.md_pink_400));
        sColors.put(md_pink_500, getColor(context, R.color.md_pink_500));
        sColors.put(md_pink_600, getColor(context, R.color.md_pink_600));
        sColors.put(md_pink_700, getColor(context, R.color.md_pink_700));
        sColors.put(md_pink_800, getColor(context, R.color.md_pink_800));
        sColors.put(md_pink_900, getColor(context, R.color.md_pink_900));

        // Purple
        sColors.put(md_purple_50,  getColor(context, R.color.md_purple_50));
        sColors.put(md_purple_100, getColor(context, R.color.md_purple_100));
        sColors.put(md_purple_200, getColor(context, R.color.md_purple_200));
        sColors.put(md_purple_300, getColor(context, R.color.md_purple_300));
        sColors.put(md_purple_400, getColor(context, R.color.md_purple_400));
        sColors.put(md_purple_500, getColor(context, R.color.md_purple_500));
        sColors.put(md_purple_600, getColor(context, R.color.md_purple_600));
        sColors.put(md_purple_700, getColor(context, R.color.md_purple_700));
        sColors.put(md_purple_800, getColor(context, R.color.md_purple_800));
        sColors.put(md_purple_900, getColor(context, R.color.md_purple_900));

        // Deep Purple
        sColors.put(md_deep_purple_50,  getColor(context, R.color.md_deep_purple_50));
        sColors.put(md_deep_purple_100, getColor(context, R.color.md_deep_purple_100));
        sColors.put(md_deep_purple_200, getColor(context, R.color.md_deep_purple_200));
        sColors.put(md_deep_purple_300, getColor(context, R.color.md_deep_purple_300));
        sColors.put(md_deep_purple_400, getColor(context, R.color.md_deep_purple_400));
        sColors.put(md_deep_purple_500, getColor(context, R.color.md_deep_purple_500));
        sColors.put(md_deep_purple_600, getColor(context, R.color.md_deep_purple_600));
        sColors.put(md_deep_purple_700, getColor(context, R.color.md_deep_purple_700));
        sColors.put(md_deep_purple_800, getColor(context, R.color.md_deep_purple_800));
        sColors.put(md_deep_purple_900, getColor(context, R.color.md_deep_purple_900));

        // Indigo
        sColors.put(md_indigo_50, getColor(context, R.color.md_indigo_50));
        sColors.put(md_indigo_100, getColor(context, R.color.md_indigo_100));
        sColors.put(md_indigo_200, getColor(context, R.color.md_indigo_200));
        sColors.put(md_indigo_300, getColor(context, R.color.md_indigo_300));
        sColors.put(md_indigo_400, getColor(context, R.color.md_indigo_400));
        sColors.put(md_indigo_500, getColor(context, R.color.md_indigo_500));
        sColors.put(md_indigo_600, getColor(context, R.color.md_indigo_600));
        sColors.put(md_indigo_700, getColor(context, R.color.md_indigo_700));
        sColors.put(md_indigo_800, getColor(context, R.color.md_indigo_800));
        sColors.put(md_indigo_900, getColor(context, R.color.md_indigo_900));

        // Blue
        sColors.put(md_blue_50,  getColor(context, R.color.md_blue_50));
        sColors.put(md_blue_100, getColor(context, R.color.md_blue_100));
        sColors.put(md_blue_200, getColor(context, R.color.md_blue_200));
        sColors.put(md_blue_300, getColor(context, R.color.md_blue_300));
        sColors.put(md_blue_400, getColor(context, R.color.md_blue_400));
        sColors.put(md_blue_500, getColor(context, R.color.md_blue_500));
        sColors.put(md_blue_600, getColor(context, R.color.md_blue_600));
        sColors.put(md_blue_700, getColor(context, R.color.md_blue_700));
        sColors.put(md_blue_800, getColor(context, R.color.md_blue_800));
        sColors.put(md_blue_900, getColor(context, R.color.md_blue_900));

        // Light Blue
        sColors.put(md_light_blue_50,  getColor(context, R.color.md_light_blue_50));
        sColors.put(md_light_blue_100, getColor(context, R.color.md_light_blue_100));
        sColors.put(md_light_blue_200, getColor(context, R.color.md_light_blue_200));
        sColors.put(md_light_blue_300, getColor(context, R.color.md_light_blue_300));
        sColors.put(md_light_blue_400, getColor(context, R.color.md_light_blue_400));
        sColors.put(md_light_blue_500, getColor(context, R.color.md_light_blue_500));
        sColors.put(md_light_blue_600, getColor(context, R.color.md_light_blue_600));
        sColors.put(md_light_blue_700, getColor(context, R.color.md_light_blue_700));
        sColors.put(md_light_blue_800, getColor(context, R.color.md_light_blue_800));
        sColors.put(md_light_blue_900, getColor(context, R.color.md_light_blue_900));

        // Cyan
        sColors.put(md_cyan_50,  getColor(context, R.color.md_cyan_50));
        sColors.put(md_cyan_100, getColor(context, R.color.md_cyan_100));
        sColors.put(md_cyan_200, getColor(context, R.color.md_cyan_200));
        sColors.put(md_cyan_300, getColor(context, R.color.md_cyan_300));
        sColors.put(md_cyan_400, getColor(context, R.color.md_cyan_400));
        sColors.put(md_cyan_500, getColor(context, R.color.md_cyan_500));
        sColors.put(md_cyan_600, getColor(context, R.color.md_cyan_600));
        sColors.put(md_cyan_700, getColor(context, R.color.md_cyan_700));
        sColors.put(md_cyan_800, getColor(context, R.color.md_cyan_800));
        sColors.put(md_cyan_900, getColor(context, R.color.md_cyan_900));

        // Teal
        sColors.put(md_teal_50,  getColor(context, R.color.md_teal_50));
        sColors.put(md_teal_100, getColor(context, R.color.md_teal_100));
        sColors.put(md_teal_200, getColor(context, R.color.md_teal_200));
        sColors.put(md_teal_300, getColor(context, R.color.md_teal_300));
        sColors.put(md_teal_400, getColor(context, R.color.md_teal_400));
        sColors.put(md_teal_500, getColor(context, R.color.md_teal_500));
        sColors.put(md_teal_600, getColor(context, R.color.md_teal_600));
        sColors.put(md_teal_700, getColor(context, R.color.md_teal_700));
        sColors.put(md_teal_800, getColor(context, R.color.md_teal_800));
        sColors.put(md_teal_900, getColor(context, R.color.md_teal_900));

        // Green
        sColors.put(md_green_50,  getColor(context, R.color.md_green_50));
        sColors.put(md_green_100, getColor(context, R.color.md_green_100));
        sColors.put(md_green_200, getColor(context, R.color.md_green_200));
        sColors.put(md_green_300, getColor(context, R.color.md_green_300));
        sColors.put(md_green_400, getColor(context, R.color.md_green_400));
        sColors.put(md_green_500, getColor(context, R.color.md_green_500));
        sColors.put(md_green_600, getColor(context, R.color.md_green_600));
        sColors.put(md_green_700, getColor(context, R.color.md_green_700));
        sColors.put(md_green_800, getColor(context, R.color.md_green_800));
        sColors.put(md_green_900, getColor(context, R.color.md_green_900));

        // Light Green
        sColors.put(md_light_green_50,  getColor(context, R.color.md_light_green_50));
        sColors.put(md_light_green_100, getColor(context, R.color.md_light_green_100));
        sColors.put(md_light_green_200, getColor(context, R.color.md_light_green_200));
        sColors.put(md_light_green_300, getColor(context, R.color.md_light_green_300));
        sColors.put(md_light_green_400, getColor(context, R.color.md_light_green_400));
        sColors.put(md_light_green_500, getColor(context, R.color.md_light_green_500));
        sColors.put(md_light_green_600, getColor(context, R.color.md_light_green_600));
        sColors.put(md_light_green_700, getColor(context, R.color.md_light_green_700));
        sColors.put(md_light_green_800, getColor(context, R.color.md_light_green_800));
        sColors.put(md_light_green_900, getColor(context, R.color.md_light_green_900));

        // Lime
        sColors.put(md_lime_50,  getColor(context, R.color.md_lime_50));
        sColors.put(md_lime_100, getColor(context, R.color.md_lime_100));
        sColors.put(md_lime_200, getColor(context, R.color.md_lime_200));
        sColors.put(md_lime_300, getColor(context, R.color.md_lime_300));
        sColors.put(md_lime_400, getColor(context, R.color.md_lime_400));
        sColors.put(md_lime_500, getColor(context, R.color.md_lime_500));
        sColors.put(md_lime_600, getColor(context, R.color.md_lime_600));
        sColors.put(md_lime_700, getColor(context, R.color.md_lime_700));
        sColors.put(md_lime_800, getColor(context, R.color.md_lime_800));
        sColors.put(md_lime_900, getColor(context, R.color.md_lime_900));

        // Yellow
        sColors.put(md_yellow_50,  getColor(context, R.color.md_yellow_50));
        sColors.put(md_yellow_100, getColor(context, R.color.md_yellow_100));
        sColors.put(md_yellow_200, getColor(context, R.color.md_yellow_200));
        sColors.put(md_yellow_300, getColor(context, R.color.md_yellow_300));
        sColors.put(md_yellow_400, getColor(context, R.color.md_yellow_400));
        sColors.put(md_yellow_500, getColor(context, R.color.md_yellow_500));
        sColors.put(md_yellow_600, getColor(context, R.color.md_yellow_600));
        sColors.put(md_yellow_700, getColor(context, R.color.md_yellow_700));
        sColors.put(md_yellow_800, getColor(context, R.color.md_yellow_800));
        sColors.put(md_yellow_900, getColor(context, R.color.md_yellow_900));

        // Amber
        sColors.put(md_amber_50,  getColor(context, R.color.md_amber_50));
        sColors.put(md_amber_100, getColor(context, R.color.md_amber_100));
        sColors.put(md_amber_200, getColor(context, R.color.md_amber_200));
        sColors.put(md_amber_300, getColor(context, R.color.md_amber_300));
        sColors.put(md_amber_400, getColor(context, R.color.md_amber_400));
        sColors.put(md_amber_500, getColor(context, R.color.md_amber_500));
        sColors.put(md_amber_600, getColor(context, R.color.md_amber_600));
        sColors.put(md_amber_700, getColor(context, R.color.md_amber_700));
        sColors.put(md_amber_800, getColor(context, R.color.md_amber_800));
        sColors.put(md_amber_900, getColor(context, R.color.md_amber_900));

        // Orange
        sColors.put(md_orange_50,  getColor(context, R.color.md_orange_50));
        sColors.put(md_orange_100, getColor(context, R.color.md_orange_100));
        sColors.put(md_orange_200, getColor(context, R.color.md_orange_200));
        sColors.put(md_orange_300, getColor(context, R.color.md_orange_300));
        sColors.put(md_orange_400, getColor(context, R.color.md_orange_400));
        sColors.put(md_orange_500, getColor(context, R.color.md_orange_500));
        sColors.put(md_orange_600, getColor(context, R.color.md_orange_600));
        sColors.put(md_orange_700, getColor(context, R.color.md_orange_700));
        sColors.put(md_orange_800, getColor(context, R.color.md_orange_800));
        sColors.put(md_orange_900, getColor(context, R.color.md_orange_900));

        // Deep Orange
        sColors.put(md_deep_orange_50,  getColor(context, R.color.md_deep_orange_50));
        sColors.put(md_deep_orange_100, getColor(context, R.color.md_deep_orange_100));
        sColors.put(md_deep_orange_200, getColor(context, R.color.md_deep_orange_200));
        sColors.put(md_deep_orange_300, getColor(context, R.color.md_deep_orange_300));
        sColors.put(md_deep_orange_400, getColor(context, R.color.md_deep_orange_400));
        sColors.put(md_deep_orange_500, getColor(context, R.color.md_deep_orange_500));
        sColors.put(md_deep_orange_600, getColor(context, R.color.md_deep_orange_600));
        sColors.put(md_deep_orange_700, getColor(context, R.color.md_deep_orange_700));
        sColors.put(md_deep_orange_800, getColor(context, R.color.md_deep_orange_800));
        sColors.put(md_deep_orange_900, getColor(context, R.color.md_deep_orange_900));

        // Brown
        sColors.put(md_brown_50,  getColor(context, R.color.md_brown_50));
        sColors.put(md_brown_100, getColor(context, R.color.md_brown_100));
        sColors.put(md_brown_200, getColor(context, R.color.md_brown_200));
        sColors.put(md_brown_300, getColor(context, R.color.md_brown_300));
        sColors.put(md_brown_400, getColor(context, R.color.md_brown_400));
        sColors.put(md_brown_500, getColor(context, R.color.md_brown_500));
        sColors.put(md_brown_600, getColor(context, R.color.md_brown_600));
        sColors.put(md_brown_700, getColor(context, R.color.md_brown_700));
        sColors.put(md_brown_800, getColor(context, R.color.md_brown_800));
        sColors.put(md_brown_900, getColor(context, R.color.md_brown_900));

        // Grey
        sColors.put(md_grey_50,  getColor(context, R.color.md_grey_50));
        sColors.put(md_grey_100, getColor(context, R.color.md_grey_100));
        sColors.put(md_grey_200, getColor(context, R.color.md_grey_200));
        sColors.put(md_grey_300, getColor(context, R.color.md_grey_300));
        sColors.put(md_grey_400, getColor(context, R.color.md_grey_400));
        sColors.put(md_grey_500, getColor(context, R.color.md_grey_500));
        sColors.put(md_grey_600, getColor(context, R.color.md_grey_600));
        sColors.put(md_grey_700, getColor(context, R.color.md_grey_700));
        sColors.put(md_grey_800, getColor(context, R.color.md_grey_800));
        sColors.put(md_grey_900, getColor(context, R.color.md_grey_900));

        // Blue Grey
        sColors.put(md_blue_grey_50,  getColor(context, R.color.md_blue_grey_50));
        sColors.put(md_blue_grey_100, getColor(context, R.color.md_blue_grey_100));
        sColors.put(md_blue_grey_200, getColor(context, R.color.md_blue_grey_200));
        sColors.put(md_blue_grey_300, getColor(context, R.color.md_blue_grey_300));
        sColors.put(md_blue_grey_400, getColor(context, R.color.md_blue_grey_400));
        sColors.put(md_blue_grey_500, getColor(context, R.color.md_blue_grey_500));
        sColors.put(md_blue_grey_600, getColor(context, R.color.md_blue_grey_600));
        sColors.put(md_blue_grey_700, getColor(context, R.color.md_blue_grey_700));
        sColors.put(md_blue_grey_800, getColor(context, R.color.md_blue_grey_800));
        sColors.put(md_blue_grey_900, getColor(context, R.color.md_blue_grey_900));
    }

    public static int getColor(int resId) {
        if (!isLoadedColors) {
            throw new IllegalArgumentException("Colors is not loaded!");
        }
        int color = sColors.get(resId);
        if (color == -1) {
            sColors.put(resId, getColor(AppLoader.appContext, resId));
            return sColors.get(resId);
        }
        return color;
    }


    public static int[][] getThemeColoursPalette(Context context) {
        loadColors(context);

        int[][] colours = new int[sColors.size() / 10][10];
        int countColor = 0;
        for (int i = 0; i < colours.length; i++) {
            for (int j = 0; j < 10; j++) {
                colours[i][j] = sColors.valueAt(countColor++);
            }
        }
        return colours;
    }

    private static int getColor(Context context, int colorRes) {
        return AndroidUtils.getColor(context, colorRes);
    }

}
