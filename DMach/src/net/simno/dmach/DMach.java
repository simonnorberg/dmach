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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.simno.dmach.R;
import net.simno.dmach.model.Channel;
import net.simno.dmach.model.Setting;
import net.simno.dmach.view.SequencerView;

import org.puredata.android.io.AudioParameters;
import org.puredata.android.service.PdService;
import org.puredata.core.PdBase;
import org.puredata.core.utils.IoUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DMach extends Activity {

    private static final String SAVED_SEQUENCE = "net.simno.dmach.SAVED_SEQUENCE";
    private static final String SAVED_CHANNELS = "net.simno.dmach.SAVED_CHANNELS";
    private static final String SAVED_TEMPO = "net.simno.dmach.SAVED_TEMPO";
    private static final String SAVED_SHUFFLE= "net.simno.dmach.SAVED_SHUFFLE";
    private static final String SAVED_CHANNEL = "net.simno.dmach.SAVED_CHANNEL";

    public static final int[] MASKS = {1, 2, 4};
    public static final int GROUPS = 2;
    public static final int CHANNELS = 6;
    public static final int STEPS = 16;

    private Typeface mTypeface;
    private int[] mSequence;
    private boolean mIsRunning;
    private List<Channel> mChannels;
    private int mSelectedChannel;
    private int mTempo;
    private int mShuffle;
    private TextView mTempoText;
    private TextView mShuffleText;
    private int mPdPatch;
    private PdService mPdService;
    private final Object mLock = new Object();
    private final ServiceConnection mPdConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mPdService = ((PdService.PdBinder)service).getService();
            startAudio();
            initPd();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {}
    };
    private OnSeekBarChangeListener mTempoListener = new OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
            switch (seekBar.getId()) {
            case R.id.tempo_seekbar_10:
                mTempo = (progress + 1) * 10 + (mTempo % 10);
                break;
            case R.id.tempo_seekbar_1:
                mTempo = Math.max((mTempo / 10) * 10 + progress, 1);
                break;
            }
            PdBase.sendFloat("tempo", mTempo);
            if (mTempoText != null) {
                mTempoText.setText(" " + mTempo);
            }
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
    };
    private OnSeekBarChangeListener mShuffleListener = new OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
            PdBase.sendFloat("shuffle", progress / 100.0f);
            if (mShuffleText != null) {
                mShuffleText.setText(" " + progress);
            }
            mShuffle = progress;
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        restoreSettings();
        initGui();
        initSystemServices();
        initPdService();
    }

    @Override
    protected void onStop() {
        storeSettings();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        cleanup();
        super.onDestroy();
    }

    private void storeSettings() {
        Editor editor = getPreferences(MODE_PRIVATE).edit();
        String mSequenceJson = new Gson().toJson(mSequence);
        String mChannelsJson = new Gson().toJson(mChannels);
        editor.putString(SAVED_SEQUENCE, mSequenceJson)
        .putString(SAVED_CHANNELS, mChannelsJson)
        .putInt(SAVED_TEMPO, mTempo)
        .putInt(SAVED_SHUFFLE, mShuffle)
        .putInt(SAVED_CHANNEL, mSelectedChannel)
        .commit();
    }

    private void restoreSettings() {
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        String mSequenceJson = prefs.getString(SAVED_SEQUENCE, "");
        if (!mSequenceJson.isEmpty()) {
            mSequence = new Gson().fromJson(mSequenceJson, int[].class);
        } else {
            mSequence = new int[GROUPS * STEPS];
        }
        Type type = new TypeToken<ArrayList<Channel>>() {}.getType();
        String mChannelsJson = prefs.getString(SAVED_CHANNELS, "");
        if (!mChannelsJson.isEmpty()) {
            mChannels = new Gson().fromJson(mChannelsJson, type);
        } else {
            initChannels();
        }
        mTempo = prefs.getInt(SAVED_TEMPO, 120);
        mShuffle = prefs.getInt(SAVED_SHUFFLE, 0);
        mSelectedChannel = prefs.getInt(SAVED_CHANNEL, -1);
    }

    private void initChannels() {
        mChannels = new ArrayList<Channel>();

        Channel bd = new Channel("bd");
        bd.addSetting(new Setting("Pitch A", "Gain", .4f, .49f, 0, 7));
        bd.addSetting(new Setting("Low-pass", "Square", .7f, 0, 5, 3));
        bd.addSetting(new Setting("Pitch B", "Curve Time", .4f, .4f, 1, 2));
        bd.addSetting(new Setting("Decay", "Noise Level", .49f, .7f, 6, 4));
        mChannels.add(bd);

        Channel sd = new Channel("sd");
        sd.addSetting(new Setting("Pitch", "Gain", .49f, .45f, 0, 9));
        sd.addSetting(new Setting("Low-pass", "Noise", .6f, .8f, 7, 1));
        sd.addSetting(new Setting("X-fade", "Attack", .35f, .55f, 8, 6));
        sd.addSetting(new Setting("Decay", "Body Decay", .55f, .42f, 4, 5));
        sd.addSetting(new Setting("Band-pass", "Band-pass Q", .7f, .6f, 2, 3));
        mChannels.add(sd);

        Channel cp = new Channel("cp");
        cp.addSetting(new Setting("Pitch", "Gain", .55f, .3f, 0, 7));
        cp.addSetting(new Setting("Delay 1", "Delay 2", .3f, .3f, 4, 5));
        cp.addSetting(new Setting("Decay", "Filter Q", .59f, .2f, 6, 1));
        cp.addSetting(new Setting("Filter From", "Filter To", .9f, .15f, 2, 3));
        mChannels.add(cp);

        Channel tt = new Channel("tt");
        tt.addSetting(new Setting("Pitch", "Gain", .49f, .49f, 0, 1));
        mChannels.add(tt);

        Channel cb = new Channel("cb");
        cb.addSetting(new Setting("Pitch", "Gain", .3f, .49f, 0, 5));
        cb.addSetting(new Setting("Decay 1", "Decay 2", .1f, .75f, 1, 2));
        cb.addSetting(new Setting("Vcf", "Vcf Q", .3f, 0, 3, 4));
        mChannels.add(cb);

        Channel hh = new Channel("hh");
        hh.addSetting(new Setting("Pitch", "Gain", .45f, .4f, 0, 11));
        hh.addSetting(new Setting("Low-pass", "Snap", .8f, .1f, 10, 5));
        hh.addSetting(new Setting("Noise Pitch", "Noise", .55f, .6f, 4, 3));
        hh.addSetting(new Setting("Ratio B", "Ratio A", .9f, 1, 2, 1));
        hh.addSetting(new Setting("Release", "Attack", .55f, .4f, 7, 6));
        hh.addSetting(new Setting("Filter", "Filter Q", .7f, .6f, 8, 9));
        mChannels.add(hh);
    }

    private void initGui() {
        mTypeface = Typeface.createFromAsset(getAssets(), "fonts/saxmono.ttf");
        setContentView(R.layout.activity_dmach);

        if (mSelectedChannel != -1) {
            LinearLayout channels = (LinearLayout) findViewById(R.id.channels);
            ImageButton channel = (ImageButton) channels.getChildAt(mSelectedChannel);
            if (channel != null) {
                channel.setSelected(true);
                getFragmentManager().beginTransaction().add(R.id.fragment_container,
                        ChannelFragment.newInstance(mChannels.get(mSelectedChannel))).commit();
            }
        } else {
            getFragmentManager().beginTransaction().add(R.id.fragment_container,
                  SequencerFragment.newInstance(mSequence)).commit();
        }
    }

    private void initSystemServices() {
        TelephonyManager telephonyManager =
                (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                synchronized (mLock) {
                    if (mPdService == null) {
                        return;
                    }
                    if (state == TelephonyManager.CALL_STATE_IDLE) {
                        if (!mPdService.isRunning()) {
                            startAudio();
                        }
                    } else {
                        if (mPdService.isRunning()) {
                            stopAudio();
                        }
                    }
                }
            }
        }, PhoneStateListener.LISTEN_CALL_STATE);
    }

    private void initPdService() {
        new Thread() {
            @Override
            public void run() {
                bindService(new Intent(DMach.this, PdService.class),
                        mPdConnection, BIND_AUTO_CREATE);
            }
        }.start();
    }

    private void initPd() {
        PdBase.sendFloat("shuffle", mShuffle / 100.0f);
        PdBase.sendFloat("tempo", mTempo);
        sendSequence();
        sendSettings();
    }

    private void sendSequence() {
        for (int step = 0; step < STEPS; ++step) {
            PdBase.sendList("step", new Object[]{0, step, mSequence[step]});
            PdBase.sendList("step", new Object[]{1, step, mSequence[step + STEPS]});
        }
    }

    private void sendSettings() {
        for (Channel channel : mChannels) {
            for (Setting setting : channel.getSettings()) {
                String name = channel.getName();
                PdBase.sendList(name, new Object[]{setting.hIndex, setting.x});
                PdBase.sendList(name, new Object[]{setting.vIndex, setting.y});
            }
        }
    }

    private void startAudio() {
        synchronized (mLock) {
            if (mPdService == null) {
                return;
            }
            int sampleRate = AudioParameters.suggestSampleRate();
            try {
                mPdService.initAudio(sampleRate, 0, 2, -1);
            } catch (IOException e) {
                finish();
                return;
            }
            if (mPdPatch == 0) {
                try {
                    File dir = getFilesDir();
                    IoUtils.extractZipResource(getResources()
                            .openRawResource(R.raw.dmach), dir, true);
                    File patchFile = new File(dir, "dmach.pd");
                    mPdPatch = PdBase.openPatch(patchFile.getAbsolutePath());
                } catch (IOException e) {
                    finish();
                    return;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {}
            }
            mPdService.startAudio(new Intent(this, DMach.class),
                    R.drawable.ic_stat_notify_dmach, "DMach", "Return to DMach.");
        }
    }

    private void stopAudio() {
        synchronized (mLock) {
            if (mPdService == null) {
                return;
            }
            mPdService.stopAudio();
        }
    }

    private void cleanup() {
        synchronized (mLock) {
            stopAudio();
            if (mPdPatch != 0) {
                PdBase.closePatch(mPdPatch);
                mPdPatch = 0;
            }
            PdBase.release();
            try {
                unbindService(mPdConnection);
            } catch (IllegalArgumentException e) {
                mPdService = null;
            }
        }
    }

    private void setFragment() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        if (mSelectedChannel != -1) {
            transaction.replace(R.id.fragment_container,
                    ChannelFragment.newInstance(mChannels.get(mSelectedChannel)));
            transaction.commit();
        } else {
            transaction.replace(R.id.fragment_container, SequencerFragment.newInstance(mSequence));
            transaction.commit();
            getFragmentManager().executePendingTransactions();
        }
    }

    public void onPlayClicked(View view) {
        ImageButton imageButton = (ImageButton) view;
        if (mIsRunning) {
            PdBase.sendBang("stop");
            imageButton.setImageResource(R.drawable.control_play);
        } else {
            PdBase.sendBang("play");
            imageButton.setImageResource(R.drawable.control_stop);
        }
        mIsRunning = !mIsRunning;
    }

    public void onConfigClicked(View view) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View layout = inflater.inflate(R.layout.dialog_config, null);
        AlertDialog alertDialog = new AlertDialog.Builder(this).setView(layout).create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();

        ((TextView) layout.findViewById(R.id.tempo_text)).setTypeface(mTypeface);
        ((TextView) layout.findViewById(R.id.shuffle_text)).setTypeface(mTypeface);

        mTempoText = (TextView) layout.findViewById(R.id.tempo_value);
        mTempoText.setTypeface(mTypeface);
        mTempoText.setText(" " + mTempo);

        SeekBar tempoSeekBar10 = (SeekBar) layout.findViewById(R.id.tempo_seekbar_10);
        tempoSeekBar10.setProgress(mTempo / 10);
        tempoSeekBar10.setOnSeekBarChangeListener(mTempoListener);

        SeekBar tempoSeekBar1 = (SeekBar) layout.findViewById(R.id.tempo_seekbar_1);
        tempoSeekBar1.setProgress(mTempo % 10);
        tempoSeekBar1.setOnSeekBarChangeListener(mTempoListener);

        mShuffleText = (TextView) layout.findViewById(R.id.shuffle_value);
        mShuffleText.setTypeface(mTypeface);
        mShuffleText.setText(" " + mShuffle);

        SeekBar shuffleSeekBar = (SeekBar) layout.findViewById(R.id.shuffle_seekbar);
        shuffleSeekBar.setProgress(mShuffle);
        shuffleSeekBar.setOnSeekBarChangeListener(mShuffleListener);
    }

    public void onResetClicked(View view) {
        mSequence = new int[GROUPS * STEPS];
        sendSequence();
        SequencerView sequencer = (SequencerView) findViewById(R.id.sequencer_view);
        if (sequencer != null) {
            sequencer.setChecked(mSequence);
        }
    }

    public void onChannelClicked(View view) {
        ImageButton channel = (ImageButton) view;
        LinearLayout channels = (LinearLayout) channel.getParent();
        int index = channels.indexOfChild(channel);

        if (index == mSelectedChannel) {
            mSelectedChannel = -1;
            channel.setSelected(false);
        } else {
            ImageButton oldChannel = (ImageButton) channels.getChildAt(mSelectedChannel);
            if (oldChannel != null) {
                oldChannel.setSelected(false);
            }
            mSelectedChannel = index;
            channel.setSelected(true);
        }
        setFragment();
    }
}
