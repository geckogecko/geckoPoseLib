package at.steinbacher.geckoposelib

import androidx.annotation.ColorRes
import at.steinbacher.geckoposelib.util.AngleUtil
import com.google.mlkit.vision.pose.PoseLandmark
import kotlinx.serialization.Serializable

@Serializable
class GeckoPoseConfiguration(
    val tag: String,
    val points: List<Point> = listOf(),
    val lines: List<Line> = listOf(),
    val angles: List<Angle> = listOf(),
    @ColorRes val defaultPointColorLight: Int,
    @ColorRes val defaultPointColorDark: Int,
    @ColorRes val defaultSelectedPointColor: Int,
    @ColorRes val defaultLineColor: Int,
    @ColorRes val defaultAngleColor: Int,
    @ColorRes val defaultNOKAngleColor: Int,
) {
    fun copy() = GeckoPoseConfiguration(
        tag = this.tag,
        points = this.points.map { it.copy() },
        lines = this.lines.map { it.copy() },
        angles = this.angles.map { it.copy() },
        defaultPointColorLight = this.defaultPointColorLight,
        defaultPointColorDark = this.defaultPointColorDark,
        defaultSelectedPointColor = this.defaultSelectedPointColor,
        defaultLineColor = this.defaultLineColor,
        defaultAngleColor = this.defaultAngleColor,
        defaultNOKAngleColor = this.defaultNOKAngleColor
    )
}

@Serializable
class GeckoPose(
    val configuration: GeckoPoseConfiguration
){
    val landmarkPoints: ArrayList<LandmarkPoint> = arrayListOf()

    val foundPointTypes: List<Int>
        get() = landmarkPoints.map { it.point.type }

    val missesPoints: Boolean
        get() = !foundPointTypes.containsAll(foundPointTypes)

    val averageInFrameLikelihood: Float
        get() = landmarkPoints.fold(0f) { acc, it -> acc + it.inFrameLikelihood } / landmarkPoints.size

    fun hasPointsBelowThreshold(threshold: Float): Boolean
        = landmarkPoints.any { it.inFrameLikelihood < threshold }

    fun getLandmarkPoint(type: Int): LandmarkPoint
        = landmarkPoints.find { it.point.type == type } ?: error("Point not found in Pose")

    fun getAnglePoints(angleTag: String): Triple<LandmarkPoint, LandmarkPoint, LandmarkPoint> {
        val angle = configuration.angles.find { it.tag == angleTag }
        if(angle != null) {
            val startPoint = getPoint(angle.startPointType)
            val middlePoint = getPoint(angle.middlePointType)
            val endPoint = getPoint(angle.endPointType)
            return Triple(startPoint, middlePoint, endPoint)
        } else {
            error("Angle with tag: $angleTag not found!")
        }
    }

    fun getAnglePointFs(angleTag: String): Triple<PointF, PointF, PointF> {
        val (start, middle, end) = getAnglePoints(angleTag)
        return Triple(start.position, middle.position, end.position)
    }

    fun getAngle(start: LandmarkPoint, middle: LandmarkPoint, end: LandmarkPoint): Double
        = AngleUtil.getAngle(start, middle, end)

    fun getAngle(points: Triple<LandmarkPoint, LandmarkPoint, LandmarkPoint>): Double
            = AngleUtil.getAngle(points.first, points.second, points.third)

    fun getAngle(angleTag: String): Double = getAngle(getAnglePoints(angleTag))

    fun getClosestPoint(x: Float, y: Float): LandmarkPoint? = landmarkPoints.minByOrNull {
        kotlin.math.hypot(
            (x - it.position.x).toDouble(),
            (y - it.position.y).toDouble()
        )
    }

    fun updatePoint(type: Int, moveX: Float, moveY: Float) {
        val point = landmarkPoints.find { it.point.type == type } ?: error("Unable to find point: $type in Pose!")
        point.position.x = point.position.x + moveX
        point.position.y = point.position.y + moveY
    }

    fun getPoint(type: Int): LandmarkPoint = landmarkPoints.firstOrNull { it.point.type == type } ?: error("Unable to find point: $type in Pose!")

    fun copy(): GeckoPose {
        return GeckoPose(this.configuration.copy()).also {
            it.landmarkPoints.addAll(this.landmarkPoints.map { lp -> lp.copy() })
        }
    }
    fun copyScale(scaleX: Float, scaleY: Float): GeckoPose {
        return GeckoPose(this.configuration.copy()).also {
            it.landmarkPoints.addAll(this.landmarkPoints.map { lp -> lp.copyScale(scaleX, scaleY) })
        }
    }

    fun copyMove(cropX: Int, cropY: Int): GeckoPose {
        return GeckoPose(this.configuration.copy()).also {
            it.landmarkPoints.addAll(this.landmarkPoints.map { lp -> lp.copyMove(cropX, cropY) })
        }
    }
}

