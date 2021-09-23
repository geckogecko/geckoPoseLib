package at.steinbacher.geckoposelib

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import java.lang.Exception

abstract class GeckoPoseFragment: ImageCaptureFragment() {
    abstract val geckoPoseConfigurations: List<GeckoPoseConfiguration>
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
                    val best = geckoPoses.getBest(inFrameLikelihoodThreshold)

                    if(best != null) {
                        geckoPoseView.pose = best
                    } else {
                        Toast.makeText(requireContext(), "onSuccess but no best", Toast.LENGTH_SHORT).show()
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


    override fun onPictureReceived(bitmap: Bitmap) {
        setPoseViewPicture(bitmap)
    }

    private fun setPoseViewPicture(bitmap: Bitmap) {
        geckoPoseView.post {
            val scaledBitmap = resizeAndRotate(bitmap, geckoPoseView.width, geckoPoseView.height)
            geckoPoseView.bitmap = scaledBitmap
            geckoPoseDetection.processImage(scaledBitmap)
            onPictureSet()
        }
    }
}