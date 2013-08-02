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

package net.simno.android.dmach;

import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

import com.michaelnovakjr.numberpicker.NumberPicker;
import com.michaelnovakjr.numberpicker.NumberPickerDialog;
import com.michaelnovakjr.numberpicker.NumberPickerDialog.OnNumberSetListener;

import net.simno.android.dmach.PatchFragment.OnPatchChangedListener;
import net.simno.android.dmach.R;
import net.simno.android.dmach.model.Channel;
import net.simno.android.dmach.model.Patch;
import net.simno.android.dmach.model.PointF;
import net.simno.android.dmach.model.Setting;
import net.simno.android.dmach.view.ProgressBarView;

import org.puredata.android.io.AudioParameters;
import org.puredata.android.service.PdService;
import org.puredata.android.utils.PdUiDispatcher;
import org.puredata.core.PdBase;
import org.puredata.core.PdListener;
import org.puredata.core.utils.IoUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * DMachActivity is the only activity in the application.
 */
public class DMachActivity extends Activity
implements OnPatchChangedListener, OnNumberSetListener {

    /**
     * Interface definition for a callback to be invoked when the sequencer sends a new beat.
     */
    public interface OnBeatListener {
        /**
         * Called when the sequencer sends a new beat.
         *
         * @param beat The index of the step that is beating.
         */
        public void onBeat(int beat);
    }

    /**
     * Interface definition for a callback to be invoked when the listener should show or hide
     * itself.
     */
    public interface OnVisibilityListener {
        /**
         * Called when the lister should show itself.
         */
        public void onShow();

        /**
         * Called when the lister should hide itself.
         */
        public void onHide();
    }

    /**
     * Interface definition for a callback to be invoked when the tempo has changed.
     */
    public interface OnTempoChangedListener {
        /**
         * Called when the tempo has changed
         *
         * @param tempo The new tempo in BPM
         */
        public void onTempoChanged(int tempo);
    }

    static final int CHANNEL_COUNT = 4;
    static final int STEP_COUNT = 8;
    private final String TAG = this.getClass().getSimpleName();

    private boolean isRunning;
    private int patch;
    private int selectedChannelIndex = -1;
    private int mTempo = 120;
    private ArrayList<Channel> channels;
    private PdService pdService;
    private PdUiDispatcher dispatcher;
    private ProgressBarView progressBarView;
    private final Object lock = new Object();
    private final ServiceConnection pdConnection = new ServiceConnection() {
        /* (non-Javadoc)
         * @see android.content.ServiceConnection#onServiceConnected(android.content.ComponentName,
         * android.os.IBinder)
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            pdService = ((PdService.PdBinder)service).getService();
            initPd();
        }

        /* (non-Javadoc)
         * @see android.content.ServiceConnection#onServiceDisconnected(android.content.ComponentName)
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            // This method will never be called
        }
    };

    /* (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initChannels();
        initGui();
        initSystemServices();
        initPdService();
        Log.d(TAG, "onCreate");
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onDestroy()
     */
    @Override
    protected void onDestroy() {
        cleanup();
        Log.d(TAG, "onDestroy " + Integer.toString(getChangingConfigurations(), 16));
        super.onDestroy();
    }

    /**
     * Initializes the list of channels. Adds a patch to each channel and adds settings to each
     * patch.
     */
    private void initChannels() {
        channels = new ArrayList<Channel>(CHANNEL_COUNT);

        Patch bdPatch = new Patch();
        bdPatch.addSetting(new Setting("Pitch A", "Gain", new PointF(.4f, .49f)));
        bdPatch.addSetting(new Setting("Low-pass", "Square", new PointF(.7f, 0)));
        bdPatch.addSetting(new Setting("Pitch B", "Curve Time", new PointF(.4f, .4f)));
        bdPatch.addSetting(new Setting("Decay", "Noise Level", new PointF(.49f, .7f)));
        channels.add(new Channel("bd", bdPatch, new boolean[STEP_COUNT]));

        Patch sdPatch = new Patch();
        sdPatch.addSetting(new Setting("Pitch", "Gain", new PointF(.49f , .45f)));
        sdPatch.addSetting(new Setting("Low-pass", "Noise", new PointF(.6f, .8f)));
        sdPatch.addSetting(new Setting("X-fade", "Attack", new PointF(.35f, .55f)));
        sdPatch.addSetting(new Setting("Decay", "Body Decay", new PointF(.55f, .42f)));
        sdPatch.addSetting(new Setting("Band-pass", "Band-pass Q", new PointF(.7f, .6f)));
        channels.add(new Channel("sd", sdPatch, new boolean[STEP_COUNT]));

        Patch ttPatch = new Patch();
        ttPatch.addSetting(new Setting("Pitch", "Gain", new PointF(.499f, .49f)));
        channels.add(new Channel("tt", ttPatch, new boolean[STEP_COUNT]));

        Patch hhPatch = new Patch();
        hhPatch.addSetting(new Setting("Pitch", "Gain", new PointF(.45f, .4f)));
        hhPatch.addSetting(new Setting("Low-pass", "Snap", new PointF(.8f, .1f)));
        hhPatch.addSetting(new Setting("Noise Pitch", "Noise", new PointF(.55f, .6f)));
        hhPatch.addSetting(new Setting("Ratio B", "Ratio A", new PointF(.9f, 1)));
        hhPatch.addSetting(new Setting("Release", "Attack", new PointF(.55f, .4f)));
        hhPatch.addSetting(new Setting("Filter", "Filter Q", new PointF(.7f, .6f)));
        channels.add(new Channel("hh", hhPatch, new boolean[STEP_COUNT]));

        Log.d(TAG, "initChannels");
    }

    /**
     * Initializes the GUI. Creates a SequencerFragment. Then creates the ProgressBarView when the
     * layout phase is finished.
     */
    private void initGui() {
        setContentView(R.layout.activity_dmach);
        getFragmentManager().beginTransaction()
        .add(R.id.fragment_container, SequencerFragment.newInstance(channels)).commit();
        final RelativeLayout container = (RelativeLayout) findViewById(R.id.fragment_container);
        container.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @SuppressLint("NewApi")
            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                initProgressBar(container.getWidth(), container.getHeight());
                addProgressBar();
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    container.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    container.getViewTreeObserver().removeOnGlobalLayoutListener(this);    
                }
            }
        });
        Log.d(TAG, "initGui");
    }

    /**
     * Initializes progressBarView
     * 
     * @param width The width in pixels of the parent view
     * @param height The height in pixels of the parent view
     */
    private void initProgressBar(int width, int height) {
        progressBarView = new ProgressBarView(this, width, height, STEP_COUNT);
        Log.d(TAG, "initProgressBar");
    }

    private void addProgressBar() {
        ((RelativeLayout) findViewById(R.id.fragment_container)).addView(progressBarView);
        Log.d(TAG, "addProgressBar");
    }

    private void removeProgressBar() {
        ((RelativeLayout) findViewById(R.id.fragment_container)).removeView(progressBarView);
        Log.d(TAG, "removeProgressBar");
    }

    /**
     * Makes the audio stop when there is a phone call
     */
    private void initSystemServices() {
        TelephonyManager telephonyManager =
                (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                synchronized (lock) {
                    if (pdService == null) {
                        return;
                    }
                    if (state == TelephonyManager.CALL_STATE_IDLE) {
                        if (!pdService.isRunning()) {
                            startAudio();
                        }
                    } else {
                        if (pdService.isRunning()) {
                            stopAudio();
                        }
                    }
                }
            }
        }, PhoneStateListener.LISTEN_CALL_STATE);
        Log.d(TAG, "initSystemServices");
    }

    private void initPdService() {
        new Thread() {
            @Override
            public void run() {
                bindService(new Intent(DMachActivity.this, PdService.class),pdConnection,
                        BIND_AUTO_CREATE);
            }
        }.start();
        Log.d(TAG, "initPdService");
    }

    private void initPd() {
        dispatcher = new PdUiDispatcher();
        PdBase.setReceiver(dispatcher);
        dispatcher.addListener("beat", new PdListener.Adapter() {
            @Override
            public void receiveFloat(String source, float x) {
                progressBarView.onBeat((int) x);
            }
        });
        startAudio();
        setTempo(mTempo);
        Log.d(TAG, "initPd");
    }

    /**
     * Sets sample rate, loads the patch then starts the audio 
     */
    private void startAudio() {
        synchronized (lock) {
            if (pdService == null) {
                return;
            }
            int sampleRate = AudioParameters.suggestSampleRate();
            try {
                pdService.initAudio(sampleRate, 0, 2, -1);
            } catch (IOException e) {
                Log.e(TAG, e.toString());
                finish();
                return;
            }
            if (patch == 0) {
                try {
                    File dir = getFilesDir();
                    IoUtils.extractZipResource(getResources()
                            .openRawResource(R.raw.dmach), dir, true);
                    File patchFile = new File(dir, "dmach.pd");
                    patch = PdBase.openPatch(patchFile.getAbsolutePath());
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                    finish();
                    return;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {}
            }
            pdService.startAudio(new Intent(this, DMachActivity.class),
                    R.drawable.ic_stat_notify_dmach, "DMach", "Return to DMach.");
        }
        Log.d(TAG, "startAudio");
    }

    private void stopAudio() {
        synchronized (lock) {
            if (pdService == null) {
                return;
            }
            pdService.stopAudio();
        }
        Log.d(TAG, "stopAudio");
    }

    private void cleanup() {
        synchronized (lock) {
            stopAudio();
            if (patch != 0) {
                PdBase.closePatch(patch);
                patch = 0;
            }
            dispatcher.release();
            PdBase.release();
            try {
                unbindService(pdConnection);
            } catch (IllegalArgumentException e) {
                pdService = null;
            }
        }
        Log.d(TAG, "cleanup");
    }

    /**
     * Switches between a PatchFragment and a SequencerFragment
     */
    private void setFragment() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        if (selectedChannelIndex != -1) {
            transaction.replace(R.id.fragment_container,
                    PatchFragment.newInstance(getSelectedChannel().getPatch()));
            transaction.commit();
            removeProgressBar();
        } else {
            transaction.replace(R.id.fragment_container, SequencerFragment.newInstance(channels));
            transaction.commit();
            getFragmentManager().executePendingTransactions();
            addProgressBar();
        }
        Log.d(TAG, "setFragment");
    }

    private Channel getSelectedChannel() {
        return channels.get(selectedChannelIndex);
    }

    /**
     * @param tempo The new tempo in BPM
     */
    private void setTempo(int tempo) {
        mTempo = tempo;
        PdBase.sendFloat("tempo", mTempo);
        ((Button) findViewById(R.id.tempoButton)).setText("" + mTempo);
        progressBarView.onTempoChanged(mTempo);
        Log.d(TAG, "setTempo: " + mTempo);
    }

    /**
     * @param view The play button
     */
    public void onPlayClicked(View view) {
        ImageButton imageButton = (ImageButton) view;
        if (true == isRunning) {
            imageButton.setImageResource(R.drawable.play);
            progressBarView.onHide();
        } else {
            imageButton.setImageResource(R.drawable.stop);
            progressBarView.onShow();
        }
        isRunning = !isRunning;
        PdBase.sendBang("run");
        Log.d(TAG, "onPlayClicked");
    }

    /**
     * @param view The tempo button
     */
    public void onTempoClicked(View view) {
        int tempo = Integer.parseInt((String) ((Button) view).getText());
        NumberPickerDialog dialog = new NumberPickerDialog(this, -1, 120);
        NumberPicker picker = dialog.getNumberPicker();
        picker.setRange(1, 1000);
        picker.setCurrent(tempo);
        picker.setSpeed(50);
        dialog.setOnNumberSetListener(this);
        dialog.show();
        Log.d(TAG, "onTempoClicked");
    }

    /**
     * @param view The reset button
     */
    public void onResetClicked(View view) {
        for (Channel channel : channels) {
            for (int i = 0; i < STEP_COUNT; ++i) {
                if (true == channel.getStep(i)) {
                    channel.setStep(i, false);
                    PdBase.sendFloat(channel.getName(), i);
                }
            }
        }
        if (selectedChannelIndex == -1) {
            ((RelativeLayout) findViewById(R.id.fragment_container)).removeAllViews();
            setFragment();
        }
        Log.d(TAG, "onResetClicked");
    }

    /**
     * @param channel The channel radio button that was clicked
     */
    public void onChannelClicked(View channel) {
        RadioGroup group = (RadioGroup) channel.getParent();
        int index = group.indexOfChild(channel);
        if (index != -1) {
            if (index == selectedChannelIndex) {
                group.clearCheck();
                selectedChannelIndex = -1;
            } else {
                selectedChannelIndex = index;
            }
            setFragment();
        }
        Log.d(TAG, "onChannelClicked");
    }

    /**
     * @param step The step toggle button that was clicked
     */
    public void onStepClicked(View step) {
        ViewGroup stepGroup = (ViewGroup) step.getParent();
        ViewGroup groupContainer = (ViewGroup) stepGroup.getParent();
        Channel channel = channels.get(groupContainer.indexOfChild(stepGroup));
        int buttonIndex = stepGroup.indexOfChild(step);
        boolean status = ((ToggleButton) step).isChecked();
        channel.setStep(buttonIndex, status);
        PdBase.sendFloat(channel.getName(), buttonIndex);
        Log.d(TAG, "onStepClicked " + channel.getName() + " " + buttonIndex);
    }

    /* (non-Javadoc)
     * @see com.michaelnovakjr.numberpicker.NumberPickerDialog.OnNumberSetListener#onNumberSet(int)
     */
    @Override
    public void onNumberSet(int selectedNumber) {
        if (selectedNumber != mTempo) setTempo(selectedNumber);
        Log.d(TAG, "onNumberSet: " + selectedNumber);
    }

    /* (non-Javadoc)
     * @see net.simno.android.dmach.PatchFragment.OnPatchChangedListener#onSettingIndexChanged(int)
     */
    @Override
    public void onSettingIndexChanged(int index) {
        getSelectedChannel().getPatch().setSelectedSettingIndex(index);
        Log.d(TAG, "onSettingIndexChanged");
    }

    /* (non-Javadoc)
     * @see net.simno.android.dmach.PatchFragment
     * .OnPatchChangedListener#onSettingPosChanged(net.simno.android.dmach.model.PointF)
     */
    @Override
    public void onSettingPosChanged(PointF pos) {
        getSelectedChannel().getPatch().setSelectedPos(pos);
        String name = getSelectedChannel().getName();
        int index = getSelectedChannel().getPatch().getSelectedSettingIndex();
        PdBase.sendFloat(name + (2 * index), pos.getX());
        PdBase.sendFloat(name + (2 * index + 1), pos.getY());
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onBackPressed()
     */
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
        .setIcon(R.drawable.ic_launcher_dmach)
        .setTitle("Closing DMach")
        .setMessage("Are you sure you want to close DMach?")
        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        })
        .setNegativeButton("No", null)
        .show();
        Log.d(TAG, "onBackPressed");
    }
}