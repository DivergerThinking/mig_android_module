package com.diverger.mig_android_sdk.ui.profile

import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.diverger.mig_android_sdk.R
import com.diverger.mig_android_sdk.support.EnvironmentManager
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

    // Lanzador para elegir imagen
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            newAvatarUri = it
            // Iniciamos la subida y actualización
            viewModel.onAvatarSelected(it, context)
        }
    }

    // Selección de permiso según versión de Android
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        android.Manifest.permission.READ_MEDIA_IMAGES
    } else {
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pickImageLauncher.launch("image/*")
        } else {
            Toast.makeText(context, "Permiso denegado", Toast.LENGTH_SHORT).show()
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
                modifier = Modifier
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "SOBRE MÍ",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    textAlign = TextAlign.Start,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(20.dp))

                // ─────── Cambios mínimos aquí ───────
                Box(
                    modifier = Modifier
                        .wrapContentSize()                         // Permite que el texto sea más ancho que 120dp
                        .clickable {
                            // Comprobamos permiso antes de lanzar el picker
                            when {
                                ContextCompat.checkSelfPermission(context, permission)
                                        == PackageManager.PERMISSION_GRANTED -> {
                                    pickImageLauncher.launch("image/*")
                                }
                                else -> {
                                    permissionLauncher.launch(permission)
                                }
                            }
                        },
                    contentAlignment = Alignment.TopCenter         // Alinea la imagen circular en la parte superior
                ) {
                    // 1. El círculo de 120dp donde se recorta la imagen
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            // Si se acaba de escoger uno, mostrar preview
                            newAvatarUri != null -> AsyncImage(
                                model = newAvatarUri,
                                contentDescription = "Nuevo Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            // Si ya había un avatar en servidor, mostrarlo
                            user?.avatar?.isNotEmpty() == true -> AsyncImage(
                                model = "${EnvironmentManager.getAssetsBaseUrl()}${user!!.avatar}",
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            else -> Icon(
                                imageVector = Icons.Filled.AccountCircle,
                                contentDescription = "Avatar por defecto",
                                modifier = Modifier.size(100.dp),
                                tint = Color.White
                            )
                        }
                    }

                    // 2. Texto “Pulsar para cambiar” justo debajo del círculo
                    Text(
                        text = "Pulsar para cambiar",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier
                            .padding(top = 128.dp)                     // 120dp de altura del círculo + 8dp de separación
                            .background(Color.Black.copy(alpha = 0.4f)) // Fondo semitransparente
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                // ──────────────────────────────────────────────

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
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://personal-area.azurewebsites.net")
                            )
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Cyan,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("ÁREA PERSONAL", style = MaterialTheme.typography.headlineSmall)
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

