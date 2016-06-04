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

package net.simno.dmach.ui.view;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;

import net.simno.dmach.DMachApp;

import javax.inject.Inject;

public class TypefaceButton extends AppCompatButton {

    @Inject Typeface typeface;

    public TypefaceButton(Context context) {
        super(context);
        init(context);
    }

    public TypefaceButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TypefaceButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        DMachApp.get(context).component().inject(this);
        setTypeface(typeface);
    }
}
