package at.steinbacher.geckoposelib

import android.graphics.Bitmap
import android.util.Log
import androidx.core.graphics.scaleMatrix
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import java.lang.Exception
import kotlin.math.abs
import kotlin.math.atan2

data class PoseAngle(
    val firstPoint: Int,
    val midPoint: Int,
    val lastPoint: Int
)

interface GeckoPoseDetectionListener {
    fun onSuccess(poseLandMarks: List<PoseLandmark>)
    fun onMissingPoseLandmarkType()
    fun onCompletedWithoutSuccess()
    fun onFailure(exception: Exception)
}

/**
 * AccuratePoseDetectorOptions.SINGLE_IMAGE_MODE, AccuratePoseDetectorOptions.STREAM_MODE
 */
class PoseDetection(
    detectorMode: Int,
    private val poseLandmarkTypes: List<Int>,
    private val poseAngles: List<PoseAngle>,
    private val listener: GeckoPoseDetectionListener
) {
    private val options = AccuratePoseDetectorOptions.Builder()
        .setDetectorMode(detectorMode)
        .build()
    private val poseDetector = PoseDetection.getClient(options)

    fun processImage(bitmap: Bitmap) {
        val inputImage = InputImage.fromBitmap(bitmap, 0)

        var successCalled = false
        poseDetector.process(inputImage)
            .addOnSuccessListener { pose ->
                successCalled = true

                val poseLandmarks = ArrayList<PoseLandmark>()
                var missingMandatoryLandmark = false

                poseLandmarkTypes.forEach {
                    val poseLandmark = pose.getPoseLandmark(it)
                    if(poseLandmark == null || poseLandmark.inFrameLikelihood < 0.8) {
                        missingMandatoryLandmark = true
                    } else {
                        poseLandmarks.add(poseLandmark)
                    }
                }

                if(missingMandatoryLandmark) {
                    listener.onMissingPoseLandmarkType()
                } else {
                    listener.onSuccess(poseLandmarks)
                }
            }
            .addOnFailureListener{ listener.onFailure(it) }
            .addOnCompleteListener {
                if(!successCalled) {
                    listener.onCompletedWithoutSuccess()
                }
            }
    }

    private fun getAngle(firstPoint: PoseLandmark, midPoint: PoseLandmark, lastPoint: PoseLandmark): Double {
        var result = Math.toDegrees(
            (atan2(lastPoint.position.y - midPoint.position.y,
                lastPoint.position.x - midPoint.position.x)
                    - atan2(firstPoint.position.y - midPoint.position.y,
                firstPoint.position.x - midPoint.position.x)).toDouble()
        )
        result = abs(result) // Angle should never be negative
        if (result > 180) {
            result = 360.0 - result // Always get the acute representation of the angle
        }
        return result
    }
}