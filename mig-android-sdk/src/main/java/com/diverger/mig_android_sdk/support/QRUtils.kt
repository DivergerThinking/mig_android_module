package com.diverger.mig_android_sdk.support

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

fun generateQrBitmap(
    qrText: String,
    qrSize: Int = 500,
    logoBitmap: Bitmap? = null
): Bitmap? {
    return runCatching {
        val bitMatrix = QRCodeWriter().encode(qrText, BarcodeFormat.QR_CODE, qrSize, qrSize)
        val qrBmp = Bitmap.createBitmap(qrSize, qrSize, Bitmap.Config.ARGB_8888)
        for (x in 0 until qrSize) {
            for (y in 0 until qrSize) {
                qrBmp.setPixel(
                    x,
                    y,
                    if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                )
            }
        }

        if (logoBitmap == null) return@runCatching qrBmp

        val canvas = android.graphics.Canvas(qrBmp)
        val scale = 0.2f
        val logoWidth = (qrSize * scale).toInt()
        val logoHeight = (qrSize * scale).toInt()
        val left = (qrSize - logoWidth) / 2f
        val top = (qrSize - logoHeight) / 2f
        val destRect = android.graphics.RectF(left, top, left + logoWidth, top + logoHeight)
        canvas.drawBitmap(logoBitmap, null, destRect, null)

        qrBmp
    }.getOrNull()
}