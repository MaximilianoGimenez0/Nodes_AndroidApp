package com.example.firebase_test.workspaces.entries

import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
// AÑADIDO
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.firebase_test.CategoryFilterRow
import com.example.firebase_test.CreateChecklistDialog
// AÑADIDO
import com.example.firebase_test.R
import com.example.firebase_test.viewmodels.AuthViewModel
import com.example.firebase_test.viewmodels.WorkspacesViewModel
import com.example.firebase_test.workspaces.ChecklistItem
import com.example.firebase_test.workspaces.EntryType
import com.example.firebase_test.workspaces.WorkspaceEntry
import kotlinx.coroutines.launch


@Composable
fun MyChatScreen(
    workspaceId: String?,
    modifier: Modifier = Modifier,
    workspacesViewModel: WorkspacesViewModel,
    authViewModel: AuthViewModel,
    contentNavController: NavController
) {
    val userId by authViewModel.userId.observeAsState()
    val viewModel = workspacesViewModel

    val entries by viewModel.workspaceEntries.collectAsStateWithLifecycle()
    val workspaceMembers by workspacesViewModel.workspaceMembers.collectAsState()
    val userInfo by workspacesViewModel.userInfo.collectAsStateWithLifecycle()


    val categories by viewModel.availableCategories.collectAsStateWithLifecycle()
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    val currentFilter: String? = selectedCategory

    val filteredEntries = remember(entries, currentFilter) {
        if (currentFilter == null) {
            entries
        } else {
            entries.filter { it.category == currentFilter }
        }
    }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val listState = rememberLazyListState()

    val isPrivateWorkspace = workspaceMembers.size < 2

    var isFabMenuExpanded by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(EntryType.NONE) }
    var messageEntry by remember { mutableStateOf("") }

    var showEntryOptionsDialog by remember { mutableStateOf(false) }
    var selectedEntry by remember { mutableStateOf<WorkspaceEntry?>(null) }
    var showCategoryDialog by remember { mutableStateOf(false) }

    val anonUser = stringResource(R.string.chat_anonymous_user)
    val imgError = stringResource(R.string.workspace_detail_image_process_error)
    val photoError = stringResource(R.string.workspace_detail_photo_process_error)
    val cameraDenied = stringResource(R.string.workspace_detail_camera_permission_denied)
    val gettingLocation = stringResource(R.string.chat_getting_location)
    val locationDenied = stringResource(R.string.chat_permission_location_denied)

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(), onResult = { uri ->
            uri?.let {
                val base64String = encodeUriToBase64(context, uri)
                if (base64String != null && workspaceId != null) {
                    viewModel.addWorkspaceEntry(
                        workspaceId = workspaceId,
                        value = base64String,
                        type = EntryType.IMAGE,
                        name = authViewModel.userProfile.value?.firstName ?: anonUser,
                        profilePicture = authViewModel.userProfile.value?.profilePicture ?: ""
                    )
                } else {
                    scope.launch { snackbarHostState.showSnackbar(imgError) }
                }
            }
            showDialog = EntryType.NONE
        })

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(), onResult = { bitmap ->
            bitmap?.let {
                val base64String = encodeBitmapToBase64(bitmap)
                if (base64String != null && workspaceId != null) {
                    viewModel.addWorkspaceEntry(
                        workspaceId = workspaceId,
                        value = base64String,
                        type = EntryType.IMAGE,
                        name = authViewModel.userProfile.value?.firstName ?: anonUser,
                        profilePicture = authViewModel.userProfile.value?.profilePicture ?: ""
                    )
                } else {
                    scope.launch { snackbarHostState.showSnackbar(photoError) }
                }
            }
            showDialog = EntryType.NONE
        })

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(), onResult = { isGranted ->
            if (isGranted) {
                cameraLauncher.launch()
            } else {
                showDialog = EntryType.NONE
                scope.launch { snackbarHostState.showSnackbar(cameraDenied) }
            }
        })

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(), onResult = { permissions ->
            if (permissions.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false)) {
                scope.launch {
                    snackbarHostState.showSnackbar(gettingLocation)
                }
                workspaceId?.let {
                    viewModel.shareCurrentLocation(
                        workspaceId = it,
                        name = authViewModel.userProfile.value?.firstName ?: anonUser,
                        profilePicture = authViewModel.userProfile.value?.profilePicture ?: ""
                    )
                }
            } else {
                scope.launch {
                    snackbarHostState.showSnackbar(locationDenied)
                }
            }
            showDialog = EntryType.NONE
        })



    LaunchedEffect(entries, isPrivateWorkspace) {
        if (isPrivateWorkspace) {
            val reminders = entries.filter { it.type == EntryType.REMINDER }
            for (entry in reminders) {
                val itemsList = entry.items as? List<ChecklistItem>
                val reminderItem = itemsList?.firstOrNull()
                val isChecked = reminderItem?.isChecked ?: false

                if (isChecked) AlarmScheduler.schedule(context, entry)
                else AlarmScheduler.cancel(context, entry.id)
            }
        } else {
            // Log.d("AlarmSync", "Workspace grupal. No se sincronizan alarmas.")
        }
    }

    LaunchedEffect(userInfo) {
        userInfo?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
                viewModel.clearUserInfo()
            }
        }
    }

    LaunchedEffect(workspaceId) {
        workspaceId?.let { viewModel.loadWorkspaceEntries(it) }
    }

    LaunchedEffect(entries.size) {
        if (entries.isNotEmpty()) {
            listState.animateScrollToItem(entries.size - 1)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
                isPrivate = isPrivateWorkspace
            )
        }) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            CategoryFilterRow(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { category ->
                    selectedCategory = if (selectedCategory == category) null else category
                })

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(top = 80.dp, bottom = 80.dp),
                reverseLayout = false
            ) {
                items(filteredEntries) { entry ->
                    val user = workspaceMembers.find { it.id == entry.userId }
                    val profilePicture = user?.profilePicture ?: ""
                    val isFromCurrentUser = entry.userId == userId

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerInput(entry.id) {
                                detectTapGestures(
                                    onLongPress = {
                                        selectedEntry = entry
                                        showEntryOptionsDialog = true
                                    })
                            }) {
                        when (entry.type) {
                            EntryType.MESSAGE -> {
                                MessageBubble(
                                    entry = entry,
                                    userName = entry.name,
                                    isFromCurrentUser = isFromCurrentUser,
                                    profilePicture = profilePicture
                                )
                            }

                            EntryType.IMAGE -> {
                                ImageBubble(
                                    entry = entry,
                                    userName = entry.name,
                                    isFromCurrentUser = isFromCurrentUser,
                                    profilePicture = profilePicture
                                )
                            }

                            EntryType.CHECK_LIST -> {
                                ChecklistBubble(
                                    entry = entry,
                                    userName = entry.name,
                                    isFromCurrentUser = isFromCurrentUser,
                                    profilePicture = profilePicture,
                                    entryId = entry.id,
                                    onItemCheckedChange = { itemIndex, newCheckedState ->
                                        workspaceId?.let {
                                            viewModel.updateChecklistItemState(
                                                workspaceId = it,
                                                entryId = entry.id,
                                                itemIndex = itemIndex,
                                                newCheckedState = newCheckedState
                                            )
                                        }
                                    })
                            }

                            EntryType.REMINDER -> {
                                ReminderBubble(
                                    entry = entry,
                                    userName = entry.name,
                                    isFromCurrentUser = isFromCurrentUser,
                                    profilePicture = profilePicture,
                                    onItemStateChange = { index, isChecked ->
                                        workspaceId?.let {
                                            viewModel.updateChecklistItemState(
                                                workspaceId = it,
                                                entryId = entry.id,
                                                itemIndex = index,
                                                newCheckedState = isChecked
                                            )
                                        }
                                    })
                            }

                            EntryType.LOCALIZATION -> {
                                if (entry.location != null) {
                                    LocationBubble(
                                        entry = entry,
                                        isFromCurrentUser = isFromCurrentUser,
                                        userName = entry.name,
                                        profilePicture = profilePicture,
                                        onLocationClick = { lat, lng ->
                                            contentNavController.navigate("map/$lat/$lng")
                                        })
                                }
                            }

                            else -> {}
                        }
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
                    workspaceId?.let {
                        viewModel.addWorkspaceEntry(
                            workspaceId = it,
                            value = messageEntry,
                            type = EntryType.MESSAGE,
                            name = authViewModel.userProfile.value?.firstName ?: anonUser,
                            profilePicture = authViewModel.userProfile.value?.profilePicture
                                ?: ""
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
                    workspaceId?.let {
                        viewModel.addWorkspaceEntry(
                            workspaceId = it,
                            value = title,
                            type = EntryType.CHECK_LIST,
                            name = authViewModel.userProfile.value?.firstName ?: anonUser,
                            profilePicture = authViewModel.userProfile.value?.profilePicture
                                ?: "",
                            items = items
                        )
                    }
                    showDialog = EntryType.NONE
                })
        }

        EntryType.IMAGE -> {
            AlertDialog(
                onDismissRequest = { showDialog = EntryType.NONE },
                title = { Text(stringResource(R.string.chat_add_image_title)) },
                text = { Text(stringResource(R.string.profile_pic_change_prompt)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }) { Text(stringResource(R.string.common_gallery)) }
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
                        }) { Text(stringResource(R.string.common_camera)) }
                })
        }

        EntryType.REMINDER -> {
            CreateReminderDialog(onDismiss = {
                showDialog = EntryType.NONE
            }, onConfirm = { title, items ->
                workspaceId?.let {
                    viewModel.addWorkspaceEntry(
                        workspaceId = it,
                        value = title,
                        type = EntryType.REMINDER,
                        name = authViewModel.userProfile.value?.firstName ?: anonUser,
                        profilePicture = authViewModel.userProfile.value?.profilePicture ?: "",
                        items = items
                    )
                }
                showDialog = EntryType.NONE
            })
        }

        EntryType.LOCALIZATION -> {
            AlertDialog(
                onDismissRequest = { showDialog = EntryType.NONE },

                title = { Text(stringResource(R.string.chat_share_location_title)) },
                text = { Text(stringResource(R.string.chat_share_location_prompt)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val locationPermissions = arrayOf(
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                            locationPermissionLauncher.launch(locationPermissions)
                        }) { Text(stringResource(R.string.confirm)) }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = EntryType.NONE }) {
                        Text(stringResource(R.string.common_cancel))
                    }
                })
        }

        EntryType.NONE -> {}
    }

    if (showEntryOptionsDialog) {
        val entryToManage = selectedEntry
        AlertDialog(onDismissRequest = {
            showEntryOptionsDialog = false
            selectedEntry = null
        },
            title = { Text(stringResource(R.string.chat_entry_options_title)) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.chat_entry_option_add_category),
                        modifier = Modifier
                            .clickable {
                                showEntryOptionsDialog = false
                                showCategoryDialog = true
                            }
                            .padding(vertical = 12.dp)
                            .fillMaxWidth())

                    Text(
                        text = stringResource(R.string.chat_entry_option_delete),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .clickable {
                                if (entryToManage != null && workspaceId != null) {
                                    viewModel.deleteWorkspaceEntry(
                                        workspaceId = workspaceId, entryId = entryToManage.id
                                    )
                                }
                                showEntryOptionsDialog = false
                                selectedEntry = null
                            }
                            .padding(vertical = 12.dp)
                            .fillMaxWidth())
                }
            }, confirmButton = {
                TextButton(
                    onClick = {
                        showEntryOptionsDialog = false
                        selectedEntry = null
                    }) {
                    Text(stringResource(R.string.common_close))
                }
            })
    }

    if (showCategoryDialog) {
        var categoryName by remember { mutableStateOf(selectedEntry?.category ?: "") }

        AlertDialog(onDismissRequest = {
            showCategoryDialog = false
            selectedEntry = null
        },

            title = { Text(stringResource(R.string.chat_assign_category_title)) },
            text = {
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = { Text(stringResource(R.string.chat_category_name_label)) },
                    singleLine = true
                )
            }, confirmButton = {
                TextButton(
                    onClick = {
                        if (workspaceId != null && selectedEntry != null) {
                            viewModel.updateEntryCategory(
                                workspaceId = workspaceId,
                                entryId = selectedEntry!!.id,
                                newCategory = categoryName
                            )
                        }
                        showCategoryDialog = false
                        selectedEntry = null
                    }) {
                    Text(stringResource(R.string.common_save))
                }
            }, dismissButton = {
                TextButton(
                    onClick = {
                        showCategoryDialog = false
                        selectedEntry = null
                    }) {
                    Text(stringResource(R.string.common_cancel))
                }
            })
    }
}