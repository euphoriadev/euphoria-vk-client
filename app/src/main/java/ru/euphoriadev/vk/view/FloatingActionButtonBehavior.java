package ru.euphoriadev.vk.view;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import ru.euphoriadev.vk.view.fab.FloatingActionButton;

/**
 * Created by Igor on 03.12.15.
 */
public class FloatingActionButtonBehavior extends
        CoordinatorLayout.Behavior<ru.euphoriadev.vk.view.fab.FloatingActionButton> {

    public FloatingActionButtonBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
        Log.w("Behavior", "layoutDependsOn");
        return dependency instanceof Snackbar.SnackbarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
        float translationY = Math.min(0,
                dependency.getTranslationY() - dependency.getHeight());
        child.setTranslationY(translationY);

        Log.w("Behavior", "onDependentViewChanged");

        return true;
    }
}
