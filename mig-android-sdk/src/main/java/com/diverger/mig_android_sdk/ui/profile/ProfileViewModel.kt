package com.diverger.mig_android_sdk.ui.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diverger.mig_android_sdk.data.ProfileRepository
import com.diverger.mig_android_sdk.data.UploadImageService
import com.diverger.mig_android_sdk.data.User
import com.diverger.mig_android_sdk.data.UserManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ProfileViewModel : ViewModel() {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()
    private val _toastMsg = MutableStateFlow<String?>(null)
    val toastMsg: StateFlow<String?> = _toastMsg.asStateFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            _user.value = UserManager.getUser()
        }
    }

    fun discardChanges() {
        _user.value = UserManager.getUser()
    }

    fun saveChanges(firstName: String, lastName: String, dni: String, email: String, username: String, phone: String?) {
        _user.value = _user.value?.copy(
            firstName = firstName,
            lastName = lastName,
            dni = dni,
            email = email,
            username = username,
            phone = phone
        )
    }

    fun onAvatarSelected(uri: Uri, context: Context) {
        viewModelScope.launch {
            _isUploading.value = true
            try {
                // 2.1 Convertir URI a ByteArray o File
                val inputStream = context.contentResolver.openInputStream(uri)!!
                val bytes = inputStream.readBytes()
                inputStream.close()

                // 2.2 Subir la imagen (igual que en iOS updateAvatar)
                val avatarId = UploadImageService().uploadImageSuspend(bytes, "${UUID.randomUUID()}.jpg")

                // 2.3 Hacer PATCH al perfil solo con avatar
                ProfileRepository.updateAvatar(avatarId)

                // 2.4 Actualizar localmente
                _user.value = _user.value?.copy(avatar = avatarId)
                _toastMsg.value = "¡Avatar actualizado!"
            } catch (e: Exception) {
                _toastMsg.value = "Error al actualizar avatar: ${e.localizedMessage}"
            } finally {
                _isUploading.value = false
                // Limpiar toast tras mostrarlo
                delay(2000)
                _toastMsg.value = null
            }
        }
    }
}
