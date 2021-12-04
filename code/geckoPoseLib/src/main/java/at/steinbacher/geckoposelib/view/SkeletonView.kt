package at.steinbacher.geckoposelib.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import at.steinbacher.geckoposelib.*
import at.steinbacher.geckoposelib.data.*
import at.steinbacher.geckoposelib.util.AngleUtil
import at.steinbacher.geckoposelib.util.BitmapUtil
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt

class SkeletonView @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr) {

    var pose: GeckoPose? = null
        set(value) {
            field = value
            invalidate()
        }

    var poseDrawConfiguration: GeckoPoseDrawConfiguration? = null
        set(value) {
            field = value
            invalidate()
        }

    var bitmap: Bitmap? = null

    var drawLines: Boolean = true
        set(value) {
            field = value
            invalidate()
        }

    var drawAngles: Boolean = true
        set(value) {
            field = value
            invalidate()
        }

    private var selectedPointType: Int? = null

    private var pointPaint = Paint().apply {
        color = Color.GRAY
    }

    private var selectedPointPaint = Paint().apply {
        color = Color.RED
    }

    private var linePaint = Paint().apply {
        strokeWidth = 8.0f
    }

    private var lastMoveX: Float = -1f
    private var lastMoveY: Float = -1f

    private var touchDownCounter = 0

    init {
        setWillNotDraw(false)
    }

    private var skeletonViewListener: SkeletonViewListener? = null
    interface SkeletonViewListener {
        fun onPointSelected()
        fun onPointChanged(type: Int)
    }

    fun setSkeletonViewListener(listener: SkeletonViewListener) {
        skeletonViewListener = listener
        invalidate()
    }

    fun saveSelectedPoint() {
        selectedPointType = null
        lastMoveX = -1f
        lastMoveY = -1f
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                if(event.isInsidePreview() && isClickable) {
                    touchDownCounter++

                    if (selectedPointType == null) {
                        skeletonViewListener?.onPointSelected()
                        selectedPointType = pose?.getClosestPoint(event.x, event.y)?.point?.type

                        selectedPointType?.let { skeletonViewListener?.onPointSelected() }
                        invalidate()
                    }
                    true
                } else {
                    false
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if(event.isInsidePreview()) {
                    if (selectedPointType != null && lastMoveX != -1f && lastMoveY != -1f) {
                        val moveX = event.x - lastMoveX
                        val moveY = event.y - lastMoveY

                        pose?.updatePoint(selectedPointType!!, moveX, moveY)
                        invalidate()

                        selectedPointType?.let { skeletonViewListener?.onPointChanged(it) }
                    }

                    lastMoveX = event.x
                    lastMoveY = event.y
                    true
                } else {
                    false
                }
            }
            MotionEvent.ACTION_UP -> {
                if(touchDownCounter > 1) {
                    saveSelectedPoint()
                    touchDownCounter = 0
                } else {
                    lastMoveX = -1f
                    lastMoveY = -1f
                }

                true
            }

            else -> {
                super.onTouchEvent(event)
            }
        }
    }

    private fun MotionEvent.isInsidePreview(): Boolean {
        return when {
            this.x < 0 -> false
            this.x > width -> false
            this.y < 0 -> false
            this.y > height -> false
            else -> true
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val currentPose = pose
        val currentDrawConfiguration = poseDrawConfiguration

       if(currentPose != null && currentDrawConfiguration != null) {
            if(drawAngles) {
                currentPose.configuration.angles.forEach { angle ->
                    canvas.drawAngleIndicator(angle, currentPose, currentDrawConfiguration)
                }
            }

            if(drawLines) {
                currentPose.configuration.lines.forEach { line ->
                    val start = currentPose.getLandmarkPoint(line.start)
                    val end = currentPose.getLandmarkPoint(line.end)

                    linePaint.color = ContextCompat.getColor(context, line.color ?: currentDrawConfiguration.defaultLineColor)

                    canvas.drawLine(start.position.x, start.position.y, end.position.x, end.position.y, linePaint)
                }
            }

           currentPose.landmarkPoints.forEach { processedPoint ->
                if(processedPoint.point.type == selectedPointType) {
                    selectedPointPaint.color = ContextCompat.getColor(context,
                        processedPoint.point.selectedColor ?: currentDrawConfiguration.defaultSelectedPointColor)
                    canvas.drawCircle(processedPoint.position.x, processedPoint.position.y, 15f, selectedPointPaint)
                } else {
                    pointPaint.color = ContextCompat.getColor(context,
                        if(processedPoint.point.color != null) {
                            processedPoint.point.color
                        } else {
                            if(bitmap != null) {
                                BitmapUtil.getContrastColor(
                                    bitmap = bitmap!!,
                                    x = processedPoint.position.x.toInt(),
                                    y = processedPoint.position.y.toInt(),
                                    contrastColorLight = currentDrawConfiguration.defaultPointColorLight,
                                    contrastColorDark = currentDrawConfiguration.defaultPointColorDark,
                                )
                            } else {
                                currentDrawConfiguration.defaultPointColorLight
                            }
                        }
                    )
                    canvas.drawCircle(processedPoint.position.x, processedPoint.position.y, 10f, pointPaint)
                }
            }
        }
    }

    private fun Canvas.drawAngleIndicator(angle: Angle, pose: GeckoPose, poseDrawConfiguration: GeckoPoseDrawConfiguration) {
        val (startPoint, middlePoint, endPoint) = pose.getAnglePointFs(angle.tag)

        val distanceMiddleStart = getDistanceBetweenPoints(middlePoint, startPoint)
        val distanceMiddleEnd = getDistanceBetweenPoints(middlePoint, endPoint)
        val biggestDistance = listOf(distanceMiddleStart, distanceMiddleEnd).minOrNull() ?: distanceMiddleStart

        val minAngleDistance = resources.getDimensionPixelSize(R.dimen.minAngleDistance)
        val angleDistance: Int  = if(biggestDistance * 0.35 < minAngleDistance) minAngleDistance else (biggestDistance * 0.35).toInt()

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

        val colorRes = angle.color ?: poseDrawConfiguration.defaultAngleColor
        val color = ContextCompat.getColor(context, colorRes)

        val anglePaint = Paint().apply {
            this.color = color
            strokeWidth = 8.0f
            style = Paint.Style.FILL
        }

        this.drawArc(
            (middlePoint.x - angleDistance),
            (middlePoint.y - angleDistance),
            (middlePoint.x + angleDistance),
            (middlePoint.y + angleDistance),
            startAngle.toFloat(),
            angleDegrees.toFloat(),
            true,
            anglePaint
        )
    }

    private fun getDistanceBetweenPoints(startPoint: PointF, endPoint: PointF)
            = sqrt((endPoint.x - startPoint.x).pow(2) + (endPoint.y - startPoint.y).pow(2))

}