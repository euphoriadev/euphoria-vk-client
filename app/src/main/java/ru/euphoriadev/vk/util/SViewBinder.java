package ru.euphoriadev.vk.util;

import android.content.Context;
import android.view.View;
import android.widget.SimpleAdapter;

/**
 * Created by user on 29.03.15.
 */
public class SViewBinder implements SimpleAdapter.ViewBinder {

    Context context;
    ThemeManagerOld manager;

    public SViewBinder(Context context) {
        this.context = context;
        manager = new ThemeManagerOld(this.context);
    }


    @Override
    public boolean setViewValue(View view, Object data, String textRepresentation) {

        String read_state;

//        switch (view.getId()) {
//            case R.id.lLayout_dialog:
//                read_state = (String) data;
//
//                if (read_state.equalsIgnoreCase("0"))
//                    view.setBackgroundColor(context.getResources().getColor(R.color.translucent_white));
//                else if (read_state.equalsIgnoreCase("1")) view.setBackgroundColor(Color.TRANSPARENT);
//
//                return true;
//
//
//        }

        return false;
    }
}