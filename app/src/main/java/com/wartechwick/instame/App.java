package com.wartechwick.instame;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.StrictMode;
import android.support.annotation.NonNull;

import com.kobakei.ratethisapp.RateThisApp;
import com.wartechwick.instame.db.DatabaseHandler;


public class App extends Application {

    public static boolean uiInForeground = false;
    private static Context sContext;
//    private ClipboardManager clipboard;


    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;
        RateThisApp.Config config = new RateThisApp.Config(70, 20);
        RateThisApp.init(config);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }
    }

    public static Context getContext() {
        return sContext;
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


}
