package playground.develop.socialnote.coilloader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.util.DisplayMetrics
import coil.Coil
import coil.api.load
import org.wordpress.aztec.Html
import playground.develop.socialnote.coilloader.extensions.upscaleTo


class CoilImageLoader(private val context: Context) : Html.ImageGetter {

    override fun loadImage(source: String, callbacks: Html.ImageGetter.Callbacks, maxWidth: Int) {
        loadImage(source, callbacks, maxWidth, 0)
    }

    override fun loadImage(source: String,
                           callbacks: Html.ImageGetter.Callbacks,
                           maxWidth: Int,
                           minWidth: Int) {
        Coil.load(context, source) {
            target { drawable ->
                var bitmap: Bitmap?
                if (drawable is BitmapDrawable) {
                    if (drawable.bitmap != null) {
                        bitmap = drawable.bitmap
                    }
                }

                bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
                    Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                } else {
                    Bitmap.createBitmap(drawable.intrinsicWidth,
                                        drawable.intrinsicHeight,
                                        Bitmap.Config.ARGB_8888)
                }

                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)

                if (bitmap != null && bitmap.width < minWidth) {
                    callbacks.onImageLoaded(BitmapDrawable(context.resources,
                                                           bitmap.upscaleTo(minWidth)))
                    return@target
                }
                bitmap.density = DisplayMetrics.DENSITY_DEFAULT
                callbacks.onImageLoaded(BitmapDrawable(context.resources, bitmap))
            }

        }
    }
}