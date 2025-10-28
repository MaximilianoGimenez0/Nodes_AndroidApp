package com.example.firebase_test.repositories // (Cambié el paquete a 'repositories')

import com.example.firebase_test.workspaces.UserProfile
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import kotlinx.coroutines.tasks.await

class UserRepository {
    // La conexión a la base de datos es privada
    private val db = Firebase.firestore
    private val usersCollection = db.collection("users")

    /**
     * Esta es la lógica que movimos desde el ViewModel.
     * Ahora vive aquí, en el repositorio.
     */

    suspend fun getUserProfile(uid: String): UserProfile? {
        return try {
            val snapshot = usersCollection.document(uid).get().await()
            snapshot.toObject(UserProfile::class.java)
        } catch (e: Exception) {
            // En una app real, loguearías el error
            null
        }
    }

    suspend fun updateUserName(id: String, nuevoNombre: String) {
        usersCollection.document(id).update("name", nuevoNombre).await()
    }

    suspend fun updateUserPicture(id: String, profilePicture: String) {
        usersCollection.document(id).update("profilePicture", profilePicture).await()
    }
}