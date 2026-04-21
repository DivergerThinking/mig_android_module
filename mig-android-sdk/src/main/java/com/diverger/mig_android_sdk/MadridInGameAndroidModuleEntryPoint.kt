package com.diverger.mig_android_sdk

import android.content.Context
import android.content.Intent
import com.diverger.mig_android_sdk.data.MadridInGameUserData
import com.diverger.mig_android_sdk.support.EnvironmentManager

object MadridInGameAndroidModuleEntryPoint {
    fun launch(
        context: Context,
        email: String,
        accessToken: String,
        userName: String? = null,
        dni: String? = null,
        logoMIG: Int? = null,
        qrMiddleLogo: Int? = null
    ) {
        val intent = Intent(context, MIGSDKActivity::class.java).apply {
            putExtra("EMAIL", email)
            putExtra("ACCESS_TOKEN", accessToken)
            putExtra("USERNAME", userName ?: "")
            putExtra("DNI", dni ?: "")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }


    /**
     * Launches the MIGSDKActivity, passing user data and an access token.
     *
     * This method starts the [MIGSDKActivity] using the provided [context]. It attaches a [MadridInGameUserData] object
     * as a Parcelable extra and sets the environment if the app is running in debug mode.
     *
     * @param context The Android [Context] to use for starting the activity. Typically, an `Activity` or `Application` context.
     * @param madridInGameUserData The [MadridInGameUserData] object containing user information to be passed to the activity. This must implement [Parcelable].
     * @param accessToken The access token associated with the current user session.
     *
     * Example usage:
     * ```
     * val user = UserData(email = "user@example.com", userName = "jdoe")
     * launch(context, user, "token_xyz")
     * ```
     */
    fun launch(context: Context, madridInGameUserData: MadridInGameUserData, accessToken: String, isPreRelease: Boolean = false) {
        if(isPreRelease) {
            EnvironmentManager.setEnvironment(false)
        }
        val intent = Intent(context, MIGSDKActivity::class.java).apply {
            putExtra("USER_DATA", madridInGameUserData)
            putExtra("ACCESS_TOKEN", accessToken)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}
