package at.steinbacher.geckoposelib

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.sqrt

class PoseView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    var bitmap: Bitmap? = null
        set(value) {
            field = value

            if(value != null && surfaceCreated) {
                drawPose(value, landmarkLineResults, landmarkAngles)
            }
        }

    var landmarkLineResults: List<LandmarkLineResult>? = null
        set(value) {
            field = value

            if(bitmap != null && surfaceCreated) {
                drawPose(bitmap!!, landmarkLineResults, landmarkAngles)
            }
        }

    var landmarkAngles: List<LandmarkAngle>? = null
        set(value) {
            field = value

            if(bitmap != null && surfaceCreated) {
                drawPose(bitmap!!, landmarkLineResults, landmarkAngles)
            }
        }

    private var selectedLandmark: PoseLandmark? = null

    private val surfaceView: SurfaceView
    private val fabSaveEdit: FloatingActionButton

    private var surfaceCreated = false

    private var paint = Paint().apply {
        color = Color.RED
        strokeWidth = 8.0f
    }

    private var selectedLandmarkPaint = Paint().apply {
        color = Color.GRAY
    }

    private var lastMoveX: Float = -1f
    private var lastMoveY: Float = -1f

    interface OnLandmarkAnglesChangeListener {
        fun onLandmarkAnglesChanged(landmarkLineResults: List<LandmarkAngle>)
    }
    private var onLandmarkAnglesChangeListener: OnLandmarkAnglesChangeListener? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_pose, this, true)

        surfaceView = findViewById(R.id.surface_view)
        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(p0: SurfaceHolder) {
                surfaceCreated = true

                if(bitmap != null) {
                    drawPose(bitmap!!, landmarkLineResults, landmarkAngles)
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
                if(selectedLandmark == null) {
                    fabSaveEdit.visibility = VISIBLE

                    selectedLandmark = landmarkLineResults?.getClosestLandmark(event.x, event.y)
                    drawPose(bitmap!!, landmarkLineResults, landmarkAngles)
                }
                true
            }
            MotionEvent.ACTION_MOVE -> {
                if(selectedLandmark != null && lastMoveX != -1f && lastMoveY != -1f) {
                    val moveX = event.x - lastMoveX
                    val moveY = event.y - lastMoveY

                    selectedLandmark?.position?.set(selectedLandmark!!.position.x + moveX, selectedLandmark!!.position.y + moveY)
                    drawPose(bitmap!!, landmarkLineResults, landmarkAngles)

                    landmarkAngles?.let { onLandmarkAnglesChangeListener?.onLandmarkAnglesChanged(it) }
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

    fun setOnLandmarkAnglesChangeListener(listener: OnLandmarkAnglesChangeListener) {
        onLandmarkAnglesChangeListener = listener
    }

    private fun save() {
        selectedLandmark = null
        lastMoveX = -1f
        lastMoveY = -1f

        drawPose(bitmap!!, landmarkLineResults, landmarkAngles)
    }

    private fun List<LandmarkLineResult>.getClosestLandmark(x: Float, y: Float): PoseLandmark? {
        val flatPoseLandmarks = ArrayList<PoseLandmark>()
        this.forEach { it.poseLandmarks.forEach { it1 -> flatPoseLandmarks.add(it1) } }

        return flatPoseLandmarks.minByOrNull { hypot((x - it.position.x).toDouble(), (y - it.position.y).toDouble()) }
    }

    private fun drawPose(
        bitmap: Bitmap,
        landmarkLineResults: List<LandmarkLineResult>? = null,
        landmarkAngles: List<LandmarkAngle>? = null
    ) {
        val canvas = surfaceView.holder.lockCanvas()

        canvas.drawBitmap(
            bitmap,
            Rect(0, 0, bitmap.width, bitmap.height),
            Rect(0, 0, bitmap.width , bitmap.height ),
            paint
        )

        //angle
        landmarkAngles?.forEach { landmarkAngleResult ->
            canvas.drawAngleIndicator(
                landmarkAngleResult.startPoint.position,
                landmarkAngleResult.middlePoint.position,
                landmarkAngleResult.endPoint.position,
                landmarkAngleResult.angle,
                landmarkAngleResult.displayTag,
                landmarkAngleResult.color
            )
        }

        //line
        landmarkLineResults?.forEach { landMarkLineResult ->
            for(i in 0..landMarkLineResult.poseLandmarks.size-2) {
                val start = landMarkLineResult.poseLandmarks[i]
                val end = landMarkLineResult.poseLandmarks[i+1]
                canvas.drawLine(start.position.x, start.position.y, end.position.x, end.position.y, paint)
            }

            landMarkLineResult.poseLandmarks.forEach {

                if(it == selectedLandmark) {
                    canvas.drawCircle(it.position.x, it.position.y, 15f, selectedLandmarkPaint)
                } else {
                    canvas.drawCircle(it.position.x, it.position.y, 10f, paint)
                }
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