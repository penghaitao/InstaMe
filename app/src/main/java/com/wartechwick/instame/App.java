package com.wartechwick.instame;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import com.kobakei.ratethisapp.RateThisApp;
import com.wartechwick.instame.db.DatabaseHandler;


public class App extends Application {

    public static Context sContext;


    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;
        RateThisApp.Config config = new RateThisApp.Config(7, 20);
        RateThisApp.init(config);
    }

    @NonNull
    public DatabaseHandler getDBHandler() {
        return new DatabaseHandler(getApplicationContext());
    }


    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
