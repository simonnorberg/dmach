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

package net.simno.dmach.model;

import org.parceler.Parcel;

/**
 * The Setting class represents two parameters of a channel in dmach.pd
 */
@Parcel
public final class Setting {

    String hText;
    String vText;
    float x;
    float y;
    int hIndex;
    int vIndex;

    Setting() {
    }

    /**
     * @param hText  Text to be displayed horizontally
     * @param vText  Text to be displayed vertically
     * @param x  Horizontal value
     * @param y  Vertical value
     * @param hIndex  Index of the parameter to change in the Pure Data patch
     * @param vIndex  Index of the parameter to change in the Pure Data patch
     */
    Setting(String hText, String vText, float x, float y, int hIndex, int vIndex) {
        this.hText = hText;
        this.vText = vText;
        this.x = x;
        this.y = y;
        this.hIndex = hIndex;
        this.vIndex = vIndex;
    }

    public String getHText() {
        return hText;
    }

    public String getVText() {
        return vText;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public int getHIndex() {
        return hIndex;
    }

    public int getVIndex() {
        return vIndex;
    }
}
