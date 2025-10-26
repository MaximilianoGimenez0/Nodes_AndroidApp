package com.example.firebase_test.workspaces.entries

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.ByteArrayOutputStream


@Composable
fun ImageCaptureAndEncode() {

    val context = LocalContext.current
    val TAG = "ImageBase64"

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                Log.d(TAG, "URI de Galería seleccionada: $uri")
                val base64String = encodeUriToBase64(context, uri)
                if (base64String != null) {
                    Log.d(TAG, "Base64 de Galería (primeros 100 chars): ${base64String.take(100)}")
                } else {
                    Log.e(TAG, "Error al codificar la imagen de galería.")
                }
            } else {
                Log.d(TAG, "El usuario no seleccionó ninguna imagen de la galería.")
            }
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = { bitmap: Bitmap? ->
            if (bitmap != null) {
                Log.d(TAG, "Bitmap de Cámara capturado.")
                val base64String = encodeBitmapToBase64(bitmap)
                if (base64String != null) {
                    Log.d(TAG, "Base64 de Cámara (primeros 100 chars): ${base64String.take(100)}")
                } else {
                    Log.e(TAG, "Error al codificar el bitmap de la cámara.")
                }
            } else {
                Log.d(TAG, "El usuario canceló la captura de foto.")
            }
        }
    )

    Column {
        Button(onClick = {
            galleryLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }) {
            Text("Seleccionar de Galería")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            cameraLauncher.launch()
        }) {
            Text("Tomar Foto")
        }
    }
}

fun encodeUriToBase64(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes()
        inputStream?.close()
        bytes?.let {
            Base64.encodeToString(it, Base64.DEFAULT)
        }
    } catch (e: Exception) {
        Log.e("Base64Encoder", "Error al codificar URI a Base64", e)
        null
    }
}

fun encodeBitmapToBase64(bitmap: Bitmap): String? {
    return try {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val bytes = outputStream.toByteArray()
        Base64.encodeToString(bytes, Base64.DEFAULT)
    } catch (e: Exception) {
        Log.e("Base64Encoder", "Error al codificar Bitmap a Base64", e)
        null
    }
}
