import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diverger.mig_android_sdk.data.NewsModel
import com.diverger.mig_android_sdk.data.NewsApi
import com.diverger.mig_android_sdk.data.UserManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NewsViewModel : ViewModel() {
    private val _allNews = MutableStateFlow<List<NewsModel>>(emptyList())
    val allNews: StateFlow<List<NewsModel>> = _allNews

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        observeSelectedTeam()
    }

    private fun observeSelectedTeam() {
        viewModelScope.launch {
            UserManager.selectedTeam
                .collect { team ->
                    if (team != null) {
                        fetchNews(team.id)
                    } else {
                        _allNews.value = emptyList()
                        _isLoading.value = false
                    }
                }
        }
    }

    private fun fetchNews(teamId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _allNews.value = NewsApi.getNews(teamId)
            } catch (e: Exception) {
                _allNews.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun forceFetchNews() {
        val selectedTeam = UserManager.selectedTeam.value
        if (selectedTeam != null) {
            fetchNews(selectedTeam.id)
        }
    }
}
