package at.steinbacher.geckoposelib

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

abstract class ImageCaptureFragment: Fragment() {
    abstract fun onPictureReceived(uri: Uri)
    abstract fun onTakePictureFailed()
    abstract fun onChoosePictureFailed()

    private lateinit var receivedImageUri: Uri

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if(success) {
            onPictureReceived(receivedImageUri)
        } else {
            onTakePictureFailed()
        }
    }

    private val choosePicture = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if(result.resultCode == RESULT_OK) {
            onPictureReceived(result.data?.data!!)
        } else {
            onChoosePictureFailed()
        }
    }

    private val takeVideo = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if(result.resultCode == RESULT_OK) {

        } else {

        }
    }

    private val chooseVideo = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if(result.resultCode == RESULT_OK) {

        } else {

        }
    }


    fun openImagePicker(uri: Uri) {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "video/*"
        }
        chooseVideo.launch(intent)
    }

    fun openTakeImage(uri: Uri) {
        receivedImageUri = uri
        takePicture.launch(receivedImageUri)
    }

    fun openTakeVideo() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        takeVideo.launch(intent)
    }

    fun openChooseImage() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }

        choosePicture.launch(intent)
    }
}