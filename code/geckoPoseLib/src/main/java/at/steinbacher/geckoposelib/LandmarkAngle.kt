package at.steinbacher.geckoposelib

import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.abs
import kotlin.math.atan2

data class LandmarkAngle(
    val startLandmarkType: Int,
    val middleLandmarkType: Int,
    val endLandmarkType: Int,
    val landmarkLine: LandmarkLineResult
) {
    val startPoint: PoseLandmark
        get() = landmarkLine.getPoseLandmark(startLandmarkType)!!
    val middlePoint: PoseLandmark
        get() = landmarkLine.getPoseLandmark(middleLandmarkType)!!
    val endPoint: PoseLandmark
        get() = landmarkLine.getPoseLandmark(endLandmarkType)!!

    val angle: Double
        get() = getAngle(startPoint, middlePoint, endPoint)

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
