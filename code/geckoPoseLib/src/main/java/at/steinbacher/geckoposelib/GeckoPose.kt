package at.steinbacher.geckoposelib

import android.graphics.Paint
import android.graphics.PointF
import com.google.mlkit.vision.pose.PoseLandmark
import java.lang.Math.hypot
import kotlin.collections.ArrayList

data class GeckoPoseConfiguration(
    val pointTypes: List<Int> = listOf(),
    val lines: List<Line> = listOf(),
    val angles: List<Angle> = listOf()
)

class GeckoPose(
    val configuration: GeckoPoseConfiguration
){
    val points: ArrayList<Point> = arrayListOf()

    val foundPointTypes: List<Int>
        get() = points.map { it.type }

    val missesPoints: Boolean
        get() = !foundPointTypes.containsAll(foundPointTypes)

    val averageInFrameLikelihood: Float
        get() = points.fold(0f) { acc, it -> acc + it.inFrameLikelihood } / points.size

    fun hasPointsBelowThreshold(threshold: Float): Boolean
        = points.any { it.inFrameLikelihood < threshold }

    fun getPose(type: Int): Point
        = points.find { it.type == type } ?: error("Point not found in Pose")

    fun getPointsFromLine(lineTag: String): Pair<Point, Point> {
        val line = configuration.lines.find { it.tag == lineTag }
        if(line != null) {
            return Pair(getPose(line.start), getPose(line.end))
        } else {
            error("Line with tag: $lineTag not found!")
        }
    }

    fun getAnglePoints(angleTag: String): Triple<Point, Point, Point> {
        val angle = configuration.angles.find { it.tag == angleTag }
        if(angle != null) {
            val (line1Start, line1End) = getPointsFromLine(angle.line1Tag)
            val (line2Start, line2End) = getPointsFromLine(angle.line2Tag)

            return when {
                line1Start == line2Start -> Triple(line1End, line1Start, line2End)
                line2Start == line1End -> Triple(line1Start, line2Start, line2End)
                line1Start == line2End -> Triple(line1End, line1Start, line2Start)
                line1Start == line2End -> Triple(line1End, line1Start, line2Start)
                else -> error("Error in finding angle points!")
            }
        } else {
            error("Angle with tag: $angleTag not found!")
        }
    }

    fun getAngle(start: Point, middle: Point, end: Point): Double
        = Util.getAngle(start, middle, end)

    fun getAngle(points: Triple<Point, Point, Point>): Double
            = Util.getAngle(points.first, points.second, points.third)

    fun getAngle(angleTag: String): Double = getAngle(getAnglePoints(angleTag))

    fun getClosestPoint(x: Float, y: Float): Point? = points.minByOrNull {
        kotlin.math.hypot(
            (x - it.position.x).toDouble(),
            (y - it.position.y).toDouble()
        )
    }

    fun updatePoint(type: Int, moveX: Float, moveY: Float) {
        val point = points.find { it.type == type } ?: error("Unable to find point: $type in Pose!")
        point.position.set(point.position.x + moveX, point.position.y + moveY)
    }
}

fun List<GeckoPose>.getBest(threshold: Float): GeckoPose? =
    this.filter { !it.missesPoints }
        .filter { !it.hasPointsBelowThreshold(threshold) }
        .maxByOrNull { it.averageInFrameLikelihood }


data class Point(
    val position: PointF,
    val type: Int,
    val inFrameLikelihood: Float
) {
    companion object {
        fun PoseLandmark.toPoint() = Point(
            position = this.position,
            type = this.landmarkType,
            inFrameLikelihood = this.inFrameLikelihood
        )
    }
}

data class Line(
    val start: Int,
    val end: Int,
    val tag: String,
    val color: Int
)

data class Angle(
    val line1Tag: String,
    val line2Tag: String,
    val tag: String,
    val color: Int
)


