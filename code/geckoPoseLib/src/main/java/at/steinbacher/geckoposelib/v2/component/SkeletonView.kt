package at.steinbacher.geckoposelib.v2.component

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import at.steinbacher.geckoposelib.data.AngleConfiguration
import at.steinbacher.geckoposelib.data.GeckoPose
import at.steinbacher.geckoposelib.data.NormalizedGeckoPose
import at.steinbacher.geckoposelib.data.PointF
import at.steinbacher.geckoposelib.util.AngleUtil
import at.steinbacher.geckoposelib.view.SkeletonView
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt


@Composable
fun SkeletonView(
    geckoPose: GeckoPose,
    srcImageBitmap: ImageBitmap,
    lineColor: Color,
    pointColor: Color,
    selectedPointColor: Color,
    contentScale: ContentScale = ContentScale.Fit,
    alignment: Alignment = Alignment.Center,
    drawAngles: Boolean = true,
    drawLines: Boolean = true,
    selectedPointType: Int? = null,
    modifier: Modifier
) {
    Canvas(modifier = modifier) {
        val scaleFactor = contentScale.computeScaleFactor(
            srcSize = Size(srcImageBitmap.width.toFloat(), srcImageBitmap.height.toFloat()),
            dstSize = Size(size.width, size.height)
        )

        val scaledGeckoPose = geckoPose.copyScale(scaleX = scaleFactor.scaleX, scaleY = scaleFactor.scaleY)

        val finalSize = IntSize((srcImageBitmap.width * scaleFactor.scaleX).roundToInt(), (srcImageBitmap.height * scaleFactor.scaleY).roundToInt())
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
                //drawAngleIndicator(angle, geckoPose)
            }
        }

        if(drawLines) {
            scaledAndMovedGeckoPose.configuration.lineConfigurations.forEach { line ->
                val start = scaledAndMovedGeckoPose.getPoint(line.start)
                val end = scaledAndMovedGeckoPose.getPoint(line.end)

                drawLine(
                    color = lineColor,
                    start = Offset(start.position.x, start.position.y),
                    end = Offset(end.position.x, end.position.y),
                    strokeWidth = 8f
                )
            }
        }

        scaledAndMovedGeckoPose.points.forEach { processedPoint ->
            if(processedPoint.pointConfiguration.type == selectedPointType) {
                drawCircle(
                    color = selectedPointColor,
                    radius = 15f,
                    center = Offset(processedPoint.position.x, processedPoint.position.y)
                )
            } else {
                drawCircle(
                    color = pointColor,
                    radius = 15f,
                    center = Offset(processedPoint.position.x, processedPoint.position.y)
                )
            }
        }
    }
}


private fun DrawScope.drawAngleIndicator(
    angleConfiguration: AngleConfiguration,
    pose: GeckoPose,
    context: Context,
    minAngleDistance: Dp = 10.dp
) {
    val (startPoint, middlePoint, endPoint) = pose.getAnglePositions(angleConfiguration.tag)

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

    /*

val color = if(angleConfiguration.color != null) {
    ContextCompat.getColor(context, angleConfiguration.color)
} else {
    Color.RED
}

val anglePaint = Paint().apply {
    this.color = color
    strokeWidth = 8.0f
    style = Paint.Style.FILL
}


drawArc(
    (middlePoint.x - angleDistance),
    (middlePoint.y - angleDistance),
    (middlePoint.x + angleDistance),
    (middlePoint.y + angleDistance),
    startAngle.toFloat(),
    angleDegrees.toFloat(),
    true,
    anglePaint
)
 */
}

private fun getDistanceBetweenPoints(startPoint: PointF, endPoint: PointF)
        = sqrt((endPoint.x - startPoint.x).pow(2) + (endPoint.y - startPoint.y).pow(2))