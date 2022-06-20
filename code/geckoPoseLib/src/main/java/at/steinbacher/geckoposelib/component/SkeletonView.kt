package at.steinbacher.geckoposelib.component

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import at.steinbacher.geckoposelib.data.AngleConfiguration
import at.steinbacher.geckoposelib.data.GeckoPose
import at.steinbacher.geckoposelib.data.IGeckoPose
import at.steinbacher.geckoposelib.data.PointF
import at.steinbacher.geckoposelib.util.AngleUtil
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt


@Composable
fun SkeletonView(
    geckoPose: IGeckoPose,
    contentScale: ContentScale = ContentScale.Fit,
    alignment: Alignment = Alignment.Center,
    drawAngles: Boolean = true,
    drawLines: Boolean = true,
    highlightedAngle: String? = null,
    highlightAngleColor: Color = Color.Red,
    modifier: Modifier
) {
    Canvas(modifier = modifier) {
        val scaleFactor = contentScale.computeScaleFactor(
            srcSize = Size(geckoPose.width.toFloat(), geckoPose.height.toFloat()),
            dstSize = Size(size.width, size.height)
        )

        val scaledGeckoPose = geckoPose.copyScale(scaleX = scaleFactor.scaleX, scaleY = scaleFactor.scaleY)

        val finalSize = IntSize((geckoPose.width * scaleFactor.scaleX).roundToInt(), (geckoPose.height * scaleFactor.scaleY).roundToInt())
        val alignedPosition = alignment.align(
            finalSize,
            IntSize(size.width.roundToInt(), size.height.roundToInt()),
            layoutDirection
        )

        val scaledAndMovedGeckoPose = scaledGeckoPose.copyMove(
            moveX = alignedPosition.x,
            moveY = alignedPosition.y,
        )

        if(drawAngles) {
            scaledAndMovedGeckoPose.configuration.angleConfigurations.forEach { angle ->
                drawAngleIndicator(
                    angleTag = angle.tag,
                    color = when {
                        highlightedAngle != null && angle.tag == highlightedAngle -> highlightAngleColor
                        highlightedAngle != null && angle.tag != highlightedAngle -> Color(angle.color).copy(alpha = 0.5f)
                        else -> Color(angle.color)
                    },
                    pose = scaledAndMovedGeckoPose
                )
            }
        }

        if(drawLines) {
            scaledAndMovedGeckoPose.configuration.lineConfigurations.forEach { line ->
                val start = scaledAndMovedGeckoPose.getPoint(line.start)
                val end = scaledAndMovedGeckoPose.getPoint(line.end)

                drawLine(
                    color = Color(line.color),
                    start = Offset(start.position.x, start.position.y),
                    end = Offset(end.position.x, end.position.y),
                    strokeWidth = 8f,
                )
            }
        }

        scaledAndMovedGeckoPose.points.forEach { processedPoint ->
            drawCircle(
                color = Color(processedPoint.pointConfiguration.color),
                radius = 15f,
                center = Offset(processedPoint.position.x, processedPoint.position.y),
            )
        }
    }
}


private fun DrawScope.drawAngleIndicator(
    angleTag: String,
    color: Color,
    pose: IGeckoPose,
    minAngleDistance: Dp = 10.dp
) {
    val (startPoint, middlePoint, endPoint) = pose.getAnglePositions(angleTag)

    val distanceMiddleStart = getDistanceBetweenPoints(middlePoint, startPoint)
    val distanceMiddleEnd = getDistanceBetweenPoints(middlePoint, endPoint)
    val biggestDistance = listOf(distanceMiddleStart, distanceMiddleEnd).minOrNull() ?: distanceMiddleStart

    val angleDistance: Int  = if(biggestDistance * 0.35 < minAngleDistance.toPx()) minAngleDistance.toPx().toInt() else (biggestDistance * 0.35).toInt()

    val angleDegrees = AngleUtil.getAngle(startPoint, middlePoint, endPoint)

    val x2 = startPoint.x - middlePoint.x
    val y2 = startPoint.y - middlePoint.y
    val d1 = sqrt(middlePoint.y.pow(2)).toDouble()
    val d2 = sqrt(x2.pow(2)+y2.pow(2)).toDouble()
    val startAngle = if(startPoint.x >= middlePoint.x) {
        Math.toDegrees(acos((-middlePoint.y * y2) / (d1*d2)))
    } else {
        360 - Math.toDegrees(acos((-middlePoint.y * y2) / (d1*d2)))
    } + 270

    drawArc(
        color = color,
        startAngle = startAngle.toFloat(),
        sweepAngle = angleDegrees.toFloat(),
        useCenter = true,
        topLeft = Offset(
            x = (middlePoint.x - angleDistance),
            y = (middlePoint.y - angleDistance),
        ),
        size = Size(
            width = (2 * angleDistance).toFloat(),
            height = (2 * angleDistance).toFloat()
        ),
    )
}

private fun getDistanceBetweenPoints(startPoint: PointF, endPoint: PointF)
        = sqrt((endPoint.x - startPoint.x).pow(2) + (endPoint.y - startPoint.y).pow(2))