package at.steinbacher.geckoposelib

import android.graphics.Bitmap
import at.steinbacher.geckoposelib.LandmarkPoint.Companion.toProcessedPoint
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import java.lang.Exception


interface GeckoPoseDetectionListener {
    fun onSuccess(geckoPoses: List<GeckoPose>)
    fun onCompletedWithoutSuccess()
    fun onFailure(exception: Exception)
}


/**
 * AccuratePoseDetectorOptions.SINGLE_IMAGE_MODE, AccuratePoseDetectorOptions.STREAM_MODE
 */
class GeckoPoseDetection(
    detectorMode: Int,
    private val configurations: List<GeckoPoseConfiguration>,
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

                val geckoPoses = processPose(configurations, pose)
                listener.onSuccess(geckoPoses)
            }
            .addOnFailureListener{ listener.onFailure(it) }
            .addOnCompleteListener {
                if(!successCalled) {
                    listener.onCompletedWithoutSuccess()
                }
            }
    }

    private fun processPose(configurations: List<GeckoPoseConfiguration>, pose: Pose): List<GeckoPose> = configurations.map {
        GeckoPose(it).apply {
            configuration.points.forEach { point ->
                pose.getPoseLandmark(point.type)?.let { poseLandmark ->
                    this.landmarkPoints.add(point.toProcessedPoint(poseLandmark))
                }
            }
        }
    }
}