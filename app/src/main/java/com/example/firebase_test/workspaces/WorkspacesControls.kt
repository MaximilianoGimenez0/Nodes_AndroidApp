package com.example.firebase_test.workspaces

import androidx.compose.ui.res.stringResource
import com.example.firebase_test.R
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material3.MaterialTheme
import com.example.firebase_test.CharacterLimitedOutlinedTextField

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
                    icon = { Icon(Icons.Default.GroupAdd, stringResource(R.string.workspaces_fab_join_description)) },
                    text = { Text(stringResource(R.string.workspaces_fab_join_text)) },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )

                ExtendedFloatingActionButton(
                    modifier = Modifier.width(140.dp),
                    onClick = onCreateClicked,
                    icon = { Icon(Icons.Default.AddCircle, stringResource(R.string.workspaces_fab_create_description)) },
                    text = { Text(stringResource(R.string.workspaces_fab_create_text)) },
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
            Icon(icon, contentDescription = stringResource(R.string.workspaces_fab_menu_description))
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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.create_workspace_dialog_title)) },
        text = {
            CharacterLimitedOutlinedTextField(
                value = workspaceName,
                onValueChange = onNameChange,
                maxLength = 25,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.create_workspace_dialog_label)) },
                singleLine = true,
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = workspaceName.isNotBlank()
            ) {
                Text(stringResource(R.string.create_workspace_dialog_confirm_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}

@Composable
fun JoinWorkspaceDialog(
    workspaceCode: String,
    onCodeChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.join_workspace_dialog_title)) },
        text = {
            CharacterLimitedOutlinedTextField(
                value = workspaceCode,
                onValueChange = onCodeChange,
                maxLength = 6,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.join_workspace_dialog_label)) },
                singleLine = true,
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = workspaceCode.isNotBlank()
            ) {
                Text(stringResource(R.string.join_workspace_dialog_confirm_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}