package at.steinbacher.geckoposelib

import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import java.lang.Exception

abstract class GeckoPoseFragment: ImageCaptureFragment() {
    abstract val geckoPoseConfigurations: List<GeckoPoseConfiguration>
    abstract val preferredPose: String
    open val inFrameLikelihoodThreshold: Float = 0.8f

    lateinit var geckoPoseView: GeckoPoseView

    private lateinit var geckoPoseDetection: GeckoPoseDetection

    abstract fun onPictureSet()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        geckoPoseDetection = GeckoPoseDetection(
            detectorMode = AccuratePoseDetectorOptions.SINGLE_IMAGE_MODE,
            configurations = geckoPoseConfigurations,
            listener = object : GeckoPoseDetectionListener {
                override fun onSuccess(geckoPoses: List<GeckoPose>) {
                    val preferred = geckoPoses.getByTag(preferredPose)
                    val best = geckoPoses.getBest(inFrameLikelihoodThreshold)

                    val pose: GeckoPose? = if(preferred != null) {
                        preferred
                    }  else if(best != null){
                        best
                    } else {
                        Toast.makeText(requireContext(), "onSuccess but none found", Toast.LENGTH_SHORT).show()
                        null
                    }

                    if(pose != null) {
                        geckoPoseView.pose = best
                        onPoseSet(pose)
                    }
                }

                override fun onCompletedWithoutSuccess() {
                    Toast.makeText(requireContext(), "onCompletedWithoutSuccess", Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(exception: Exception) {
                    Toast.makeText(requireContext(), "onFailure", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    open fun onPoseSet(pose: GeckoPose) {}

    override fun onPictureReceived(bitmap: Bitmap) {
        setPoseViewPicture(bitmap)
    }

    private fun setPoseViewPicture(bitmap: Bitmap) {
        geckoPoseView.post {
            geckoPoseView.bitmap = bitmap

            val scaledBitmap = resize(
                image = bitmap,
                maxWidth = geckoPoseView.previewWidth,
                maxHeight = geckoPoseView.previewHeight
            )
            geckoPoseDetection.processImage(scaledBitmap)

            onPictureSet()
        }
    }
}