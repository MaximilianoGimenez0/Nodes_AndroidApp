package com.example.firebase_test.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firebase_test.repositories.UserRepository
import com.example.firebase_test.workspaces.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModelProvider
import com.example.firebase_test.workspaces.Workspace
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * Esta es la Inyección de Dependencias Manual.
 * El ViewModel "declara" lo que necesita en su constructor.
 */
class UserViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _userInfo = MutableStateFlow<String?>(null)
    val userInfo: StateFlow<String?> = _userInfo.asStateFlow()

    fun clearUserInfo() {
        _userInfo.value = null
    }

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile = _userProfile.asStateFlow()

    fun loadUserProfile(uid: String) {
        viewModelScope.launch {
            // El ViewModel DELEGA el trabajo al repositorio
            val profile = userRepository.getUserProfile(uid)
            _userProfile.value = profile
        }
    }

    fun updateProfilePicture(uid: String, newPicture: String) {
        viewModelScope.launch {
            try {
                // 1. Le pides al repositorio que actualice la base de datos
                userRepository.updateUserPicture(uid, newPicture)

                // 2. Si eso no falló, actualizas tu StateFlow localmente
                _userProfile.update { currentUser ->
                    // .copy() crea un nuevo UserProfile con solo el campo modificado
                    currentUser?.copy(profilePicture = newPicture)
                }

                // 3. Informas a la UI que todo salió bien
                _userInfo.value = "Foto actualizada correctamente"

            } catch (e: Exception) {
                Log.e("ViewModelError", "Error al actualizar la imagen: ${e.message}")
                _userInfo.value = "Error al guardar: ${e.message}"
            }
        }
    }


    suspend fun getUser(uid: String): UserProfile? {

        return try {
            userRepository.getUserProfile(uid)
        } catch (e: Exception) {
            Log.e("ViewModelError", "Error al cargar el usuario: ${e.message}")
            _userInfo.value = "Error al cargar usuario: ${e.message}"
            // 6. Retorna null en caso de error
            null
        }
    }

}


/**
 * Esta clase es el "pegamento" de la Inyección Manual.
 * Su único trabajo es crear el UserViewModel.
 */
class UserViewModelFactory() : ViewModelProvider.Factory {

    val repository = UserRepository()

    /**
     * El sistema llamará a esta función cuando pida un ViewModel.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Comprueba si el ViewModel que pide es el nuestro
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {

            // Si es, lo crea "a mano" pasándole el repositorio
            @Suppress("UNCHECKED_CAST") return UserViewModel(repository) as T
        }
        // Si no es un UserViewModel, lanza un error
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}