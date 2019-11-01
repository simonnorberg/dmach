package net.simno.dmach

import android.app.Application
import android.os.Looper
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.plugins.RxJavaPlugins
import net.simno.dmach.db.Db

class DMachApp : Application() {
    val db: Db by lazy { Db.create(this) }

    override fun onCreate() {
        super.onCreate()
        RxJavaPlugins.setErrorHandler { error ->
            logError("DMachApp", "RxJava", error)
        }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler {
            AndroidSchedulers.from(Looper.getMainLooper(), true)
        }
    }
}
