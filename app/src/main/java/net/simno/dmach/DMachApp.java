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

package net.simno.dmach;

import android.app.Application;
import android.content.Context;

import com.squareup.leakcanary.LeakCanary;

import butterknife.ButterKnife;
import timber.log.Timber;

public class DMachApp extends Application {

    private DMachComponent component;

    @Override
    public void onCreate() {
        super.onCreate();
        LeakCanary.install(this);
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
            ButterKnife.setDebug(true);
        }
        buildComponentAndInject();
    }

    private void buildComponentAndInject() {
        component = DaggerDMachComponent.builder()
                .dMachModule(new DMachModule(this))
                .build();
        component.inject(this);
    }

    public DMachComponent getComponent() {
        return component;
    }

    public static DMachApp get(Context context) {
        return (DMachApp) context.getApplicationContext();
    }
}
