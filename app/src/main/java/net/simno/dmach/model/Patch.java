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

import android.database.Cursor;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.simno.dmach.db.Db;
import net.simno.dmach.db.PatchTable;

import org.parceler.Parcel;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import rx.functions.Func1;

/**
 * The Patch class contains all variables used in dmach.pd
 */
@Parcel
public class Patch {

    public static final Func1<Cursor, Patch> MAPPER = new Func1<Cursor, Patch>() {
        @Override
        public Patch call(Cursor cursor) {
            String title = Db.getString(cursor, PatchTable.TITLE);
            String sequence = Db.getString(cursor, PatchTable.SEQUENCE);
            String channels = Db.getString(cursor, PatchTable.CHANNELS);
            int selectedChannel = Db.getInt(cursor, PatchTable.SELECTED);
            int tempo = Db.getInt(cursor, PatchTable.TEMPO);
            int swing = Db.getInt(cursor, PatchTable.SWING);
            return create(title, sequence, channels, selectedChannel, tempo, swing);
        }
    };

    private static final Type CHANNEL_TYPE = new TypeToken<ArrayList<Channel>>() {}.getType();

    String title;
    int[] sequence;
    List<Channel> channels;
    int selectedChannel;
    int tempo;
    int swing;

    public Patch() {
    }

    /**
     *
     * @param title  Title of the patch, unique in the database
     * @param sequence  The sequence that is used in dmach.pd
     * @param channels  List of channels in the patch
     * @param selectedChannel  The selected channel index
     * @param tempo  The tempo in BPM that is used in dmach.pd
     * @param swing  The swing value that is used in dmach.pd
     */
    public Patch(String title, int[] sequence, List<Channel> channels, int selectedChannel,
                 int tempo, int swing) {
        this.title = title;
        this.sequence = sequence;
        this.channels = channels;
        this.selectedChannel = selectedChannel;
        this.tempo = tempo;
        this.swing = swing;
    }

    public String getTitle() {
        return title;
    }

    public int[] getSequence() {
        return sequence;
    }

    public List<Channel> getChannels() {
        return channels;
    }

    public int getSelectedChannel() {
        return selectedChannel;
    }

    public int getTempo() {
        return tempo;
    }

    public int getSwing() {
        return swing;
    }

    public static String sequenceToJson(int[] sequence) {
        return new Gson().toJson(sequence);
    }

    public static String channelsToJson(List<Channel> channels) {
        return new Gson().toJson(channels);
    }

    public static int[] jsonToSequence(String json) {
        return new Gson().fromJson(json, int[].class);
    }

    public static List<Channel> jsonToChannels(String json) {
        return new Gson().fromJson(json, CHANNEL_TYPE);
    }

    public static Patch create(String title, String sequence, String channels, int selectedChannel,
                               int tempo, int swing) {
        return new Patch(
                title,
                jsonToSequence(sequence),
                jsonToChannels(channels),
                selectedChannel,
                tempo,
                swing
        );
    }
}
