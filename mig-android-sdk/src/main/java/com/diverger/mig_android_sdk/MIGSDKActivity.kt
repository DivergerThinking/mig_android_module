package com.diverger.mig_android_sdk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.diverger.mig_android_sdk.data.MadridInGameUserData

class MIGSDKActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val email = intent.getStringExtra("EMAIL") ?: ""
        val accessToken = intent.getStringExtra("ACCESS_TOKEN") ?: ""
        val userName = intent.getStringExtra("USERNAME") ?: ""
        val dni = intent.getStringExtra("DNI") ?: ""
        val madridInGameUserData = intent.getParcelableExtra<MadridInGameUserData>("USER_DATA") ?: MadridInGameUserData(email = email, userName = userName, dni = dni)
        setContent {
            MIGAndroidSDKScreen(madridInGameUserData = madridInGameUserData, accessToken = accessToken)
        }
    }
}
