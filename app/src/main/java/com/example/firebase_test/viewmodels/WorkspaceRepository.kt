package com.example.firebase_test.viewmodels

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.tasks.await

class WorkspaceRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val fusedLocationClient: FusedLocationProviderClient,
) {

    private val db = Firebase.firestore
    private val workspacesCollection = db.collection("workspaces")

    suspend fun actualizarNombreEnFirebase(id: String, nuevoNombre: String) {
        workspacesCollection.document(id).update("name", nuevoNombre).await()
    }

    suspend fun updateWorkspacePicture(id: String, profilePicture: String) {
        workspacesCollection.document(id).update("workspacePicture", profilePicture).await()
    }

    @SuppressLint("MissingPermission")
    fun fetchCurrentLocation(
        onSuccess: (GeoPoint) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val geoPoint = GeoPoint(location.latitude, location.longitude)
                onSuccess(geoPoint)
            } else {
                onFailure(Exception("No se pudo obtener la ubicación (location is null)."))
            }
        }.addOnFailureListener {
            onFailure(it)
        }
    }

}