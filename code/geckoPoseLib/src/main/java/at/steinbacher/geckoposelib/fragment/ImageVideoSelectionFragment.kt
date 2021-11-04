package at.steinbacher.geckoposelib.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import at.steinbacher.geckoposelib.util.DialogUtil

abstract class ImageVideoSelectionFragment: Fragment() {
    abstract fun onPictureReceived(uri: Uri)
    abstract fun onTakePictureFailed()
    abstract fun onChoosePictureFailed()

    abstract fun onVideoReceived(uri: Uri)
    abstract fun onTakeVideoFailed()
    abstract fun onChooseVideoFailed()

    open fun onDialogDismissed() {}

    private lateinit var receivedImageUri: Uri
    private lateinit var receivedVideoUri: Uri

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

    private val takeVideo = registerForActivityResult(ActivityResultContracts.CaptureVideo()) { success ->
        if(success) {
            onVideoReceived(receivedVideoUri)
        } else {
            onTakeVideoFailed()
        }
    }

    private val chooseVideo = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if(result.resultCode == RESULT_OK) {
            onVideoReceived(result.data?.data!!)
        } else {
            onChooseVideoFailed()
        }
    }


    fun openImagePicker(uri: Uri) {
        DialogUtil.showChooseDialog(requireContext(), object : DialogUtil.ChooseDialogListener {
            override fun onResult(selection: DialogUtil.Selection) {
                when (selection) {
                    DialogUtil.Selection.CAMERA -> openTakeImage(uri)
                    DialogUtil.Selection.GALLERY -> openChooseImage()
                }
            }

            override fun onDismiss() {
                onDialogDismissed()
            }
        })
    }

    fun openVideoPicker(uri: Uri) {
        DialogUtil.showChooseDialog(requireContext(), object : DialogUtil.ChooseDialogListener {
            override fun onResult(selection: DialogUtil.Selection) {
                when (selection) {
                    DialogUtil.Selection.CAMERA -> openTakeVideo(uri)
                    DialogUtil.Selection.GALLERY -> openChooseVideo()
                }
            }

            override fun onDismiss() {
                onDialogDismissed()
            }
        })
    }

    fun openTakeImage(uri: Uri) {
        receivedImageUri = uri
        takePicture.launch(receivedImageUri)
    }

    fun openTakeVideo(uri: Uri) {
        receivedVideoUri = uri
        takeVideo.launch(receivedVideoUri)
    }

    fun openChooseImage() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }

        choosePicture.launch(intent)
    }

    fun openChooseVideo() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "video/*"
        }

        chooseVideo.launch(intent)
    }
}