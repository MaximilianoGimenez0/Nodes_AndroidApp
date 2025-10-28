package com.example.firebase_test.workspaces.entries

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.firebase_test.workspaces.ChecklistItem
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
                contentDescription = "Foto de perfil",
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

@Composable
fun ImageBubble(
    base64String: String,
    userName: String,
    isFromCurrentUser: Boolean,
    date: Date?,
    profilePicture: String
) {

    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val timeString = date?.let { timeFormatter.format(it) } ?: "--:--"

    val imageBitmap = remember(base64String) {
        try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)?.asImageBitmap()
        } catch (e: Exception) {
            Log.e("ImageBubble", "Error al decodificar Base64 a Bitmap", e)
            null
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
                    text = if (isFromCurrentUser) "Yo" else userName,
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
                            contentDescription = "Imagen de chat",
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier.fillMaxWidth()
                        )

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
                    } else {
                        Box(
                            modifier = Modifier
                                .width(screenWidth * 0.5f)
                                .height(100.dp)
                                .padding(16.dp), contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Error al cargar imagen",
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

@Composable
fun ChecklistBubble(
    title: String,
    items: List<ChecklistItem>,
    userName: String,
    isFromCurrentUser: Boolean,
    date: Date?,
    profilePicture: String,
    entryId: String,
    onItemCheckedChange: (index: Int, isChecked: Boolean) -> Unit
) {
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val timeString = date?.let { timeFormatter.format(it) } ?: "--:--"

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
                    text = if (isFromCurrentUser) "Yo" else userName,
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
                                .widthIn(max = screenWidth * 0.75f) // Esto está bien
                        ) {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            items.forEachIndexed { index, item ->
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
                                        style = MaterialTheme.typography.bodyMedium
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


@Composable
fun MessageBubble(
    text: String,
    userName: String,
    isFromCurrentUser: Boolean,
    date: Date?,
    profilePicture: String // TODO -> Mostrar foto de perfil del usuario
) {

    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val timeString = date?.let { timeFormatter.format(it) } ?: "--:--"

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
                    text = if (isFromCurrentUser) "Yo" else userName,
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