package com.example.firebase_test.workspaces

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.firebase_test.R
import com.example.firebase_test.viewmodels.WorkspacesViewModel
import kotlinx.coroutines.launch
@Composable
fun WorkspacesScreen(
    contentController: NavController, workspacesViewModel: WorkspacesViewModel, modifier: Modifier
) {
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
    var showDialog by remember { mutableStateOf(DialogType.NONE) }
    var workspaceName by remember { mutableStateOf("") }
    var workspaceCode by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            WorkspacesControls(
                isFabMenuExpanded = isFabMenuExpanded,
                onFabClicked = { isFabMenuExpanded = !isFabMenuExpanded },
                onCreateClicked = {
                    isFabMenuExpanded = false
                    showDialog = DialogType.CREATE_WORKSPACE
                },
                onJoinClicked = {
                    isFabMenuExpanded = false
                    showDialog = DialogType.JOIN_WORKSPACE
                })
        }) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            WorkspaceList_Grid(
                modifier = Modifier.padding(paddingValues),
                workspaces = uiState.workspaces,
                contentNavController = contentController
            )
        }
    }

    when (showDialog) {
        DialogType.CREATE_WORKSPACE -> {
            CreateWorkspaceDialog(
                workspaceName = workspaceName,
                onNameChange = { workspaceName = it },
                onConfirm = {
                    viewModel.onNewWorkspaceRequested(workspaceName)
                    showDialog = DialogType.NONE
                    workspaceName = ""
                },
                onDismiss = {
                    showDialog = DialogType.NONE
                    workspaceName = ""
                })
        }

        DialogType.JOIN_WORKSPACE -> {
            JoinWorkspaceDialog(
                workspaceCode = workspaceCode,
                onCodeChange = { workspaceCode = it.uppercase() },
                onConfirm = {
                    viewModel.joinWorkspace(workspaceCode)
                    showDialog = DialogType.NONE
                    workspaceCode = ""
                },
                onDismiss = {
                    showDialog = DialogType.NONE
                    workspaceCode = ""
                })
        }

        DialogType.NONE -> {}
    }
}

@Composable
fun WorkspaceList_Grid(
    modifier: Modifier, workspaces: List<Workspace>, contentNavController: NavController
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(workspaces) { workspace ->


            val memberCount = workspace.members.size


            WorkspaceGridItem(
                workspace = workspace, memberCount = memberCount,
                onClick = { contentNavController.navigate("chat/${workspace.inviteCode}") })
        }
    }
}

@Composable
fun WorkspaceGridItem(
    workspace: Workspace, memberCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {

            WorkspaceItemImage(
                base64String = workspace.workspacePicture,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )


            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = workspace.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Group,
                        contentDescription = "Integrantes",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$memberCount ${if (memberCount == 1) "integrante" else "integrantes"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}


@Composable
fun WorkspaceItemImage(
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
                Log.e("WorkspaceItemImage", "Error al decodificar Base64", e)
                null
            }
        }
    }

    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap,
            contentDescription = "Foto del workspace",
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
        )
    } else {

        Box(
            modifier = modifier
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.outline_gamepad_circle_right_24),
                contentDescription = "Workspace sin imagen",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}