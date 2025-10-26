package com.example.firebase_test.viewmodels

import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import kotlinx.coroutines.tasks.await

class WorkspaceRepository {

    private val db = Firebase.firestore
    private val workspacesCollection = db.collection("workspaces")
    suspend fun actualizarNombreEnFirebase(id: String, nuevoNombre: String) {
        workspacesCollection.document(id).update("name", nuevoNombre).await()
    }

    suspend fun updateWorkspacePicture(id: String, profilePicture: String) {
        workspacesCollection.document(id).update("workspacePicture", profilePicture).await()
    }

}