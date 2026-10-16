package com.diverger.mig_android_sdk.ui.profile

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.InputStream

class AvatarImageReaderTest {

    @Test
    fun `reads all bytes and closes the stream`() {
        val data = byteArrayOf(1, 2, 3, 4)
        var closed = false
        val stream = object : InputStream() {
            private val delegate = ByteArrayInputStream(data)
            override fun read(): Int = delegate.read()
            override fun close() { closed = true }
        }

        val result = AvatarImageReader.readBytes { stream }

        assertArrayEquals(data, result)
        assertTrue(closed)
    }

    @Test(expected = AvatarImageReader.UnreadableImageException::class)
    fun `null stream throws a controlled error`() {
        AvatarImageReader.readBytes { null }
    }

    @Test(expected = AvatarImageReader.UnreadableImageException::class)
    fun `empty image throws a controlled error`() {
        AvatarImageReader.readBytes { ByteArrayInputStream(ByteArray(0)) }
    }
}
