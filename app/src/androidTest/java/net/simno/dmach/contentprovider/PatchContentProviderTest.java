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

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;
import android.text.TextUtils;

import net.simno.dmach.DMachActivity;
import net.simno.dmach.database.PatchTable;
import net.simno.dmach.model.Channel;
import net.simno.dmach.model.Patch;

import java.util.ArrayList;

public class PatchContentProviderTest extends ProviderTestCase2<PatchContentProvider> {

    private static final String TITLE_SELECTION = PatchTable.COLUMN_TITLE + " = ?";

    private MockContentResolver mResolver;
    private Patch mPatch;
    private String[] mSelectionArgs;

    public PatchContentProviderTest() {
        super(PatchContentProvider.class, "net.simno.dmach.contentprovider.PatchContentProvider");
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mResolver = this.getMockContentResolver();
        mPatch = new Patch(
                "title",
                new int[DMachActivity.STEPS * DMachActivity.GROUPS],
                new ArrayList<Channel>(),
                -1, 120, 5);
        mSelectionArgs = new String[] {mPatch.getTitle()};
    }

    @Override
    public void tearDown() throws Exception {
        mResolver.delete(PatchContentProvider.CONTENT_URI, null, null);
        super.tearDown();
    }

    private ContentValues getContentValues() {
        final ContentValues values = new ContentValues();
        values.put(PatchTable.COLUMN_TITLE, mPatch.getTitle());
        values.put(PatchTable.COLUMN_SEQUENCE, Patch.sequenceToJson(mPatch.getSequence()));
        values.put(PatchTable.COLUMN_CHANNELS, Patch.channelsToJson(mPatch.getChannels()));
        values.put(PatchTable.COLUMN_CHANNEL, mPatch.getSelectedChannel());
        values.put(PatchTable.COLUMN_TEMPO, mPatch.getTempo());
        values.put(PatchTable.COLUMN_SWING, mPatch.getSwing());
        return values;
    }

    public void testUri() throws Exception {
        Uri uri = Uri.parse(PatchContentProvider.CONTENT_URI + "unknown");
        try {
            mResolver.query(uri, null, null, null, null);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Success
        }
        try {
            mResolver.insert(uri, null);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Success
        }
        try {
            mResolver.delete(uri, null, null);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Success
        }
        try {
            mResolver.update(uri, null, null, null);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Success
        }
    }

    public void testProjection() throws Exception {
        String[] projection = {PatchTable.COLUMN_ID, "unknown"};
        try {
            mResolver.query(PatchContentProvider.CONTENT_URI, projection, null, null, null);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Success
        }
    }

    public void testOperations() throws Exception {
        // Insert new patch
        Uri uri1 = mResolver.insert(PatchContentProvider.CONTENT_URI, getContentValues());
        assertNotNull(uri1);
        assertEquals(false, TextUtils.equals("-1", uri1.getLastPathSegment()));

        // Insert again with same title should fail
        Uri uri2 = mResolver.insert(PatchContentProvider.CONTENT_URI, getContentValues());
        assertNotNull(uri2);
        assertEquals("-1", uri2.getLastPathSegment());

        // Update patch
        int rowsUpdated = mResolver.update(PatchContentProvider.CONTENT_URI, getContentValues(),
                TITLE_SELECTION, mSelectionArgs);
        assertNotNull(rowsUpdated);
        assertEquals(1, rowsUpdated);

        // Query title
        Cursor cursor1 = mResolver.query(PatchContentProvider.CONTENT_URI, null, TITLE_SELECTION,
                mSelectionArgs, null);
        assertNotNull(cursor1);
        assertEquals(true, cursor1.moveToFirst());

        // Query id
        long id = cursor1.getLong(cursor1.getColumnIndex(PatchTable.COLUMN_ID));
        Uri uri = Uri.parse(PatchContentProvider.CONTENT_URI + "/" + id);
        Cursor cursor2 = mResolver.query(uri, null, null, null, null);
        assertNotNull(cursor2);
        assertEquals(true, cursor2.moveToFirst());

        // Delete id
        int rowsDeleted = mResolver.delete(uri, null, null);
        assertNotNull(rowsDeleted);
        assertEquals(1, rowsDeleted);
    }
}
