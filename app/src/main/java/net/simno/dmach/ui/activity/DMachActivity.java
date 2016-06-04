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

package net.simno.dmach.ui.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatTextView;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.afollestad.materialdialogs.MaterialDialog;

import net.simno.dmach.R;
import net.simno.dmach.model.Channel;
import net.simno.dmach.model.Patch;
import net.simno.dmach.model.Setting;
import net.simno.dmach.ui.view.PanView;
import net.simno.dmach.ui.view.SequencerView;
import net.simno.dmach.ui.view.SettingView;
import net.simno.dmach.ui.view.TypefaceButton;

import org.parceler.Parcels;
import org.puredata.android.io.AudioParameters;
import org.puredata.android.service.PdService;
import org.puredata.core.PdBase;
import org.puredata.core.utils.IoUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

import static butterknife.ButterKnife.findById;

public class DMachActivity extends AppCompatActivity {

    private static final String PREF_TITLE = "net.simno.dmach.PREF_TITLE";
    private static final String PREF_SEQUENCE = "net.simno.dmach.PREF_SEQUENCE";
    private static final String PREF_CHANNELS = "net.simno.dmach.PREF_CHANNELS";
    private static final String PREF_TEMPO = "net.simno.dmach.PREF_TEMPO";
    private static final String PREF_SWING = "net.simno.dmach.PREF_SWING";
    private static final String PREF_CHANNEL = "net.simno.dmach.PREF_CHANNEL";

    private static final int PATCH_REQUEST = 1;
    public static final int[] MASKS = {1, 2, 4};
    public static final int GROUPS = 2;
    public static final int CHANNELS = 6;
    public static final int STEPS = 16;

    @BindView(R.id.play_button) AppCompatImageButton playButton;
    @BindView(R.id.config_button) AppCompatImageButton configButton;
    @BindView(R.id.patch_button) AppCompatImageButton patchButton;
    @BindView(R.id.channel_container) LinearLayout channelContainer;
    @BindView(R.id.setting_container) LinearLayout settingContainer;
    @BindView(R.id.patch_container) RelativeLayout patchContainer;
    @BindView(R.id.sequencer_view) SequencerView sequencerView;
    @BindView(R.id.setting_view) SettingView settingView;
    @BindView(R.id.pan_view) PanView panView;

