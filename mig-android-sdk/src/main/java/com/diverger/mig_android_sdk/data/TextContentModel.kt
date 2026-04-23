package com.diverger.mig_android_sdk.data

data class TextContentItem(
    val key: String,
    val en: String?,
    val es: String?
)

data class TextContentResponse(
    val data: List<TextContentItem>
)
