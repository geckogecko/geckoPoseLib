package at.steinbacher.geckoposelib

import android.graphics.Bitmap
import at.steinbacher.geckoposelib.data.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import java.util.*
import kotlin.collections.ArrayList
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

typealias ChoosePoseLogic = (geckoPoses: List<OnImagePose?>) -> OnImagePose?
typealias ManipulatePoseLogic = (bitmap: Bitmap, onImagePose: OnImagePose) -> Pair<Bitmap, OnImagePose>

class SingleImagePoseDetection(
    private val configurations: List<GeckoPoseConfiguration>
) {
    private val options = AccuratePoseDetectorOptions.Builder()
        .setDetectorMode(AccuratePoseDetectorOptions.SINGLE_IMAGE_MODE)
        .build()
    private val poseDetector = PoseDetection.getClient(options)

    suspend fun processImage(bitmap: Bitmap): List<OnImagePose?>? = suspendCoroutine { cont ->
        val inputImage = InputImage.fromBitmap(bitmap, 0)

        var successCalled = false
        var failureCalled = false
        poseDetector.process(inputImage)
            .addOnSuccessListener { pose ->
                successCalled = true

                val geckoPoses = processPose(configurations, pose)
                cont.resume(geckoPoses)
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

    private fun processPose(configurations: List<GeckoPoseConfiguration>, pose: Pose): List<OnImagePose?> = configurations.map {
        val landmarkPoints = ArrayList<LandmarkPoint>()
        var missesPoints = false
        it.points.forEach { point ->
            val poseLandmark = pose.getPoseLandmark(point.type)

            if(poseLandmark != null && poseLandmark.position.x >= 0 && poseLandmark.position.y >= 0) {
                landmarkPoints.add(point.toProcessedPoint(poseLandmark))
            } else {
                missesPoints = true
                return@forEach
            }
        }

        if(missesPoints) {
            null
        } else {
            OnImagePose(pose = GeckoPose(configuration = it, landmarkPoints = landmarkPoints))
        }
    }
}
