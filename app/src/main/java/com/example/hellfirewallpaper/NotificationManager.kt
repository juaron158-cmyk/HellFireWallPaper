package com.example.hellfirewallpaper

import com.example.hellfirewallpaper.FireWallpaperService.FireEngine

object NotificationManager {
    @Volatile
    var instance: FireEngine? = null

    fun impactFuego() {
        try {
            instance?.triggerImpact(0.7f)
        } catch (_: Exception) { }
    }
}