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

package net.simno.dmach;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import net.simno.dmach.contentprovider.PatchContentProvider;
import net.simno.dmach.database.PatchTable;
import net.simno.dmach.model.Patch;

public class PatchListActivity extends ListActivity implements LoaderCallbacks<Cursor> {

    private static final int LOAD_TOKEN = 1;
    private static final int SAVE_TOKEN = 2;
    private static final int DELETE_ID = Menu.FIRST + 1;
    private static final String TITLE_SELECTION = PatchTable.COLUMN_TITLE + " = ?";
    static final String PATCH_EXTRA = "patch";

    private String[] mSelectionArgs;
    private SimpleCursorAdapter mAdapter;
    private PatchQueryHandler mHandler;
    private Button mSaveButton;
    private EditText mSaveText;
    private Patch mPatch;
    private String mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_activity_patch);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mPatch = extras.getParcelable(PATCH_EXTRA);
            mHandler = new PatchQueryHandler(getContentResolver());
            mSaveButton = (Button) findViewById(R.id.save_button);
            mSaveText = (EditText) findViewById(R.id.save_text);
            mSaveText.setText(mPatch.getTitle());
            mSaveText.setSelection(mSaveText.getText().length());

            String[] from = new String[] {PatchTable.COLUMN_TITLE};
            int[] to = new int[] {R.id.row_label};
            getLoaderManager().initLoader(0, null, this);
            mAdapter = new SimpleCursorAdapter(this, R.layout.row_patch, null, from, to, 0);
            setListAdapter(mAdapter);

            getListView().setDividerHeight(6);
            registerForContextMenu(getListView());
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case DELETE_ID:
                AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
                Uri uri = Uri.parse(PatchContentProvider.CONTENT_URI + "/" + info.id);
                mHandler.startDelete(0, null, uri, null, null);
                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, R.string.delete);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Uri uri = Uri.parse(PatchContentProvider.CONTENT_URI + "/" + id);
        mHandler.startQuery(LOAD_TOKEN, null, uri, null, null, null, null);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {PatchTable.COLUMN_ID, PatchTable.COLUMN_TITLE};
        return new CursorLoader(this, PatchContentProvider.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    public void onSaveClicked(View v) {
        mTitle = mSaveText.getText().toString();
        if (!TextUtils.isEmpty(mTitle)) {
            disableSaveButton();
            // Check if title exists in database
            mSelectionArgs = new String[] {mTitle};
            mHandler.startQuery(SAVE_TOKEN, null, PatchContentProvider.CONTENT_URI, null,
                    TITLE_SELECTION, mSelectionArgs, null);
        }
    }

    private void enableSaveButton() {
        mSaveButton.setEnabled(true);
    }

    private void disableSaveButton() {
        mSaveButton.setEnabled(false);
    }

    private void clearTextAndHideKeyboard() {
        mSaveText.setText("");
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSaveText.getWindowToken(), 0);
    }

    private ContentValues getContentValues() {
        final ContentValues values = new ContentValues();
        values.put(PatchTable.COLUMN_TITLE, mTitle);
        values.put(PatchTable.COLUMN_SEQUENCE, mPatch.getSequenceAsJson());
        values.put(PatchTable.COLUMN_CHANNELS, mPatch.getChannelsAsJson());
        values.put(PatchTable.COLUMN_CHANNEL, mPatch.getSelectedChannel());
        values.put(PatchTable.COLUMN_TEMPO, mPatch.getTempo());
        values.put(PatchTable.COLUMN_SWING, mPatch.getSwing());
        return values;
    }

    private class PatchQueryHandler extends AsyncQueryHandler {

        private PatchQueryHandler(ContentResolver cr) {
            super(cr);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            if (cursor == null) {
                return;
            }
            switch (token) {
                case LOAD_TOKEN:
                    if (cursor.moveToFirst()) {
                        Patch patch = new Patch();
                        patch.setTitle(cursor.getString(cursor.getColumnIndex(PatchTable.COLUMN_TITLE)));
                        patch.setSequenceFromJson(cursor.getString(cursor.getColumnIndex(PatchTable.COLUMN_SEQUENCE)));
                        patch.setChannelsFromJson(cursor.getString(cursor.getColumnIndex(PatchTable.COLUMN_CHANNELS)));
                        patch.setSelectedChannel(cursor.getInt(cursor.getColumnIndex(PatchTable.COLUMN_CHANNEL)));
                        patch.setTempo(cursor.getInt(cursor.getColumnIndex(PatchTable.COLUMN_TEMPO)));
                        patch.setSwing(cursor.getInt(cursor.getColumnIndex(PatchTable.COLUMN_SWING)));
                        cursor.close();
                        Intent intent = new Intent();
                        intent.putExtra(PATCH_EXTRA, patch);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                    break;
                case SAVE_TOKEN:
                    if (cursor.getCount() == 0) {
                        // Title is not in database
                        mHandler.startInsert(0, null, PatchContentProvider.CONTENT_URI, getContentValues());
                    } else {
                        // Ask to overwrite existing title
                        AlertDialog.Builder builder = new AlertDialog.Builder(PatchListActivity.this);
                        builder.setMessage("Overwrite " + mTitle + "?");
                        builder.setPositiveButton("Overwrite", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mHandler.startUpdate(0, null, PatchContentProvider.CONTENT_URI,
                                        getContentValues(), TITLE_SELECTION, mSelectionArgs);
                                dialog.dismiss();
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                clearTextAndHideKeyboard();
                                enableSaveButton();
                                dialog.cancel();
                            }
                        });
                        builder.show();
                    }
                    break;
            }
            cursor.close();
        }

        @Override
        protected void onInsertComplete(int token, Object cookie, Uri uri) {
            clearTextAndHideKeyboard();
            enableSaveButton();
        }

        @Override
        protected void onUpdateComplete(int token, Object cookie, int result) {
            clearTextAndHideKeyboard();
            enableSaveButton();
        }
    }
}
