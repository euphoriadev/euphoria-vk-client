package ru.euphoriadev.vk.util;

import android.app.Activity;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.util.Log;

/**
 * Created by user on 17.02.16.
 */
public class PermissionAllower {
    private static final String TAG = "Euphoria.Permission";

    /** Request code for successful granted permission */
    public static final int PERMISSION_GRANTED_CODE = 100;

    /**
     * Gives the specified permission to this app, if it is not granted
     *
     * @param activity       the activity for accessing resources
     * @param permissionName the permission name to check
     * @see android.Manifest.permission
     */
    public static void allowPermission(Activity activity, String permissionName) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        // if permission is still not granted
        if (PermissionChecker.checkSelfPermission(activity, permissionName) != PermissionChecker.PERMISSION_GRANTED) {
            // try get access for permission
            ActivityCompat.requestPermissions(activity,
                    new String[]{
                            permissionName
                    }, PERMISSION_GRANTED_CODE);
        } else {
            Log.i(TAG, "Permission " + permissionName + " is granted");
        }
    }
}
