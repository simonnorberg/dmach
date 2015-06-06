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
import android.database.sqlite.SQLiteOpenHelper;

import com.squareup.sqlbrite.SqlBrite;

import net.simno.dmach.BuildConfig;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import timber.log.Timber;

@Module
public class DbModule {

    @Provides @Singleton
    SQLiteOpenHelper provideSQLiteOpenHelper(Context context) {
        return new DbOpenHelper(context);
    }

    @Provides @Singleton
    SqlBrite provideSqlBrite(SQLiteOpenHelper sqLiteOpenHelper) {
        SqlBrite db = SqlBrite.create(sqLiteOpenHelper);
        if (BuildConfig.DEBUG) {
            db.setLogger(new SqlBrite.Logger() {
                @Override
                public void log(String s) {
                    Timber.tag("Database").v(s);
                }
            });
            db.setLoggingEnabled(true);
        }
        return db;
    }
}
