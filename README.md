# 📌 Madrid In Game Android SDK

## 🚀 Instalación

Para agregar la librería a tu proyecto, usa **JitPack** en tu archivo `build.gradle`:

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation("com.github.DivergerThinking:mig_android_module:1.2.3")
}
```

---

## 📌 Uso del Módulo

El módulo se integra llamando a la función `MadridInGameAndroidModuleEntryPoint.launch()`, que inicia una nueva **Activity** para la experiencia completa.

### **📌 Punto de entrada**

```kotlin
import android.content.Context
import com.diverger.mig_android_sdk.MadridInGameAndroidModuleEntryPoint

fun iniciarMadridInGameSDK(context: Context) {
    MadridInGameAndroidModuleEntryPoint.launch(
        context = context,
        email = "usuario@example.com",
        accessToken = "TOKEN_DE_ACCESO",
        userName = "Nombre Usuario",
        dni = "12345678A",
        logoMIG = null,
        qrMiddleLogo = null
    )
}
```

Esto lanzará la **Activity del módulo** con la interfaz completa de Madrid In Game.

---