fun List<GeckoPose?>.getBest(threshold: Float): GeckoPose? =
    this.filterNotNull()
        .filter { !it.missesPoints }
        .filter { !it.hasPointsBelowThreshold(threshold) }
        .maxByOrNull { it.averageInFrameLikelihood }

fun List<GeckoPose?>.getByTag(poseTag: String): GeckoPose? =
    this.find { it?.configuration?.tag == poseTag }


@Serializable
class LandmarkPoint(
    val position: PointF,
    val point: Point,
    val inFrameLikelihood: Float
) {
    fun copy() = LandmarkPoint(
        position = PointF(this.position.x, this.position.y),
        point = this.point.copy(),
        inFrameLikelihood = this.inFrameLikelihood

    )

    fun copyScale(scaleX: Float, scaleY: Float) = LandmarkPoint(
        position = PointF(this.position.x * scaleX, this.position.y * scaleY),
        point = this.point.copy(),
        inFrameLikelihood = this.inFrameLikelihood
    )

    fun copyMove(cropX: Int, cropY: Int) = LandmarkPoint(
        position = PointF(this.position.x + cropX, this.position.y + cropY),
        point = this.point.copy(),
        inFrameLikelihood = this.inFrameLikelihood
    )

    fun distanceTo(second: LandmarkPoint): Double = Math.hypot(
        (second.position.x-this.position.x).toDouble(),
        (second.position.y-this.position.y).toDouble()
    )
}

@Serializable
class Point(
    val type: Int,
    @ColorRes val color: Int? = null,
    @ColorRes val selectedColor: Int? = null
) {
    fun toProcessedPoint(poseLandmark: PoseLandmark) = LandmarkPoint(
        position = PointF(poseLandmark.position.x, poseLandmark.position.y),
        point = this,
        inFrameLikelihood = poseLandmark.inFrameLikelihood
    )

    fun copy() = Point(
        type = this.type,
        color = this.color,
        selectedColor = this.selectedColor
    )
}

@Serializable
class Line(
    val start: Int,
    val end: Int,
    val tag: String,
    @ColorRes val color: Int? = null
) {
    fun copy() = Line(
        start = this.start,
        end = this.end,
        tag = this.tag,
        color = this.color
    )
}

@Serializable
open class Angle(
    val startPointType: Int,
    val middlePointType: Int,
    val endPointType: Int,
    val tag: String,
    @ColorRes val color: Int? = null
) {
    open fun copy() = Angle(
        startPointType = this.startPointType,
        middlePointType = this.middlePointType,
        endPointType = this.endPointType,
        tag = this.tag,
        color = this.color
    )
}

class MinMaxAngle(startPointType: Int,
                  middlePointType: Int,
                  endPointType: Int,
                  tag: String,
                  color: Int? = null,
                  val minAngle: Float,
                  val maxAngle: Float,
                  @ColorRes val errorColor: Int? = null,
): Angle(startPointType, middlePointType, endPointType, tag, color) {

    override fun copy() = MinMaxAngle(
        startPointType = this.startPointType,
        middlePointType = this.middlePointType,
        endPointType = this.endPointType,
        tag = this.tag,
        color = this.color,
        minAngle = this.minAngle,
        maxAngle = this.maxAngle,
        errorColor = this.errorColor
    )

    fun isAngleNotInside(angle: Double): Boolean = !isAngleInside(angle)
    fun isAngleInside(angle: Double): Boolean = angle in minAngle..maxAngle
}

@Serializable
data class PointF(var x: Float, var y: Float)


