/*
* Copyright (C) 2016 Simon Norberg
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

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import net.simno.dmach.R;
import net.simno.dmach.model.Patch;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

class PatchViewHolder extends RecyclerView.ViewHolder {

    interface OnClickListener {
        void onClick(int position);
        void onLongClick(int position);
    }

    @BindView(R.id.title_text) TextView title;
    @BindView(R.id.swing_text) TextView swing;
    @BindView(R.id.tempo_text) TextView tempo;

    private final OnClickListener listener;

    PatchViewHolder(View view, OnClickListener listener) {
        super(view);
        ButterKnife.bind(this, view);
        this.listener = listener;
    }

    @OnClick(R.id.item_patch)
    void onClicked() {
        final int position = getAdapterPosition();
        if (position >= 0 && listener != null) {
            listener.onClick(position);
        }
    }

    @OnLongClick(R.id.item_patch)
    boolean onLongClicked() {
        final int position = getAdapterPosition();
        if (position >= 0 && listener != null) {
            listener.onLongClick(position);
            return true;
        }
        return false;
    }

    void bindModel(Patch patch) {
        title.setText(patch.getTitle());
        swing.setText(String.valueOf(patch.getSwing()));
        tempo.setText(String.valueOf(patch.getTempo()));
    }
}
