package at.steinbacher.geckoposelib

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import java.lang.Exception
import kotlin.math.abs
import kotlin.math.atan2


interface GeckoPoseDetectionListener {
    fun onSuccess(landmarkLineResults: List<LandmarkLineResult>)
    fun onMissingPoseLandmarkType()
    fun onCompletedWithoutSuccess()
    fun onFailure(exception: Exception)
}


/**
 * AccuratePoseDetectorOptions.SINGLE_IMAGE_MODE, AccuratePoseDetectorOptions.STREAM_MODE
 */
class PoseDetection(
    detectorMode: Int,
    private val landmarkLines: List<LandmarkLine>,
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

                var missingMandatoryLandmark = false

                //lines
                val landMarkLineResults = ArrayList<LandmarkLineResult>()
                landmarkLines.forEach { landmarkLine ->
                    val poseLandmarksLine = ArrayList<PoseLandmark>()
                    landmarkLine.poseLandmarkTypes.forEach {
                        val poseLandmark = pose.getPoseLandmark(it)
                        if(poseLandmark == null || poseLandmark.inFrameLikelihood < 0.8) {
                            missingMandatoryLandmark = true
                        } else {
                            poseLandmarksLine.add(poseLandmark)
                        }
                    }

                    landMarkLineResults.add(
                        LandmarkLineResult(
                            tag = landmarkLine.tag,
                            poseLandmarks = poseLandmarksLine
                        )
                    )
                }


                if(missingMandatoryLandmark) {
                    listener.onMissingPoseLandmarkType()
                } else {
                    listener.onSuccess(landMarkLineResults)
                }
            }
            .addOnFailureListener{ listener.onFailure(it) }
            .addOnCompleteListener {
                if(!successCalled) {
                    listener.onCompletedWithoutSuccess()
                }
            }
    }
}