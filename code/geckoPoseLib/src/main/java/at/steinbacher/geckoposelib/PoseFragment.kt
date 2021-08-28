package at.steinbacher.geckoposelib

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Toast
import java.lang.Exception
import kotlin.math.max
import kotlin.math.min

abstract class PoseFragment: ImageCaptureFragment(), GeckoPoseDetectionListener {

    lateinit var poseView: PoseView

    abstract val poseDetection: PoseDetection

    abstract fun onPictureSet()

    override fun onPictureTaken(picturePath: String) {
        setPoseViewPicture(picturePath)
    }

    override fun onMissingPoseLandmarkType() {
        Toast.makeText(requireContext(), "onMissingPoseLandmarkType", Toast.LENGTH_SHORT).show()
    }

    override fun onCompletedWithoutSuccess() {
        Toast.makeText(requireContext(), "onCompletedWithoutSuccess", Toast.LENGTH_SHORT).show()
    }

    override fun onFailure(exception: Exception) {
        Toast.makeText(requireContext(), "onFailure", Toast.LENGTH_SHORT).show()
    }

    private fun setPoseViewPicture(picturePath: String) {
        poseView.visibility = View.VISIBLE
        poseView.post {
            BitmapFactory.decodeFile(picturePath)?.also { bitmap ->
                val scaledBitmap = resizeAndRotate(bitmap, picturePath, poseView.width, poseView.height)
                poseView.bitmap = scaledBitmap
                poseDetection.processImage(scaledBitmap)
                onPictureSet()
            }
        }
    }

}