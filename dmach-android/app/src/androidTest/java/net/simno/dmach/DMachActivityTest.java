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

package net.simno.dmach;

import android.test.ActivityInstrumentationTestCase2;

public class DMachActivityTest extends ActivityInstrumentationTestCase2<DMachActivity> {

    private DMachActivity mActivity;

    public DMachActivityTest() {
        super(DMachActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
    }

    public void testExists() throws Exception {
        assertNotNull(mActivity);
    }
}
