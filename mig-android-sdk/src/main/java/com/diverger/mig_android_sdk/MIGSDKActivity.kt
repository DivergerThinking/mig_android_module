package com.diverger.mig_android_sdk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MIGSDKActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val email = intent.getStringExtra("EMAIL") ?: ""
        val accessToken = intent.getStringExtra("ACCESS_TOKEN") ?: ""
        val userName = intent.getStringExtra("USERNAME") ?: ""
        val dni = intent.getStringExtra("DNI") ?: ""

        setContent {
            MIGAndroidSDKScreen(email = email, userName = userName, dni = dni, accessToken = accessToken)
        }
    }
}
