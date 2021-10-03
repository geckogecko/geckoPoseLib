package at.steinbacher.geckoposelib.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore

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
}