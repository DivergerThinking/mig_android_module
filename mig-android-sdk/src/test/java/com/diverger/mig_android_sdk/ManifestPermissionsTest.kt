package com.diverger.mig_android_sdk

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Guarda de regresión de la política de fotos y vídeos de Google Play:
 * el SDK no puede declarar permisos de acceso a la galería (se usa el Photo Picker).
 */
class ManifestPermissionsTest {

    private val manifest = File("src/main/AndroidManifest.xml").readText()

    @Test
    fun `manifest does not declare media or storage read permissions`() {
        listOf(
            "android.permission.READ_MEDIA_IMAGES",
            "android.permission.READ_MEDIA_VIDEO",
            "android.permission.READ_MEDIA_VISUAL_USER_SELECTED",
            "android.permission.READ_EXTERNAL_STORAGE",
        ).forEach { permission ->
            assertFalse("$permission must not be declared", manifest.contains(permission))
        }
    }

    @Test
    fun `manifest declares photo picker backport module`() {
        assertTrue(manifest.contains("com.google.android.gms.metadata.ModuleDependencies"))
        assertTrue(manifest.contains("photopicker_activity:0:required"))
    }
}
