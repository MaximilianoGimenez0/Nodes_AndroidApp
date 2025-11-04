package com.example.firebase_test.pages


import android.Manifest
import android.provider.Settings
import android.os.Build
import androidx.core.content.ContextCompat
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.firebase_test.CharacterLimitedOutlinedTextField
import com.example.firebase_test.R
import com.example.firebase_test.viewmodels.AuthState
import com.example.firebase_test.viewmodels.AuthViewModel
import com.example.firebase_test.viewmodels.SettingsViewModel
import com.example.firebase_test.viewmodels.UserViewModel
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt
import androidx.compose.ui.res.stringResource


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(
    modifier: Modifier,
    authViewmodel: AuthViewModel,
    settingsViewModel: SettingsViewModel,
    navController: NavController,
    userViewModel: UserViewModel,
    contentController: NavController
) {
    val TAG = "SettingsPage"
    val context = LocalContext.current

    val settings by settingsViewModel.appSettings.collectAsState()
    val authState = authViewmodel.authState.observeAsState()
    val userId by authViewmodel.userId.observeAsState()
    val userProfile by userViewModel.userProfile.collectAsState()
    val mensajeUsuario by userViewModel.userInfo.collectAsState()

    var FirstName by remember(userProfile?.firstName) {
        mutableStateOf(userProfile?.firstName ?: "")
    }
    var LastName by remember(userProfile?.lastName) {
        mutableStateOf(userProfile?.lastName ?: "")
    }
    val Email = userProfile?.email ?: ""

    val isDirty =
        (FirstName != (userProfile?.firstName ?: "")) || (LastName != (userProfile?.lastName ?: ""))

    var isLanguageMenuExpanded by remember { mutableStateOf(false) }
    var isThemeMenuExpanded by remember { mutableStateOf(false) }
    var showChangeImageDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userProfile) {
        if (userProfile != null) {
            Log.d(TAG, "Datos del perfil actualizados: $userProfile")
        } else {
            Log.d(TAG, "Datos del perfil son nulos.")
        }
    }

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.UnAuthenticated -> navController.navigate("login")
            else -> {
                userId?.let { userViewModel.loadUserProfile(it) }
            }
        }
    }

    LaunchedEffect(key1 = mensajeUsuario) {
        if (mensajeUsuario != null) {
            Toast.makeText(context, mensajeUsuario, Toast.LENGTH_SHORT).show()
            userViewModel.clearUserInfo()
        }
    }

    val encodeUriToBase64: (Uri) -> String? = remember {
        { uri ->
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val bytes = inputStream.readBytes()
                    Base64.encodeToString(bytes, Base64.DEFAULT)
                }
            } catch (e: Exception) {
                Log.e("Base64Encoder", "Error al codificar URI", e); null
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
                Log.e("Base64Encoder", "Error al codificar Bitmap", e); null
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(), onResult = { uri: Uri? ->
            if (uri != null && userId != null) {
                val base66String = encodeUriToBase64(uri)
                if (base66String != null) {
                    userViewModel.updateProfilePicture(userId!!, base66String)
                } else {
                    Toast.makeText(
                        context, context.getString(R.string.error_image), Toast.LENGTH_SHORT
                    ).show()
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
                    Toast.makeText(
                        context, context.getString(R.string.error_image), Toast.LENGTH_SHORT
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
                Toast.makeText(
                    context,
                    context.getString(R.string.permission_camera_denied),
                    Toast.LENGTH_SHORT
                ).show()
                showChangeImageDialog = false
            }
        })


    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0), topBar = {
            TopAppBar(
                title = {
                Text(
                    stringResource(R.string.settings),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }, navigationIcon = {
                IconButton(onClick = { contentController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            }, colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            )
            )
        }) { innerPadding ->
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
            SectionHeader(stringResource(R.string.profile_section))
            SectionDescription(stringResource(R.string.profile_description))
            Spacer(modifier = Modifier.height(16.dp))

            CharacterLimitedOutlinedTextField(
                value = FirstName,
                onValueChange = { FirstName = it },
                maxLength = 40,
                label = { Text(stringResource(R.string.name_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            CharacterLimitedOutlinedTextField(
                value = LastName,
                onValueChange = { LastName = it },
                maxLength = 40,
                label = { Text(stringResource(R.string.name_lastname)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = Email,
                onValueChange = {},
                label = { Text(stringResource(R.string.name_email)) },
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (isDirty) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End)
                ) {
                    OutlinedButton(
                        onClick = {
                            FirstName = userProfile?.firstName ?: ""
                            LastName = userProfile?.lastName ?: ""
                        }) {
                        Text(stringResource(R.string.cancel))
                    }
                    Button(
                        onClick = {
                            userId?.let {
                                userViewModel.updateUserInfo(
                                    userProfile?.id ?: "",
                                    FirstName = FirstName,
                                    LastName = LastName,
                                )
                                Log.d(TAG, "Confirmar cambios: $FirstName, $LastName")
                            }
                        }) {
                        Text(stringResource(R.string.confirm))
                    }
                }
            }


            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            SectionHeader(stringResource(R.string.application_section))
            SectionDescription(stringResource(R.string.application_description))
            Spacer(modifier = Modifier.height(16.dp))

            val languages = listOf("Español", "Inglés", "Portugués")
            ExposedDropdownMenuBox(
                expanded = isLanguageMenuExpanded,
                onExpandedChange = { isLanguageMenuExpanded = !isLanguageMenuExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = settings.language,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.language)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isLanguageMenuExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = isLanguageMenuExpanded,
                    onDismissRequest = { isLanguageMenuExpanded = false }) {
                    languages.forEach { language ->
                        DropdownMenuItem(text = { Text(language) }, onClick = {
                            isLanguageMenuExpanded = false
                            settingsViewModel.setLanguage(language)
                        })
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val themes = listOf(
                stringResource(R.string.theme_system),
                stringResource(R.string.theme_light),
                stringResource(R.string.theme_dark)
            )
            val themeToSave = mapOf(
                stringResource(R.string.theme_system) to "Automático (Sistema)",
                stringResource(R.string.theme_light) to "Claro",
                stringResource(R.string.theme_dark) to "Oscuro"
            )
            val themeToDisplay = mapOf(
                "Automático (Sistema)" to stringResource(R.string.theme_system),
                "Claro" to stringResource(R.string.theme_light),
                "Oscuro" to stringResource(R.string.theme_dark)
            )

            ExposedDropdownMenuBox(
                expanded = isThemeMenuExpanded,
                onExpandedChange = { isThemeMenuExpanded = !isThemeMenuExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = themeToDisplay[settings.theme] ?: settings.theme,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.theme)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isThemeMenuExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = isThemeMenuExpanded,
                    onDismissRequest = { isThemeMenuExpanded = false }) {
                    themes.forEach { themeName ->
                        DropdownMenuItem(text = { Text(themeName) }, onClick = {
                            isThemeMenuExpanded = false
                            settingsViewModel.setTheme(themeToSave[themeName] ?: themeName)
                        })
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            FontSizeSlider(
                currentSize = settings.fontSize, onSizeChange = { newSize ->
                    settingsViewModel.setFontSize(newSize)
                })

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            SectionHeader(stringResource(R.string.notifications_section))
            SectionDescription(stringResource(R.string.notifications_description))
            Spacer(modifier = Modifier.height(16.dp))

            SettingSwitchItem(
                text = stringResource(R.string.allow_notifications),
                checked = settings.allowNotifications,
                onCheckedChange = { newValue ->
                    settingsViewModel.setAllowNotifications(newValue)
                })

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

                var hasAlarmPerm by remember {
                    mutableStateOf(hasExactAlarmPermission(context))
                }

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult(), onResult = {
                        hasAlarmPerm = hasExactAlarmPermission(context)
                    })

                Spacer(modifier = Modifier.height(8.dp))
                SettingSwitchItem(
                    text = stringResource(R.string.allow_exact_alarms),
                    checked = hasAlarmPerm,
                    onCheckedChange = {
                        if (!hasAlarmPerm) {
                            permissionLauncher.launch(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                        } else {
                            permissionLauncher.launch(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                        }
                    })
                SectionDescription(
                    stringResource(R.string.allow_exact_alarms_desc)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                onClick = {
                    authViewmodel.signOut()
                    navController.navigate("login")
                }) {
                Text(stringResource(R.string.logout))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showChangeImageDialog) {
        AlertDialog(
            onDismissRequest = { showChangeImageDialog = false },
            title = { Text(stringResource(R.string.profile_pic_change_title)) },
            text = { Text(stringResource(R.string.profile_pic_change_prompt)) },
            confirmButton = {
                TextButton(onClick = {
                    galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) {
                    Text(stringResource(R.string.gallery))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    val permission = Manifest.permission.CAMERA
                    val permissionCheckResult =
                        ContextCompat.checkSelfPermission(context, permission)

                    if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                        cameraLauncher.launch()
                    } else {
                        cameraPermissionLauncher.launch(permission)
                    }
                }) {
                    Text(stringResource(R.string.camera))
                }
            })
    }
}

@Composable
private fun FontSizeSlider(
    currentSize: Int, onSizeChange: (Int) -> Unit
) {
    val labels = listOf(
        stringResource(R.string.font_small),
        stringResource(R.string.font_medium),
        stringResource(R.string.font_large)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.font_size),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = labels[currentSize],
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = currentSize.toFloat(), onValueChange = { newValue ->
                onSizeChange(newValue.roundToInt())
            }, valueRange = 0f..2f, steps = 1
        )
    }
}


@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun SectionDescription(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun SettingSwitchItem(
    text: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .toggleable(
                value = checked, onValueChange = onCheckedChange, enabled = enabled
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = if (enabled) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
        Switch(
            checked = checked, onCheckedChange = null, enabled = enabled
        )
    }
}

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

    Box(
        modifier = modifier
            .clip(CircleShape)
            .clickable(onClick = onImageChange)
    ) {
        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap,
                contentDescription = stringResource(R.string.profile_pic_desc),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.outline_person_edit_24),
                    contentDescription = stringResource(R.string.profile_pic_change_desc),
                    tint = Color.LightGray,
                    modifier = Modifier.size(48.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.outline_person_edit_24),
                    contentDescription = stringResource(R.string.profile_pic_empty_desc),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}


private fun hasExactAlarmPermission(context: Context): Boolean {
    // En Android 12 (S) y superior, se necesita un permiso especial.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return alarmManager.canScheduleExactAlarms()
    }
    // En versiones anteriores, el permiso se da con el Manifest.
    return true
}

/**
 * Lanza un Intent para que el usuario vaya a la pantalla
 * de "Alarmas y Recordatorios" y conceda el permiso.
 */
private fun requestExactAlarmPermission(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).also {
            context.startActivity(it)
        }
    }
}