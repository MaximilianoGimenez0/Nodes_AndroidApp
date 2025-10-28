package com.example.firebase_test.workspaces

import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import java.io.ByteArrayOutputStream
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.firebase_test.viewmodels.AuthViewModel
import com.example.firebase_test.viewmodels.WorkspacesViewModel
import com.example.firebase_test.workspaces.entries.MyChatScreen
import java.text.SimpleDateFormat
import java.util.Locale
import android.util.Base64
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import kotlin.io.encoding.ExperimentalEncodingApi

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

    Column(modifier = modifier.fillMaxSize()) {

        TopAppBar(title = {
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
            ) {
                Text(text = workspace?.name ?: "Cargando detalles...")
            }

        }, navigationIcon = {
            IconButton(onClick = { contentNavController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack, contentDescription = "Volver atrás"
                )
            }
        }, actions = {
            IconButton(onClick = { contentNavController.navigate("info/$workspaceId") }) {
                Icon(
                    imageVector = Icons.Default.Info, contentDescription = "Descripción"
                )
            }
        })

        MyChatScreen(
            workspaceId,
            modifier = Modifier,
            workspacesViewModel,
            authViewModel = authViewModel
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

    val context = LocalContext.current

    val encodeUriToBase64: (Uri) -> String? = remember {
        { uri ->
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val bytes = inputStream.readBytes()
                    Base64.encodeToString(bytes, Base64.DEFAULT)
                }
            } catch (e: Exception) {
                Log.e("Base64Encoder", "Error al codificar URI a Base64", e)
                null
            }
        }
    }

    val encodeBitmapToBase64: (Bitmap) -> String? = remember {
        { bitmap ->
            try {
                ByteArrayOutputStream().use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                    Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
                }
            } catch (e: Exception) {
                Log.e("Base64Encoder", "Error al codificar Bitmap a Base64", e)
                null
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(), onResult = { uri: Uri? ->
            if (uri != null && workspace?.id != null) {
                val base64String = encodeUriToBase64(uri)
                if (base64String != null) {
                    workspacesViewModel.updateWorkspacePicture(workspace?.id ?: "", base64String)
                } else {
                    // TODO -> mostrar un Toast/Snackbar si hay error en la codificación
                }
            }
            showChangeImageDialog = false
        })

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(), onResult = { bitmap: Bitmap? ->
            if (bitmap != null && workspace?.id != null) {
                val base64String = encodeBitmapToBase64(bitmap)
                if (base64String != null) {
                    workspacesViewModel.updateWorkspacePicture(workspace?.id ?: "", base64String)
                } else {
                    // TODO -> mostrar un Toast/Snackbar si hay error en la codificación
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
            }
        })

    val mensajeUsuario by workspacesViewModel.userInfo.collectAsState()

    LaunchedEffect(key1 = mensajeUsuario) {

        if (mensajeUsuario != null) {
            Toast.makeText(context, mensajeUsuario, Toast.LENGTH_SHORT).show()
            workspacesViewModel.clearUserInfo()
        }
    }

    var showDialog by remember { mutableStateOf(false) }

    fun formatDate(date: java.util.Date?): String {
        if (date == null) return "Fecha no disponible"
        val formatter = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale("es", "ES"))
        return "Creado el: ${formatter.format(date)}"
    }

    Scaffold(
        modifier = modifier, topBar = {
            TopAppBar(title = { }, navigationIcon = {
                IconButton(onClick = { contentNavController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack, contentDescription = "Volver"
                    )
                }
            })
        }) { innerPadding ->


        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
            ) {
                WorkspaceProfileImage(
                    base64String = workspace?.workspacePicture,
                    modifier = Modifier
                        .size(120.dp)
                        .clickable {
                            showChangeImageDialog = true
                        })
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text(
                    text = workspace?.name ?: "",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )

                TextButton(
                    {
                        showDialog = true
                    }, modifier = Modifier.padding(horizontal = 5.dp)
                ) { Text("Cambiar nombre") }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = "Código de Invitación",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    SelectionContainer {
                        Text(
                            text = workspace?.inviteCode ?: "",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                }
            }


            Text(
                text = formatDate(workspace?.createdAt), style = MaterialTheme.typography.bodyMedium
            )


            Text(
                text = "Miembros (${workspace?.members?.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Column {
                MembersList(members)
            }
        }
    }

    if (showDialog) {
        MyTextDialog(startValue = workspace?.name ?: "", onConfirm = { inputName: String ->
            val workspaceId = workspacesViewModel.selectedWorkspace.value?.id

            if (workspaceId != null) {
                workspacesViewModel.updateWorkspaceName(workspaceId, inputName)
            }

            showDialog = false
        }, onDismiss = {
            showDialog = false
        })
    }

    if (showChangeImageDialog) {
        AlertDialog(
            onDismissRequest = { showChangeImageDialog = false },
            title = { Text("Cambiar foto de perfil") },
            text = { Text("¿Desde dónde quieres seleccionar la imagen?") },
            confirmButton = {
                TextButton(onClick = {
                    galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) {
                    Text("Galería")
                }
            },
            dismissButton = {
                TextButton(onClick = {
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

}

@Composable
fun MembersList(
    members: List<UserProfile>, modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(members) { userProfile ->
            UserProfileItem(user = userProfile)
        }
    }
}

@Composable
fun UserProfileItem(user: UserProfile) {
    Text(
        text = user.firstName + " " + user.lastName, modifier = Modifier.padding(8.dp)
    )
}

@Composable
fun WorkspaceProfileImage(
    base64String: String?, modifier: Modifier = Modifier
) {
    val imageBitmap = remember(base64String) {
        if (base64String.isNullOrBlank()) {
            null
        } else {
            try {
                val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)?.asImageBitmap()
            } catch (e: Exception) {
                Log.e("WorkspaceProfileImage", "Error al decodificar Base64", e)
                null
            }
        }
    }

    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap,
            contentDescription = "Foto de perfil del workspace",
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
                contentDescription = "Workspace sin imagen",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(60.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTextDialog(
    startValue: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit
) {
    var tempText by remember { mutableStateOf(startValue) }

    AlertDialog(onDismissRequest = onDismiss, title = {
        Text("Ingresá un nuevo nombre")
    }, text = {
        OutlinedTextField(
            value = tempText,
            onValueChange = { tempText = it },
            label = { Text("Nombre") },
            singleLine = true
        )
    }, confirmButton = {
        Button(
            onClick = {
                onConfirm(tempText)
            }) {
            Text("Guardar")
        }
    }, dismissButton = {
        TextButton(
            onClick = onDismiss
        ) {
            Text("Cancelar")
        }
    })
}