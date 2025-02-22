import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diverger.mig_android_sdk.data.Competition
import com.diverger.mig_android_sdk.data.CompetitionsApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CompetitionDetailViewModel : ViewModel() {
    private val _competition = MutableStateFlow<Competition?>(null)
    val competition: StateFlow<Competition?> = _competition

    suspend fun fetchCompetition(competitionId: String) {
        viewModelScope.launch {
            try {
                val competition = CompetitionsApi.getCompetitionById(competitionId)
                _competition.value = competition
            } catch (e: Exception) {
                _competition.value = null
            }
        }
    }
}