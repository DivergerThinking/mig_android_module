package com.diverger.mig_android_sdk.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

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
        ProfileTextField(label = "Nombre", value = firstName, onValueChange = { onValueChange("firstName", it) })
        ProfileTextField(label = "Apellidos", value = lastName, onValueChange = { onValueChange("lastName", it) })
        ProfileTextField(label = "DNI", value = dni, onValueChange = { onValueChange("dni", it) })
        ProfileTextField(label = "Email", value = email, onValueChange = { onValueChange("email", it) })
        ProfileTextField(label = "Nick", value = username, onValueChange = { onValueChange("username", it) })
        ProfileTextField(label = "Teléfono (Opcional)", value = phone, keyboardType = KeyboardType.Phone, onValueChange = { onValueChange("phone", it) })
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
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .background(Color.Gray.copy(alpha = 0.2f), shape = MaterialTheme.shapes.medium)
                .padding(12.dp)
        )
    }
}
