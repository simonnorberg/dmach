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

package net.simno.dmach.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import net.simno.dmach.R;

class CustomFontHelper {

    public static void setCustomFont(TextView textView, Context context, AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CustomFont);
        String font = array.getString(R.styleable.CustomFont_font);
        setCustomFont(textView, font, context);
        array.recycle();
    }

    private static void setCustomFont(TextView textView, String font, Context context) {
        if (font == null) {
            return;
        }
        Typeface typeface = FontCache.get(font, context);
        if (typeface != null) {
            textView.setTypeface(typeface);
        }
    }
}
