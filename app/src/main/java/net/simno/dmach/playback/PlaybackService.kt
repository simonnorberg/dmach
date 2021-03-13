package net.simno.dmach.playback

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import net.simno.dmach.R
import net.simno.dmach.machine.MachineActivity
import net.simno.kortholt.Kortholt

class PlaybackService : Service() {

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
        stopForeground(true)
        Kortholt.destroy()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_NAME, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
        getSystemService<NotificationManager>()?.createNotificationChannel(channel)
    }

    private fun createNotification(contentTitle: String, contentText: String): Notification {
        val intent = Intent(this, MachineActivity::class.java)
        val contentIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, CHANNEL_NAME)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setContentIntent(contentIntent)
            .setSmallIcon(R.drawable.ic_stat_playback)
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
