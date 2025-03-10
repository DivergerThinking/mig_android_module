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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.diverger.mig_android_sdk.R
import com.diverger.mig_android_sdk.support.EnvironmentManager
import com.diverger.mig_android_sdk.ui.theme.MIGAndroidSDKTheme

@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel()) {
    val user by viewModel.user.collectAsState()

    var firstName by remember { mutableStateOf(user?.firstName.orEmpty()) }
    var lastName by remember { mutableStateOf(user?.lastName.orEmpty()) }
    var dni by remember { mutableStateOf(user?.dni.orEmpty()) }
    var email by remember { mutableStateOf(user?.email.orEmpty()) }
    var username by remember { mutableStateOf(user?.username.orEmpty()) }
    var phone by remember { mutableStateOf(user?.phone.orEmpty()) }
    val avatar by remember { mutableStateOf(user?.avatar) }

    MIGAndroidSDKTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(16.dp)
                .padding(top = 80.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("SOBRE MÍ", modifier = Modifier.fillMaxWidth(), style = MaterialTheme.typography.headlineLarge, color = Color.White, textAlign = TextAlign.Start, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(20.dp))

                ProfileAvatar(avatar)

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
                    /*Button(
                        onClick = { viewModel.discardChanges() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Descartar")
                    }
                    Spacer(modifier = Modifier.width(10.dp))*/
                    Button(
                        onClick = { viewModel.saveChanges(firstName, lastName, dni, email, username, phone) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan, contentColor = Color.Black),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("EDITAR", style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileAvatar(userAvatar: String?) {
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        if (!userAvatar.isNullOrEmpty()) {
            // 📌 Mostrar la imagen si hay un avatar
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("${EnvironmentManager.getAssetsBaseUrl()}${userAvatar}")
                    .crossfade(true)
                    .build(),
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
            )
        } else {
            // 📌 Mostrar ícono por defecto si no hay avatar
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = "Avatar",
                modifier = Modifier.size(100.dp),
                tint = Color.White
            )
        }
    }
}

