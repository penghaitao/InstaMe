package com.wartechwick.instame.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TextView;

import com.wartechwick.instame.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class LicenceActivity extends AppCompatActivity {

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.logo)
    TextView logo;
    @Bind(R.id.web_view)
    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_licence);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(" ");

//        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/billabong.ttf");
//        logo.setTypeface(typeface);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        webView.loadUrl("file:///android_asset/license.html");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
