package at.steinbacher.geckoposelib

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import com.github.dhaval2404.imagepicker.ImagePicker
import android.provider.MediaStore
import android.util.Log

abstract class ImageCaptureFragment: Fragment() {

    companion object {
     const val REQUEST_IMAGE_CAPTURE = 1001
    }

    abstract fun onPictureReceived(uri: Uri)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            onPictureReceived(data?.data!!)
        }
    }

    fun openImagePicker() {
        ImagePicker.with(this)
            .start(REQUEST_IMAGE_CAPTURE)
    }

    fun openTakeImage() {
        ImagePicker.with(this)
            .cameraOnly()
            .start(REQUEST_IMAGE_CAPTURE)
    }

    fun openChooseFromGallery() {
        ImagePicker.with(this)
            .galleryOnly()
            .start(REQUEST_IMAGE_CAPTURE)
    }

    protected fun resize(image: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
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

            Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true)
        } else {
            error("maxWidth or maxHeight is 0")
        }
    }

}