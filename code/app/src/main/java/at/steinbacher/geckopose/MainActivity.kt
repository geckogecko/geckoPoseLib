package at.steinbacher.geckopose

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import at.steinbacher.geckoposelib.GeckoPoseDetectionListener
import at.steinbacher.geckoposelib.PoseAngle
import at.steinbacher.geckoposelib.PoseDetection
import at.steinbacher.geckoposelib.PoseDrawer
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import java.lang.Exception
import javax.security.auth.callback.Callback

class MainActivity : AppCompatActivity(), GeckoPoseDetectionListener {
    private lateinit var surfaceView: SurfaceView
    private lateinit var files: List<String>
    private var bitmap: Bitmap? = null

    private var index = 0

    private val poseDetection = PoseDetection(
        detectorMode = AccuratePoseDetectorOptions.SINGLE_IMAGE_MODE,
        poseLandmarkTypes = listOf(PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_KNEE, PoseLandmark.LEFT_ANKLE),
        poseAngles = listOf(PoseAngle(PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_KNEE, PoseLandmark.LEFT_ANKLE)),
        listener = this
    )

    private lateinit var poseDrawer: PoseDrawer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        files = assets.list("")!!
            .toList()
            .filter { it.contains(".") }

        surfaceView = findViewById(R.id.surfaceView)
        surfaceView.setOnClickListener {
            index++
            Log.i("GEORG", "file: ${files[index]}")
            val file = assets.open(files[index])
            bitmap = BitmapFactory.decodeStream(file)

            if(bitmap != null) {
                poseDetection.processImage(bitmap!!)
            }
        }
        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(p0: SurfaceHolder) {
                val file = assets.open(files[index])
                bitmap = BitmapFactory.decodeStream(file)

                poseDetection.processImage(bitmap!!)
            }

            override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {}

            override fun surfaceDestroyed(p0: SurfaceHolder) {}
        })

        poseDrawer = PoseDrawer(surfaceView)
    }

    override fun onSuccess(poseLandMarks: List<PoseLandmark>) {
        poseDrawer.draw(bitmap!!, poseLandMarks)
    }

    override fun onMissingPoseLandmarkType() {
        Log.i("GEORG", "onMissingPoseLandmarkType: ")
    }

    override fun onCompletedWithoutSuccess() {
        Log.i("GEORG", "onCompletedWithoutSuccess: ")
    }

    override fun onFailure(exception: Exception) {
        Log.i("GEORG", "onFailure: ")
    }
}