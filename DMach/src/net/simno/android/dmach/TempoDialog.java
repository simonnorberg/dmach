/**
 * Copyright (C) 2013 Simon Norberg
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 * 
 */

package net.simno.android.dmach;

import net.simno.android.dmach.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class TempoDialog extends DialogFragment {

	public interface TempoDialogListener {
        public void onTempoPositiveClick(DialogFragment dialog, int index);
        public void onTempoNegativeClick(DialogFragment dialog);
    }

	TempoDialogListener mListener;
	
	public TempoDialog() {
		super();
    }
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (TempoDialogListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ "must implement TempoDialogListener");
		}
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
//		builder.setTitle(R.string.dialog_set_tempo)
//			.setPositiveButton(R.string.set, new DialogInterface.OnClickListener() {
//				@Override
//				public void onClick(DialogInterface dialog, int which) {
//					mListener.onDialogPositiveClick(TempoDialogFragment.this);
//				}
//			})
//			.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//				@Override
//				public void onClick(DialogInterface dialog, int which) {
//					//TempoDialogFragment.this.getDialog().cancel();
//					mListener.onDialogNegativeClick(TempoDialogFragment.this);
//				}
//			});
		
		builder.setTitle(R.string.dialog_set_tempo)
			.setItems(R.array.tempos, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					mListener.onTempoPositiveClick(TempoDialog.this, which + 1);
				}
			});
		return builder.create();
	}
}