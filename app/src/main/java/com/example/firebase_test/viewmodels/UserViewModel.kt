package com.example.firebase_test.viewmodels

import android.annotation.SuppressLint
import android.app.Application // <-- 1. Importar Application
import android.util.Log
// 2. Importar AndroidViewModel y ViewModelProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
// 3. Importar tus recursos R
import com.example.firebase_test.R
import com.example.firebase_test.workspaces.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update


class UserViewModel(
    private val userRepository: UserRepository,
    application: Application
) : AndroidViewModel(application) {

    @SuppressLint("StaticFieldLeak")
    private val context = application.applicationContext

    private val _userInfo = MutableStateFlow<String?>(null)
    val userInfo: StateFlow<String?> = _userInfo.asStateFlow()

    fun clearUserInfo() {
        _userInfo.value = null
    }

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile = _userProfile.asStateFlow()

    fun loadUserProfile(uid: String) {
        viewModelScope.launch {
            val profile = userRepository.getUserProfile(uid)
            _userProfile.value = profile
        }
    }

    fun updateProfilePicture(uid: String, newPicture: String) {
        viewModelScope.launch {
            try {
                userRepository.updateUserPicture(uid, newPicture)

                _userProfile.update { currentUser ->
                    currentUser?.copy(profilePicture = newPicture)
                }

                _userInfo.value = context.getString(R.string.profile_picture_update_success)

            } catch (e: Exception) {
                Log.e("ViewModelError", "Error al actualizar la imagen: ${e.message}")

                _userInfo.value = context.getString(
                    R.string.workspace_generic_save_error, e.message ?: "Error"
                )
            }
        }
    }

    fun updateUserInfo(uid: String, FirstName: String, LastName: String) {
        viewModelScope.launch {
            try {
                userRepository.updateUserInfo(uid, FirstName, LastName)

                _userProfile.update { currentUser ->
                    currentUser?.copy(firstName = FirstName, lastName = LastName)
                }

                _userInfo.value = context.getString(R.string.profile_data_update_success)

            } catch (e: Exception) {
                Log.e("ViewModelError", "Error al guardar los datos: ${e.message}")

                _userInfo.value = context.getString(
                    R.string.workspace_generic_save_error, e.message ?: "Error"
                )
            }
        }
    }

}

class UserViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    private val repository = UserRepository()

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(repository, application) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
