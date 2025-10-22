package com.diverger.mig_android_sdk.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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

@Composable
fun ProfileForm(
    firstName: String,
    lastName: String,
    dni: String,
    email: String,
    username: String,
    phone: String,
    onValueChange: (String, String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ProfileTextField(label = stringResource(R.string.profile_name_label), value = firstName, onValueChange = { onValueChange("firstName", it) })
        ProfileTextField(label = stringResource(R.string.profile_lastname_label), value = lastName, onValueChange = { onValueChange("lastName", it) })
        ProfileTextField(label = stringResource(R.string.profile_id_label), value = dni, onValueChange = { onValueChange("dni", it) })
        ProfileTextField(label = stringResource(R.string.profile_email_label), value = email, onValueChange = { onValueChange("email", it) })
        ProfileTextField(label = stringResource(R.string.profile_nickname_label), value = username, onValueChange = { onValueChange("username", it) })
        ProfileTextField(label = stringResource(R.string.profile_phone_label), value = phone, keyboardType = KeyboardType.Phone, onValueChange = { onValueChange("phone", it) })
    }
}

@Composable
fun ProfileTextField(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.5f))
        BasicTextField(
            textStyle = TextStyle(color = Color.White, fontFamily = FontFamily(
                Font(R.font.madrid_in_game_font)
            )
            ),
            value = value,
            onValueChange = onValueChange,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .background(Color.Gray.copy(alpha = 0.2f), shape = MaterialTheme.shapes.medium)
                .padding(12.dp),
            enabled = false
        )
    }
}
