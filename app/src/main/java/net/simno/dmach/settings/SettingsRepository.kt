package net.simno.dmach.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(
    private val preferences: DataStore<Preferences>
) {
    val settings: Flow<Settings> = preferences.data.map { data ->
        Settings(
            ignoreAudioFocus = data[IGNORE_AUDIO_FOCUS] == true,
            sequenceEnabled = data[SEQUENCE_ENABLED] != false,
            soundEnabled = data[SOUND_ENABLED] != false,
            panEnabled = data[PAN_ENABLED] != false
        )
    }

    suspend fun updateSettings(settings: Settings) {
        preferences.edit { prefs ->
            prefs[IGNORE_AUDIO_FOCUS] = settings.ignoreAudioFocus
            prefs[SEQUENCE_ENABLED] = settings.sequenceEnabled
            prefs[SOUND_ENABLED] = settings.soundEnabled
            prefs[PAN_ENABLED] = settings.panEnabled
        }
    }

    companion object {
        private val IGNORE_AUDIO_FOCUS = booleanPreferencesKey("IGNORE_AUDIO_FOCUS")
        private val SEQUENCE_ENABLED = booleanPreferencesKey("SEQUENCE_ENABLED")
        private val SOUND_ENABLED = booleanPreferencesKey("SOUND_ENABLED")
        private val PAN_ENABLED = booleanPreferencesKey("PAN_ENABLED")
    }
}
