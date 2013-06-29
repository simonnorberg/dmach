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

package net.simno.android.dmach.model;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public final class Patch implements Parcelable {
	private List<Setting> settings;
	private int selectedSettingIndex;
	
	public Patch() {
		this(0, new ArrayList<Setting>());
	}
	
	public Patch(Patch p) {
		this.selectedSettingIndex = p.getSelectedSettingIndex();
		this.settings = new ArrayList<Setting>(p.getCount());
		for (int i = 0; i < p.getCount(); ++i) {
			settings.add(new Setting(p.getHText(i), p.getVText(i), p.getPos(i)));
		}
	}

	public Patch(int selectedSettingIndex, List<Setting> settings) {
		this.selectedSettingIndex = selectedSettingIndex;
		this.settings = new ArrayList<Setting>(settings.size());
		for (Setting s : settings) {
			settings.add(new Setting(s));
		}
	}
	
	public Patch(Parcel in) {
		readFromParcel(in);
	}
	
	public void addSetting(Setting s) {
		settings.add(new Setting(s));
	}
	
	public int getSelectedSettingIndex() {
		return selectedSettingIndex;
	}

	public void setSelectedSettingIndex(int selectedSettingIndex) {
		this.selectedSettingIndex = selectedSettingIndex;
	}
	
	public PointF getPos(int index) {
		return settings.get(index).getPos();
	}
	
	public void setPos(int index, PointF pos) {
		settings.get(index).setPos(pos);
	}

	public PointF getSelectedPos() {
		return getPos(selectedSettingIndex);
	}
	
	public void setSelectedPos(PointF pos) {
		setPos(selectedSettingIndex, pos);
	}
	
	private String getVText(int index) {
		return settings.get(index).getVText();
	}

	private String getHText(int index) {
		return settings.get(index).getHText();
	}
	
	public String getSelectedHText() {
		return getHText(selectedSettingIndex);
	}
	
	public String getSelectedVText() {
		  return getVText(selectedSettingIndex);
	}

	public int getCount() {
		return settings.size();
	}
	
	public static final Parcelable.Creator<Patch> CREATOR = new Parcelable.Creator<Patch>() {

		@Override
		public Patch createFromParcel(Parcel in) {
			return new Patch(in);
		}
		
		@Override
		public Patch[] newArray(int size) {
			return new Patch[size];
		}
	};
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeList(settings);
		out.writeInt(selectedSettingIndex);
	}
	
	private void readFromParcel(Parcel in) {
		in.readList(settings, Setting.class.getClassLoader());
		selectedSettingIndex = in.readInt();
	}
}