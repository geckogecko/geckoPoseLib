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
            .cropSquare()
            .setImageProviderInterceptor { imageProvider -> onImagePickerSelected(imageProvider.name) }
            .start(REQUEST_IMAGE_CAPTURE)
    }

    open fun onImagePickerSelected(name: String) {
        Log.i("ImageCaptureFragment", "onImagePickerSelected: $name")
    }

    fun openTakeImage() {
        ImagePicker.with(this)
            .cameraOnly()
            .cropSquare()
            .start(REQUEST_IMAGE_CAPTURE)
    }

    fun openChooseFromGallery() {
        ImagePicker.with(this)
            .galleryOnly()
            .cropSquare()
            .start(REQUEST_IMAGE_CAPTURE)
    }
}