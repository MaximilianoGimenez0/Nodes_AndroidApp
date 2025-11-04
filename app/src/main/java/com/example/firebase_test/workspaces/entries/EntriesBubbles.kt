package com.example.firebase_test.workspaces.entries

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.firebase_test.CharacterLimitedOutlinedTextField
import com.example.firebase_test.R
import com.example.firebase_test.workspaces.ChecklistItem
import com.example.firebase_test.workspaces.WorkspaceEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun ProfileAvatar(userName: String, profilePicture: String) {
    val initials = userName.take(1).uppercase()

    val imageBitmap = remember(profilePicture) {
        try {
            val decodedBytes = Base64.decode(profilePicture, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)?.asImageBitmap()
        } catch (e: Exception) {
            Log.e("ImageBubble", "Error al decodificar Base64 a Bitmap", e)
            null
        }
    }

    if (imageBitmap != null) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = imageBitmap,
                // CAMBIADO (reutilizado)
                contentDescription = stringResource(R.string.common_profile_picture_description),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    } else {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

// --- 2. BURBUJA DE IMAGEN ---

@Composable
fun ImageBubble(
    entry: WorkspaceEntry, userName: String, isFromCurrentUser: Boolean, profilePicture: String
) {
    val base64String = entry.value ?: ""
    val date = entry.createdAt
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // CAMBIADOS
    val timeFormat = stringResource(R.string.chat_timestamp_format)
    val timeFormatter = remember(timeFormat) { SimpleDateFormat(timeFormat, Locale.getDefault()) }
    val timeString = date?.let { timeFormatter.format(it) }
        ?: stringResource(R.string.chat_timestamp_placeholder)

    val (rawBitmap, imageBitmap) = remember(base64String) {
        try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            bitmap to bitmap?.asImageBitmap()
        } catch (e: Exception) {
            Log.e("ImageBubble", "Error al decodificar Base64 a Bitmap", e)
            null to null
        }
    }

    val bubbleShape = if (isFromCurrentUser) {
        RoundedCornerShape(
            topStart = 16.dp, topEnd = 0.dp, bottomEnd = 16.dp, bottomStart = 16.dp
        )
    } else {
        RoundedCornerShape(
            topStart = 0.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        contentAlignment = if (isFromCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.Top) {

            if (!isFromCurrentUser) {
                ProfileAvatar(userName, profilePicture)
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(
                horizontalAlignment = if (isFromCurrentUser) Alignment.End else Alignment.Start
            ) {
                Text(
                    // CAMBIADO
                    text = if (isFromCurrentUser) stringResource(R.string.chat_user_me) else userName,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 2.dp, start = 4.dp, end = 4.dp)
                )

                val screenWidth = LocalConfiguration.current.screenWidthDp.dp

                Box(
                    modifier = Modifier
                        .widthIn(max = screenWidth * 0.75f)
                        .clip(bubbleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    if (imageBitmap != null) {
                        Image(
                            bitmap = imageBitmap,
                            // CAMBIADO
                            contentDescription = stringResource(R.string.chat_image_description),
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Categoría (Overlay)
                        if (entry.category != null && entry.category.isNotBlank()) {
                            Surface(
                                color = Color.Black.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(bottomEnd = 8.dp),
                                modifier = Modifier.align(Alignment.TopStart)
                            ) {
                                Row(
                                    Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Label,
                                        // CAMBIADO
                                        contentDescription = stringResource(R.string.chat_category_label),
                                        modifier = Modifier.size(14.dp),
                                        tint = Color.White
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = entry.category,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Timestamp (Overlay)
                        Surface(
                            color = Color.Black.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(topStart = 8.dp),
                            modifier = Modifier.align(Alignment.BottomEnd)
                        ) {
                            Text(
                                text = timeString,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        // CAMBIADOS (Mensajes de Toast)
                        val msgSuccess = stringResource(R.string.chat_image_save_success)
                        val msgError = stringResource(R.string.chat_image_save_error)
                        val msgProcessError =
                            stringResource(R.string.workspace_detail_image_process_error)

                        // Botón de descarga (Overlay)
                        Surface(
                            color = Color.Black.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(topEnd = 8.dp),
                            modifier = Modifier.align(Alignment.BottomStart)
                        ) {
                            IconButton(
                                onClick = {
                                    if (rawBitmap != null) {
                                        scope.launch(Dispatchers.IO) {
                                            val success = saveBitmapToStorage(context, rawBitmap)
                                            withContext(Dispatchers.Main) {
                                                if (success) {
                                                    Toast.makeText(
                                                        context, msgSuccess, Toast.LENGTH_SHORT
                                                    ).show()
                                                } else {
                                                    Toast.makeText(
                                                        context, msgError, Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        }
                                    } else {
                                        Toast.makeText(context, msgProcessError, Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                }) {
                                Icon(
                                    imageVector = Icons.Default.FileDownload,
                                    // CAMBIADO
                                    contentDescription = stringResource(R.string.chat_image_download_description),
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                    } else {
                        Box(
                            modifier = Modifier
                                .width(screenWidth * 0.5f)
                                .height(100.dp)
                                .padding(16.dp), contentAlignment = Alignment.Center
                        ) {
                            Text(
                                // CAMBIADO
                                text = stringResource(R.string.chat_image_load_error),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            if (isFromCurrentUser) {
                Spacer(modifier = Modifier.width(8.dp))
                ProfileAvatar(userName, profilePicture)
            }
        }
    }
}
// --- 3. BURBUJA DE CHECKLIST ---

@Composable
fun ChecklistBubble(
    entry: WorkspaceEntry,
    userName: String,
    isFromCurrentUser: Boolean,
    profilePicture: String,
    entryId: String,
    onItemCheckedChange: (Int, Boolean) -> Unit
) {
    val title = entry.value ?: ""
    val items = entry.items
    val date = entry.createdAt

    // CAMBIADOS
    val timeFormat = stringResource(R.string.chat_timestamp_format)
    val timeFormatter = remember(timeFormat) { SimpleDateFormat(timeFormat, Locale.getDefault()) }
    val timeString = date?.let { timeFormatter.format(it) }
        ?: stringResource(R.string.chat_timestamp_placeholder)


    val bubbleColor = if (isFromCurrentUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val bubbleShape = if (isFromCurrentUser) {
        RoundedCornerShape(
            topStart = 16.dp, topEnd = 0.dp, bottomEnd = 16.dp, bottomStart = 16.dp
        )
    } else {
        RoundedCornerShape(
            topStart = 0.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        contentAlignment = if (isFromCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.Top) {

            if (!isFromCurrentUser) {
                ProfileAvatar(userName, profilePicture)
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(
                horizontalAlignment = if (isFromCurrentUser) Alignment.End else Alignment.Start
            ) {
                Text(
                    // CAMBIADO
                    text = if (isFromCurrentUser) stringResource(R.string.chat_user_me) else userName,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 2.dp, start = 4.dp, end = 4.dp)
                )

                val screenWidth = LocalConfiguration.current.screenWidthDp.dp

                Box(
                    modifier = Modifier
                        .widthIn(max = screenWidth * 0.75f)
                        .clip(bubbleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Surface(
                        color = bubbleColor, shape = bubbleShape, shadowElevation = 1.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .widthIn(max = screenWidth * 0.75f)
                        ) {
                            CategoryDisplay(category = entry.category)

                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            items?.forEachIndexed { index, item ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Checkbox(
                                        checked = item.isChecked,
                                        enabled = true,
                                        onCheckedChange = { newCheckedState ->
                                            onItemCheckedChange(index, newCheckedState)
                                        })

                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = item.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        textDecoration = if (item.isChecked) {
                                            TextDecoration.LineThrough
                                        } else {
                                            null
                                        },
                                        color = if (item.isChecked) {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = timeString,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }
                }
            }

            if (isFromCurrentUser) {
                Spacer(modifier = Modifier.width(8.dp))
                ProfileAvatar(userName, profilePicture)
            }
        }
    }
}

// --- 4. BURBUJA DE MENSAJE ---

@Composable
fun MessageBubble(
    entry: WorkspaceEntry, userName: String, isFromCurrentUser: Boolean, profilePicture: String
) {
    val text = entry.value ?: ""
    val date = entry.createdAt

    // CAMBIADOS
    val timeFormat = stringResource(R.string.chat_timestamp_format)
    val timeFormatter = remember(timeFormat) { SimpleDateFormat(timeFormat, Locale.getDefault()) }
    val timeString = date?.let { timeFormatter.format(it) }
        ?: stringResource(R.string.chat_timestamp_placeholder)

    val bubbleColor = if (isFromCurrentUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val bubbleShape = if (isFromCurrentUser) {
        RoundedCornerShape(
            topStart = 16.dp, topEnd = 0.dp, bottomEnd = 16.dp, bottomStart = 16.dp
        )
    } else {
        RoundedCornerShape(
            topStart = 0.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        contentAlignment = if (isFromCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.Top) {

            if (!isFromCurrentUser) {
                ProfileAvatar(userName, profilePicture)
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(
                modifier = Modifier.weight(1f, fill = false),
                horizontalAlignment = if (isFromCurrentUser) Alignment.End else Alignment.Start
            ) {
                Text(
                    // CAMBIADO
                    text = if (isFromCurrentUser) stringResource(R.string.chat_user_me) else userName,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 2.dp, start = 4.dp, end = 4.dp)
                )

                val screenWidth = LocalConfiguration.current.screenWidthDp.dp
                Surface(
                    color = bubbleColor, shape = bubbleShape, shadowElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .widthIn(max = screenWidth * 0.75f)
                    ) {
                        CategoryDisplay(category = entry.category)

                        Text(
                            text = text, style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = timeString,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }

            if (isFromCurrentUser) {
                Spacer(modifier = Modifier.width(8.dp))
                ProfileAvatar(userName, profilePicture)
            }
        }
    }
}

// --- 5. BURBUJA DE UBICACIÓN ---

@Composable
fun LocationBubble(
    entry: WorkspaceEntry,
    isFromCurrentUser: Boolean,
    userName: String,
    profilePicture: String,
    onLocationClick: (Double, Double) -> Unit
) {
    val lat = entry.location?.latitude ?: return
    val lng = entry.location?.longitude ?: return
    val timeString = formatTimestamp(entry.createdAt)

    val bubbleColor = if (isFromCurrentUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val bubbleShape = if (isFromCurrentUser) {
        RoundedCornerShape(
            topStart = 16.dp, topEnd = 0.dp, bottomEnd = 16.dp, bottomStart = 16.dp
        )
    } else {
        RoundedCornerShape(
            topStart = 0.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        contentAlignment = if (isFromCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.Top) {

            if (!isFromCurrentUser) {
                ProfileAvatar(userName, profilePicture)
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(
                modifier = Modifier.weight(1f, fill = false),
                horizontalAlignment = if (isFromCurrentUser) Alignment.End else Alignment.Start
            ) {
                Text(
                    // CAMBIADO
                    text = if (isFromCurrentUser) stringResource(R.string.chat_user_me) else userName,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 2.dp, start = 4.dp, end = 4.dp)
                )

                val screenWidth = LocalConfiguration.current.screenWidthDp.dp

                Surface(
                    color = bubbleColor,
                    shape = bubbleShape,
                    shadowElevation = 1.dp,
                    modifier = Modifier.clickable { onLocationClick(lat, lng) }) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .widthIn(max = screenWidth * 0.75f)
                    ) {
                        CategoryDisplay(category = entry.category)

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val contentColor = if (isFromCurrentUser) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }

                            Icon(
                                imageVector = Icons.Default.PinDrop,
                                // CAMBIADO
                                contentDescription = stringResource(R.string.chat_location_icon_description),
                                tint = contentColor
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                // CAMBIADO (reutilizado)
                                text = entry.value
                                    ?: stringResource(R.string.workspace_location_shared),
                                style = MaterialTheme.typography.bodyMedium,
                                color = contentColor
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = timeString,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.align(Alignment.End),
                            color = if (isFromCurrentUser) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (isFromCurrentUser) {
                Spacer(modifier = Modifier.width(8.dp))
                ProfileAvatar(userName, profilePicture)
            }
        }
    }
}

// --- 6. HELPER PRIVADO DE FORMATO DE HORA ---
@Composable

private fun formatTimestamp(date: java.util.Date?): String {

    if (date == null) return ""

    return try {

        val sdf = java.text.SimpleDateFormat("HH:mm", Locale.getDefault())

        sdf.format(date)

    } catch (e: Exception) {

        Log.e("formatTimestamp", "Error al formatear fecha", e)

        ""

    }

}

// --- 7. HELPER PRIVADO PARA MOSTRAR CATEGORÍA ---
@Composable
private fun CategoryDisplay(category: String?) {
    if (category != null && category.isNotBlank()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Label,
                // CAMBIADO
                contentDescription = stringResource(R.string.chat_category_label),
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = category,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


// --- 8. BURBUJA DE RECORDATORIO ---

@Composable
fun ReminderBubble(
    entry: WorkspaceEntry,
    userName: String,
    isFromCurrentUser: Boolean,
    profilePicture: String,
    onItemStateChange: (index: Int, isChecked: Boolean) -> Unit
) {
    val TAG = "ReminderItem"

    val itemsList = entry.items as? List<ChecklistItem>
    val reminderItem = itemsList?.firstOrNull()
    // CAMBIADO
    val title = entry.value ?: stringResource(R.string.chat_reminder_default_title)
    val timestamp = reminderItem?.text?.toLongOrNull() ?: 0L
    val isChecked = reminderItem?.isChecked ?: false
    val date = entry.createdAt

    // CAMBIADOS
    val invalidTime = stringResource(R.string.chat_reminder_invalid_time)
    val dateFormat = stringResource(R.string.chat_reminder_date_format)

    val formattedAlarmTime = remember(timestamp, invalidTime, dateFormat) {
        if (timestamp == 0L) invalidTime
        else SimpleDateFormat(dateFormat, Locale.getDefault()).format(Date(timestamp))
    }

    val timeString = formatTimestamp(date)

    val hasAlarmPassed = remember(timestamp) {
        timestamp != 0L && timestamp < System.currentTimeMillis()
    }

    val bubbleColor = if (isFromCurrentUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val bubbleShape = if (isFromCurrentUser) {
        RoundedCornerShape(
            topStart = 16.dp, topEnd = 0.dp, bottomEnd = 16.dp, bottomStart = 16.dp
        )
    } else {
        RoundedCornerShape(
            topStart = 0.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        contentAlignment = if (isFromCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.Top) {

            if (!isFromCurrentUser) {
                ProfileAvatar(userName, profilePicture)
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(
                modifier = Modifier.weight(1f, fill = false),
                horizontalAlignment = if (isFromCurrentUser) Alignment.End else Alignment.Start
            ) {
                Text(
                    // CAMBIADO
                    text = if (isFromCurrentUser) stringResource(R.string.chat_user_me) else userName,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 2.dp, start = 4.dp, end = 4.dp)
                )

                val screenWidth = LocalConfiguration.current.screenWidthDp.dp

                Surface(
                    color = bubbleColor,
                    shape = bubbleShape,
                    shadowElevation = 1.dp,
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .widthIn(max = screenWidth * 0.75f)
                    ) {
                        CategoryDisplay(category = entry.category)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleMedium,
                                    textDecoration = if (hasAlarmPassed) TextDecoration.LineThrough else TextDecoration.None,
                                    color = if (hasAlarmPassed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Alarm,
                                        // CAMBIADO
                                        contentDescription = stringResource(R.string.chat_reminder_alarm_icon_description),
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = formattedAlarmTime,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (hasAlarmPassed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Switch(
                                checked = isChecked, onCheckedChange = { newState ->
                                    Log.d(
                                        TAG,
                                        "Switch toggleado: entryId=${entry.id}, nuevo estado=$newState"
                                    )
                                    onItemStateChange(0, newState)
                                }, enabled = !hasAlarmPassed
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = timeString,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.align(Alignment.End),
                            color = if (isFromCurrentUser) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (isFromCurrentUser) {
                Spacer(modifier = Modifier.width(8.dp))
                ProfileAvatar(userName, profilePicture)
            }
        }
    }
}

// --- (saveBitmapToStorage sin cambios en los strings) ---
private fun saveBitmapToStorage(context: Context, bitmap: Bitmap): Boolean {
    val displayName = "IMG_${System.currentTimeMillis()}.jpg"
    val mimeType = "image/jpeg"

    val collectionUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    } else {
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }

    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
        put(MediaStore.Images.Media.MIME_TYPE, mimeType)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Download/Nodes")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
    }

    var outputStream: OutputStream? = null
    var uri: android.net.Uri? = null
    val contentResolver = context.contentResolver

    try {
        uri = contentResolver.insert(collectionUri, contentValues)
        if (uri == null) {
            Log.e("SaveImage", "Error al crear MediaStore entry")
            return false
        }

        outputStream = contentResolver.openOutputStream(uri)
        if (outputStream == null) {
            Log.e("SaveImage", "Error al obtener output stream")
            return false
        }

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            contentResolver.update(uri, contentValues, null, null)
        }

        Log.d("SaveImage", "Imagen guardada con éxito en $uri")
        return true

    } catch (e: Exception) {
        Log.e("SaveImage", "Error al guardar imagen", e)
        uri?.let { contentResolver.delete(it, null, null) }
        return false
    } finally {
        outputStream?.close()
    }
}

// --- (CreateMessageDialog ya estaba usando resources) ---
@Composable
fun CreateMessageDialog(
    messageValue: String,
    onMessageChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.create_message_dialog_title)) },
        text = {
            CharacterLimitedOutlinedTextField(
                value = messageValue,
                onValueChange = onMessageChanged,
                maxLength = 250,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.create_message_dialog_label)) },
                minLines = 3,
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm, enabled = messageValue.isNotBlank()
            ) {
                Text(stringResource(R.string.common_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        })
}