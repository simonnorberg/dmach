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

import android.content.Context;
import android.graphics.Typeface;
import android.media.AudioManager;

import net.simno.dmach.db.DbModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(includes = DbModule.class)
public class DMachModule {

    private final DMachApp app;

    public DMachModule(DMachApp app) {
        this.app = app;
    }

    @Provides @Singleton
    Context provideContext() {
        return app;
    }

    @Provides @Singleton
    Typeface provideTypeface(Context context) {
        return Typeface.createFromAsset(context.getAssets(), "fonts/saxmono.ttf");
    }

    @Provides
    AudioManager provideAudioManager(Context context) {
        return (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }
}
