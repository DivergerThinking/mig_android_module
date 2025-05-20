package com.diverger.mig_android_sdk.support

object EnvironmentManager {
    private const val BASE_URL_ASSETS_PRE = "https://premig.randomkesports.com/cms/assets/"
    private const val BASE_URL_ASSETS_PRO = "https://webesports.madridingame.es/cms/assets/"

    private const val BASE_URL_FILES_PRE = "https://premig.randomkesports.com/cms/"
    private const val BASE_URL_FILES_PRO = "https://webesports.madridingame.es/cms/"

    private const val BASE_URL_PRE = "https://premig.randomkesports.com/cms/items/"
    private const val BASE_URL_PRO = "https://webesports.madridingame.es/cms/items/"

    private var isProduction: Boolean = false

    fun getAssetsBaseUrl(): String {
        return if (isProduction) BASE_URL_ASSETS_PRO else BASE_URL_ASSETS_PRE
    }

    fun getFilesBaseUrl(): String {
        return if (isProduction) BASE_URL_FILES_PRO else BASE_URL_FILES_PRE
    }

    fun getBaseUrl(): String {
        return if (isProduction) BASE_URL_PRO else BASE_URL_PRE
    }

    fun setEnvironment(isProd: Boolean) {
        isProduction = isProd
    }
}
