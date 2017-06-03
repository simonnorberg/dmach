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

package net.simno.dmach.ui.activity;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;

import com.squareup.sqlbrite.BriteDatabase;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import net.simno.dmach.DMachApp;
import net.simno.dmach.R;
import net.simno.dmach.db.Db;
import net.simno.dmach.db.PatchTable;
import net.simno.dmach.model.Patch;
import net.simno.dmach.ui.adapter.PatchAdapter;
import net.simno.dmach.ui.adapter.PatchAdapter.OnPatchClickListener;

import org.parceler.Parcels;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class PatchActivity extends RxAppCompatActivity implements OnPatchClickListener {

    static final String TITLE_EXTRA = "title";
    static final String PATCH_EXTRA = "patch";
    static final int RESULT_SAVED = RESULT_FIRST_USER;
    static final int RESULT_LOADED = RESULT_FIRST_USER + 1;

    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    @BindView(R.id.save_button) Button saveButton;
    @BindView(R.id.save_text) EditText saveText;

    @Inject BriteDatabase db;
    private PatchAdapter adapter;
    private Patch patch;
    private String title;
    private Subscription subscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patch);
        DMachApp.get(this).component().inject(this);
        ButterKnife.bind(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            patch = Parcels.unwrap(extras.getParcelable(PATCH_EXTRA));

            saveText.setText(patch.getTitle());
            saveText.setSelection(saveText.getText().length());

            adapter = new PatchAdapter(this);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setHasFixedSize(true);
            recyclerView.setAdapter(adapter);
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        subscription = db.createQuery(PatchTable.TABLE, Db.QUERY_PATCH)
                .mapToList(Patch.MAPPER)
                .compose(this.<List<Patch>>bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(adapter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }

    @OnClick(R.id.save_button)
    void onSaveClicked() {
        title = saveText.getText().toString();
        if (!TextUtils.isEmpty(title) && patch != null) {
            saveButton.setEnabled(false);
            try {
                db.insert(PatchTable.TABLE, getInsertValues(), SQLiteDatabase.CONFLICT_FAIL);
                returnResultSaved();
            } catch (SQLiteConstraintException e) {
                showOverwriteDialog();
            }
        }
    }

    private void showOverwriteDialog() {
        new AlertDialog.Builder(this, R.style.DialogTheme)
                .setMessage(getString(R.string.overwrite_patch, title))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.overwrite, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        db.update(PatchTable.TABLE, getUpdateValues(),
                                SQLiteDatabase.CONFLICT_REPLACE, "title = ?", title);
                        returnResultSaved();
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        saveButton.setEnabled(true);
                    }
                })
                .create()
                .show();
    }

    private ContentValues getInsertValues() {
        return new PatchTable.Builder()
                .title(title)
                .sequence(Patch.sequenceToJson(patch.getSequence()))
                .channels(Patch.channelsToJson(patch.getChannels()))
                .selected(patch.getSelectedChannel())
                .tempo(patch.getTempo())
                .swing(patch.getSwing())
                .build();
    }

    private ContentValues getUpdateValues() {
        return new PatchTable.Builder()
                .sequence(Patch.sequenceToJson(patch.getSequence()))
                .channels(Patch.channelsToJson(patch.getChannels()))
                .selected(patch.getSelectedChannel())
                .tempo(patch.getTempo())
                .swing(patch.getSwing())
                .build();
    }

    private void returnResultSaved() {
        Intent intent = new Intent();
        intent.putExtra(TITLE_EXTRA, title);
        setResult(RESULT_SAVED, intent);
        finish();
    }

    @Override
    public void onPatchClick(Patch patch) {
        Intent intent = new Intent();
        intent.putExtra(PATCH_EXTRA, Parcels.wrap(patch));
        setResult(RESULT_LOADED, intent);
        finish();
    }

    @Override
    public void onPatchLongClick(final Patch patch) {
        new AlertDialog.Builder(this, R.style.DialogTheme)
                .setMessage(getString(R.string.delete_patch, patch.getTitle()))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        db.delete(PatchTable.TABLE, "title = ?", patch.getTitle());
                    }
                })
                .create()
                .show();
    }
}
