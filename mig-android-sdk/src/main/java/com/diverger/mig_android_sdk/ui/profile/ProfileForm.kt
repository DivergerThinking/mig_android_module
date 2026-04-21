package com.diverger.mig_android_sdk.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.diverger.mig_android_sdk.R
import com.diverger.mig_android_sdk.support.localizedString

@Composable
fun ProfileForm(
    firstName: String,
    lastName: String,
    dni: String,
    email: String,
    username: String,
    phone: String,
    onValueChange: (String, String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ProfileTextField(
            label = localizedString("dashboard.profile.about.form.name", R.string.profile_name_label),
            value = firstName)
        ProfileTextField(
            label = localizedString("dashboard.profile.about.form.surname", R.string.profile_lastname_label),
            value = lastName)
        ProfileTextField(
            label = localizedString("dashboard.profile.about.form.dni", R.string.profile_id_label),
            value = dni)
        ProfileTextField(
            label = localizedString("dashboard.profile.about.form.email", R.string.profile_email_label),
            value = email)
        ProfileTextField(
            label = localizedString("dashboard.profile.about.form.nick", R.string.profile_nickname_label),
            value = username)
        ProfileTextField(
            label = localizedString("dashboard.profile.about.form.phone", R.string.profile_phone_label),
            value = phone)
    }
}

@Composable
fun ProfileTextField(
    label: String,
    value: String
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(Color.Gray.copy(alpha = 0.2f), shape = MaterialTheme.shapes.medium)
                .padding(8.dp)
                .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.titleSmall, color = Color.White.copy(0.5f))
        Text(value, style = MaterialTheme.typography.titleSmall, color = Color.White)
    }
}
