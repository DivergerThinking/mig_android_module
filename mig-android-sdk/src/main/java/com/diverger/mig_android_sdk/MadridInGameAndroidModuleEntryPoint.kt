package com.diverger.mig_android_sdk

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.vector.ImageVector

object MadridInGameAndroidModuleEntryPoint {
    fun launch(
        context: Context,
        email: String,
        accessToken: String,
        userName: String? = null,
        dni: String? = null,
        logoMIG: ImageVector? = null,
        qrMiddleLogo: ImageVector? = null
    ) {
        val intent = Intent(context, MIGSDKActivity::class.java).apply {
            putExtra("EMAIL", email)
            putExtra("ACCESS_TOKEN", accessToken)
            putExtra("USER_NAME", userName ?: "")
            putExtra("DNI", dni ?: "")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}
