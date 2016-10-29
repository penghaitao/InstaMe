package com.wartechwick.instame;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.kobakei.ratethisapp.RateThisApp;
import com.wartechwick.instame.db.DatabaseHandler;


public class App extends Application {

    public static Context sContext;
    public FirebaseAnalytics mFirebaseAnalytics;
//    private ClipboardManager clipboard;


    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;
        RateThisApp.Config config = new RateThisApp.Config(7, 20);
        RateThisApp.init(config);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        if (BuildConfig.DEBUG) {
            Toast.makeText(this, "debug", Toast.LENGTH_SHORT).show();
            mFirebaseAnalytics.setAnalyticsCollectionEnabled(false);
        }
    }

//    public ClipboardManager getClipboard() {
//        if (clipboard == null) {
//            clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
//            ClipboardManager.OnPrimaryClipChangedListener mPrimaryClipChangedListener = new ClipboardManager.OnPrimaryClipChangedListener() {
//                @Override
//                public void onPrimaryClipChanged() {
////                    checkClipboard.checkClipboard();
//                }
//            };
//            clipboard.addPrimaryClipChangedListener(mPrimaryClipChangedListener);
//        }
//        return clipboard;
//    }


    @NonNull
    public DatabaseHandler getDBHandler() {
        return new DatabaseHandler(getApplicationContext());
    }


    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public void logFirebaseEvent(String name, String type) {
        Bundle params = new Bundle();
        params.putString("FILE_NAME", name);
        mFirebaseAnalytics.logEvent(type, params);
    }
}
