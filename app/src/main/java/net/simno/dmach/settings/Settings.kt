package net.simno.dmach.settings

data class Settings(
    val ignoreAudioFocus: Boolean = false,
    val sequenceEnabled: Boolean = false,
    val soundEnabled: Boolean = false,
    val panEnabled: Boolean = false
) {
    val isAnyEnabled: Boolean = sequenceEnabled || soundEnabled || panEnabled
}
