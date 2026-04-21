import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diverger.mig_android_sdk.R
import com.diverger.mig_android_sdk.data.TeamUser
import com.diverger.mig_android_sdk.data.UserManager
import com.diverger.mig_android_sdk.support.StringResourcesProvider
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PlayersTeamViewModel(private val stringResourcesProvider: StringResourcesProvider) : ViewModel() {

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
                        _teamName.value = team.name ?: stringResourcesProvider.getString(R.string.no_team)
                        _teamPlayers.value = team.users ?: emptyList()
                        _errorMessage.value = null
                    } else {
                        _errorMessage.value = stringResourcesProvider.getString(R.string.no_team_selected)
                        _teamPlayers.value = emptyList()
                    }

                    _isLoading.value = false
                }
        }
    }
}
