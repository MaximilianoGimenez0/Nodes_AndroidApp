package com.example.firebase_test.workspaces

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.firebase_test.CharacterLimitedOutlinedTextField
import com.example.firebase_test.viewmodels.AuthViewModel
import com.example.firebase_test.viewmodels.WorkspacesViewModel
import com.example.firebase_test.workspaces.entries.MyChatScreen
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date // Importado
import java.util.Locale
import kotlin.io.encoding.ExperimentalEncodingApi
import androidx.compose.ui.res.stringResource
import com.example.firebase_test.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceDetailScreen(
    workspaceId: String,
    workspacesViewModel: WorkspacesViewModel,
    contentNavController: NavController,
    modifier: Modifier,
    authViewModel: AuthViewModel
) {
    LaunchedEffect(key1 = workspaceId) {
        workspacesViewModel.loadWorkspaceDetails(workspaceId)
    }

    val workspace by workspacesViewModel.selectedWorkspace.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(title = {
                Text(
                    text = workspace?.name ?: stringResource(R.string.common_loading),
                    maxLines = 1,
                    style = MaterialTheme.typography.titleLarge
                )
            }, navigationIcon = {
                IconButton(onClick = { contentNavController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.common_back)
                    )
                }
            }, actions = {
                IconButton(onClick = { contentNavController.navigate("info/$workspaceId") }) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = stringResource(R.string.workspace_detail_info_description)
                    )
                }
            })
        }) { innerPadding ->

        MyChatScreen(
            workspaceId = workspaceId,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .padding(bottom = innerPadding.calculateBottomPadding()),
            workspacesViewModel = workspacesViewModel,
            authViewModel = authViewModel,
            contentNavController
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalEncodingApi::class)
@Composable
fun WorkspaceDetailsContent(
    workspacesViewModel: WorkspacesViewModel,
    modifier: Modifier,
    contentNavController: NavController
) {
    val workspace by workspacesViewModel.selectedWorkspace.collectAsState()
    val members by workspacesViewModel.workspaceMembers.collectAsStateWithLifecycle()

    var showChangeImageDialog by remember { mutableStateOf(false) }
    var showChangeNameDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(), onResult = { uri: Uri? ->
            if (uri != null && workspace?.id != null) {
                val base64String = encodeUriToBase64(context, uri)
                if (base64String != null) {
                    workspacesViewModel.updateWorkspacePicture(workspace?.id!!, base64String)
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.workspace_detail_image_process_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            showChangeImageDialog = false
        })

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(), onResult = { bitmap: Bitmap? ->
            if (bitmap != null && workspace?.id != null) {
                val base64String = encodeBitmapToBase64(bitmap)
                if (base64String != null) {
                    workspacesViewModel.updateWorkspacePicture(workspace?.id!!, base64String)
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.workspace_detail_photo_process_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            showChangeImageDialog = false
        })

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(), onResult = { isGranted: Boolean ->
            if (isGranted) {
                cameraLauncher.launch()
            } else {
                showChangeImageDialog = false
                Toast.makeText(
                    context,
                    context.getString(R.string.workspace_detail_camera_permission_denied),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

    val mensajeUsuario by workspacesViewModel.userInfo.collectAsState()
    LaunchedEffect(key1 = mensajeUsuario) {
        if (mensajeUsuario != null) {
            Toast.makeText(context, mensajeUsuario, Toast.LENGTH_SHORT).show()
            workspacesViewModel.clearUserInfo()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.workspace_detail_info_title)) },
                navigationIcon = {
                    IconButton(onClick = { contentNavController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.workspace_detail_back_to_chat_description)
                        )
                    }
                })
        }) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .padding(top = innerPadding.calculateTopPadding()),
            contentPadding = PaddingValues(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                WorkspaceProfileImage(
                    base64String = workspace?.workspacePicture,
                    modifier = Modifier
                        .size(120.dp)
                        .clickable {
                            showChangeImageDialog = true
                        })
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = workspace?.name ?: "",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = { showChangeNameDialog = true },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.workspace_detail_change_name_description)
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.workspace_detail_invite_code_label),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        SelectionContainer {
                            Text(
                                text = workspace?.inviteCode
                                    ?: stringResource(R.string.common_not_available),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = formatDate(workspace?.createdAt, context),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                Text(
                    text = stringResource(R.string.workspace_detail_members_label, members.size),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            items(members) { userProfile ->
                UserProfileItem(user = userProfile)
                HorizontalDivider(Modifier.padding(horizontal = 8.dp))
            }
        }
    }

    if (showChangeNameDialog) {
        MyTextDialog(
            startValue = workspace?.name ?: "",
            onConfirm = { inputName: String ->
                workspace?.id?.let {
                    workspacesViewModel.updateWorkspaceName(it, inputName)
                }
                showChangeNameDialog = false
            },
            onDismiss = {
                showChangeNameDialog = false
            },
            title = stringResource(R.string.dialog_change_name_title),
            label = stringResource(R.string.common_name)
        )
    }

    if (showChangeImageDialog) {
        AlertDialog(
            onDismissRequest = { showChangeImageDialog = false },
            title = { Text(stringResource(R.string.workspace_detail_change_picture_title)) },
            text = { Text(stringResource(R.string.workspace_detail_change_picture_prompt)) },
            confirmButton = {
                TextButton(onClick = {
                    galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) {
                    Text(stringResource(R.string.common_gallery))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    val permission = android.Manifest.permission.CAMERA
                    if (ContextCompat.checkSelfPermission(
                            context, permission
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        cameraLauncher.launch()
                    } else {
                        cameraPermissionLauncher.launch(permission)
                    }
                }) {
                    Text(stringResource(R.string.common_camera))
                }
            })
    }
}


@Composable
fun UserProfileItem(user: UserProfile) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        ProfileImage(
            base64String = user.profilePicture, modifier = Modifier.size(40.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = "${user.firstName} ${user.lastName}", style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun ProfileImage(base64String: String?, modifier: Modifier = Modifier) {
    val imageBitmap = remember(base64String) {
        decodeBase64ToBitmap(base64String)
    }

    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap,
            contentDescription = stringResource(R.string.common_profile_picture_description),
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(CircleShape)
        )
    } else {

        Box(
            modifier = modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = stringResource(R.string.common_user_no_image_description),
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}


@Composable
fun WorkspaceProfileImage(
    base64String: String?, modifier: Modifier = Modifier
) {
    val imageBitmap = remember(base64String) {
        decodeBase64ToBitmap(base64String)
    }

    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap,
            contentDescription = stringResource(R.string.workspace_detail_picture_description),
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(CircleShape)
        )
    } else {

        Box(
            modifier = modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Apartment,
                contentDescription = stringResource(R.string.workspace_detail_no_image_description),
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(60.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTextDialog(
    startValue: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    title: String,
    label: String,
    maxLength: Int = 30
) {
    var tempText by remember(startValue) { mutableStateOf(startValue) }

    AlertDialog(onDismissRequest = onDismiss, title = { Text(title) }, text = {

        CharacterLimitedOutlinedTextField(
            value = tempText,
            onValueChange = { tempText = it },
            maxLength = maxLength,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            singleLine = true,
        )
    }, confirmButton = {
        Button(
            onClick = { onConfirm(tempText) }, enabled = tempText.isNotBlank()
        ) {
            Text(stringResource(R.string.common_save))
        }
    }, dismissButton = {
        TextButton(onClick = onDismiss) {
            Text(stringResource(R.string.common_cancel))
        }
    })
}

private fun formatDate(date: Date?, context: Context): String {
    if (date == null) return context.getString(R.string.common_date_not_available)
    val formatPattern = context.getString(R.string.common_date_format_long)
    val formatter = SimpleDateFormat(formatPattern, Locale.getDefault())
    return context.getString(R.string.common_created_on, formatter.format(date))
}


private fun decodeBase64ToBitmap(base64String: String?): androidx.compose.ui.graphics.ImageBitmap? {
    if (base64String.isNullOrBlank()) return null
    return try {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)?.asImageBitmap()
    } catch (e: Exception) {
        Log.e("Base64Decoder", "Error al decodificar Base64", e)
        null
    }
}

private fun encodeUriToBase64(context: Context, uri: Uri): String? {

    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val bytes = inputStream.readBytes()
            Base64.encodeToString(bytes, Base64.DEFAULT)
        }
    } catch (e: Exception) {
        Log.e("Base64Encoder", "Error al codificar URI a Base64", e)
        null
    }
}

private fun encodeBitmapToBase64(bitmap: Bitmap): String? {

    return try {
        ByteArrayOutputStream().use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
        }
    } catch (e: Exception) {
        Log.e("Base64Encoder", "Error al codificar Bitmap a Base64", e)
        null
    }
}