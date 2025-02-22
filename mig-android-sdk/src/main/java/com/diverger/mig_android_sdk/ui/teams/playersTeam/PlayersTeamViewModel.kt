package com.diverger.mig_android_sdk.ui.teams.playersTeam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diverger.mig_android_sdk.data.TeamUser
import com.diverger.mig_android_sdk.data.UserManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlayersTeamViewModel : ViewModel() {

    private val _teamName = MutableStateFlow("")
    val teamName: StateFlow<String> = _teamName

    private val _teamPlayers = MutableStateFlow<List<TeamUser>>(emptyList())
    val teamPlayers: StateFlow<List<TeamUser>> = _teamPlayers

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        loadTeamData()
    }

    private fun loadTeamData() {
        viewModelScope.launch {
            val user = UserManager.getUser()
            if (user?.teams.isNullOrEmpty()) {
                _errorMessage.value = "El usuario no pertenece a ningún equipo"
                _isLoading.value = false
                return@launch
            }

            val team = user!!.teams.first()
            _teamName.value = team.name ?: "Sin equipo"
            _teamPlayers.value = team.users ?: emptyList()

            _isLoading.value = false
        }
    }
}