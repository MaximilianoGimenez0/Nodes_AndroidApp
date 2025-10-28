package com.example.firebase_test.pages

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.firebase_test.R
import com.example.firebase_test.viewmodels.AuthState
import com.example.firebase_test.viewmodels.AuthViewModel
import com.example.firebase_test.viewmodels.UserViewModel
import java.io.ByteArrayOutputStream


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(
    modifier: Modifier,
    authViewmodel: AuthViewModel,
    navController: NavController,
    userViewModel: UserViewModel,
    contentController: NavController
) {
    // --- Estados para Perfil de Usuario ---
    var FirstName by remember { mutableStateOf("") }
    var LastName by remember { mutableStateOf("") }
    var Email by remember { mutableStateOf("") }

    // --- Estados para Preferencias (Configuración) ---

    // Estado para Idioma
    val languages = listOf("Español", "Inglés", "Portugués")
    var selectedLanguage by remember { mutableStateOf(languages[0]) } // Default "Español"
    var isLanguageMenuExpanded by remember { mutableStateOf(false) }

    // Estado para Tema
    val themes = listOf("Automático (Sistema)", "Claro", "Oscuro")
    var selectedTheme by remember { mutableStateOf(themes[0]) } // Default "Automático"
    var isThemeMenuExpanded by remember { mutableStateOf(false) }

    // Estado para Notificaciones
    var allowNotifications by remember { mutableStateOf(true) }
    var newEntries by remember { mutableStateOf(true) }
    var reminders by remember { mutableStateOf(true) }

    // --- ViewModels y Lógica de UI ---
    val TAG = "SettingsPage"
    val authState = authViewmodel.authState.observeAsState()
    val userId by authViewmodel.userId.observeAsState()
    val userProfile by userViewModel.userProfile.collectAsState()
    val context = LocalContext.current
    val mensajeUsuario by userViewModel.userInfo.collectAsState()
    var showChangeImageDialog by remember { mutableStateOf(false) }

    // --- Effects ---

    // Cargar datos del perfil en los campos de texto
    LaunchedEffect(userProfile) {
        if (userProfile != null) {
            FirstName = userProfile?.firstName ?: ""
            LastName = userProfile?.lastName ?: ""
            Email = userProfile?.email ?: ""
            Log.d(TAG, "Datos del perfil actualizados: $userProfile")
        } else {
            Log.d(TAG, "Datos del perfil son nulos.")
        }
    }

    // Observar estado de autenticación
    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.UnAuthenticated -> navController.navigate("login")
            else -> {
                userId?.let { userViewModel.loadUserProfile(it) }
            }
        }
    }

    // Mostrar Toasts de mensajes del ViewModel
    LaunchedEffect(key1 = mensajeUsuario) {
        if (mensajeUsuario != null) {
            Toast.makeText(context, mensajeUsuario, Toast.LENGTH_SHORT).show()
            userViewModel.clearUserInfo()
        }
    }

    // --- Lógica para seleccionar imágenes ---
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
            if (uri != null && userId != null) {
                val base64String = encodeUriToBase64(uri)
                if (base64String != null) {
                    userViewModel.updateProfilePicture(userId!!, base64String)
                } else {
                    Toast.makeText(context, "Error al codificar la imagen", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            showChangeImageDialog = false
        })

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(), onResult = { bitmap: Bitmap? ->
            if (bitmap != null && userId != null) {
                val base64String = encodeBitmapToBase64(bitmap)
                if (base64String != null) {
                    userViewModel.updateProfilePicture(userId!!, base64String)
                } else {
                    Toast.makeText(context, "Error al procesar la imagen", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            showChangeImageDialog = false
        })

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(), onResult = { isGranted: Boolean ->
            if (isGranted) {
                cameraLauncher.launch()
            } else {
                Toast.makeText(
                    context,
                    "Permiso de cámara denegado",
                    Toast.LENGTH_SHORT
                ).show()
                showChangeImageDialog = false
            }
        })


    // --- UI (Scaffold) ---
    Scaffold(content = { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            ProfilePicture(
                userProfile?.profilePicture,
                modifier = Modifier
                    .width(100.dp)
                    .height(100.dp),
                onImageChange = { showChangeImageDialog = true })

            Spacer(modifier = Modifier.height(24.dp))

            // --- SECCIÓN DE PERFIL ---
            SectionHeader("Mi Perfil")
            SectionDescription("Tu nombre, apellido y correo asociados a esta cuenta.")
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = FirstName,
                onValueChange = { FirstName = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = LastName,
                onValueChange = { LastName = it },
                label = { Text("Apellido") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = Email,
                onValueChange = { Email = it },
                label = { Text("Email") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // --- SECCIÓN DE APLICACIÓN ---
            SectionHeader("Aplicación")
            SectionDescription("Personaliza el idioma y la apariencia de la app.")
            Spacer(modifier = Modifier.height(16.dp))
            ExposedDropdownMenuBox(
                expanded = isLanguageMenuExpanded,
                onExpandedChange = { isLanguageMenuExpanded = !isLanguageMenuExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedLanguage,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Idioma") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isLanguageMenuExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = isLanguageMenuExpanded,
                    onDismissRequest = { isLanguageMenuExpanded = false }
                ) {
                    languages.forEach { language ->
                        DropdownMenuItem(
                            text = { Text(language) },
                            onClick = {
                                selectedLanguage = language
                                isLanguageMenuExpanded = false
                                // TODO: Llamar a settingsViewModel.setLanguage(language)
                                Log.d(TAG, "Idioma seleccionado: $language")
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBox(
                expanded = isThemeMenuExpanded,
                onExpandedChange = { isThemeMenuExpanded = !isThemeMenuExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedTheme,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tema de la aplicación") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isThemeMenuExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = isThemeMenuExpanded,
                    onDismissRequest = { isThemeMenuExpanded = false }
                ) {
                    themes.forEach { theme ->
                        DropdownMenuItem(
                            text = { Text(theme) },
                            onClick = {
                                selectedTheme = theme
                                isThemeMenuExpanded = false
                                // TODO: Llamar a settingsViewModel.setTheme(theme)
                                Log.d(TAG, "Tema seleccionado: $theme")
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // --- SECCIÓN DE NOTIFICACIONES ---
            SectionHeader("Notificaciones")
            SectionDescription("Elige qué alertas y recordatorios quieres recibir.")
            Spacer(modifier = Modifier.height(16.dp))
            SettingSwitchItem(
                text = "Permitir notificaciones",
                checked = allowNotifications,
                onCheckedChange = { newValue ->
                    allowNotifications = newValue
                    // TODO: Llamar a settingsViewModel.setAllowNotifications(newValue)
                    Log.d(TAG, "Permitir notificaciones: $newValue")
                }
            )

            SettingSwitchItem(
                text = "Nuevas entradas",
                checked = newEntries,
                onCheckedChange = { newValue ->
                    newEntries = newValue
                    // TODO: Llamar a settingsViewModel.setWorkspaceNotifications(newValue)
                    Log.d(TAG, "Notificaciones de Workspaces: $newValue")
                },
                enabled = allowNotifications // Se deshabilita si el switch principal está apagado
            )

            SettingSwitchItem(
                text = "Recordatorios",
                checked = reminders,
                onCheckedChange = { newValue ->
                    reminders = newValue
                    // TODO: Llamar a settingsViewModel.setWorkspaceNotifications(newValue)
                    Log.d(TAG, "Notificaciones de Workspaces: $newValue")
                },
                enabled = allowNotifications // Se deshabilita si el switch principal está apagado
            )

            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // --- SECCIÓN DE SALIR ---
            Button(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                onClick = {
                    authViewmodel.signOut()
                    navController.navigate("login")
                }) { Text("Cerrar Sesión") }

            Spacer(modifier = Modifier.height(16.dp))
        }

    }, topBar = {
        TopAppBar(
            title = {
                Text(
                    "Configuración", style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }, navigationIcon = {
                IconButton(onClick = { contentController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver atrás"
                    )
                }
            }, colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            )
        )
    })

    // --- Diálogo para cambiar imagen ---
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

// --- Composable para el Título de Sección ---
@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        modifier = Modifier.fillMaxWidth()
    )
}

// --- Composable para la Descripción de Sección ---
@Composable
private fun SectionDescription(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium, // Estilo más sutil
        color = MaterialTheme.colorScheme.onSurfaceVariant, // Color más suave
        modifier = Modifier.fillMaxWidth()
    )
}


// --- Composable reutilizable para los Switches ---
@Composable
private fun SettingSwitchItem(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .toggleable( // Hace que toda la fila sea clicable
                value = checked,
                onValueChange = onCheckedChange,
                enabled = enabled
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = if (enabled) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) // Color deshabilitado
        )
        Switch(
            checked = checked,
            onCheckedChange = null, // Nulo porque el 'toggleable' del Row ya lo maneja
            enabled = enabled
        )
    }
}


// --- Composable ProfilePicture (Sin cambios) ---
@Composable
private fun ProfilePicture(
    base64String: String?, modifier: Modifier = Modifier, onImageChange: () -> Unit
) {
    val imageBitmap = remember(base64String) {
        if (base64String.isNullOrBlank()) {
            null
        } else {
            try {
                val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)?.asImageBitmap()
            } catch (e: Exception) {
                Log.e("WorkspaceItemImage", "Error al decodificar Base64", e)
                null
            }
        }
    }

    // Aquí usamos un Box para permitir la superposición
    Box(
        modifier = modifier
            .clip(CircleShape) // Recorta todo el contenido del Box en un círculo
            .clickable(onClick = onImageChange) // Hacemos clicable TODO el Box
    ) {
        if (imageBitmap != null) {
            // Imagen de perfil cargada
            Image(
                bitmap = imageBitmap,
                contentDescription = "Foto de perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize() // La imagen llena todo el Box
            )

            // Icono de cambio superpuesto
            Box(
                modifier = Modifier
                    .fillMaxSize() // Llena el Box padre
                    .background(Color.Black.copy(alpha = 0.1f)), // Fondo semitransparente oscuro
                contentAlignment = Alignment.Center // Centra el icono dentro de este Box
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.outline_person_edit_24),
                    contentDescription = "Cambiar foto de perfil",
                    tint = Color.LightGray, // Icono blanco para contraste
                    modifier = Modifier.size(48.dp) // Tamaño del icono
                )
            }
        } else {
            // Perfil sin imagen (el placeholder original)
            Box(
                modifier = Modifier
                    .fillMaxSize() // Llena el Box padre
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.outline_person_edit_24),
                    contentDescription = "Perfil sin imagen",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}