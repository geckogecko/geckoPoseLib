package at.steinbacher.geckoposelib.data

import at.steinbacher.geckoposelib.util.AngleUtil
import com.google.mlkit.vision.pose.PoseLandmark
import kotlinx.serialization.Serializable
import kotlin.math.abs

@Serializable
class GeckoPoseConfiguration(
    val tag: String,
    val pointConfigurations: List<PointConfiguration> = listOf(),
    val lineConfigurations: List<LineConfiguration> = listOf(),
    val angleConfigurations: List<AngleConfiguration> = listOf(),
    val poseCenterPointsTargets: List<Int> = listOf() //targets point to base the center point calc on
) {
    fun copy() = GeckoPoseConfiguration(
        tag = this.tag,
        pointConfigurations = this.pointConfigurations.map { it.copy() },
        lineConfigurations = this.lineConfigurations.map { it.copy() },
        angleConfigurations = this.angleConfigurations.map { it.copy() },
        poseCenterPointsTargets = this.poseCenterPointsTargets
    )
}

@Serializable
class GeckoPose(
    override val configuration: GeckoPoseConfiguration,
    override val points: List<Point>,
    override val width: Int,
    override val height: Int,
    override val tag: String? = null,
): IGeckoPose {
    val foundPointTypes: List<Int>
        = points.map { it.pointConfiguration.type }

    val missesPoints: Boolean
        = configuration.pointConfigurations.map { it.type }.containsAll(foundPointTypes)

    val averageInFrameLikelihood: Float
        = points.fold(0f) { acc, it -> acc + it.inFrameLikelihood } / points.size

    val poseCenterPoint: PointF = calculateCenterPoint(
            if(configuration.poseCenterPointsTargets.isNotEmpty()) {
                configuration.poseCenterPointsTargets.map { getPoint(it) }
            } else {
                points
            }
        )

    val angles: List<Angle> = configuration.angleConfigurations.map { calculateAngle(it) }

    fun hasPointsBelowThreshold(threshold: Float): Boolean
        = points.any { it.inFrameLikelihood < threshold }

    override fun getPoint(type: Int): Point
        = points.find { it.pointConfiguration.type == type } ?: error("Point not found in Pose")

    fun getAngle(angleTag: String): Angle
        = angles.find { it.tag == angleTag } ?: error("AngleTag: $angleTag not found!")

    override fun getAnglePositions(angleTag: String): Triple<PointF, PointF, PointF> {
        val angle = getAngle(angleTag)
        val startPosition = getPoint(angle.startPointType)
        val middlePosition = getPoint(angle.middlePointType)
        val endPosition = getPoint(angle.endPointType)
        return Triple(startPosition.position, middlePosition.position, endPosition.position)
    }

    fun getClosestPoint(x: Float, y: Float): Point? = points.minByOrNull {
        kotlin.math.hypot(
            (x - it.position.x).toDouble(),
            (y - it.position.y).toDouble()
        )
    }

    fun updatePoint(type: Int, moveX: Float, moveY: Float) {
        val point = points.find { it.pointConfiguration.type == type } ?: error("Unable to find point: $type in Pose!")
        point.position.x = point.position.x + moveX
        point.position.y = point.position.y + moveY
    }

    fun copy(): GeckoPose {
        return GeckoPose(
            configuration = this.configuration.copy(),
            points = this.points.map { lp -> lp.copy() },
            width = this.width,
            height = this.height,
            tag = this.tag
        )
    }
    override fun copyScale(scaleX: Float, scaleY: Float): GeckoPose {
        return GeckoPose(
            configuration = this.configuration.copy(),
            points = this.points.map { lp -> lp.copyScale(scaleX, scaleY) },
            width = this.width,
            height = this.height,
            tag = this.tag
        )
    }

    override fun copyMove(moveX: Int, moveY: Int): GeckoPose {
        return GeckoPose(
            configuration = this.configuration.copy(),
            points = this.points.map { lp -> lp.copyMove(moveX, moveY) },
            width = this.width,
            height = this.height,
            tag = this.tag
        )
    }

    fun getNormalizedPose(): NormalizedGeckoPose {
        val minX = points.minByOrNull { it.position.x }?.position?.x ?: error("minX is null!")
        val maxX = points.maxByOrNull { it.position.x }?.position?.x ?: error("maxX is null!")
        val minY = points.minByOrNull { it.position.y }?.position?.y ?: error("minY is null!")
        val maxY = points.maxByOrNull { it.position.y }?.position?.y ?: error("maxY is null!")

        val maxHalfWidth = listOf(poseCenterPoint.x - minX, maxX - poseCenterPoint.x).maxOrNull() ?: error("maxHalfWidth is null!")
        val maxHalfHeight = listOf(poseCenterPoint.y - minY, maxY - poseCenterPoint.y).maxOrNull() ?: error("maxHalfHeight is null!")

        val newSideLengthHalf: Float = listOf(maxHalfWidth, maxHalfHeight).maxOrNull() ?: error("newSideLengthHalf is null")
        val newSideLength = newSideLengthHalf * 2

        //we move all points by halfSquareSideLength to be sure they are all in bounds of the new squared boundary box
        val movedPoints = points.map { it.copyMove(newSideLengthHalf.toInt(), newSideLengthHalf.toInt()) }
        val movedPoseCenter = PointF(poseCenterPoint.x + newSideLengthHalf, poseCenterPoint.y + newSideLengthHalf)

        val topLeft = PointF(movedPoseCenter.x - newSideLengthHalf, movedPoseCenter.y - newSideLengthHalf)

        val normalizedPoints = movedPoints.map {
            val topLeftPointDistanceX = it.position.x - topLeft.x
            val topLeftPointDistanceY = it.position.y - topLeft.y

            val normalizedX = ((topLeftPointDistanceX / newSideLength) * 100)
            val normalizedY = ((topLeftPointDistanceY / newSideLength) * 100)

            val normalizedPosition = PointF(
                x = if(normalizedX > 0) normalizedX else 0f,
                y = if(normalizedY > 0) normalizedY else 0f
            )

            it.copy(newPosition = normalizedPosition)
        }

        return NormalizedGeckoPose(
            points = normalizedPoints,
            angles = angles.map { it.copy() },
            configuration = this.configuration.copy(),
            tag = this.tag
        )
    }

    private fun calculateCenterPoint(points: List<Point>): PointF  {
        val minX = points.minByOrNull { it.position.x }?.position?.x ?: error("minX is null!")
        val maxX = points.maxByOrNull { it.position.x }?.position?.x ?: error("maxX is null!")
        val minY = points.minByOrNull { it.position.y }?.position?.y ?: error("minY is null!")
        val maxY = points.maxByOrNull { it.position.y }?.position?.y ?: error("maxY is null!")

        return PointF(x = (maxX + minX)/2, y = (maxY + minY)/2)
    }

    private fun calculateAngle(angleConfiguration: AngleConfiguration): Angle {
        return Angle(
            tag = angleConfiguration.tag,
            startPointType = angleConfiguration.startPointType,
            middlePointType = angleConfiguration.middlePointType,
            endPointType = angleConfiguration.endPointType,
            value = AngleUtil.getAngle(
                getPoint(angleConfiguration.startPointType),
                getPoint(angleConfiguration.middlePointType),
                getPoint(angleConfiguration.endPointType),
            )
        )
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
class Point(
    val position: PointF,
    val pointConfiguration: PointConfiguration,
    val inFrameLikelihood: Float
) {
    fun copy(newPosition: PointF = PointF(this.position.x, this.position.y)) = Point(
        position = newPosition,
        pointConfiguration = this.pointConfiguration.copy(),
        inFrameLikelihood = this.inFrameLikelihood

    )

    fun copyScale(scaleX: Float, scaleY: Float) = Point(
        position = PointF(this.position.x * scaleX, this.position.y * scaleY),
        pointConfiguration = this.pointConfiguration.copy(),
        inFrameLikelihood = this.inFrameLikelihood
    )

    fun copyMove(moveX: Int, moveY: Int) = Point(
        position = PointF(this.position.x + moveX, this.position.y + moveY),
        pointConfiguration = this.pointConfiguration.copy(),
        inFrameLikelihood = this.inFrameLikelihood
    )

    fun distanceTo(second: Point): Double =
        this.position.distanceTo(second.position)
}

@Serializable
data class Angle(
    val tag: String,
    val startPointType: Int,
    val middlePointType: Int,
    val endPointType: Int,
    val value: Double,
) {
    fun distance(to: Angle): Double {
        return distance(to.value)
    }

    //https://math.stackexchange.com/a/110236
    fun distance(toValue: Double): Double {
        val a = abs(toValue - value)
        val b = abs(toValue - value + 360)
        val c = abs(toValue - value - 360)

        return listOf(a, b, c).minOrNull()!!
    }
}

@Serializable
class PointConfiguration(
    val type: Int,
    val color: Int,
    val selectedColor: Int,
) {
    fun toProcessedPoint(poseLandmark: PoseLandmark) = Point(
        position = PointF(poseLandmark.position.x, poseLandmark.position.y),
        pointConfiguration = this,
        inFrameLikelihood = poseLandmark.inFrameLikelihood,
    )

    fun copy() = PointConfiguration(
        type = this.type,
        color = this.color,
        selectedColor = this.selectedColor,
    )
}

@Serializable
class LineConfiguration(
    val start: Int,
    val end: Int,
    val tag: String,
    val color: Int,
) {
    fun copy() = LineConfiguration(
        start = this.start,
        end = this.end,
        tag = this.tag,
        color = this.color,
    )
}

@Serializable
open class AngleConfiguration(
    val startPointType: Int,
    val middlePointType: Int,
    val endPointType: Int,
    val tag: String,
    val color: Int,
) {
    open fun copy() = AngleConfiguration(
        startPointType = this.startPointType,
        middlePointType = this.middlePointType,
        endPointType = this.endPointType,
        tag = this.tag,
        color = this.color,
    )
}

@Serializable
data class PointF(var x: Float, var y: Float) {
    fun distanceTo(second: PointF): Double = Math.hypot(
        (second.x-this.x).toDouble(),
        (second.y-this.y).toDouble()
    )
}


