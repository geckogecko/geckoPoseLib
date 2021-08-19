package at.steinbacher.geckoposelib

import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.abs
import kotlin.math.atan2

data class LandmarkAngle(
    val startPoint: PoseLandmark,
    val middlePoint: PoseLandmark,
    val lastPoint: PoseLandmark,
) {
    val angle: Double = getAngle(startPoint, middlePoint, lastPoint)

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