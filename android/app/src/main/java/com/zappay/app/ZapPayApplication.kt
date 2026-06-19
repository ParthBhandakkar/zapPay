package com.zappay.app

import android.app.Application
import com.zappay.app.util.NotificationHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ZapPayApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)
    }
}
