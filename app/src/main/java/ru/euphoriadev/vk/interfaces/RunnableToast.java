package ru.euphoriadev.vk.interfaces;

import android.content.Context;
import android.widget.Toast;

import ru.euphoriadev.vk.common.AppLoader;

/**
 * Created by Igor on 02.02.16.
 * <p/>
 * Toast for async show
 */
public class RunnableToast implements Runnable {
    private Context context;
    private String message;
    private boolean longShow;

    /**
     * Create a new runnable with toast config
     *
     * @param context  The context to use.  Usually your {@link android.app.Application}
     *                 or {@link android.app.Activity} object.
     * @param message  the text to show.  Can be formatted text.
     * @param longShow show a message with a large period of time
     */
    public RunnableToast(Context context, String message, boolean longShow) {
        this.context = context;
        this.message = message;
        this.longShow = longShow;

    }

    /**
     * Create a new runnable with toast config
     *
     * @param context  The context to use.  Usually your {@link android.app.Application}
     *                 or {@link android.app.Activity} object.
     * @param resId    the resource id of the string resource to use.
     *                 Can be formatted text.
     * @param longShow show a message with a large period of time
     */
    public RunnableToast(Context context, int resId, boolean longShow) {
        this(context, context.getResources().getString(resId), longShow);
    }

    @Override
    public void run() {
        Toast.makeText(context == null ? AppLoader.appContext : context, message, longShow ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT)
                .show();
    }
}
