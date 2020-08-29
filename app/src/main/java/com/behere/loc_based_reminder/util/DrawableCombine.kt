package com.behere.loc_based_reminder.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix

class DrawableCombine() {
    fun overlay(bitmap1: Bitmap, bitmap2: Bitmap?): Bitmap? {
        val bmOverlay =
            Bitmap.createBitmap(bitmap1.width, bitmap1.height, bitmap1.config)
        val canvas = Canvas(bmOverlay)
        canvas.drawBitmap(bitmap1, Matrix(), null)
        canvas.drawBitmap(bitmap2!!, 0f, 0f, null)
        return bmOverlay
    }
}
