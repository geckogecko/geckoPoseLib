package at.steinbacher.geckoposelib

import android.graphics.PointF
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.abs
import kotlin.math.atan2

object Util {
    fun getAngle(firstPoint: PoseLandmark, midPoint: PoseLandmark, lastPoint: PoseLandmark): Double {
        return getAngle(firstPoint.position, midPoint.position, lastPoint.position)
    }

    fun getAngle(firstPoint: PointF, midPoint: PointF, lastPoint: PointF): Double {
        var result = Math.toDegrees(
            (atan2(lastPoint.y - midPoint.y,
                lastPoint.x - midPoint.x)
                    - atan2(firstPoint.y - midPoint.y,
                firstPoint.x - midPoint.x)).toDouble()
        )
        result = abs(result) // Angle should never be negative
        if (result > 180) {
            result = 360.0 - result // Always get the acute representation of the angle
        }
        return result
    }
}