package com.example.firebase_test.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.firebase_test.R
import com.example.firebase_test.workspaces.ChecklistItem
import com.example.firebase_test.workspaces.EntryType
import com.example.firebase_test.workspaces.UserProfile
import com.example.firebase_test.workspaces.Workspace
import com.example.firebase_test.workspaces.WorkspaceEntry
import com.example.firebase_test.workspaces.WorkspacesUiState
import com.google.android.gms.location.LocationServices
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore

@SuppressLint("StaticFieldLeak")
class WorkspacesViewModel(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val workspaceRepository: WorkspaceRepository,
    application: Application
) : AndroidViewModel(application) {

    private val context = application.applicationContext

    private val _workspaceMembers = MutableStateFlow<List<UserProfile>>(emptyList())
    val workspaceMembers: StateFlow<List<UserProfile>> = _workspaceMembers.asStateFlow()

    private val _userInfo = MutableStateFlow<String?>(null)
    val userInfo: StateFlow<String?> = _userInfo.asStateFlow()

    fun clearUserInfo() {
        _userInfo.value = null
    }

    private val _uiState = MutableStateFlow(WorkspacesUiState())
    val uiState: StateFlow<WorkspacesUiState> = _uiState.asStateFlow()

    private val _selectedWorkspace = MutableStateFlow<Workspace?>(null)
    val selectedWorkspace: StateFlow<Workspace?> = _selectedWorkspace.asStateFlow()

    private var workspaceListener: ListenerRegistration? = null

    fun loadWorkspaceDetails(workspaceId: String) {
        workspaceListener?.remove()
        _selectedWorkspace.value = null
        _workspaceMembers.value = emptyList()
        val docRef = firestore.collection("workspaces").document(workspaceId)

        workspaceListener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                _selectedWorkspace.value = null
                _workspaceMembers.value = emptyList()
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val workspace = snapshot.toObject(Workspace::class.java)?.copy(id = snapshot.id)
                _selectedWorkspace.value = workspace
                if (workspace != null) {
                    loadWorkspaceMembers(workspace.members)
                } else {
                    _workspaceMembers.value = emptyList()
                }

            } else {
                _selectedWorkspace.value = null
                _workspaceMembers.value = emptyList()
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        entriesListener?.remove()
        workspaceListener?.remove()
    }


    init {
        loadWorkspaces()
    }

    fun onNewWorkspaceRequested(name: String) {
        viewModelScope.launch {
            createWorkspace(name)
        }
    }

    private fun loadWorkspaces() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            // 👇 CAMBIO
            _uiState.update {
                it.copy(
                    error = context.getString(R.string.workspace_unauthenticated_error),
                    isLoading = false
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        firestore.collection("workspaces").whereArrayContains("members", userId)
            .addSnapshotListener { snapshots, error ->

                if (error != null) {
                    _uiState.update {
                        it.copy(
                            error = context.getString(R.string.workspace_load_data_error),
                            isLoading = false
                        )
                    }
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val workspacesList = snapshots.documents.mapNotNull { document ->
                        val workspace = document.toObject(Workspace::class.java)
                        workspace?.copy(id = document.id)
                    }
                    _uiState.update { it.copy(workspaces = workspacesList, isLoading = false) }
                }
            }
    }


    fun joinWorkspace(workspaceId: String) {
        val userId = auth.currentUser?.uid ?: return
        val workspaceRef = firestore.collection("workspaces").document(workspaceId)

        workspaceRef.update("members", FieldValue.arrayUnion(userId)).addOnSuccessListener {
            _userInfo.value = context.getString(R.string.workspace_join_success)
        }.addOnFailureListener { e ->
            _userInfo.value = context.getString(R.string.workspace_join_error)
            println("Error al intentar unirse al workspace: $e")
        }
    }


    suspend fun createWorkspace(name: String) {
        val db = firestore
        val userId = auth.currentUser?.uid

        if (userId == null) {
            println("Error: Usuario no autenticado.")
            return
        }

        try {
            val newWorkspaceId = generateUniqueWorkspaceId()

            val newWorkspace = Workspace(
                name = name, ownerId = userId, members = listOf(userId), inviteCode = newWorkspaceId
            )

            db.collection("workspaces").document(newWorkspaceId).set(newWorkspace).await()

            _userInfo.value =
                context.getString(R.string.workspace_create_success, newWorkspace.name)
            println("Firestore: ¡Workspace '${newWorkspace.name}' creado con éxito con ID: $newWorkspaceId!")

        } catch (e: Exception) {
            _userInfo.value = context.getString(R.string.workspace_create_error)
            println("Firestore: Error al crear el workspace: $e")
        }
    }

    private suspend fun generateUniqueWorkspaceId(): String {
        val db = firestore
        var workspaceId = generateRandomCode(6)
        var isUnique = false


        while (!isUnique) {
            val docRef = db.collection("workspaces").document(workspaceId)
            try {

                val document = docRef.get().await()
                if (!document.exists()) {
                    isUnique = true
                } else {
                    println("Colisión de ID encontrada: $workspaceId. Regenerando...")
                    workspaceId = generateRandomCode(6)
                }
            } catch (e: Exception) {
                println("Error al verificar la unicidad del ID: $e")
                throw e
            }
        }
        return workspaceId
    }

    private fun generateRandomCode(length: Int): String {
        val allowedChars = ('A'..'Z') + ('2'..'9')
        return (1..length).map { allowedChars.random() }.joinToString("")
    }


    fun addWorkspaceEntry(
        workspaceId: String,
        value: String? = null,
        type: EntryType,
        name: String,
        profilePicture: String,
        items: List<ChecklistItem>? = emptyList(),
        location: GeoPoint? = null
    ) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Log.e(TAG, "No se puede añadir entry, usuario no logueado.")
            return
        }

        val newEntry = WorkspaceEntry(
            value = value,
            type = type,
            name = name,
            profilePicture = profilePicture,
            items = items,
            userId = currentUserId,
            location = location,
            createdAt = null
        )


        viewModelScope.launch {
            try {
                firestore.collection("workspaces").document(workspaceId).collection("entries")
                    .add(newEntry).await()

                Log.d(TAG, "Nueva entry añadida con éxito a $workspaceId.")

            } catch (e: Exception) {
                Log.e(TAG, "Error al añadir la entry a $workspaceId", e)
                _userInfo.value = context.getString(R.string.workspace_entry_add_error)
            }
        }
    }


    fun deleteWorkspaceEntry(workspaceId: String, entryId: String) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Log.e(TAG, "No se puede eliminar, usuario no logueado.")
            _userInfo.value = context.getString(R.string.workspace_delete_auth_error)
            return
        }

        viewModelScope.launch {
            try {
                firestore.collection("workspaces").document(workspaceId).collection("entries")
                    .document(entryId).delete().await()

                _userInfo.value = context.getString(R.string.workspace_delete_entry_success)
                Log.d(TAG, "Entry $entryId eliminada de $workspaceId.")

            } catch (e: Exception) {
                Log.e(TAG, "Error al eliminar la entry $entryId de $workspaceId", e)
                _userInfo.value = context.getString(R.string.workspace_delete_entry_error)
            }
        }
    }

    fun updateEntryCategory(workspaceId: String, entryId: String, newCategory: String) {
        viewModelScope.launch {
            try {
                firestore.collection("workspaces").document(workspaceId).collection("entries")
                    .document(entryId).update("category", newCategory).await()

                _userInfo.value =
                    context.getString(R.string.workspace_category_update_success, newCategory)
                Log.d(TAG, "Categoría actualizada para entry $entryId.")

            } catch (e: Exception) {
                Log.e(TAG, "Error al actualizar la categoría", e)
                _userInfo.value = context.getString(R.string.workspace_category_update_error)
            }
        }
    }

    private var entriesListener: ListenerRegistration? = null
    private val _workspaceEntries = MutableStateFlow<List<WorkspaceEntry>>(emptyList())
    val workspaceEntries: StateFlow<List<WorkspaceEntry>> = _workspaceEntries.asStateFlow()

    private val _availableCategories = MutableStateFlow<List<String>>(emptyList())
    val availableCategories: StateFlow<List<String>> = _availableCategories.asStateFlow()


    fun loadWorkspaceEntries(workspaceId: String) {

        entriesListener?.remove()

        val entriesRef =
            firestore.collection("workspaces").document(workspaceId).collection("entries")
                .orderBy("createdAt")

        entriesListener = entriesRef.addSnapshotListener { snapshots, error ->
            if (error != null) {
                println("Error al cargar entries del workspace: $error")
                _workspaceEntries.value = emptyList()
                return@addSnapshotListener
            }

            if (snapshots != null) {
                val entries = snapshots.documents.mapNotNull { doc ->

                    val entry = doc.toObject(WorkspaceEntry::class.java)

                    entry?.copy(id = doc.id)
                }

                _workspaceEntries.value = entries

                _availableCategories.value =
                    entries.mapNotNull { it.category }.filter { it.isNotBlank() }.distinct()
                        .sorted()


            } else {
                _workspaceEntries.value = emptyList()
            }
        }
    }


    private fun loadWorkspaceMembers(memberIds: List<String>) {

        if (memberIds.isEmpty()) {
            _workspaceMembers.value = emptyList()
            return
        }

        viewModelScope.launch {
            try {
                val memberProfiles = memberIds.mapNotNull { userId ->
                    try {
                        val document = firestore.collection("users").document(userId).get().await()
                        document.toObject(UserProfile::class.java)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error al cargar el perfil del miembro $userId", e)
                        null
                    }
                }

                _workspaceMembers.value = memberProfiles
                Log.d(TAG, "Se cargaron ${memberProfiles.size} perfiles de miembros.")

            } catch (e: Exception) {
                Log.w(TAG, "Error general al cargar los miembros del workspace", e)
                _workspaceMembers.value = emptyList()
            }
        }
    }

    fun updateChecklistItemState(
        workspaceId: String, entryId: String, itemIndex: Int, newCheckedState: Boolean
    ) {
        viewModelScope.launch {
            try {
                val entryRef =
                    firestore.collection("workspaces").document(workspaceId).collection("entries")
                        .document(entryId)

                val currentEntry = _workspaceEntries.value.firstOrNull { it.id == entryId }
                if (currentEntry == null) {
                    Log.e(TAG, "No se encontró la entry $entryId para actualizar")
                    return@launch
                }

                val currentItemsList = currentEntry.items as? List<ChecklistItem>

                if (currentItemsList == null) {
                    Log.e(
                        TAG,
                        "Error: La entry $entryId no es una checklist o sus items no son una lista."
                    )
                    return@launch
                }

                val newItemsList = currentItemsList.toMutableList()

                if (itemIndex < 0 || itemIndex >= newItemsList.size) {
                    Log.e(TAG, "Error: Índice $itemIndex está fuera de rango.")
                    return@launch
                }

                val itemToUpdate = newItemsList[itemIndex]
                newItemsList[itemIndex] = itemToUpdate.copy(isChecked = newCheckedState)

                entryRef.update("items", newItemsList).await()

                Log.d(TAG, "Checklist item $itemIndex en entry $entryId actualizado.")

            } catch (e: Exception) {
                Log.e(TAG, "Error al actualizar el checklist item", e)
                _userInfo.value = context.getString(R.string.workspace_checklist_update_error)
            }
        }
    }


    fun updateWorkspaceName(id: String, newName: String) {

        viewModelScope.launch {

            try {
                workspaceRepository.actualizarNombreEnFirebase(id, newName)

                var workspaceActualizado: Workspace? = null

                _uiState.update { estadoActual ->
                    val updatedWorkspaces = estadoActual.workspaces.map { workspace ->
                        if (workspace.id == id) {
                            val copiaActualizada = workspace.copy(name = newName)
                            workspaceActualizado = copiaActualizada
                            copiaActualizada
                        } else {
                            workspace
                        }
                    }
                    estadoActual.copy(workspaces = updatedWorkspaces)
                }

                if (workspaceActualizado != null) {
                    _selectedWorkspace.value = workspaceActualizado
                }

                _userInfo.value = context.getString(R.string.workspace_name_update_success)

            } catch (e: Exception) {
                Log.e("ViewModelError", "Error al actualizar nombre: ${e.message}")

                _userInfo.value = context.getString(
                    R.string.workspace_generic_save_error, e.message ?: "Error desconocido"
                )
            }
        }
    }

    fun updateWorkspacePicture(id: String, newPicture: String) {

        viewModelScope.launch {
            try {
                workspaceRepository.updateWorkspacePicture(id, newPicture)

                var workspaceActualizado: Workspace? = null

                _uiState.update { estadoActual ->
                    val updatedWorkspaces = estadoActual.workspaces.map { workspace ->
                        if (workspace.id == id) {
                            val copiaActualizada = workspace.copy(workspacePicture = newPicture)
                            workspaceActualizado = copiaActualizada
                            copiaActualizada
                        } else {
                            workspace
                        }
                    }
                    estadoActual.copy(workspaces = updatedWorkspaces)
                }

                if (workspaceActualizado != null) {
                    _selectedWorkspace.value = workspaceActualizado
                }

                _userInfo.value = context.getString(R.string.workspace_picture_update_success)

            } catch (e: Exception) {
                Log.e("ViewModelError", "Error al actualizar la imagen: ${e.message}")

                _userInfo.value = context.getString(
                    R.string.workspace_generic_save_error, e.message ?: "Error desconocido"
                )
            }
        }
    }

    fun shareCurrentLocation(workspaceId: String, name: String, profilePicture: String) {
        viewModelScope.launch {
            workspaceRepository.fetchCurrentLocation(onSuccess = { geoPoint ->

                addWorkspaceEntry(
                    workspaceId = workspaceId,
                    type = EntryType.LOCALIZATION,
                    name = name,
                    profilePicture = profilePicture,
                    location = geoPoint,
                    value = context.getString(R.string.workspace_location_shared)
                )

            }, onFailure = { exception ->
                Log.e("LocationError", "Error al obtener ubicación: ${exception.message}")

                _userInfo.value = context.getString(R.string.workspace_location_error)
            })
        }
    }
}


class WorkspacesViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(WorkspacesViewModel::class.java)) {

            val firestore: FirebaseFirestore = Firebase.firestore
            val auth: FirebaseAuth = FirebaseAuth.getInstance()
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

            val repository = WorkspaceRepository(firestore, auth, fusedLocationClient)

            return WorkspacesViewModel(firestore, auth, repository, application) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}