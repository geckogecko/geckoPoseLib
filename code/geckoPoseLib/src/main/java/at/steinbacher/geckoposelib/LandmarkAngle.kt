package at.steinbacher.geckoposelib

import android.graphics.Color
import android.graphics.Paint
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.abs
import kotlin.math.atan2

data class LandmarkAngle(
    val startLandmarkType: Int,
    val middleLandmarkType: Int,
    val endLandmarkType: Int,
    val landmarkLine: LandmarkLineResult,
    val displayTag: String,
    val color: Int
) {
    val startPoint: PoseLandmark
        get() = landmarkLine.getPoseLandmark(startLandmarkType)!!
    val middlePoint: PoseLandmark
        get() = landmarkLine.getPoseLandmark(middleLandmarkType)!!
    val endPoint: PoseLandmark
        get() = landmarkLine.getPoseLandmark(endLandmarkType)!!

    val angle: Double
        get() = Util.getAngle(startPoint, middlePoint, endPoint)
}
