package com.example.hellfirewallpaper

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.hellfirewallpaper.ui.HellFireScreen
import com.example.hellfirewallpaper.ui.HellFireTheme

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(Localization.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HellFireTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HellFireScreen()
                }
            }
        }
    }
}