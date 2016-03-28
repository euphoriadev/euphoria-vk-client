package ru.euphoriadev.vk;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import ru.euphoriadev.vk.http.HttpClient;
import ru.euphoriadev.vk.http.HttpException;
import ru.euphoriadev.vk.http.HttpRequest;
import ru.euphoriadev.vk.http.HttpResponse;
import ru.euphoriadev.vk.util.AndroidUtils;
import ru.euphoriadev.vk.util.ViewUtil;

public class DocTextViewActivity extends BaseThemedActivity {
    private Toolbar toolbar;
    private TextView textView;
    private String title;
    private String url;

    String hint;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doc_text_view);
        title = getIntent().getStringExtra("title");

        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        ViewUtil.setTypeface(toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        textView = (TextView) findViewById(R.id.docTextView);
        ViewUtil.setTypeface(textView);

        View shadowToolbar = findViewById(R.id.toolbarShadow);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            shadowToolbar.setVisibility(View.GONE);
        }


        hint = getResources().getString(R.string.loading_text);
        url = getIntent().getStringExtra("data");
        downloadText();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;

            case R.id.menu_copy:
                AndroidUtils.copyTextToClipboard(this, textView.getText().toString());
                Toast.makeText(DocTextViewActivity.this, R.string.message_copied, Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean createWarningDialog(final String text) {
        if (text.length() >= 102400) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getResources().getString(R.string.file_large));
            builder.setMessage(String.format(getResources().getString(R.string.file_large_description), text.length()));
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    textView.setText(text);
                }
            });
            builder.show();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.doc_view_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void downloadText() {
        if (!AndroidUtils.hasConnection(this)) {
            Snackbar.make(findViewById(android.R.id.content), R.string.check_internet, Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        HttpClient.execute(HttpRequest.builder(url)
                .cachePolicy(HttpRequest.CachePolicy.ENABLED)
                .build(), new HttpRequest.OnResponseListener() {
            @Override
            public void onResponse(HttpClient client, HttpResponse response) {
                String text = response.asString();
                if (!createWarningDialog(text)) {
                    textView.setText(text);
                }
            }

            @Override
            public void onProgress(char[] buffer, int progress, long totalSize) {
//                textView.setText(String.format(hint, progress));
            }

            @Override
            public void onError(HttpClient client, HttpException exception) {
                textView.setText(R.string.error);
                Snackbar.make(findViewById(android.R.id.content), R.string.error, Snackbar.LENGTH_LONG)
                        .show();
            }
        });
    }
}
