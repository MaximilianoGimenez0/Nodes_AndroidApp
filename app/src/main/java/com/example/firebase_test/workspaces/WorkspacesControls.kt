package com.example.firebase_test.workspaces

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GroupAdd
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
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material3.MaterialTheme

enum class DialogType {
    NONE, CREATE_WORKSPACE, JOIN_WORKSPACE
}


@Composable
fun WorkspacesControls(
    isFabMenuExpanded: Boolean,
    onFabClicked: () -> Unit,
    onCreateClicked: () -> Unit,
    onJoinClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AnimatedVisibility(visible = isFabMenuExpanded) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ExtendedFloatingActionButton(
                    modifier = Modifier.width(140.dp),
                    onClick = onJoinClicked,
                    icon = { Icon(Icons.Default.GroupAdd, "Unirse a workspace") },
                    text = { Text("Unirse") },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )

                ExtendedFloatingActionButton(
                    modifier = Modifier.width(140.dp),
                    onClick = onCreateClicked,
                    icon = { Icon(Icons.Default.AddCircle, "Crear workspace") },
                    text = { Text("Crear") },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        FloatingActionButton(
            onClick = onFabClicked,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = modifier
        ) {
            val icon = if (isFabMenuExpanded) Icons.Default.Close else Icons.Default.Add
            Icon(icon, contentDescription = "Menú de acciones")
        }



    }
}

@Composable
fun CreateWorkspaceDialog(
    workspaceName: String,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Crear nuevo Workspace") }, text = {
        OutlinedTextField(
            value = workspaceName,
            onValueChange = onNameChange,
            label = { Text("Nombre del Workspace") },
            singleLine = true
        )
    }, confirmButton = {
        Button(
            onClick = {
                if (workspaceName.isNotBlank()) {
                    onConfirm()
                }
            }) {
            Text("Crear")
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