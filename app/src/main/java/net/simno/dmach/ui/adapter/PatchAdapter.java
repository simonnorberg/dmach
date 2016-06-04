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

package net.simno.dmach.ui.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.simno.dmach.R;
import net.simno.dmach.model.Patch;

import java.util.Collections;
import java.util.List;

import butterknife.BindColor;
import butterknife.ButterKnife;
import rx.functions.Action1;

public class PatchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements Action1<List<Patch>> {

    public interface OnPatchClickListener {
        void onPatchClick(Patch patch);
        void onPatchLongClick(Patch patch);
    }

    @BindColor(R.color.khaki) int khaki;
    @BindColor(R.color.gurkha) int gurkha;

    private final OnPatchClickListener listener;
    private List<Patch> patches = Collections.emptyList();

    public PatchAdapter(Activity activity) {
        this.listener = (OnPatchClickListener) activity;
        ButterKnife.bind(this, activity);
    }

    @Override
    public void call(List<Patch> patches) {
        this.patches = patches;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View view = inflater.inflate(R.layout.item_patch, parent, false);
        view.setBackgroundColor(viewType == 1 ? khaki : gurkha);

        return new PatchViewHolder(view, new PatchViewHolder.OnClickListener() {
            @Override
            public void onClick(int position) {
                listener.onPatchClick(patches.get(position));
            }

            @Override
            public void onLongClick(int position) {
                listener.onPatchLongClick(patches.get(position));
            }
        });
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((PatchViewHolder) holder).bindModel(patches.get(position));
    }

    @Override
    public int getItemCount() {
        return patches.size();
    }

    @Override
    public int getItemViewType(int position) {
        return (position % 2 == 0) ? 1 : 0;
    }
}
