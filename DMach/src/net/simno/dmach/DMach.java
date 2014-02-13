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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package net.simno.dmach;

import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.gson.Gson;

import net.simno.dmach.R;
import net.simno.dmach.PatchFragment.OnPatchChangedListener;
import net.simno.dmach.model.Channel;
import net.simno.dmach.model.Patch;
import net.simno.dmach.model.PointF;
import net.simno.dmach.model.Setting;
import net.simno.dmach.view.SequencerView;
import net.simno.dmach.view.SequencerView.OnStepChangedListener;

import org.puredata.android.io.AudioParameters;
import org.puredata.android.service.PdService;
import org.puredata.core.PdBase;
import org.puredata.core.utils.IoUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class DMach extends Activity
implements OnStepChangedListener, OnPatchChangedListener {
    
    public static final int[] MASKS = {1, 2, 4};
    public static final int GROUPS = 2;
    public static final int CHANNELS = 6;
    public static final int STEPS = 16;

    private int[] mSequence;
    private boolean mIsRunning;
    
    private int mSelectedChannel;
    private int mSelectedSetting;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        initChannels();
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
        String json = new Gson().toJson(mSequence);
        editor.putString(getString(R.string.saved_sequence), json)
        .putInt(getString(R.string.saved_tempo), mTempo)
        .putInt(getString(R.string.saved_shuffle), mShuffle)
        .putInt(getString(R.string.saved_channel), mSelectedChannel)
        .commit();
    }
    
    private void restoreSettings() {
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        String json = prefs.getString(getString(R.string.saved_sequence), "");
        if (!json.isEmpty()) {
            mSequence = new Gson().fromJson(json, int[].class);    
        } else {
            mSequence = new int[GROUPS * STEPS];
        }
        mTempo = prefs.getInt(getString(R.string.saved_tempo), 120);
        mShuffle = prefs.getInt(getString(R.string.saved_shuffle), 0);
        mSelectedChannel = prefs.getInt(getString(R.string.saved_channel), -1);
    }
    
