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
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import net.simno.dmach.contentprovider.PatchContentProvider;
import net.simno.dmach.database.PatchTable;
import net.simno.dmach.model.Patch;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class PatchListActivity extends ListActivity implements LoaderCallbacks<Cursor> {

    static final String TITLE_EXTRA = "title";
    static final String PATCH_EXTRA = "patch";

    static final int RESULT_SAVED = RESULT_FIRST_USER;
    static final int RESULT_LOADED = RESULT_FIRST_USER + 1;
    private static final int LOAD_TOKEN = 1;
    private static final int SAVE_TOKEN = 2;
    private static final int DELETE_ID = Menu.FIRST + 1;

    private static final String TITLE_SELECTION = PatchTable.COLUMN_TITLE + " = ?";
    private static final String[] FROM = new String[] {
            PatchTable.COLUMN_TEMPO,
            PatchTable.COLUMN_TITLE
    };
    private static int[] TO = new int[] {
            R.id.tempo_column,
            R.id.title_column
    };

    @InjectView(R.id.save_button) Button mSaveButton;
    @InjectView(R.id.save_text) EditText mSaveText;

    private String[] mSelectionArgs;
    private SimpleCursorAdapter mAdapter;
    private PatchQueryHandler mHandler;
    private Patch mPatch;
    private String mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_activity_patch);
        ButterKnife.inject(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mPatch = extras.getParcelable(PATCH_EXTRA);
            mHandler = new PatchQueryHandler(getContentResolver());

            mSaveText.setText(mPatch.getTitle());
            mSaveText.setSelection(mSaveText.getText().length());
            mSaveText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    boolean handled = false;
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        save();
                        handled = true;
                    }
                    return handled;
                }
            });

            mAdapter = new SimpleCursorAdapter(this, R.layout.row_patch, null, FROM, TO, 0);
            setListAdapter(mAdapter);
            getLoaderManager().initLoader(0, null, this);

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
        String[] projection = {
                PatchTable.COLUMN_ID,
                PatchTable.COLUMN_TITLE,
                PatchTable.COLUMN_TEMPO
        };
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
        save();
    }

    private void save() {
        mTitle = mSaveText.getText().toString();
        if (!TextUtils.isEmpty(mTitle)) {
            disableSaveButton();
            // Check if title exists in database. The title column is unique.
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

    private ContentValues getContentValues() {
        final ContentValues values = new ContentValues();
        values.put(PatchTable.COLUMN_TITLE, mTitle);
        values.put(PatchTable.COLUMN_SEQUENCE, Patch.sequenceToJson(mPatch.getSequence()));
        values.put(PatchTable.COLUMN_CHANNELS, Patch.channelsToJson(mPatch.getChannels()));
        values.put(PatchTable.COLUMN_CHANNEL, mPatch.getSelectedChannel());
        values.put(PatchTable.COLUMN_TEMPO, mPatch.getTempo());
        values.put(PatchTable.COLUMN_SWING, mPatch.getSwing());
        return values;
    }

    private void returnTitle() {
        Intent intent = new Intent();
        intent.putExtra(TITLE_EXTRA, mTitle);
        setResult(RESULT_SAVED, intent);
        finish();
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
                        patch.setSequence(cursor.getString(cursor.getColumnIndex(PatchTable.COLUMN_SEQUENCE)));
                        patch.setChannels(cursor.getString(cursor.getColumnIndex(PatchTable.COLUMN_CHANNELS)));
                        patch.setSelectedChannel(cursor.getInt(cursor.getColumnIndex(PatchTable.COLUMN_CHANNEL)));
                        patch.setTempo(cursor.getInt(cursor.getColumnIndex(PatchTable.COLUMN_TEMPO)));
                        patch.setSwing(cursor.getInt(cursor.getColumnIndex(PatchTable.COLUMN_SWING)));
                        cursor.close();
                        Intent intent = new Intent();
                        intent.putExtra(PATCH_EXTRA, patch);
                        setResult(RESULT_LOADED, intent);
                        finish();
                    }
                    break;
                case SAVE_TOKEN:
                    if (cursor.getCount() == 0) {
                        // Title does not exist and we can insert a new entry
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
            returnTitle();
        }

        @Override
        protected void onUpdateComplete(int token, Object cookie, int result) {
            returnTitle();
        }
    }
}