    private boolean isRunning;
    private int[] sequence;
    private int selectedChannel;
    private int tempo;
    private int swing;
    private List<Channel> channels;
    private String title;
    private AppCompatTextView tempoText;
    private AppCompatTextView swingText;
    private int pdPatch;
    private PdService pdService;
    private final Object lock = new Object();
    private final ServiceConnection pdConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            pdService = ((PdService.PdBinder)service).getService();
            startAudio();
            initPd();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };
    private final OnSeekBarChangeListener tempoListener = new OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            switch (seekBar.getId()) {
                case R.id.tempo_seekbar_10:
                    tempo = (progress + 1) * 10 + (tempo % 10);
                    break;
                case R.id.tempo_seekbar_1:
                    tempo = Math.max((tempo / 10) * 10 + progress, 1);
                    break;
            }
            PdBase.sendFloat("tempo", tempo);
            if (tempoText != null) {
                tempoText.setText(" ");
                tempoText.append(String.valueOf(tempo));
            }
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };
    private final OnSeekBarChangeListener swingListener = new OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
            PdBase.sendFloat("swing", progress / 100.0f);
            if (swingText != null) {
                swingText.setText(" ");
                swingText.append(String.valueOf(progress));
            }
            swing = progress;
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        restoreSettings();
        initUi();
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
        String sequenceJson = Patch.sequenceToJson(sequence);
        String channelsJson = Patch.channelsToJson(channels);
        getPreferences(MODE_PRIVATE).edit()
                .putString(PREF_TITLE, title)
                .putString(PREF_SEQUENCE, sequenceJson)
                .putString(PREF_CHANNELS, channelsJson)
                .putInt(PREF_TEMPO, tempo)
                .putInt(PREF_SWING, swing)
                .putInt(PREF_CHANNEL, selectedChannel)
                .apply();
    }

    private void restoreSettings() {
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        title = prefs.getString(PREF_TITLE, "untitled");
        String sequenceJson = prefs.getString(PREF_SEQUENCE, "");
        if (!sequenceJson.isEmpty()) {
            sequence = Patch.jsonToSequence(sequenceJson);
        } else {
            sequence = new int[GROUPS * STEPS];
        }
        String channelsJson = prefs.getString(PREF_CHANNELS, "");
        if (!channelsJson.isEmpty()) {
            channels = Patch.jsonToChannels(channelsJson);
        } else {
            channels = Channel.createChannels();
        }
        tempo = prefs.getInt(PREF_TEMPO, 120);
        swing = prefs.getInt(PREF_SWING, 0);
        selectedChannel = prefs.getInt(PREF_CHANNEL, -1);
    }

    private void initUi() {
        setContentView(R.layout.activity_dmach);
        ButterKnife.bind(this);

        sequencerView.setOnStepChangedListener(new SequencerView.OnStepChangedListener() {
            @Override
            public void onStepChanged(int group, int step, int mask, int index) {
                sequence[index] ^= mask;
                PdBase.sendList("step", group, step, sequence[index]);
            }
        });

        settingView.setOnSettingChangedListener(new SettingView.OnSettingChangedListener() {
            @Override
            public void onSettingChanged(Channel channel, float x, float y) {
                String name = channel.getName();
                PdBase.sendList(name, channel.getSetting().getHIndex(), x);
                PdBase.sendList(name, channel.getSetting().getVIndex(), y);
                channel.getSetting().setX(x);
                channel.getSetting().setY(y);
            }
        });

        panView.setOnPanChangedListener(new PanView.OnPanChangedListener() {
            @Override
            public void onPanChanged(Channel channel, float pan) {
                String name = channel.getName();
                PdBase.sendFloat(name + "p", pan);
                channel.setPan(pan);
            }
        });

        setView();
        setChannelSelection();
    }

    private void initSystemServices() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
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
    }

    private void initPdService() {
        new Thread() {
            @Override
            public void run() {
                bindService(new Intent(DMachActivity.this, PdService.class), pdConnection, BIND_AUTO_CREATE);
            }
        }.start();
    }

    private void initPd() {
        PdBase.sendFloat("swing", swing / 100.0f);
        PdBase.sendFloat("tempo", tempo);
        sendSequence();
        sendSettings();
    }

    private void sendSequence() {
        for (int step = 0; step < STEPS; ++step) {
            PdBase.sendList("step", 0, step, sequence[step]);
            PdBase.sendList("step", 1, step, sequence[step + STEPS]);
        }
    }

    private void sendSettings() {
        for (Channel channel : channels) {
            String name = channel.getName();
            PdBase.sendFloat(name + "p", channel.getPan());
            for (Setting setting : channel.getSettings()) {
                PdBase.sendList(name, setting.getHIndex(), setting.getX());
                PdBase.sendList(name, setting.getVIndex(), setting.getY());
            }
        }
    }

    private void startAudio() {
        synchronized (lock) {
            if (pdService == null) {
                return;
            }
            int sampleRate = AudioParameters.suggestSampleRate();
            try {
                pdService.initAudio(sampleRate, 0, 2, -1);
            } catch (IOException e) {
                finish();
                return;
            }
            if (pdPatch == 0) {
                try {
                    File dir = getFilesDir();
                    IoUtils.extractZipResource(getResources().openRawResource(R.raw.dmach), dir, true);
                    File patchFile = new File(dir, "dmach.pd");
                    pdPatch = PdBase.openPatch(patchFile.getAbsolutePath());
                } catch (IOException e) {
                    finish();
                    return;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {
                }
            }
            pdService.startAudio(new Intent(this, DMachActivity.class),
                    R.drawable.ic_stat_dmach, "DMach is running", "Touch to return.");
        }
    }

    private void stopAudio() {
        synchronized (lock) {
            if (pdService == null) {
                return;
            }
            pdService.stopAudio();
        }
    }

    private void cleanup() {
        synchronized (lock) {
            stopAudio();
            if (pdPatch != 0) {
                PdBase.closePatch(pdPatch);
                pdPatch = 0;
            }
            PdBase.release();
            try {
                unbindService(pdConnection);
            } catch (IllegalArgumentException e) {
                pdService = null;
            }
        }
    }

    private void setView() {
        if (selectedChannel == -1) {
            showSequencer();
        } else {
            showPatch();
        }
    }

    @OnClick(R.id.play_button)
    public void onPlayClicked() {
        if (isRunning) {
            PdBase.sendBang("stop");
        } else {
            PdBase.sendBang("play");
        }
        isRunning = !isRunning;
        playButton.setSelected(isRunning);
    }

    @OnClick(R.id.config_button)
    public void onConfigClicked() {
        configButton.setSelected(true);

        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .customView(R.layout.dialog_config, false)
                .backgroundColorRes(R.color.dune)
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        configButton.setSelected(false);
                    }
                })
                .show();

        View dialogView = dialog.getCustomView();
        if (dialogView == null) {
            return;
        }

        tempoText = findById(dialogView, R.id.tempo_value);
        tempoText.setText(" ");
        tempoText.append(String.valueOf(tempo));

        SeekBar tempoTenSeek = findById(dialogView, R.id.tempo_seekbar_10);
        tempoTenSeek.setProgress((tempo / 10) - 1);
        tempoTenSeek.setOnSeekBarChangeListener(tempoListener);

        SeekBar tempoOneSeek = findById(dialogView, R.id.tempo_seekbar_1);
        tempoOneSeek.setProgress(tempo % 10);
        tempoOneSeek.setOnSeekBarChangeListener(tempoListener);

        swingText = findById(dialogView, R.id.swing_value);
        swingText.setText(" ");
        swingText.append(String.valueOf(swing));

        SeekBar swingSeek = findById(dialogView, R.id.swing_seekbar);
        swingSeek.setProgress(swing);
        swingSeek.setOnSeekBarChangeListener(swingListener);
    }

    @OnClick(R.id.reset_button)
    public void onResetClicked() {
        sequence = new int[GROUPS * STEPS];
        sendSequence();
        setView();
    }

    @OnClick(R.id.patch_button)
    public void onPatchClicked() {
        patchButton.setSelected(true);
        Patch patch = new Patch(title, sequence, channels, selectedChannel, tempo, swing);
        Intent intent = new Intent(this, PatchActivity.class);
        intent.putExtra(PatchActivity.PATCH_EXTRA, Parcels.wrap(patch));
        startActivityForResult(intent, PATCH_REQUEST);
    }

    @OnLongClick(R.id.logo_text)
    public boolean onLogoClicked() {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .customView(R.layout.dialog_licenses, false)
                .show();

        View dialogView = dialog.getCustomView();
        if (dialogView == null) {
            return true;
        }

        WebView webView = findById(dialogView, R.id.web_view);
        webView.loadUrl("file:///android_asset/licenses.html");

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        patchButton.setSelected(false);
        if (requestCode == PATCH_REQUEST) {
            if (resultCode == PatchActivity.RESULT_LOADED) {
                Patch patch = Parcels.unwrap(data.getParcelableExtra(PatchActivity.PATCH_EXTRA));
                title = patch.getTitle();
                sequence = patch.getSequence();
                channels = patch.getChannels();
                selectedChannel = patch.getSelectedChannel();
                tempo = patch.getTempo();
                swing = patch.getSwing();
                initPd();
                setView();
                setChannelSelection();
            } else if (resultCode == PatchActivity.RESULT_SAVED) {
                title = data.getStringExtra(PatchActivity.TITLE_EXTRA);
            }
        }
    }

    @OnClick({R.id.channel_bd, R.id.channel_sd, R.id.channel_cp, R.id.channel_tt, R.id.channel_cb, R.id.channel_hh})
    public void onChannelClicked(TypefaceButton channel) {
        int index = channelContainer.indexOfChild(channel);
        selectedChannel = selectedChannel == index ? -1 : index;
        setView();
        setChannelSelection();
    }

    private void setChannelSelection() {
        for (int i = 0; i < CHANNELS; ++i) {
            Button channel = (Button) channelContainer.getChildAt(i);
            channel.setSelected(i == selectedChannel);
        }
    }

    private void showSequencer() {
        if (selectedChannel != -1) {
            return;
        }
        patchContainer.setVisibility(View.GONE);
        sequencerView.setVisibility(View.VISIBLE);

        if (sequence != null) {
            sequencerView.setChecked(sequence);
        }
    }

    private void showPatch() {
        if (selectedChannel == -1) {
            return;
        }
        sequencerView.setVisibility(View.GONE);
        patchContainer.setVisibility(View.VISIBLE);

        Channel channel = channels.get(selectedChannel);
        settingView.setChannel(channel);
        panView.setChannel(channel);

        // Enable or disable buttons depending on the number of settings
        int selected = channel.getSelection();
        int count = channel.getCount();
        for (int i = 0; i < settingContainer.getChildCount(); ++i) {
            TypefaceButton button = (TypefaceButton) settingContainer.getChildAt(i);
            if (i < count) {
                button.setEnabled(true);
                button.setVisibility(View.VISIBLE);
                button.setSelected(i == selected);
            } else {
                button.setEnabled(false);
                button.setVisibility(View.INVISIBLE);
            }
        }
    }

    @OnClick({R.id.setting_1, R.id.setting_2, R.id.setting_3, R.id.setting_4, R.id.setting_5, R.id.setting_6})
    public void onSettingClick(TypefaceButton button) {
        if (selectedChannel == -1) {
            return;
        }
        Channel channel = channels.get(selectedChannel);
        int index = settingContainer.indexOfChild(button);
        if (index != channel.getSelection()) {
            TypefaceButton oldButton = (TypefaceButton) settingContainer.getChildAt(channel.getSelection());
            if (oldButton != null) {
                oldButton.setSelected(false);
            }
            channel.selectSetting(index);
            button.setSelected(true);
            settingView.setChannel(channel);
        }
    }
}
