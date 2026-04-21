package com.diverger.mig_android_sdk.support

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.diverger.mig_android_sdk.data.TextContentItem
import java.util.Locale

object LocalizationManager {
    private var translations: Map<String, String> = emptyMap()

    fun load(items: List<TextContentItem>) {
        val primaryLang = Locale.getDefault().language
        val useSpanish = primaryLang == "es" || primaryLang == "ca"

        translations = items.mapNotNull { item ->
            val value = if (useSpanish) item.es else item.en
            if (!value.isNullOrBlank()) item.key to value else null
        }.toMap()
    }

    fun string(key: String): String? = translations[key]
}

@Composable
fun localizedString(key: String, @StringRes fallback: Int): String {
    return LocalizationManager.string(key) ?: stringResource(fallback)
}
