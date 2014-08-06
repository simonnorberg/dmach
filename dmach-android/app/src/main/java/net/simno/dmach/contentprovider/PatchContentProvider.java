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

package net.simno.dmach.contentprovider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import net.simno.dmach.database.PatchDatabaseHelper;
import net.simno.dmach.database.PatchTable;

import java.util.Arrays;
import java.util.HashSet;

public class PatchContentProvider extends ContentProvider {

    private PatchDatabaseHelper mDatabase;

    private static final int PATCHES = 1;
    private static final int PATCHES_ID = 2;

    private static final String AUTHORITY = "net.simno.dmach.contentprovider.PatchContentProvider";

    private static final String BASE_PATH = "patches";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/patch";
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/patch";

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(AUTHORITY, BASE_PATH, PATCHES);
        sUriMatcher.addURI(AUTHORITY, BASE_PATH + "/#", PATCHES_ID);
    }

    @Override
    public boolean onCreate() {
        mDatabase = new PatchDatabaseHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        checkColumns(projection);

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(PatchTable.TABLE_PATCH);
        switch (sUriMatcher.match(uri)) {
            case PATCHES:
                break;
            case PATCHES_ID:
                queryBuilder.appendWhere(PatchTable.COLUMN_ID + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = mDatabase.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null,
                sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mDatabase.getWritableDatabase();
        long id;
        switch (sUriMatcher.match(uri)) {
            case PATCHES:
                id = db.insert(PatchTable.TABLE_PATCH, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDatabase.getWritableDatabase();
        int rowsDeleted;
        switch (sUriMatcher.match(uri)) {
            case PATCHES:
                rowsDeleted = db.delete(PatchTable.TABLE_PATCH, selection, selectionArgs);
                break;
            case PATCHES_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = db.delete(PatchTable.TABLE_PATCH,
                            PatchTable.COLUMN_ID + "="+ id, null);
                } else {
                    rowsDeleted = db.delete(PatchTable.TABLE_PATCH,
                            PatchTable.COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDatabase.getWritableDatabase();
        int rowsUpdated;
        switch (sUriMatcher.match(uri)) {
            case PATCHES:
                rowsUpdated = db.update(PatchTable.TABLE_PATCH, values, selection, selectionArgs);
                break;
            case PATCHES_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = db.update(PatchTable.TABLE_PATCH, values,
                            PatchTable.COLUMN_ID + "=" + id, null);
                } else {
                    rowsUpdated = db.update(PatchTable.TABLE_PATCH, values,
                            PatchTable.COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private void checkColumns(String[] projection) {
        String[] available = {
                PatchTable.COLUMN_ID,
                PatchTable.COLUMN_TITLE,
                PatchTable.COLUMN_SEQUENCE,
                PatchTable.COLUMN_CHANNELS,
                PatchTable.COLUMN_CHANNEL,
                PatchTable.COLUMN_TEMPO,
                PatchTable.COLUMN_SWING
        };
        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }
}
