package ru.euphoriadev.vk;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;

/**
 * Created by Igor on 15.03.16.
 */
public class FileManagerActivity extends BaseThemedActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_manager);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        View toolbarShadow = findViewById(R.id.toolbarShadow);
        ListView listView = (ListView) findViewById(R.id.listView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbarShadow.setVisibility(View.GONE);
        }
    }
}
