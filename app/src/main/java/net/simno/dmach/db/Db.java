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

import android.database.Cursor;

import com.squareup.sqlbrite.SqlBrite;

import net.simno.dmach.model.Patch;

import java.util.ArrayList;
import java.util.List;

import rx.functions.Func1;

public final class Db {

    static String getString(Cursor cursor, String columnName) {
        return cursor.getString(cursor.getColumnIndexOrThrow(columnName));
    }

    static int getInt(Cursor cursor, String columnName) {
        return cursor.getInt(cursor.getColumnIndexOrThrow(columnName));
    }

    public static final String QUERY_PATCH = "select * from patch order by title";

    public static final Func1<SqlBrite.Query, List<Patch>> MAP_PATCH = new Func1<SqlBrite.Query, List<Patch>>() {
        @Override
        public List<Patch> call(SqlBrite.Query query) {
            Cursor cursor = query.run();
            //noinspection TryFinallyCanBeTryWithResources
            try {
                List<Patch> patches = new ArrayList<>();
                while (cursor.moveToNext()) {
                    String title = getString(cursor, PatchTable.TITLE);
                    String sequence = getString(cursor, PatchTable.SEQUENCE);
                    String channels = getString(cursor, PatchTable.CHANNELS);
                    int selectedChannel = getInt(cursor, PatchTable.SELECTED);
                    int tempo = getInt(cursor, PatchTable.TEMPO);
                    int swing = getInt(cursor, PatchTable.SWING);

                    Patch patch = Patch.create(title, sequence, channels, selectedChannel, tempo, swing);
                    patches.add(patch);
                }
                return patches;
            } finally {
                cursor.close();
            }
        }
    };
}
