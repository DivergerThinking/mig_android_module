package com.diverger.mig_android_sdk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diverger.mig_android_sdk.data.TextContentApi
import com.diverger.mig_android_sdk.data.User
import com.diverger.mig_android_sdk.data.MadridInGameUserData
import com.diverger.mig_android_sdk.data.UserManager
import com.diverger.mig_android_sdk.support.LocalizationManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MIGSDKViewModel : ViewModel() {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun initializeUser(madridInGameUserData: MadridInGameUserData, accessToken: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            // Set token first so translations fetch can authenticate
            UserManager.setAccessToken(accessToken)

            // Fetch remote translations before rendering (mirrors iOS initializeModule)
            launch {
                try {
                    val items = TextContentApi.fetchTextContent()
                    LocalizationManager.load(items)
                } catch (_: Exception) {
                    // Non-fatal: static strings.xml used as fallback
                }
            }

            val result = UserManager.initializeUser(
                madridInGameUserData,
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
