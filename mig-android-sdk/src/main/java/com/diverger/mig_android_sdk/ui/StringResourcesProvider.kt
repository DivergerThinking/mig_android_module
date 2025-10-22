package com.diverger.mig_android_sdk.ui

import android.content.Context
import androidx.annotation.StringRes

class StringResourcesProvider(private val context: Context) {
    fun getString(@StringRes resId: Int): String = context.getString(resId)
    fun getString(@StringRes resId: Int, vararg args: Any): String = context.getString(resId, *args)
}