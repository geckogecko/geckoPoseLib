package at.steinbacher.geckoposelib.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.ColorRes
import at.steinbacher.geckoposelib.GeckoPose
import com.google.mlkit.vision.pose.Pose

object BitmapUtil {
    fun getBitmap(uri: Uri, context: Context): Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        ImageDecoder.decodeBitmap(
            ImageDecoder.createSource(context.contentResolver, uri)
        ) { decoder, _, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            decoder.isMutableRequired = true
        }
    } else {
        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
    }

    fun resize(image: Bitmap, maxWidth: Int, maxHeight: Int): Triple<Bitmap, Float, Float> {
        return if (maxHeight > 0 && maxWidth > 0) {
            val width = image.width
            val height = image.height
            val ratioBitmap = width.toFloat() / height.toFloat()
            val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()
            var finalWidth = maxWidth
            var finalHeight = maxHeight

            if (ratioMax > ratioBitmap) {
                finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
            } else {
                finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
            }

            val scaleX = finalWidth / width.toFloat()
            val scaleY = finalHeight / height.toFloat()

            Triple(Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true), scaleX, scaleY)
        } else {
            error("maxWidth or maxHeight is 0")
        }
    }

    fun getContrastColor(bitmap: Bitmap, x: Int, y: Int, @ColorRes contrastColorLight: Int, @ColorRes contrastColorDark: Int): Int {
        val pixel = bitmap.getPixel(x, y)
        return getContrastColor(pixel, contrastColorLight, contrastColorDark)
    }

    private fun getContrastColor(color: Int, @ColorRes contrastColorLight: Int, @ColorRes contrastColorDark: Int): Int {
        val r = color shr 16 and 0xFF
        val g = color shr 8 and 0xFF
        val b = color and 0xFF
        val a = 1 - (0.299 * r + 0.578 * g + 0.114 * b) / 255

        return if (a < 0.5) {
            contrastColorDark
        } else {
            contrastColorLight
        }
    }
}