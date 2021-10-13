package at.steinbacher.geckoposelib

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.*
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import at.steinbacher.geckoposelib.util.AngleUtil
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.math.acos
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
            imageView.setImageBitmap(field)

            field?.let {
                skeletonView.updateLayoutParams {
                    width = it.width
                    height = it.height
                }
            }
        }

    var pose: GeckoPose? = null
        set(value) {
            field = value
            skeletonView.pose = field
        }

    var drawLines
        set(value) { skeletonView.drawLines = value }
        get() = skeletonView.drawLines

    var drawAngles
        set(value) { skeletonView.drawAngles = value }
        get() = skeletonView.drawAngles

    private val skeletonView: SkeletonView
    private val imageView: ImageView
    private val fabSaveEdit: FloatingActionButton

    interface OnPoseChangedListener {
        fun onPoseChanged(pose: GeckoPose)
    }
    private var onPoseChangedListener: OnPoseChangedListener? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_pose, this, true)

        fabSaveEdit = findViewById(R.id.fab_save_edit)
        fabSaveEdit.setOnClickListener {
            save()
            fabSaveEdit.visibility = GONE
        }

        imageView = findViewById(R.id.image_view)

        skeletonView = findViewById(R.id.skeleton_view)
        skeletonView.setSkeletonViewListener(object : SkeletonView.SkeletonViewListener {
            override fun onPointSelected() {
                fabSaveEdit.visibility = VISIBLE
            }

            override fun onPointChanged(type: Int) {
                pose?.let { onPoseChangedListener?.onPoseChanged(it) }
            }
        })
    }

    fun setOnPoseChangedListener(listener: OnPoseChangedListener) {
        onPoseChangedListener = listener
    }

    private fun save() {
        skeletonView.saveSelectedPoint()
    }
}

class SkeletonView @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr) {

    var pose: GeckoPose? = null
        set(value) {
            field = value
            invalidate()
        }

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
                if(event.isInsidePreview()) {
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
                lastMoveX = -1f
                lastMoveY = -1f
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
                    val start = it.getPose(line.start)
                    val end = it.getPose(line.end)

                    linePaint.color = ContextCompat.getColor(context, line.color ?: it.configuration.defaultLineColor)

                    canvas.drawLine(start.position.x, start.position.y, end.position.x, end.position.y, linePaint)
                }
            }

            it.landmarkPoints.forEach { processedPoint ->
                if(processedPoint.point.type == selectedPointType) {
                    selectedPointPaint.color = ContextCompat.getColor(context,
                        processedPoint.point.selectedColor ?: it.configuration.defaultSelectedPointColor)
                    canvas.drawCircle(processedPoint.position.x, processedPoint.position.y, 15f, selectedPointPaint)
                } else {
                    pointPaint.color = ContextCompat.getColor(context,
                        processedPoint.point.color ?: it.configuration.defaultPointColor)
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

        val colorRes = if(angle is MinMaxAngle && angle.isAngleNotInside(angleDegrees)) {
            angle.errorColor ?: pose.configuration.defaultNOKAngleColor
        } else {
            angle.color ?: pose.configuration.defaultAngleColor
        }
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

        /*
        //draw the tag of the angle inside the angle indication
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
         */
    }

    private fun getDistanceBetweenPoints(startPoint: PointF, endPoint: PointF)
            = sqrt((endPoint.x - startPoint.x).pow(2) + (endPoint.y - startPoint.y).pow(2))

}