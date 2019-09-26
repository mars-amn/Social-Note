package playground.develop.socialnote.coilloader.extensions

import android.graphics.Bitmap
import android.graphics.Bitmap.createScaledBitmap
import kotlin.math.round

internal fun Bitmap.upscaleTo(desiredWidth: Int): Bitmap {
    val ratio = this.height.toFloat() / this.width.toFloat()
    val proportionateHeight = ratio * desiredWidth
    val finalHeight = round(proportionateHeight.toDouble()).toInt()

    return createScaledBitmap(this, desiredWidth, finalHeight, true)
}