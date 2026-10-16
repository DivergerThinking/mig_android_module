package com.diverger.mig_android_sdk.ui.profile

import java.io.InputStream

/** Lee los bytes de la imagen elegida en el Photo Picker con errores controlados. */
object AvatarImageReader {

    class UnreadableImageException : Exception()

    fun readBytes(openStream: () -> InputStream?): ByteArray {
        val bytes = openStream()?.use { it.readBytes() } ?: throw UnreadableImageException()
        if (bytes.isEmpty()) throw UnreadableImageException()
        return bytes
    }
}
