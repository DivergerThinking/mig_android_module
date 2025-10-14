package com.diverger.mig_android_sdk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diverger.mig_android_sdk.data.User
import com.diverger.mig_android_sdk.data.UserManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MIGSDKViewModel : ViewModel() {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun initializeUser(email: String, userName: String, dni: String, accessToken: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = UserManager.initializeUser(
                email= email,
                userName = userName,
                dni = dni,
                accessToken = accessToken
            )
            result.fold(
                onSuccess = {
                    _user.value = UserManager.getUser()
                    _isLoading.value = false
                },
                onFailure = {
                    _errorMessage.value = it.localizedMessage
                    _isLoading.value = false
                }
            )
        }
    }
}
