package at.steinbacher.geckoposelib

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
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
        TODO("Not yet implemented")
    }

    override fun onCompletedWithoutSuccess() {
        TODO("Not yet implemented")
    }

    override fun onFailure(exception: Exception) {
        TODO("Not yet implemented")
    }

    private fun setPoseViewPicture(picturePath: String) {
        poseView.visibility = View.VISIBLE
        poseView.post {
            BitmapFactory.decodeFile(picturePath)?.also { bitmap ->
                val scaledBitmap = resizeAndRotate(bitmap, picturePath, poseView.width, poseView.height)
                poseView.bitmap = scaledBitmap
                onPictureSet()
            }
        }
    }

}