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

import android.os.Parcel;
import android.os.Parcelable;

public final class Setting implements Parcelable {
	private String hText;
	private String vText;
	private PointF pos;
	
	public Setting() {
		this("init", "init");
	}
	
	public Setting(String hText, String vText) {
		this(hText, vText, new PointF(0.5f, 0.5f));
	}
		
	public Setting(String hText, String vText, PointF pos) {
		this.hText = hText;
		this.vText = vText;
		this.pos = new PointF(pos);
	}
	
	public Setting(Setting s) {
		this(s.getHText(), s.getVText(), s.getPos());
	}
	
	public Setting(Parcel in) {
		readFromParcel(in);
	}

	public String getHText() {
		return hText;
	}

	public String getVText() {
		return vText;
	}

	public PointF getPos() {
		return new PointF(pos);
	}

	public void setPos(PointF pos) {
		this.pos = new PointF(pos);
	}
		
	public static final Parcelable.Creator<Setting> CREATOR = new Parcelable.Creator<Setting>() {

		@Override
		public Setting createFromParcel(Parcel in) {
			return new Setting(in);
		}

		@Override
		public Setting[] newArray(int size) {
			return new Setting[size];
		}
	};
	
	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(hText);
		out.writeString(vText);
		out.writeParcelable(pos, flags);
	}
	
	private void readFromParcel(Parcel in) {
		hText = in.readString();
		vText = in.readString();
		pos = in.readParcelable(PointF.class.getClassLoader());
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
}