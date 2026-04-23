package com.diverger.mig_android_sdk.ui.competitions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diverger.mig_android_sdk.data.Competition
import com.diverger.mig_android_sdk.data.CompetitionsApi
import com.diverger.mig_android_sdk.data.Split
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CompetitionDetailViewModel : ViewModel() {
    private val _competition = MutableStateFlow<Competition?>(null)
    val competition: StateFlow<Competition?> = _competition

    private val _selectedSplit = MutableStateFlow<Split?>(null)
    val selectedSplit: StateFlow<Split?> = _selectedSplit

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab

    suspend fun fetchCompetition(competitionId: String) {
        viewModelScope.launch {
            try {
                val competition = CompetitionsApi.getCompetitionById(competitionId)
                _competition.value = competition
                _selectedTab.value = 0
                val splits = competition.splits ?: emptyList()
                _selectedSplit.value = splits.firstOrNull { it.active == true } ?: splits.firstOrNull()
            } catch (e: Exception) {
                _competition.value = null
            }
        }
    }

    fun selectTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    fun selectSplit(split: Split) {
        _selectedSplit.value = split
    }
}
