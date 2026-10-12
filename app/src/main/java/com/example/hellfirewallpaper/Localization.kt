package com.example.hellfirewallpaper


import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object Localization {
    fun wrap(context: Context): Context {
        val code = FirePrefs.getLang(context)
        if (code.isEmpty()) return context // seguir idioma del sistema
        val locale = Locale(code)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}