package at.steinbacher.geckoposelib

import android.graphics.PointF
import androidx.annotation.ColorRes
import at.steinbacher.geckoposelib.util.AngleUtil
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.collections.ArrayList

data class GeckoPoseConfiguration(
    val tag: String,
    val points: List<Point> = listOf(),
    val lines: List<Line> = listOf(),
    val angles: List<Angle> = listOf(),
    @ColorRes val defaultPointColor: Int,
    @ColorRes val defaultSelectedPointColor: Int,
    @ColorRes val defaultLineColor: Int,
    @ColorRes val defaultAngleColor: Int,
    @ColorRes val defaultNOKAngleColor: Int,
) {
    companion object {
        fun GeckoPoseConfiguration.copy() = GeckoPoseConfiguration(
            tag = this.tag,
            points = this.points.toMutableList(),
            lines = this.lines.toMutableList(),
            angles = this.angles.toMutableList(),
            defaultPointColor = this.defaultPointColor,
            defaultSelectedPointColor = this.defaultSelectedPointColor,
            defaultLineColor = this.defaultLineColor,
            defaultAngleColor = this.defaultAngleColor,
            defaultNOKAngleColor = this.defaultNOKAngleColor
        )
    }
}

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

    companion object {
        fun GeckoPose.copy(): GeckoPose {
            return GeckoPose(this.configuration.copy()).also {
                it.landmarkPoints.addAll(this.landmarkPoints.toMutableList())
            }
        }
    }

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
        point.position.set(point.position.x + moveX, point.position.y + moveY)
    }

    fun getPoint(type: Int): LandmarkPoint = landmarkPoints.firstOrNull { it.point.type == type } ?: error("Unable to find point: $type in Pose!")
}

fun List<GeckoPose>.getBest(threshold: Float): GeckoPose? =
    this.filter { !it.missesPoints }
        .filter { !it.hasPointsBelowThreshold(threshold) }
        .maxByOrNull { it.averageInFrameLikelihood }

fun List<GeckoPose>.getByTag(poseTag: String): GeckoPose? =
    this.find { it.configuration.tag == poseTag }


data class LandmarkPoint(
    val position: PointF,
    val point: Point,
    val inFrameLikelihood: Float
) {
    companion object {
        fun Point.toProcessedPoint(poseLandmark: PoseLandmark) = LandmarkPoint(
            position = poseLandmark.position,
            point = this,
            inFrameLikelihood = poseLandmark.inFrameLikelihood
        )
    }
}

data class Point(
    val type: Int,
    @ColorRes val color: Int? = null,
    @ColorRes val selectedColor: Int? = null
)

data class Line(
    val start: Int,
    val end: Int,
    val tag: String,
    @ColorRes val color: Int? = null
)

open class Angle(
    val startPointType: Int,
    val middlePointType: Int,
    val endPointType: Int,
    val tag: String,
    @ColorRes val color: Int? = null
)

class MinMaxAngle(startPointType: Int,
                  middlePointType: Int,
                  endPointType: Int,
                  tag: String,
                  color: Int? = null,
                  val minAngle: Float,
                  val maxAngle: Float,
                  @ColorRes val errorColor: Int? = null,
): Angle(startPointType, middlePointType, endPointType, tag, color) {

    fun isAngleNotInside(angle: Double): Boolean = !isAngleInside(angle)
    fun isAngleInside(angle: Double): Boolean = angle in minAngle..maxAngle
}


