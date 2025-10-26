package com.example.firebase_test.workspaces.entries

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton

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
                    Icon(Icons.Default.Message, "Mensaje")
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

                SmallFloatingActionButton(
                    onClick = { onReminder() },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Icon(Icons.Default.NotificationsActive, "Recordatorio")
                }

                Spacer(modifier = Modifier.height(12.dp))

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
                imageVector = Icons.Default.Add,
                contentDescription = "Crear entrada"
            )
        }
    }
}


@Composable
fun CreateMessageDialog(
    messageValue: String,
    onMessageChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Mandar un mensaje") }, text = {
        OutlinedTextField(
            value = messageValue,
            onValueChange = onMessageChanged,
            label = { Text("Mensaje") },
            singleLine = true
        )
    }, confirmButton = {
        Button(
            onClick = {
                if (messageValue.isNotBlank()) {
                    onConfirm()
                }
            }) {
            Text("Mandar")
        }
    }, dismissButton = {
        TextButton(onClick = onDismiss) {
            Text("Cancelar")
        }
    })
}

@Composable
fun JoinWorkspaceDialog(
    workspaceCode: String,
    onCodeChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Unirse a un Workspace") }, text = {
        OutlinedTextField(
            value = workspaceCode,
            onValueChange = onCodeChange,
            label = { Text("Código de invitación") },
            singleLine = true
        )
    }, confirmButton = {
        Button(
            onClick = {
                if (workspaceCode.isNotBlank()) {
                    onConfirm()
                }
            }) {
            Text("Unirse")
        }
    }, dismissButton = {
        TextButton(onClick = onDismiss) {
            Text("Cancelar")
        }
    })
}

@Composable
private fun ExtendedFabItem(
    width: Dp, text: String, icon: @Composable () -> Unit, onClick: () -> Unit
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        icon = icon,
        text = {
            Text(
                text = text, maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        },
        modifier = Modifier
            .width(width)
            .fillMaxWidth(fraction = 1f)
    )
}
