package com.diverger.mig_android_sdk.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.diverger.mig_android_sdk.R

@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel()) {
    val user by viewModel.user.collectAsState()

    var firstName by remember { mutableStateOf(user?.firstName.orEmpty()) }
    var lastName by remember { mutableStateOf(user?.lastName.orEmpty()) }
    var dni by remember { mutableStateOf(user?.dni.orEmpty()) }
    var email by remember { mutableStateOf(user?.email.orEmpty()) }
    var username by remember { mutableStateOf(user?.username.orEmpty()) }
    var phone by remember { mutableStateOf(user?.phone.orEmpty()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("SOBRE MÍ", style = MaterialTheme.typography.titleLarge, color = Color.White)

            Spacer(modifier = Modifier.height(20.dp))

            AvatarSelector()

            Spacer(modifier = Modifier.height(20.dp))

            ProfileForm(
                firstName = firstName,
                lastName = lastName,
                dni = dni,
                email = email,
                username = username,
                phone = phone,
                onValueChange = { key, value ->
                    when (key) {
                        "firstName" -> firstName = value
                        "lastName" -> lastName = value
                        "dni" -> dni = value
                        "email" -> email = value
                        "username" -> username = value
                        "phone" -> phone = value
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row {
                Button(
                    onClick = { viewModel.discardChanges() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Descartar")
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = { viewModel.saveChanges(firstName, lastName, dni, email, username, phone) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Guardar")
                }
            }
        }
    }
}

@Composable
fun AvatarSelector() {
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
            .background(Color.Gray),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.AccountCircle,
            contentDescription = "Avatar",
            modifier = Modifier.size(80.dp),
            tint = Color.White
        )
    }
}
