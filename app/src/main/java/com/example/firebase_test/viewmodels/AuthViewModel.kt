package com.example.firebase_test.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.firebase_test.R
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firebase_test.workspaces.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    @SuppressLint("StaticFieldLeak")
    private val context = application.applicationContext

    val db = FirebaseFirestore.getInstance()

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableLiveData<AuthState>()

    val authState: LiveData<AuthState> = _authState

    private val _userId = MutableLiveData<String?>()
    val userId: LiveData<String?> = _userId

    private val _firebaseUser = MutableLiveData<FirebaseUser?>()
    val firebaseUser: LiveData<FirebaseUser?> = _firebaseUser


    init {
        checkAuthStatus()

        auth.addAuthStateListener { firebaseAuth ->
            val currentUser = firebaseAuth.currentUser
            updateUser(currentUser)

            currentUser?.let {
                loadUserProfile(it.uid)
                updateFcmToken()
            }
        }
    }

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile = _userProfile.asStateFlow()

    fun loadUserProfile(uid: String) {
        viewModelScope.launch {
            val profile = getUserProfile(uid)
            _userProfile.value = profile
        }
    }

    private fun updateUser(user: FirebaseUser?) {
        if (user != null) {
            _authState.value = AuthState.Authenticated
            _userId.value = user.uid
            _firebaseUser.value = user
        } else {
            _authState.value = AuthState.UnAuthenticated
            _userId.value = null
            _firebaseUser.value = null
        }
    }

    fun checkAuthStatus() {

        if (auth.currentUser == null) {
            _authState.value = AuthState.UnAuthenticated
        } else {
            _authState.value = AuthState.Authenticated
        }
    }

    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error(context.getString(R.string.auth_error_empty_fields))
            return
        }

        _authState.value = AuthState.Loading

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _authState.value = AuthState.Authenticated
            } else {
                _authState.value =
                    AuthState.Error(task.exception?.message ?: context.getString(R.string.auth_error_unknown))
            }
        }

    }

    fun signUp(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        profilePicture: String = ""
    ) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error(context.getString(R.string.auth_error_empty_fields))
            return
        }

        _authState.value = AuthState.Loading

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _authState.value = AuthState.Authenticated

                val uuid = userId.value

                if (uuid != null) {
                    val userProfile = mapOf(
                        "id" to uuid,
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "email" to email,
                        "profilePicture" to null
                    )

                    db.collection("users").document(uuid).set(userProfile).addOnSuccessListener {
                        _authState.value = AuthState.Authenticated
                    }.addOnFailureListener { e ->
                        _authState.value = AuthState.Error(
                            context.getString(R.string.auth_error_save_profile, e.message ?: "Error")
                        )
                    }

                }

            } else {
                _authState.value =
                    AuthState.Error(task.exception?.message ?: context.getString(R.string.auth_error_unknown))
            }
        }

    }

    fun signOut() {

        val userId = auth.currentUser?.uid

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful && userId != null) {
                val token = task.result


                db.collection("users").document(userId).collection("tokens").document(token)
                    .delete().addOnCompleteListener {
                        auth.signOut()
                        _authState.value = AuthState.UnAuthenticated
                    }
            } else {
                auth.signOut()
                _authState.value = AuthState.UnAuthenticated
            }
        }
    }


    suspend fun getUserProfile(uid: String): UserProfile? {
        return try {
            val snapshot = db.collection("users").document(uid).get().await()

            snapshot.toObject(UserProfile::class.java)
        } catch (e: Exception) {
            null
        }
    }


    fun updateFcmToken() {
        val userId = auth.currentUser?.uid ?: return

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Error al obtener el token actual", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            Log.d("FCM", "Token obtenido en Login: $token")

            val tokenData = hashMapOf(
                "token" to token, "lastUsed" to System.currentTimeMillis()
            )

            db.collection("users").document(userId).collection("tokens").document(token)
                .set(tokenData).addOnSuccessListener {
                    Log.d("FCM", "Token guardado en Login exitosamente.")
                }
        }
    }

}

sealed class AuthState() {
    object Authenticated : AuthState()
    object UnAuthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}