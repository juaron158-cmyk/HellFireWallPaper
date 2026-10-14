package com.example.hellfirewallpaper.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.hellfirewallpaper.CropActivity
import com.example.hellfirewallpaper.FirePrefs
import com.example.hellfirewallpaper.R

@Composable
fun SeccionImagen(onChanged: () -> Unit) {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf(FirePrefs.getImageUri(context)) }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            FirePrefs.saveImageUri(context, uri.toString())
            FirePrefs.saveCrop(context, 1f, 0f, 0f)
            imageUri = uri.toString()
            onChanged()
        }
    }

    Button(
        onClick = { picker.launch(arrayOf("image/*")) },
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Text(stringResource(if (imageUri == null) R.string.choose_bg else R.string.change_img))
    }

    if (imageUri != null) {
        Text(stringResource(R.string.img_selected), modifier = Modifier.padding(top = 8.dp))

        Button(
            onClick = { context.startActivity(Intent(context, CropActivity::class.java)) },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(stringResource(R.string.crop_img))
        }

        Button(
            onClick = {
                try {
                    context.contentResolver.releasePersistableUriPermission(
                        Uri.parse(imageUri),
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) { }
                FirePrefs.saveImageUri(context, null)
                FirePrefs.saveCrop(context, 1f, 0f, 0f)
                imageUri = null
                onChanged()
            },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(stringResource(R.string.remove_img))
        }
    }
}