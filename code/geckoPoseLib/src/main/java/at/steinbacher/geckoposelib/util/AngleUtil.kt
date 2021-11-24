package at.steinbacher.geckoposelib.util

import at.steinbacher.geckoposelib.data.LandmarkPoint
import at.steinbacher.geckoposelib.data.PointF
import kotlin.math.*

object AngleUtil {
    fun getAngle(firstPoint: LandmarkPoint, midPoint: LandmarkPoint, lastPoint: LandmarkPoint): Double {
        return getAngle(firstPoint.position, midPoint.position, lastPoint.position)
    }

    fun getAngle(firstPoint: PointF, midPoint: PointF, lastPoint: PointF): Double {
        val vectorAx = (firstPoint.x - midPoint.x)
        val vectorAy = (firstPoint.y - midPoint.y)

        val vectorBx = (lastPoint.x - midPoint.x)
        val vectorBy = (lastPoint.y - midPoint.y)

        var angle: Double = atan2(vectorBy, vectorBx) - atan2(vectorAy, vectorAx).toDouble()
        if(angle < 0) {
            angle += 2 * Math.PI
        }

        return Math.toDegrees(angle)
    }

    /**
     * Get the pointing angle if a line between the two points. Pointing direction is from second to first
     */
    fun getClockWiseAngle(firstPoint: PointF, secondPoint: PointF): Double {
        val angleStart = PointF(secondPoint.x, 0f)
        return getAngle(angleStart, secondPoint, firstPoint)
    }

    fun getCounterClockWiseAngle(firstPoint: PointF, secondPoint: PointF): Double {
        return 360 - getClockWiseAngle(firstPoint, secondPoint)
    }
}