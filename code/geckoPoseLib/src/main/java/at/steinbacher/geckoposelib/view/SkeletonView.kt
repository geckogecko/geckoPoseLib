package at.steinbacher.geckoposelib.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import at.steinbacher.geckoposelib.*
import at.steinbacher.geckoposelib.data.*
import at.steinbacher.geckoposelib.util.AngleUtil
import at.steinbacher.geckoposelib.util.BitmapUtil
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt

class SkeletonView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr) {
    var pose: GeckoPose? = null
        set(value) {
            field = value
            invalidate()
        }

    @ColorInt var defaultPointColorLight: Int
    @ColorInt var defaultPointColorDark: Int
    @ColorInt var defaultSelectedPointColor: Int
    @ColorInt var defaultLineColor: Int
    @ColorInt var defaultAngleColor: Int

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

    private var pointPaint = Paint()
    private var selectedPointPaint = Paint()

    private var linePaint = Paint().apply {
        strokeWidth = 8.0f
    }

    private var lastMoveX: Float = -1f
    private var lastMoveY: Float = -1f

    private var touchDownCounter = 0

    init {
        setWillNotDraw(false)

        context.theme.obtainStyledAttributes(attrs, R.styleable.GeckoPoseView, 0, 0).apply {
            try {
                drawLines = getBoolean(R.styleable.GeckoPoseView_drawLines, true)
                drawAngles = getBoolean(R.styleable.GeckoPoseView_drawAngles, true)

                defaultPointColorLight = getColor(R.styleable.GeckoPoseView_defaultPointColorLight, ContextCompat.getColor(context, R.color.white))
                defaultPointColorDark = getColor(R.styleable.GeckoPoseView_defaultPointColorDark, ContextCompat.getColor(context, R.color.black))
                defaultSelectedPointColor = getColor(R.styleable.GeckoPoseView_defaultSelectedPointColor, ContextCompat.getColor(context, R.color.red))
                defaultLineColor = getColor(R.styleable.GeckoPoseView_defaultLineColor, ContextCompat.getColor(context, R.color.blue))
                defaultAngleColor = getColor(R.styleable.GeckoPoseView_defaultAngleColor, ContextCompat.getColor(context, R.color.green))
            } finally {
                recycle()
            }
        }
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

        pose?.let {
            if(drawAngles) {
                it.configuration.angles.forEach { angle ->
                    canvas.drawAngleIndicator(angle, it)
                }
            }

            if(drawLines) {
                it.configuration.lines.forEach { line ->
                    val start = it.getLandmarkPoint(line.start)
                    val end = it.getLandmarkPoint(line.end)

                    linePaint.color = if(line.color != null) {
                        ContextCompat.getColor(context, line.color)
                    } else {
                        defaultLineColor
                    }

                    canvas.drawLine(start.position.x, start.position.y, end.position.x, end.position.y, linePaint)
                }
            }

            it.landmarkPoints.forEach { processedPoint ->
                if(processedPoint.point.type == selectedPointType) {
                    selectedPointPaint.color = ContextCompat.getColor(context,
                        processedPoint.point.selectedColor ?: defaultSelectedPointColor)
                    canvas.drawCircle(processedPoint.position.x, processedPoint.position.y, 15f, selectedPointPaint)
                } else {
                    pointPaint.color = when {
                        processedPoint.point.color != null -> ContextCompat.getColor(context, processedPoint.point.color)
                        bitmap != null -> {
                            BitmapUtil.getContrastColor(
                                bitmap = bitmap!!,
                                x = processedPoint.position.x.toInt(),
                                y = processedPoint.position.y.toInt(),
                                contrastColorLight = defaultPointColorLight,
                                contrastColorDark = defaultPointColorDark,
                            )
                        }
                        else -> {
                            defaultPointColorLight
                        }
                    }

                    canvas.drawCircle(processedPoint.position.x, processedPoint.position.y, 10f, pointPaint)
                }
            }
        }
    }

    private fun Canvas.drawAngleIndicator(angle: Angle, pose: GeckoPose) {
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

        val color = if(angle.color != null) {
            ContextCompat.getColor(context, angle.color)
        } else {
            defaultAngleColor
        }

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