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

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.widget.Toast;

import com.squareup.sqlbrite.SqlBrite;

import net.simno.dmach.DMachApp;
import net.simno.dmach.ui.adapter.PatchAdapter;
import net.simno.dmach.R;
import net.simno.dmach.db.Db;
import net.simno.dmach.db.PatchTable;
import net.simno.dmach.model.Patch;

import org.parceler.Parcels;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.FindView;
import butterknife.OnClick;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class PatchActivity extends AppCompatActivity implements PatchAdapter.OnPatchClickListener {

    static final String TITLE_EXTRA = "title";
    static final String PATCH_EXTRA = "patch";
    static final int RESULT_SAVED = RESULT_FIRST_USER;
    static final int RESULT_LOADED = RESULT_FIRST_USER + 1;

    @Inject SqlBrite db;
    @FindView(R.id.recycler_view) RecyclerView recyclerView;
    @FindView(R.id.save_button) AppCompatButton saveButton;
    @FindView(R.id.save_text) AppCompatEditText saveText;
    private PatchAdapter adapter;
    private Patch patch;
    private String title;
    private CompositeSubscription subscriptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patch);
        DMachApp.get(this).getComponent().inject(this);
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

            subscriptions = new CompositeSubscription();
            loadPatches();
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        subscriptions.unsubscribe();
        super.onDestroy();
    }

    private void loadPatches() {
        subscriptions.add(db.createQuery(PatchTable.TABLE, Db.QUERY_PATCH)
                .map(Db.MAP_PATCH)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Patch>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(PatchActivity.this, "Error loading patches.",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(List<Patch> patches) {
                        adapter.set(patches);
                    }
                }));
    }

    @OnClick(R.id.save_button)
    public void onSaveClicked() {
        title = saveText.getText().toString();
        if (!TextUtils.isEmpty(title) && patch != null) {
            disableSaveButton();
            try {
                db.insert(PatchTable.TABLE, getInsertValues(), SQLiteDatabase.CONFLICT_FAIL);
                returnResultSaved();
            } catch (SQLiteConstraintException e) {
                // Ask to overwrite existing title
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Overwrite " + title + "?");
                builder.setPositiveButton("Overwrite", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        db.update(PatchTable.TABLE, getUpdateValues(),
                                SQLiteDatabase.CONFLICT_REPLACE, "title = ?", title);
                        dialog.dismiss();
                        returnResultSaved();
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
        }
    }

    private ContentValues getInsertValues() {
        return new PatchTable.Builder()
                .title(title)
                .sequence(Patch.sequenceToJson(patch.getSequence()))
                .channels(Patch.channelsToJson(patch.getChannels()))
                .channel(patch.getSelectedChannel())
                .tempo(patch.getTempo())
                .swing(patch.getSwing())
                .build();
    }

    private ContentValues getUpdateValues() {
        return new PatchTable.Builder()
                .sequence(Patch.sequenceToJson(patch.getSequence()))
                .channels(Patch.channelsToJson(patch.getChannels()))
                .channel(patch.getSelectedChannel())
                .tempo(patch.getTempo())
                .swing(patch.getSwing())
                .build();
    }

    private void enableSaveButton() {
        saveButton.setEnabled(true);
    }

    private void disableSaveButton() {
        saveButton.setEnabled(false);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete " + patch.getTitle() + "?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                db.delete(PatchTable.TABLE, "title = ?", patch.getTitle());
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }
}
