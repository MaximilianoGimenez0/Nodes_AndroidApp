package com.example.firebase_test.workspaces.entries

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import com.example.firebase_test.CharacterLimitedOutlinedTextField

@Composable
fun EntriesControls(
    isFabMenuExpanded: Boolean,
    onFabClicked: () -> Unit,
    modifier: Modifier = Modifier,
    onMessage: () -> Unit,
    onCheckList: () -> Unit,
    onImage: () -> Unit,
    onReminder: () -> Unit,
    onLocation: () -> Unit,
    isPrivate: Boolean
) {
    Column(
        modifier = modifier, horizontalAlignment = Alignment.End
    ) {

        AnimatedVisibility(visible = isFabMenuExpanded) {
            Column(horizontalAlignment = Alignment.End) {
                SmallFloatingActionButton(
                    onClick = { onMessage() },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Icon(Icons.AutoMirrored.Filled.Message, "Mensaje")
                }

                Spacer(modifier = Modifier.height(12.dp))

                SmallFloatingActionButton(
                    onClick = { onCheckList() },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Icon(Icons.Default.Checklist, "Checklist")
                }

                Spacer(modifier = Modifier.height(12.dp))

                SmallFloatingActionButton(
                    onClick = { onImage() },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Icon(Icons.Default.Image, "Imagen")
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isPrivate) {
                    SmallFloatingActionButton(
                        onClick = { onReminder() },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ) {
                        Icon(Icons.Default.NotificationsActive, "Recordatorio")
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                SmallFloatingActionButton(
                    onClick = { onLocation() },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Icon(Icons.Default.LocationOn, "Ubicación")
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        FloatingActionButton(
            onClick = { onFabClicked() },
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = modifier
        ) {
            Icon(
                imageVector = Icons.Default.Add, contentDescription = "Crear entrada"
            )
        }
    }
}


