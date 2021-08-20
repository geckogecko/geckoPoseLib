package at.steinbacher.geckoposelib

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.hypot

class PoseView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : SurfaceView(context, attrs) {

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

    var selectedLandmark: PoseLandmark? = null

    private var surfaceCreated = false

    private var paint = Paint().apply {
        color = Color.RED
        textSize = 80.0f
        strokeWidth = 8.0f
    }

    init {
        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(p0: SurfaceHolder) {
                surfaceCreated = true

                if(bitmap != null) {
                    drawPose(bitmap!!, landmarkLineResults, landmarkAngles)
                }
            }

            override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {}
            override fun surfaceDestroyed(p0: SurfaceHolder) {}
        })
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                selectedLandmark = landmarkLineResults?.getClosestLandmark(event.x, event.y)
                true
            }
            MotionEvent.ACTION_MOVE -> {
                selectedLandmark?.position?.set(event.x, event.y)
                landmarkAngles?.forEach {
                    selectedLandmark?.landmarkType?.let { it1 -> it.getPoseLandmark(it1)?.position?.set(event.x, event.y) }
                }
                drawPose(bitmap!!, landmarkLineResults, landmarkAngles)
                true
            }
            else -> {
                super.onTouchEvent(event)
            }
        }
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
        val canvas = holder.lockCanvas()

        canvas.drawBitmap(
            bitmap,
            Rect(0, 0, bitmap.width, bitmap.height),
            Rect(0, 0, bitmap.width, bitmap.height),
            paint
        )

        //line
        landmarkLineResults?.forEach { landMarkLineResult ->
            for(i in 0..landMarkLineResult.poseLandmarks.size-2) {
                val start = landMarkLineResult.poseLandmarks[i]
                val end = landMarkLineResult.poseLandmarks[i+1]
                canvas.drawLine(start.position.x, start.position.y, end.position.x, end.position.y, paint)
            }

            landMarkLineResult.poseLandmarks.forEach {
                canvas.drawCircle(it.position.x, it.position.y, 5f, paint)
            }
        }

        //angle
        landmarkAngles?.forEach { landmarkAngleResult ->
            canvas.drawText(
                landmarkAngleResult.angle.toString(),
                landmarkAngleResult.middlePoint.position.x,
                landmarkAngleResult.middlePoint.position.y,
                paint
            )
        }

        holder.unlockCanvasAndPost(canvas)
    }
}