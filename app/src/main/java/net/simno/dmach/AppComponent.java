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

package net.simno.dmach;

import net.simno.dmach.ui.activity.PatchActivity;
import net.simno.dmach.ui.view.PanView;
import net.simno.dmach.ui.view.SettingView;
import net.simno.dmach.ui.view.TypefaceButton;
import net.simno.dmach.ui.view.TypefaceEditText;
import net.simno.dmach.ui.view.TypefaceTextView;

public interface AppComponent {
    void inject(PatchActivity activity);
    void inject(TypefaceButton button);
    void inject(TypefaceTextView textView);
    void inject(TypefaceEditText editText);
    void inject(SettingView view);
    void inject(PanView view);
}
