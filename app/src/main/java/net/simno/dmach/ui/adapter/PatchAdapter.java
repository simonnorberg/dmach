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
import net.simno.dmach.ui.view.CustomFontTextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import rx.functions.Action1;

public class PatchAdapter extends RecyclerView.Adapter<PatchAdapter.ViewHolder>
        implements Action1<List<Patch>> {

    public interface OnPatchClickListener {
        void onPatchClick(Patch patch);
        void onPatchLongClick(Patch patch);
    }

    @BindColor(R.color.khaki) int khaki;
    @BindColor(R.color.gurkha) int gurkha;

    private final OnPatchClickListener listener;
    private List<Patch> patches = new ArrayList<>();

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public interface OnClickListener {
            void onClick(int position);
            void onLongClick(int position);
        }

        @Bind(R.id.title_text) CustomFontTextView title;
        @Bind(R.id.swing_text) CustomFontTextView swing;
        @Bind(R.id.tempo_text) CustomFontTextView tempo;

        private final OnClickListener listener;

        public ViewHolder(View view, OnClickListener listener) {
            super(view);
            ButterKnife.bind(this, view);
            this.listener = listener;
        }

        @OnClick(R.id.item_patch)
        public void onClicked() {
            final int position = getAdapterPosition();
            if (position >= 0 && listener != null) {
                listener.onClick(position);
            }
        }

        @OnLongClick(R.id.item_patch)
        public boolean onLongClicked() {
            final int position = getAdapterPosition();
            if (position >= 0 && listener != null) {
                listener.onLongClick(position);
                return true;
            }
            return false;
        }
    }

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
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_patch, parent, false);
        view.setBackgroundColor(viewType == 1 ? khaki : gurkha);
        return new ViewHolder(view, new ViewHolder.OnClickListener() {
            @Override
            public void onClick(int position) {
                if (listener != null) {
                    listener.onPatchClick(patches.get(position));
                }
            }

            @Override
            public void onLongClick(int position) {
                listener.onPatchLongClick(patches.get(position));
            }
        });
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Patch patch = patches.get(position);
        holder.title.setText(patch.getTitle());
        holder.swing.setText(String.valueOf(patch.getSwing()));
        holder.tempo.setText(String.valueOf(patch.getTempo()));
    }

    @Override
    public int getItemCount() {
        return patches.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position % 2 == 0) {
            return 1;
        }
        return 0;
    }
}
