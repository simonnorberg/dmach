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

package net.simno.dmach.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

public class PatchTable {

    public static final String TABLE = "patch";
    private static final String ID = "_id";
    public static final String TITLE = "title";
    public static final String SEQUENCE = "sequence";
    public static final String CHANNELS = "channels";
    public static final String SELECTED = "selected";
    public static final String TEMPO = "tempo";
    public static final String SWING = "swing";

    private static final String CREATE_TABLE = "create table "
            + TABLE
            + "("
            + ID + " integer primary key autoincrement, "
            + TITLE + " text unique not null, "
            + SEQUENCE + " text not null, "
            + CHANNELS + " text not null, "
            + SELECTED + " integer not null, "
            + TEMPO + " integer not null, "
            + SWING + " integer not null "
            + ");";

    static void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Timber.tag(PatchTable.class.getName())
                .w("Upgrading database from version " + oldVersion + " to " + newVersion);
        if (oldVersion == 1 && newVersion == 2) {
            try {
                upgrade(db);
            } catch (Exception e) {
                db.execSQL("drop table if exists " + TABLE);
                onCreate(db);
            }
        } else {
            db.execSQL("drop table if exists " + TABLE);
            onCreate(db);
        }
    }

    private static void upgrade(SQLiteDatabase db) {
        db.execSQL("alter table patch rename to old_patch;");
        onCreate(db);
        db.execSQL("insert into patch(title, sequence, channels, selected, tempo, swing) "
                + "select title, sequence, channels, channel, tempo, swing from old_patch;");
        db.execSQL("drop table if exists old_patch");

        Cursor cursor = db.rawQuery("select title, channels from patch", null);
        Map<String, String> data = new HashMap<>();
        //noinspection TryFinallyCanBeTryWithResources
        try {
            while (cursor.moveToNext()) {
                data.put(Db.getString(cursor, "title"), Db.getString(cursor, "channels"));
            }
        } finally {
            cursor.close();
        }
        if (data.isEmpty()) {
            return;
        }
        for (Map.Entry<String, String> entry : data.entrySet()) {
            String oldJson = entry.getValue();
            String newJson = oldJson.replace("mName", "name")
                    .replace("mPan", "pan")
                    .replace("mSelectedSetting", "selectedSetting")
                    .replace("mSettings", "settings");
            db.update(TABLE, new Builder().channels(newJson).build(), "title = ?",
                    new String[]{entry.getKey()});
        }
    }

    public static final class Builder {
        private final ContentValues values = new ContentValues();

        public Builder title(String title) {
            values.put(TITLE, title);
            return this;
        }

        public Builder sequence(String sequence) {
            values.put(SEQUENCE, sequence);
            return this;
        }

        public Builder channels(String channels) {
            values.put(CHANNELS, channels);
            return this;
        }

        public Builder selected(int selected) {
            values.put(SELECTED, selected);
            return this;
        }

        public Builder tempo(int tempo) {
            values.put(TEMPO, tempo);
            return this;
        }

        public Builder swing(int swing) {
            values.put(SWING, swing);
            return this;
        }

        public ContentValues build() {
            return values;
        }
    }
}
