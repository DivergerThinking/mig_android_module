package com.diverger.mig_android_sdk.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MadridInGameUserData(
    var name: String? = null,
    var lastName: String? = null,
    val email: String,
    val userName: String,
    var phone: String? = null,
    val dni: String? = null,
    val logoMIG: Int? = null,
    val qrMiddleLogo: Int? = null
) : Parcelable