package net.simno.dmach.playback

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaMetadata
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import net.simno.dmach.MainActivity
import net.simno.dmach.R
import net.simno.kortholt.Kortholt

class PlaybackService : Service() {

    private val notificationImage
        get() = ContextCompat.getDrawable(this, R.drawable.ic_launcher_foreground)?.toBitmap()

    private var mediaSession: MediaSessionCompat? = null

    override fun onCreate() {
        super.onCreate()
        Kortholt.create(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val contentTitle = intent?.getStringExtra(TITLE) ?: getString(R.string.app_name)
        val contentText = intent?.getStringExtra(TEMPO).orEmpty()
        startForeground(NOTIFICATION_ID, createNotification(contentTitle, contentText))
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        Kortholt.destroy()
        mediaSession?.release()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannelCompat.Builder(CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
            .setName(CHANNEL_NAME)
            .build()
        NotificationManagerCompat.from(applicationContext).createNotificationChannel(channel)
    }

    private fun createNotification(contentTitle: String, contentText: String): Notification {
        val metadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadata.METADATA_KEY_TITLE, contentTitle)
            .putString(MediaMetadata.METADATA_KEY_ARTIST, contentText)
            .putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, notificationImage)
            .build()

        mediaSession?.release()

        val session = MediaSessionCompat(this, CHANNEL_NAME)
        session.setMetadata(metadata)

        val mediaStyle = androidx.media.app.NotificationCompat.MediaStyle()
            .setMediaSession(session.sessionToken)

        mediaSession = session

        val intent = Intent(this, MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, CHANNEL_NAME)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setContentIntent(contentIntent)
            .setSmallIcon(R.drawable.ic_stat_playback)
            .setStyle(mediaStyle)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    companion object {
        private const val TITLE = "TITLE"
        private const val TEMPO = "TEMPO"
        private const val CHANNEL_NAME = "Playback"
        private const val NOTIFICATION_ID = 1337

        fun intent(
            context: Context,
            title: String? = null,
            tempo: String? = null
        ): Intent {
            return Intent(context, PlaybackService::class.java).apply {
                putExtra(TITLE, title)
                putExtra(TEMPO, tempo)
            }
        }
    }
}
