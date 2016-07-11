package com.wartechwick.instame;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import com.wartechwick.instame.db.DatabaseHandler;


public class App extends Application {

    public static Context sContext;


    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;
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
