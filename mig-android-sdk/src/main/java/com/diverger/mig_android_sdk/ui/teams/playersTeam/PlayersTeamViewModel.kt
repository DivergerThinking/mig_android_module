import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diverger.mig_android_sdk.data.TeamUser
import com.diverger.mig_android_sdk.data.UserManager
import kotlinx.coroutines.flow.*
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
        observeSelectedTeam()
    }

    private fun observeSelectedTeam() {
        viewModelScope.launch {
            UserManager.selectedTeam
                .collect { team ->
                    _isLoading.value = true

                    if (team != null) {
                        _teamName.value = team.name ?: "Sin equipo"
                        _teamPlayers.value = team.users ?: emptyList()
                        _errorMessage.value = null
                    } else {
                        _errorMessage.value = "No se ha seleccionado ningún equipo"
                        _teamPlayers.value = emptyList()
                    }

                    _isLoading.value = false
                }
        }
    }
}
