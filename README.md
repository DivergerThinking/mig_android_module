# 📌 **Guía de Integración del Módulo Madrid In Game SDK en Android**

Este documento describe los pasos necesarios para integrar el **Madrid In Game Android SDK** en cualquier aplicación Android. El SDK proporciona acceso a las funcionalidades principales de Madrid In Game, incluyendo reservas, equipos, competiciones y perfil de usuario.

---

## **📌 Requisitos Previos**
Antes de comenzar, asegúrate de cumplir con los siguientes requisitos:

### **📌 Requisitos del Proyecto**
- **Android Studio**: Última versión recomendada.
- **Mínimo SDK**: 25
- **Target SDK**: 35
- **Lenguaje**: Kotlin
- **Jetpack Compose**: Habilitado en el proyecto.
- **Hilt**: Configurado para inyección de dependencias.

---

## **1️⃣ Agregar el Módulo desde JitPack**
El módulo está publicado en **JitPack**, por lo que debes agregarlo a tu proyecto.

### **📌 1.1 Agregar el repositorio de JitPack**
Abre el archivo `settings.gradle.kts` y asegúrate de incluir el repositorio:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

### **📌 1.2 Agregar la dependencia en `build.gradle.kts`**
En el archivo **`build.gradle.kts` (nivel del módulo)**, agrega la siguiente dependencia:

```kotlin
dependencies {
    implementation("com.github.DivergerThinking:mig_android_module:1.1.2")
}
```

Si no tienes **Hilt** habilitado en tu proyecto, **añádelo en el `build.gradle.kts` del nivel del proyecto**:

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}
hilt {
    enableAggregatingTask = false
}
```

---

## **2️⃣ Configurar la Aplicación para Usar Hilt**
Tu aplicación debe heredar de `Application` y estar anotada con `@HiltAndroidApp`.

Crea o modifica la clase `Application`:

```kotlin
package com.tuapp.android

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TuAppAndroid : Application()
```

**Asegúrate de declarar tu aplicación en `AndroidManifest.xml`**:

```xml
<application
    android:name=".TuAppAndroid"
    android:allowBackup="true"
    android:theme="@style/Theme.TuApp">
```

---

## **3️⃣ Configurar el `AndroidManifest.xml`**
Para asegurar que el SDK funcione correctamente, debes agregar las siguientes configuraciones en `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />

<application
    android:name=".TuAppAndroid"
    android:allowBackup="true"
    android:supportsRtl="true"
    android:theme="@style/Theme.TuApp">
    
    <!-- Registro de la actividad del SDK -->
    <activity
        android:name=".MIGSDKActivity"
        android:exported="true"
        android:theme="@style/Theme.TuApp" />
</application>
```

---

## **4️⃣ Iniciar el Módulo Madrid In Game**
Para iniciar el SDK de Madrid In Game en tu aplicación, llama al siguiente `Composable` en la pantalla donde quieras mostrar el módulo:

```kotlin
import com.diverger.mig_android_sdk.MadridInGameAndroidModule

MadridInGameAndroidModule(
    email = "usuario@email.com",
    accessToken = "ABC123TOKEN",
    userName = "Demo User",
    dni = "12345678A",
    logoMIG = null,
    qrMiddleLogo = null
)
```

---

## **5️⃣ Agregar una Pantalla para Iniciar el SDK**
Puedes configurar una pantalla donde el usuario introduzca su email y lo guarde para futuras sesiones.

```kotlin
package com.tuapp.android

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.TextFieldValue
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen(context = this)
        }
    }
}

@Composable
fun MainScreen(context: Context) {
    var email by remember { mutableStateOf(TextFieldValue("")) }
    val savedEmails = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        savedEmails.addAll(loadSavedEmails(context))
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Ingresa tu correo", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(10.dp))

        BasicTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                val trimmedEmail = email.text.trim()
                if (trimmedEmail.isNotEmpty()) {
                    saveEmail(context, trimmedEmail)
                    openSDKActivity(context, trimmedEmail)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Iniciar sesión")
        }
    }
}
```
