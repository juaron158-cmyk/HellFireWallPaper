package com.example.hellfirewallpaper

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class FireNotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        val pkg = sbn?.packageName ?: return
        if (pkg == packageName || pkg.startsWith("android.")) return
        NotificationManager.impactFuego()
    }
}