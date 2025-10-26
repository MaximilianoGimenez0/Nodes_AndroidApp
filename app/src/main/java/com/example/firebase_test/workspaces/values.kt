package com.example.firebase_test.workspaces

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date


data class Workspace(
    val id: String = "",
    val name: String = "",
    val inviteCode: String = "",
    @ServerTimestamp val createdAt: Date? = null,
    val members: List<String> = emptyList(),
    val ownerId: String = "",
    val workspacePicture: String = ""
)

data class WorkspacesUiState(
    val workspaces: List<Workspace> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

enum class EntryType {
    MESSAGE, CHECK_LIST, LOCALIZATION, IMAGE, REMINDER, NONE
}

data class ChecklistItem(
    val text: String = "",
    @get:PropertyName("isChecked")
    @set:PropertyName("isChecked")
    var isChecked: Boolean = false
)

data class WorkspaceEntry(
    val id: String = "",
    @ServerTimestamp val createdAt: Date? = null,
    val value: String = "",
    val userId: String = "",
    val name: String = "",
    val profilePicture: String = "",
    val items: List<ChecklistItem> = emptyList(),
    val type: EntryType = EntryType.NONE
)

data class UserProfile(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val profilePicture: String? = null
)