//    private void initChannels() {
//        channels = new ArrayList<Channel>(CHANNEL_COUNT);
//
//        Patch bdPatch = new Patch();
//        bdPatch.addSetting(new Setting("Pitch A", "Gain", new PointF(.4f, .49f)));
//        bdPatch.addSetting(new Setting("Low-pass", "Square", new PointF(.7f, 0)));
//        bdPatch.addSetting(new Setting("Pitch B", "Curve Time", new PointF(.4f, .4f)));
//        bdPatch.addSetting(new Setting("Decay", "Noise Level", new PointF(.49f, .7f)));
//        channels.add(new Channel("bd", bdPatch, new boolean[STEP_COUNT]));
//
//        Patch sdPatch = new Patch();
//        sdPatch.addSetting(new Setting("Pitch", "Gain", new PointF(.49f , .45f)));
//        sdPatch.addSetting(new Setting("Low-pass", "Noise", new PointF(.6f, .8f)));
//        sdPatch.addSetting(new Setting("X-fade", "Attack", new PointF(.35f, .55f)));
//        sdPatch.addSetting(new Setting("Decay", "Body Decay", new PointF(.55f, .42f)));
//        sdPatch.addSetting(new Setting("Band-pass", "Band-pass Q", new PointF(.7f, .6f)));
//        channels.add(new Channel("sd", sdPatch, new boolean[STEP_COUNT]));
//
//        Patch ttPatch = new Patch();
//        ttPatch.addSetting(new Setting("Pitch", "Gain", new PointF(.499f, .49f)));
//        channels.add(new Channel("tt", ttPatch, new boolean[STEP_COUNT]));
//
//        Patch hhPatch = new Patch();
//        hhPatch.addSetting(new Setting("Pitch", "Gain", new PointF(.45f, .4f)));
//        hhPatch.addSetting(new Setting("Low-pass", "Snap", new PointF(.8f, .1f)));
//        hhPatch.addSetting(new Setting("Noise Pitch", "Noise", new PointF(.55f, .6f)));
//        hhPatch.addSetting(new Setting("Ratio B", "Ratio A", new PointF(.9f, 1)));
//        hhPatch.addSetting(new Setting("Release", "Attack", new PointF(.55f, .4f)));
//        hhPatch.addSetting(new Setting("Filter", "Filter Q", new PointF(.7f, .6f)));
//        channels.add(new Channel("hh", hhPatch, new boolean[STEP_COUNT]));
//    }

    private void initGui() {
        setContentView(R.layout.activity_dmach);
        
        if (mSelectedChannel != -1) {
            LinearLayout channels = (LinearLayout) findViewById(R.id.channels);
            ImageButton channel = (ImageButton) channels.getChildAt(mSelectedChannel);
            if (channel != null) {
                channel.setSelected(true);
                // start patch fragment
            }
        } else {
            // start sequencer fragment
        }
//        getFragmentManager().beginTransaction().add(R.id.fragment_container,
//                SequencerFragment.newInstance(mSequence)).commit();
        getFragmentManager().beginTransaction().add(R.id.fragment_container,
                PatchFragment.newInstance(new Patch())).commit();
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
    }
    
    private void sendSequence() {
        for (int step = 0; step < STEPS; ++step) {
            PdBase.sendList("step", new Object[]{0, step, mSequence[step]});
            PdBase.sendList("step", new Object[]{1, step, mSequence[step + STEPS]});
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

//    private void setFragment() {
//        FragmentTransaction transaction = getFragmentManager().beginTransaction();
//        if (selectedChannelIndex != -1) {
//            transaction.replace(R.id.fragment_container,
//                    PatchFragment.newInstance(getSelectedChannel().getPatch()));
//            transaction.commit();
//        } else {
//            transaction.replace(R.id.fragment_container, SequencerFragment.newInstance(channels));
//            transaction.commit();
//            getFragmentManager().executePendingTransactions();
//        }
//    }

//    private Channel getSelectedChannel() {
//        return channels.get(selectedChannelIndex);
//    }

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
                
        mTempoText = (TextView) layout.findViewById(R.id.tempo_value);
        mTempoText.setText(" " + mTempo);
        SeekBar tempoSeekBar = (SeekBar) layout.findViewById(R.id.tempo_seekbar);
        tempoSeekBar.setProgress(mTempo);
        tempoSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                int tempo = progress + 1;
                PdBase.sendFloat("tempo", tempo);
                if (mTempoText != null) {
                    mTempoText.setText(" " + tempo);
                }
                mTempo = tempo;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        mShuffleText = (TextView) layout.findViewById(R.id.shuffle_value); 
        mShuffleText.setText(" " + mShuffle);
        SeekBar shuffleSeekBar = (SeekBar) layout.findViewById(R.id.shuffle_seekbar);
        shuffleSeekBar.setProgress(mShuffle);
        shuffleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
        });
    }

    public void onResetClicked(View view) {
        mSequence = new int[GROUPS * STEPS];
        sendSequence();
        SequencerView sequencer = (SequencerView) findViewById(R.id.sequencer);
        if (sequencer != null) {
            sequencer.setChecked(mSequence);
        }
//        if (selectedChannelIndex == -1) {
//            ((RelativeLayout) findViewById(R.id.fragment_container)).removeAllViews();
//            setFragment();
//        }
        System.out.println("onResetClicked");
    }

    public void onChannelClicked(View view) {
//        RadioGroup group = (RadioGroup) channel.getParent();
//        int index = group.indexOfChild(channel);
//        if (index != -1) {
//            if (index == selectedChannelIndex) {
//                group.clearCheck();
//                selectedChannelIndex = -1;
//            } else {
//                selectedChannelIndex = index;
//            }
//            setFragment();
//        }
//      
        ImageButton channel = (ImageButton) view;
        LinearLayout channels = (LinearLayout) channel.getParent();
        int index = channels.indexOfChild(channel);
        
        if (index != -1) {
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
        }
//        System.out.println("onChannelClicked " + channel.isSelected());
    }
    
    public void onSettingClicked(View channel) {
        System.out.println("onSettingClicked");
    }

    @Override
    public void onStepChanged(int channel, int step) {
        int mask = channel % (CHANNELS / GROUPS);
        int group = channel / (CHANNELS / GROUPS);
        int index = (group * STEPS) + step; 
        mSequence[index] ^= MASKS[mask];
        PdBase.sendList("step", new Object[]{group, step, mSequence[index]});
    }

    @Override
    public void onSettingIndexChanged(int index) {
//        getSelectedChannel().getPatch().setSelectedSettingIndex(index);
    }

    @Override
    public void onSettingPosChanged(PointF pos) {
//        getSelectedChannel().getPatch().setSelectedPos(pos);
//        String name = getSelectedChannel().getName();
//        int index = getSelectedChannel().getPatch().getSelectedSettingIndex();
//        PdBase.sendFloat(name + (2 * index), pos.getX());
//        PdBase.sendFloat(name + (2 * index + 1), pos.getY());
    }
}
