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
        startForeground(
            NOTIFICATION_ID,
            createNotification(
                intent?.getStringExtra(TITLE) ?: getString(R.string.app_name),
                intent?.getStringExtra(TEMPO).orEmpty()
            )
        )
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopForeground(true)
        Kortholt.destroy()
        super.onDestroy()
    }

    private fun createNotification(title: String, tempo: String): Notification {
        val manager = getSystemService<NotificationManager>()
        val channel = manager?.getNotificationChannel(CHANNEL_NAME)
            ?: NotificationChannel(CHANNEL_NAME, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
        manager?.createNotificationChannel(channel)
        val intent = Intent(this, MachineActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, CHANNEL_NAME)
            .setContentTitle(title)
            .setContentText(tempo)
            .setContentIntent(pendingIntent)
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
