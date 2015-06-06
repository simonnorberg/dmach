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
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.simno.dmach.R;
import net.simno.dmach.model.Patch;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.FindView;
import butterknife.OnClick;
import butterknife.OnLongClick;
import butterknife.ResourceColor;

public class PatchAdapter extends RecyclerView.Adapter<PatchAdapter.ViewHolder> {

    public interface OnPatchClickListener {
        void onPatchClick(Patch patch);
        void onPatchLongClick(Patch patch);
    }

    @ResourceColor(R.color.khaki) int khaki;
    @ResourceColor(R.color.gurkha) int gurkha;
    private final List<Patch> dataset = new ArrayList<>();
    private final OnPatchClickListener listener;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public interface OnClickListener {
            void onClick(int position);
            void onLongClick(int position);
        }

        @FindView(R.id.title_text) AppCompatTextView title;
        @FindView(R.id.tempo_text) AppCompatTextView tempo;
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
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_patch, parent, false);
        view.setBackgroundColor(viewType == 1 ? khaki : gurkha);
        return new ViewHolder(view, new ViewHolder.OnClickListener() {
            @Override
            public void onClick(int position) {
                if (listener != null) {
                    listener.onPatchClick(dataset.get(position));
                }
            }

            @Override
            public void onLongClick(int position) {
                listener.onPatchLongClick(dataset.get(position));
            }
        });
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Patch patch = dataset.get(position);
        holder.title.setText(patch.getTitle());
        holder.tempo.setText(String.valueOf(patch.getTempo()));
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position % 2 == 0) {
            return 1;
        }
        return 0;
    }

    public void set(List<Patch> patches) {
        dataset.clear();
        dataset.addAll(patches);
        notifyDataSetChanged();
    }
}
