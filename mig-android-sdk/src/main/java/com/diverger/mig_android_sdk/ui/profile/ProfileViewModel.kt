package com.diverger.mig_android_sdk.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diverger.mig_android_sdk.data.User
import com.diverger.mig_android_sdk.data.UserManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

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
}
