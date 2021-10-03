package at.steinbacher.geckoposelib

import android.graphics.PointF
import kotlin.math.abs
import kotlin.math.atan2

data class AnglePair(val firstAngle: Double, val secondAngle: Double)

object AngleUtil {
    fun getSmallestAngle(firstPoint: Point, midPoint: Point, lastPoint: Point): Double {
        return getSmallestAngle(firstPoint.position, midPoint.position, lastPoint.position)
    }

    fun getSmallestAngle(firstPoint: PointF, midPoint: PointF, lastPoint: PointF): Double {
        var result = getAngle(firstPoint, midPoint, lastPoint)

        if (result > 180) {
            result = 360.0 - result // Always get the acute representation of the angle
        }
        return result
    }

    fun getAngle(firstPoint: Point, midPoint: Point, lastPoint: Point): Double {
        return getAngle(firstPoint.position, midPoint.position, lastPoint.position)
    }

    fun getAngle(firstPoint: PointF, midPoint: PointF, lastPoint: PointF): Double {
        val result = Math.toDegrees(
            (atan2(lastPoint.y - midPoint.y,
                lastPoint.x - midPoint.x)
                    - atan2(firstPoint.y - midPoint.y,
                firstPoint.x - midPoint.x)).toDouble()
        )
        return abs(result) // Angle should never be negative
    }

    fun getAnglePair(firstPoint: Point, midPoint: Point, lastPoint: Point): AnglePair {
        return getAnglePair(firstPoint.position, midPoint.position, lastPoint.position)
    }

    fun getAnglePair(firstPoint: PointF, midPoint: PointF, lastPoint: PointF): AnglePair {
        val result = Math.toDegrees(
            (atan2(lastPoint.y - midPoint.y, lastPoint.x - midPoint.x)
                    - atan2(firstPoint.y - midPoint.y, firstPoint.x - midPoint.x)).toDouble()
        )
        val angle = abs(result)
        return AnglePair(angle, 360-angle)
    }
}