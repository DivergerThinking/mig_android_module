package com.diverger.mig_android_sdk.ui.profile

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.diverger.mig_android_sdk.R
import com.diverger.mig_android_sdk.support.EnvironmentManager
import com.diverger.mig_android_sdk.support.localizedString
import com.diverger.mig_android_sdk.ui.theme.MIGAndroidSDKTheme

@Composable
fun ProfileScreen(viewModel: ProfileViewModel = viewModel()) {
    val context = LocalContext.current
    val user by viewModel.user.collectAsState()

    var firstName by remember { mutableStateOf(user?.firstName.orEmpty()) }
    var lastName by remember { mutableStateOf(user?.lastName.orEmpty()) }
    var dni by remember { mutableStateOf(user?.dni.orEmpty()) }
    var email by remember { mutableStateOf(user?.email.orEmpty()) }
    var username by remember { mutableStateOf(user?.username.orEmpty()) }
    var phone by remember { mutableStateOf(user?.phone.orEmpty()) }
    val avatar by remember { mutableStateOf(user?.avatar) }

    var newAvatarUri by remember { mutableStateOf<Uri?>(null) }

    // Photo Picker del sistema: no requiere permisos de galería (política Google Play)
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            newAvatarUri = it
            viewModel.onAvatarSelected(it, context)
        }
    }

    MIGAndroidSDKTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(16.dp)
                .padding(top = 80.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(R.string.profile).uppercase(),
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    textAlign = TextAlign.Start,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(20.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .wrapContentSize()
                            .clickable {
                                pickImageLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                newAvatarUri != null -> AsyncImage(
                                    model = newAvatarUri,
                                    contentDescription = "Nuevo Avatar",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                    placeholder = rememberVectorPainter(Icons.Filled.AccountCircle),
                                    error = rememberVectorPainter(Icons.Filled.AccountCircle)
                                )

                                user?.avatar?.isNotEmpty() == true -> AsyncImage(
                                    model = "${EnvironmentManager.getAssetsBaseUrl()}${user!!.avatar}",
                                    contentDescription = "Avatar",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize().background(Color.LightGray),
                                    placeholder = rememberVectorPainter(Icons.Filled.AccountCircle),
                                    error = rememberVectorPainter(Icons.Filled.AccountCircle)
                                )

                                else -> Icon(
                                    imageVector = Icons.Filled.AccountCircle,
                                    contentDescription = "Avatar por defecto",
                                    modifier = Modifier.size(100.dp),
                                    tint = Color.White
                                )
                            }
                        }

                        Text(
                            text = stringResource(R.string.profile_chante_avatar_label),
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            modifier = Modifier
                                .padding(top = 128.dp)
                                .background(Color.Black.copy(alpha = 0.4f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

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
                }

                Button(
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            "https://personal-area.azurewebsites.net".toUri()
                        )
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Cyan,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .padding(vertical = 20.dp)
                ) {
                    Text(
                        localizedString("dashboard.profile.about.title", R.string.personal_area),
                        style = MaterialTheme.typography.headlineSmall
                    )
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

