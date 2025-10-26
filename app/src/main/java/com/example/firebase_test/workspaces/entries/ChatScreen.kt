package com.example.firebase_test.workspaces.entries

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.firebase_test.viewmodels.AuthViewModel
import com.example.firebase_test.viewmodels.WorkspacesViewModel
import com.example.firebase_test.workspaces.EntryType
import kotlinx.coroutines.launch
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.core.content.ContextCompat


@Composable
fun MyChatScreen(
    workspaceId: String?,
    modifier: Modifier = Modifier,
    workspacesViewModel: WorkspacesViewModel,
    authViewModel: AuthViewModel
) {

    val userId by authViewModel.userId.observeAsState()

    LaunchedEffect(userId) {
        println("El UID actual es: $userId")
    }

    val viewModel = workspacesViewModel

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }

    val userInfo by workspacesViewModel.userInfo.collectAsStateWithLifecycle()

    LaunchedEffect(userInfo) {
        userInfo?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
                workspacesViewModel.clearUserInfo()
            }
        }
    }

    var isFabMenuExpanded by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(EntryType.NONE) }

    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(), onResult = { uri: Uri? ->
            if (uri != null) {
                val base64String = encodeUriToBase64(context, uri)
                if (base64String != null && workspaceId != null) {
                    // Si todo OK, envía la imagen codificada al ViewModel
                    viewModel.addWorkspaceEntry(
                        workspaceId = workspaceId,
                        value = base64String,
                        type = EntryType.IMAGE,
                        name = authViewModel.userProfile.value?.firstName ?: "Anonimo",
                        profilePicture = authViewModel.userProfile.value?.profilePicture ?: ""
                    )
                } else if (base64String == null) {
                    scope.launch { snackbarHostState.showSnackbar("Error al codificar la imagen") }
                }
            }
            showDialog = EntryType.NONE
        })

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(), onResult = { bitmap: Bitmap? ->
            if (bitmap != null) {
                val base64String = encodeBitmapToBase64(bitmap)
                if (base64String != null && workspaceId != null) {
                    // Si todo OK, envía la foto codificada al ViewModel
                    viewModel.addWorkspaceEntry(
                        workspaceId = workspaceId,
                        value = base64String,
                        type = EntryType.IMAGE,
                        name = authViewModel.userProfile.value?.firstName ?: "Anonimo",
                        profilePicture = authViewModel.userProfile.value?.profilePicture ?: ""
                    )
                } else if (base64String == null) {
                    scope.launch { snackbarHostState.showSnackbar("Error al procesar la foto") }
                }
            }
            showDialog = EntryType.NONE
        })

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(), onResult = { isGranted: Boolean ->
            if (isGranted) {
                cameraLauncher.launch()
            } else {
                showDialog = EntryType.NONE
                scope.launch {
                    snackbarHostState.showSnackbar("Permiso de cámara denegado.")
                }
            }
        })

    var messageEntry by remember { mutableStateOf("") }

    val entries by viewModel.workspaceEntries.collectAsStateWithLifecycle()

    LaunchedEffect(workspaceId) {
        if (workspaceId != null) {
            viewModel.loadWorkspaceEntries(workspaceId)
        }
    }


    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            EntriesControls(
                isFabMenuExpanded = isFabMenuExpanded,
                onFabClicked = { isFabMenuExpanded = !isFabMenuExpanded },
                modifier = Modifier,
                onMessage = {
                    isFabMenuExpanded = false
                    showDialog = EntryType.MESSAGE
                },
                onCheckList = {
                    isFabMenuExpanded = false
                    showDialog = EntryType.CHECK_LIST
                },
                onImage = {
                    isFabMenuExpanded = false
                    showDialog = EntryType.IMAGE
                },
                onReminder = {
                    isFabMenuExpanded = false
                    showDialog = EntryType.REMINDER
                },
                onLocation = {
                    isFabMenuExpanded = false
                    showDialog = EntryType.LOCALIZATION
                },
            )

        }) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding), contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp
            ), verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(entries) { entry ->

                val isFromCurrentUser = entry.userId == userId

                when (entry.type) {
                    EntryType.MESSAGE -> {
                        MessageBubble(
                            text = entry.value,
                            userName = entry.name,
                            isFromCurrentUser = isFromCurrentUser,
                            date = entry.createdAt,
                            profilePicture = entry.profilePicture
                        )
                    }

                    EntryType.IMAGE -> {
                        ImageBubble(
                            base64String = entry.value,
                            userName = entry.name,
                            isFromCurrentUser = isFromCurrentUser,
                            date = entry.createdAt,
                            profilePicture = entry.profilePicture
                        )
                    }

                    EntryType.CHECK_LIST -> {
                        ChecklistBubble(
                            title = entry.value,
                            items = entry.items,
                            userName = entry.name,
                            isFromCurrentUser = isFromCurrentUser,
                            date = entry.createdAt,
                            profilePicture = entry.profilePicture,
                            entryId = entry.id,
                            onItemCheckedChange = { itemIndex, newCheckedState ->
                                if (workspaceId != null) {
                                    viewModel.updateChecklistItemState(
                                        workspaceId = workspaceId,
                                        entryId = entry.id,
                                        itemIndex = itemIndex,
                                        newCheckedState = newCheckedState
                                    )
                                }
                            })
                    }

                    else -> {

                    }
                }
            }
        }


    }

    when (showDialog) {

        EntryType.MESSAGE -> {
            CreateMessageDialog(
                messageValue = messageEntry,
                onMessageChanged = { messageEntry = it },
                onConfirm = {
                    if (workspaceId != null) {
                        viewModel.addWorkspaceEntry(
                            workspaceId = workspaceId,
                            value = messageEntry,
                            type = EntryType.MESSAGE,
                            name = authViewModel.userProfile.value?.firstName ?: "Anonimo",
                            profilePicture = authViewModel.userProfile.value?.profilePicture ?: ""
                        )
                    }
                    showDialog = EntryType.NONE
                    messageEntry = ""
                },
                onDismiss = {
                    showDialog = EntryType.NONE
                    messageEntry = ""
                })
        }

        EntryType.CHECK_LIST -> {
            CreateChecklistDialog(
                onDismiss = { showDialog = EntryType.NONE },
                onConfirm = { title, items ->
                    if (workspaceId != null) {
                        viewModel.addWorkspaceEntry(
                            workspaceId = workspaceId,
                            value = title,
                            type = EntryType.CHECK_LIST,
                            name = authViewModel.userProfile.value?.firstName ?: "Anonimo",
                            profilePicture = authViewModel.userProfile.value?.profilePicture ?: "",
                            items = items
                        )
                    }
                    showDialog = EntryType.NONE
                })
        }

        EntryType.LOCALIZATION -> TODO()

        EntryType.IMAGE -> {
            AlertDialog(
                onDismissRequest = {
                showDialog = EntryType.NONE
            },
                title = { Text("Añadir imagen") },
                text = { Text("¿Desde dónde quieres seleccionar la imagen?") },

                confirmButton = {
                    TextButton(
                        onClick = {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }) {
                        Text("Galería")
                    }
                },

                dismissButton = {
                    TextButton(
                        onClick = {
                            val permission = android.Manifest.permission.CAMERA

                            val permissionCheckResult =
                                ContextCompat.checkSelfPermission(context, permission)

                            if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                                cameraLauncher.launch()
                            } else {
                                cameraPermissionLauncher.launch(permission)
                            }
                        }) {
                        Text("Cámara")
                    }
                })
        }

        EntryType.REMINDER -> TODO()
        EntryType.NONE -> {

        }
    }
}