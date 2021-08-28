package at.steinbacher.geckoposelib

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

abstract class ImageCaptureFragment: Fragment() {

    companion object {
     const val REQUEST_IMAGE_CAPTURE = 1001
    }

    private lateinit var currentPicturePath: String

    abstract fun onPictureTaken(picturePath: String)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            onPictureTaken(currentPicturePath)
        }
    }

    fun openTakePicture() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(requireContext().packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        requireContext(),
                        "at.steinbacher.geckoposelib.provider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }

    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPicturePath = absolutePath
        }
    }

    protected fun resizeAndRotate(image: Bitmap, imagePath: String, maxWidth: Int, maxHeight: Int): Bitmap {
        return if (maxHeight > 0 && maxWidth > 0) {
            val exifInterface = ExifInterface(imagePath)
            val rotationDegrees = when(exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }

            val rotationMatrix = Matrix().apply {
                postRotate(rotationDegrees)
            }

            val rotated = Bitmap.createBitmap(image, 0, 0, image.width, image.height, rotationMatrix, true)

            val width = rotated.width
            val height = rotated.height
            val ratioBitmap = width.toFloat() / height.toFloat()
            val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()
            var finalWidth = maxWidth
            var finalHeight = maxHeight
            if (ratioMax > ratioBitmap) {
                finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
            } else {
                finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
            }


            Bitmap.createScaledBitmap(rotated, finalWidth, finalHeight, true)
        } else {
            image
        }
    }

}