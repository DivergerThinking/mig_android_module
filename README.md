# 📌 Madrid In Game Android SDK

Este SDK proporciona una integración sencilla para incluir la funcionalidad de **Madrid In Game** en tu aplicación Android.

---

## **🔧 Instalación**

Este SDK se encuentra disponible en **JitPack**. Para agregarlo a tu proyecto:

### 1️⃣ **Añadir JitPack en `settings.gradle.kts`**
```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

### 2️⃣ **Añadir la dependencia en `build.gradle.kts`**
```kotlin
dependencies {
    implementation("com.github.DivergerThinking:mig_android_module:1.1.4")
}
```

---

## **🚀 Implementación**

Para integrar el SDK en tu aplicación, llama a `MadridInGameAndroidModule` en la pantalla donde desees cargar el módulo.

### **📌 Implementación básica**
```kotlin
import com.diverger.mig_android_sdk.MadridInGameAndroidModule

MadridInGameAndroidModule(
    email = "usuario@example.com",
    userName = "Usuario Prueba",
    dni = "12345678A",
    accessToken = "TOKEN_DE_ACCESO"
)

---

## **📱 Ejemplo de uso en una `Activity`**

```kotlin
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.diverger.mig_android_sdk.MadridInGameAndroidModule

typealias MIGActivity = ComponentActivity

class MainActivity : MIGActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MadridInGameAndroidModule(
                email = "usuario@example.com",
                accessToken = "TOKEN_DE_ACCESO"
            )
        }
    }
}
```

