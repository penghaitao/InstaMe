/*
 * Copyright (C) 2016 Glucosio Foundation
 *
 * This file is part of Glucosio.
 *
 * Glucosio is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * Glucosio is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Glucosio.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package com.wartechwick.instame.db;

import android.content.Context;
import android.preference.PreferenceManager;

import com.wartechwick.instame.R;

import java.io.FileNotFoundException;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class DatabaseHandler {

    private static RealmConfiguration mRealmConfig;
    private final Context mContext;
    private final Realm realm;

    public DatabaseHandler(Context context) {
        this.mContext = context;
        int databaseUpdateVersion = PreferenceManager.getDefaultSharedPreferences(mContext).getInt(mContext.getResources().getString(R.string.update), 0);
        if (databaseUpdateVersion == 0) {
            Realm.init(mContext);
            mRealmConfig = new RealmConfiguration.Builder()
                    .schemaVersion(1)
                    .build();
            try {
                Realm.migrateRealm(mRealmConfig, new Migration());
            } catch (FileNotFoundException ignored) {
                // If the Realm file doesn't exist, just ignore.
            }
            PreferenceManager.getDefaultSharedPreferences(mContext).edit().putInt(mContext.getResources().getString(R.string.update), 1).apply();
        }
        this.realm = getNewRealmInstance();
    }

    private Realm getNewRealmInstance(){
        if (mRealmConfig == null) {
            Realm.init(mContext);
            mRealmConfig = new RealmConfiguration.Builder()
                    .schemaVersion(1)
                    .build();
        }

        return Realm.getInstance(mRealmConfig); // Automatically run migration if needed
    }

    public Realm getRealmInstance(){
        return realm;
    }

}
