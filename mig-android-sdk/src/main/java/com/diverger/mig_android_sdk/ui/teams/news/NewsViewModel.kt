package com.diverger.mig_android_sdk.ui.teams.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diverger.mig_android_sdk.data.NewsApi
import com.diverger.mig_android_sdk.data.NewsModel
import com.diverger.mig_android_sdk.data.UserManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NewsViewModel : ViewModel() {
    private val _allNews = MutableStateFlow<List<NewsModel>>(emptyList())
    val allNews: StateFlow<List<NewsModel>> = _allNews

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        fetchNews()
    }

    private fun fetchNews() {
        viewModelScope.launch {
            _isLoading.value = true
            val team = UserManager.getSelectedTeam()

            if (team != null) {
                try {
                    _allNews.value = NewsApi.getNews(team.id)
                } catch (e: Exception) {
                    _allNews.value = emptyList()
                }
            }

            _isLoading.value = false
        }
    }
}