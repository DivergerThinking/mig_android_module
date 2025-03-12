package com.diverger.mig_android_sdk

import com.diverger.mig_android_sdk.ui.competitions.CompetitionsViewModel
import com.diverger.mig_android_sdk.ui.dashboard.DashboardViewModel
import com.diverger.mig_android_sdk.ui.profile.ProfileViewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module

fun initializeKoin() {
    startKoin {
        modules(appModule)
    }
}

// 📌 Módulo de Koin
val appModule: Module = module {
    viewModel { MIGSDKViewModel() }
    viewModel { ProfileViewModel() }
    viewModel { DashboardViewModel() }
    viewModel { CompetitionsViewModel() }
}
