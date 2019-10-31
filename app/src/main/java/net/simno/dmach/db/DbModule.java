/*
* Copyright (C) 2015 Simon Norberg
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package net.simno.dmach.db;

import android.content.Context;

import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import androidx.sqlite.db.SupportSQLiteOpenHelper.Callback;
import androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration;
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory;

import com.squareup.sqlbrite3.BriteDatabase;
import com.squareup.sqlbrite3.SqlBrite;

import net.simno.dmach.BuildConfig;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

@Module
public class DbModule {

    private static final String DB_NAME = "dmach.db";
    private static final int DB_VERSION = 2;

    @Provides @Singleton
    SupportSQLiteOpenHelper provideSQLiteOpenHelper(Context context) {
        Callback callback = new Callback(DB_VERSION) {
            @Override
            public void onCreate(SupportSQLiteDatabase db) {
                PatchTable.onCreate(db);
            }

            @Override
            public void onUpgrade(SupportSQLiteDatabase db, int oldVersion, int newVersion) {
                PatchTable.onUpgrade(db, oldVersion, newVersion);
            }
        };
        Configuration configuration = Configuration.builder(context)
                .name(DB_NAME)
                .callback(callback)
                .build();
        return new FrameworkSQLiteOpenHelperFactory().create(configuration);
    }

    @Provides @Singleton
    SqlBrite provideSqlBrite() {
        return new SqlBrite.Builder().logger(new SqlBrite.Logger() {
            @Override
            public void log(String message) {
                Timber.tag("Database").v(message);
            }
        }).build();
    }

    @Provides @Singleton
    BriteDatabase provideDatabase(SqlBrite sqlBrite, SupportSQLiteOpenHelper helper) {
        BriteDatabase db = sqlBrite.wrapDatabaseHelper(helper, Schedulers.io());
        db.setLoggingEnabled(BuildConfig.DEBUG);
        return db;
    }
}
