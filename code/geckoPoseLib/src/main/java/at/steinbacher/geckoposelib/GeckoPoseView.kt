package at.steinbacher.geckoposelib

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.math.pow
import kotlin.math.sqrt

class GeckoPoseView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    var bitmap: Bitmap? = null
        set(value) {
            field = value

            if(value != null && surfaceCreated) {
                drawPose(value, pose)
            }
        }

    var pose: GeckoPose? = null
        set(value) {
            field = value

            if(bitmap != null && surfaceCreated) {
                drawPose(bitmap!!, pose)
            }
        }

    private var selectedPointType: Int? = null

    private val surfaceView: SurfaceView
    private val fabSaveEdit: FloatingActionButton

    private var surfaceCreated = false

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

    interface OnPointChangedListener {
        fun onPointChanged(type: Int)
    }
    private var onPointChangedListener: OnPointChangedListener? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_pose, this, true)

        surfaceView = findViewById(R.id.surface_view)
        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(p0: SurfaceHolder) {
                surfaceCreated = true

                if(bitmap != null) {
                    drawPose(bitmap!!, pose)
                }
            }

            override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {}
            override fun surfaceDestroyed(p0: SurfaceHolder) {
                surfaceCreated = false
            }
        })

        fabSaveEdit = findViewById(R.id.fab_save_edit)
        fabSaveEdit.setOnClickListener {
            save()
            fabSaveEdit.visibility = GONE
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                if(selectedPointType == null) {
                    fabSaveEdit.visibility = VISIBLE
                    selectedPointType = pose?.getClosestPoint(event.x, event.y)?.type
                    drawPose(bitmap!!, pose)
                }
                true
            }
            MotionEvent.ACTION_MOVE -> {
                if(selectedPointType != null && lastMoveX != -1f && lastMoveY != -1f) {
                    val moveX = event.x - lastMoveX
                    val moveY = event.y - lastMoveY

                    pose?.updatePoint(selectedPointType!!, moveX, moveY)
                    drawPose(bitmap!!, pose)

                    selectedPointType?.let { onPointChangedListener?.onPointChanged(it) }
                }

                lastMoveX = event.x
                lastMoveY = event.y
                true
            }
            MotionEvent.ACTION_UP -> {
                lastMoveX = -1f
                lastMoveY = -1f
                true
            }

            else -> {
                super.onTouchEvent(event)
            }
        }
    }

    fun setOnPointChangedListener(listener: OnPointChangedListener) {
        onPointChangedListener = listener
    }

    private fun save() {
        selectedPointType = null
        lastMoveX = -1f
        lastMoveY = -1f

        drawPose(bitmap!!, pose)
    }

    private fun drawPose(
        bitmap: Bitmap,
        pose: GeckoPose? = null,
    ) {
        val canvas = surfaceView.holder.lockCanvas()

        canvas.drawBitmap(
            bitmap,
            Rect(0, 0, bitmap.width, bitmap.height),
            Rect(0, 0, bitmap.width , bitmap.height),
            pointPaint
        )

        pose?.let {
            it.configuration.lines.forEach { line ->
                val start = it.getPose(line.start)
                val end = it.getPose(line.end)

                linePaint.color = line.color

                canvas.drawLine(start.position.x, start.position.y, end.position.x, end.position.y, linePaint)
            }

            it.points.forEach { point ->
                if(point.type == selectedPointType) {
                    canvas.drawCircle(point.position.x, point.position.y, 15f, selectedPointPaint)
                } else {
                    canvas.drawCircle(point.position.x, point.position.y, 10f, pointPaint)
                }
            }

            it.configuration.angles.forEach { angle ->
                val (start, middle, end) = it.getAnglePoints(angle.tag)
                val angleValue = it.getAngle(start, middle, end)
                canvas.drawAngleIndicator(start.position, middle.position, end.position, angleValue, angle.tag, angle.color)
            }
        }

        surfaceView.holder.unlockCanvasAndPost(canvas)
    }

    private fun Canvas.drawAngleIndicator(startPoint: PointF, middlePoint: PointF, endPoint: PointF, angle: Double, displayTag: String, color: Int) {
        val distanceMiddleStart = getDistanceBetweenPoints(middlePoint, startPoint)
        val distanceMiddleEnd = getDistanceBetweenPoints(middlePoint, endPoint)
        val biggestDistance = listOf(distanceMiddleStart, distanceMiddleEnd).minOrNull() ?: distanceMiddleStart
        val angleDistance = biggestDistance * 0.35

        val startAngle = if(startPoint.y < middlePoint.y) {
            360 - Util.getAngle(PointF(middlePoint.x + 10, middlePoint.y), middlePoint, startPoint)
        } else {
            Util.getAngle(PointF(middlePoint.x + 10, middlePoint.y), middlePoint, startPoint)
        }

        val anglePaint = Paint().apply {
            this.color = color
            strokeWidth = 8.0f
            style = Paint.Style.STROKE
        }

        this.drawArc(
            (middlePoint.x - angleDistance).toFloat(),
            (middlePoint.y - angleDistance).toFloat(),
            (middlePoint.x + angleDistance).toFloat(),
            (middlePoint.y + angleDistance).toFloat(),
            startAngle.toFloat(),
            angle.toFloat(),
            false,
            anglePaint
        )

        val textPath = Path().apply {
            this.addArc(
                (middlePoint.x - angleDistance).toFloat(),
                (middlePoint.y - angleDistance).toFloat(),
                (middlePoint.x + angleDistance).toFloat(),
                (middlePoint.y + angleDistance).toFloat(),
                startAngle.toFloat(),
                angle.toFloat()
            )
        }

        val textPaint = Paint().apply {
            this.color = color
            textSize = resources.getDimensionPixelSize(at.steinbacher.geckoposelib.R.dimen.angleFontSize).toFloat()
        }

        val arcLength = (angle/360)*(2*angleDistance*Math.PI)
        this.drawTextOnPath(
            displayTag,
            textPath,
            (arcLength / 2).toFloat(),
            (angleDistance / 2).toFloat(),
            textPaint
        )
    }

    private fun getDistanceBetweenPoints(startPoint: PointF, endPoint: PointF)
        = sqrt((endPoint.x - startPoint.x).pow(2) + (endPoint.y - startPoint.y).pow(2))
}