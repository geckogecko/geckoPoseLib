package at.steinbacher.geckoposelib.data

import androidx.annotation.ColorRes
import at.steinbacher.geckoposelib.util.AngleUtil
import com.google.mlkit.vision.pose.PoseLandmark
import kotlinx.serialization.Serializable
import kotlin.math.abs


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
    val poseCenterPointsTargets: List<Int> = listOf() //targets point to base the center point calc on
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
class OnImagePose(
    val pose: GeckoPose,
) {
    var normalizedPose: GeckoPose = pose.copyAndNormalize()
}

@Serializable
class GeckoPose(
    val configuration: GeckoPoseConfiguration,
    val landmarkPoints: List<LandmarkPoint>,
){
    val foundPointTypes: List<Int>
        get() = landmarkPoints.map { it.point.type }

    val missesPoints: Boolean
        get() = !foundPointTypes.containsAll(foundPointTypes)

    val averageInFrameLikelihood: Float
        get() = landmarkPoints.fold(0f) { acc, it -> acc + it.inFrameLikelihood } / landmarkPoints.size

    val poseCenterPoint: PointF
        get() = calculateCenterPoint(configuration.poseCenterPointsTargets.map { getLandmarkPoint(it) })

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
        return GeckoPose(
            configuration = this.configuration.copy(),
            landmarkPoints = this.landmarkPoints.map { lp -> lp.copy() }
        )
    }
    fun copyScale(scaleX: Float, scaleY: Float): GeckoPose {
        return GeckoPose(
            configuration = this.configuration.copy(),
            landmarkPoints = this.landmarkPoints.map { lp -> lp.copyScale(scaleX, scaleY) }
        )
    }

    fun copyMove(moveX: Int, moveY: Int): GeckoPose {
        return GeckoPose(
            configuration = this.configuration.copy(),
            landmarkPoints = this.landmarkPoints.map { lp -> lp.copyMove(moveX, moveY) }
        )
    }

    fun copyAndNormalize(): GeckoPose {
        val minX: Float = landmarkPoints.minByOrNull { it.position.x }!!.position.x
        val maxX: Float = landmarkPoints.maxByOrNull { it.position.x }!!.position.x
        val minY: Float = landmarkPoints.minByOrNull { it.position.y }!!.position.y
        val maxY: Float = landmarkPoints.maxByOrNull { it.position.y }!!.position.y

        val width = maxX - minX
        val height = minY - maxY

        val squareSideLength  = when {
            width > height -> width
            height > width -> height
            else -> width
        }
        val halfSquareSideLength = squareSideLength/2

        //we move all points by halfSquareSideLength to be sure they are all in bounds of the new squared boundary box
        val movedPoints = landmarkPoints.map { it.copyMove(halfSquareSideLength.toInt(), halfSquareSideLength.toInt()) }

        val topLeft = PointF(minX, minY)

        val normalizedPoints = movedPoints.map {
            val topLeftPointDistanceX = it.position.x - topLeft.x
            val topLeftPointDistanceY = it.position.y - topLeft.y

            val normalizedPosition = PointF(
                x = ((topLeftPointDistanceX / squareSideLength) * 100),
                y = ((topLeftPointDistanceY / squareSideLength) * 100)
            )

            it.copy(newPosition = normalizedPosition)
        }

        return GeckoPose(
            configuration = this.configuration.copy(),
            landmarkPoints = normalizedPoints
        )
    }

    private fun calculateCenterPoint(landmarkPoints: List<LandmarkPoint>): PointF  {
        val minX = landmarkPoints.minByOrNull { it.position.x }!!.position.x
        val maxX = landmarkPoints.maxByOrNull { it.position.x }!!.position.x
        val minY = landmarkPoints.minByOrNull { it.position.y }!!.position.y
        val maxY = landmarkPoints.maxByOrNull { it.position.y }!!.position.y

        return PointF(x = maxX - minX, y = maxY - minY)
    }
}

fun List<OnImagePose?>.getBest(threshold: Float): OnImagePose? =
    this.filterNotNull()
        .filter { !it.pose.missesPoints }
        .filter { !it.pose.hasPointsBelowThreshold(threshold) }
        .maxByOrNull { it.pose.averageInFrameLikelihood }

fun List<OnImagePose?>.getByTag(poseTag: String): OnImagePose? =
    this.find { it?.pose?.configuration?.tag == poseTag }


@Serializable
class LandmarkPoint(
    val position: PointF,
    val point: Point,
    val inFrameLikelihood: Float
) {
    fun copy(newPosition: PointF = PointF(this.position.x, this.position.y)) = LandmarkPoint(
        position = newPosition,
        point = this.point.copy(),
        inFrameLikelihood = this.inFrameLikelihood

    )

    fun copyScale(scaleX: Float, scaleY: Float) = LandmarkPoint(
        position = PointF(this.position.x * scaleX, this.position.y * scaleY),
        point = this.point.copy(),
        inFrameLikelihood = this.inFrameLikelihood
    )

    fun copyMove(moveX: Int, moveY: Int) = LandmarkPoint(
        position = PointF(this.position.x + moveX, this.position.y + moveY),
        point = this.point.copy(),
        inFrameLikelihood = this.inFrameLikelihood
    )

    fun distanceTo(second: LandmarkPoint): Double =
        this.position.distanceTo(second.position)
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

@Serializable
data class PointF(var x: Float, var y: Float) {
    fun distanceTo(second: PointF): Double = Math.hypot(
        (second.x-this.x).toDouble(),
        (second.y-this.y).toDouble()
    )
}


