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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PatchDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "dmach.db";
    private static final int DATABASE_VERSION = 1;
    private static PatchDatabaseHelper singleton;

    public synchronized static PatchDatabaseHelper getInstance(Context context) {
        if (singleton == null) {
            singleton = new PatchDatabaseHelper(context.getApplicationContext());
        }
        return singleton;
    }

    private PatchDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        PatchTable.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        PatchTable.onUpgrade(db, oldVersion, newVersion);
    }
}
