package at.steinbacher.geckoposelib

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.SurfaceView
import com.google.mlkit.vision.pose.PoseLandmark

class PoseDrawer(private val surfaceView: SurfaceView) {

    private var paint = Paint().apply {
        color = Color.RED
        textSize = 80.0f
        strokeWidth = 8.0f
    }

    fun draw(bitmap: Bitmap, poseLandmarks: List<PoseLandmark>? = null) {
        val canvas = surfaceView.holder.lockCanvas()

        canvas.drawBitmap(
            bitmap,
            Rect(0, 0, bitmap.width, bitmap.height),
            Rect(0, 0, bitmap.width, bitmap.height),
            paint
        )

        poseLandmarks?.forEach { canvas.drawCircle(it.position.x, it.position.y, 5f, paint) }

        surfaceView.holder.unlockCanvasAndPost(canvas)
    }
}