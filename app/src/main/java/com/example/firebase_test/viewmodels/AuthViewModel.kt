package com.example.firebase_test.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firebase_test.workspaces.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

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
            _authState.value = AuthState.Error("Email or Password can't be empty")
            return
        }

        _authState.value = AuthState.Loading

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _authState.value = AuthState.Authenticated
            } else {
                _authState.value =
                    AuthState.Error(task.exception?.message ?: "something went wrong")
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
            _authState.value = AuthState.Error("Email or Password can't be empty")
            return
        }

        _authState.value = AuthState.Loading

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _authState.value = AuthState.Authenticated

                val uuid = userId.value

                if (uuid != null) {
                    val userProfile = mapOf(
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "email" to email,
                        "profilePicture" to null
                    )

                    FirebaseFirestore.getInstance().collection("users").document(uuid)
                        .set(userProfile).addOnSuccessListener {
                            _authState.value = AuthState.Authenticated
                        }.addOnFailureListener { e ->
                            _authState.value =
                                AuthState.Error("Error al guardar perfil: ${e.message}")
                        }
                }

            } else {
                _authState.value =
                    AuthState.Error(task.exception?.message ?: "something went wrong")
            }
        }

    }

    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.UnAuthenticated
    }


    suspend fun getUserProfile(uid: String): UserProfile? {
        return try {
            val snapshot =
                FirebaseFirestore.getInstance().collection("users").document(uid).get().await()

            snapshot.toObject(UserProfile::class.java)
        } catch (e: Exception) {
            null
        }
    }
}

sealed class AuthState() {
    object Authenticated : AuthState()
    object UnAuthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}