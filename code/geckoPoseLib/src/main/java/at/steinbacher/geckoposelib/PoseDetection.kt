package at.steinbacher.geckoposelib

import android.graphics.Bitmap
import at.steinbacher.geckoposelib.data.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import kotlin.collections.ArrayList
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class PoseDetection(
    private val configuration: GeckoPoseConfiguration,
    private val detectorMode: Int,
) {
    private val options = AccuratePoseDetectorOptions.Builder()
        .setDetectorMode(detectorMode) // AccuratePoseDetectorOptions.STREAM_MODE
        .build()
    private val poseDetector = PoseDetection.getClient(options)

    suspend fun processImage(bitmap: Bitmap): GeckoPose? = suspendCoroutine { cont ->
        val inputImage = InputImage.fromBitmap(bitmap, 0)

        var successCalled = false
        var failureCalled = false
        poseDetector.process(inputImage)
            .addOnSuccessListener { pose ->
                successCalled = true

                val geckoPose = processPose(
                    configuration = configuration,
                    pose = pose,
                    srcWidth = bitmap.width,
                    srcHeight = bitmap.height
                )
                cont.resume(geckoPose)
            }
            .addOnFailureListener {
                failureCalled = true

                cont.resume(null)
            }
            .addOnCompleteListener {
                if(!successCalled && !failureCalled) {
                    cont.resume(null)
                }
            }
    }

    private fun processPose(
        configuration: GeckoPoseConfiguration,
        pose: Pose,
        srcWidth: Int,
        srcHeight: Int,
    ): GeckoPose? {
        val landmarkPoints = ArrayList<Point>()
        var missesPoints = false
        configuration.pointConfigurations.forEach { point ->
            val poseLandmark = pose.getPoseLandmark(point.type)

            if(poseLandmark != null && poseLandmark.position.x >= 0 && poseLandmark.position.y >= 0) {
                landmarkPoints.add(point.toProcessedPoint(poseLandmark))
            } else {
                missesPoints = true
                return@forEach
            }
        }

        return if(missesPoints) {
            null
        } else {
            GeckoPose(
                configuration = configuration,
                points = landmarkPoints,
                width = srcWidth,
                height = srcHeight
            )
        }
    }
}
