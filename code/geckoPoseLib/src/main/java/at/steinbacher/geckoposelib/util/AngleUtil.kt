package at.steinbacher.geckoposelib.util

import at.steinbacher.geckoposelib.LandmarkPoint
import at.steinbacher.geckoposelib.PointF
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
}