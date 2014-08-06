/*
* Copyright (C) 2014 Simon Norberg
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

package net.simno.dmach.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class PatchTable {

    public static final String TABLE_PATCH = "patch";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_SEQUENCE = "sequence";
    public static final String COLUMN_CHANNELS = "channels";
    public static final String COLUMN_CHANNEL = "channel";
    public static final String COLUMN_TEMPO = "tempo";
    public static final String COLUMN_SWING = "swing";

    private static final String DATABASE_CREATE = "create table "
            + TABLE_PATCH
            + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_TITLE + " text unique not null, "
            + COLUMN_SEQUENCE + " text not null, "
            + COLUMN_CHANNELS + " integer not null, "
            + COLUMN_CHANNEL + " text not null, "
            + COLUMN_TEMPO + " integer not null, "
            + COLUMN_SWING + " integer not null "
            + ");";

    public static void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(PatchTable.class.getName(),
                "Upgrading database from version " + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PATCH);
        onCreate(db);
    }
}
