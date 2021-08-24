package at.steinbacher.geckopose

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import at.steinbacher.geckoposelib.*
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import java.lang.Exception

class MainActivity : AppCompatActivity(), GeckoPoseDetectionListener {
    private lateinit var poseView: PoseView

    private lateinit var files: List<String>
    private var bitmap: Bitmap? = null

    private var index = 0

    private val poseDetection = PoseDetection(
        detectorMode = AccuratePoseDetectorOptions.SINGLE_IMAGE_MODE,
        landmarkLines = listOf(
            LandmarkLine(
                tag = "kneeAngle",
                poseLandmarkTypes = listOf(PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_KNEE, PoseLandmark.LEFT_ANKLE)
            ),
            LandmarkLine(
                tag = "bodyAngle",
                poseLandmarkTypes = listOf(PoseLandmark.LEFT_KNEE, PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_SHOULDER)
            ),
            LandmarkLine(
                tag = "armAngle",
                poseLandmarkTypes = listOf(PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_WRIST)
            )

        ),
        listener = this
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        files = assets.list("")!!
            .toList()
            .filter { it.contains(".") }

        poseView = findViewById(R.id.surfaceView)
        poseView.setOnClickListener {
            index++
            loadTestImage(index)
        }

        loadTestImage(index)
    }

    private fun loadTestImage(index: Int) {
        val file = assets.open(files[index])
        bitmap = BitmapFactory.decodeStream(file)

        if(bitmap != null) {
            poseDetection.processImage(bitmap!!)
        }
    }

    override fun onSuccess(
        landmarkLineResults: List<LandmarkLineResult>
    ) {
        poseView.bitmap = bitmap
        poseView.landmarkLineResults = landmarkLineResults

        val kneeAngleLines = landmarkLineResults.getPoseLandmarksByTag("kneeAngle")!!
        val bodyAngleLines = landmarkLineResults.getPoseLandmarksByTag("bodyAngle")!!
        val armAngleLines = landmarkLineResults.getPoseLandmarksByTag("armAngle")!!
        poseView.landmarkAngles = listOf(
            LandmarkAngle(
                startLandmarkType = PoseLandmark.LEFT_HIP,
                middleLandmarkType = PoseLandmark.LEFT_KNEE,
                endLandmarkType = PoseLandmark.LEFT_ANKLE,
                landmarkLine = kneeAngleLines
            ),
            LandmarkAngle(
                startLandmarkType = PoseLandmark.LEFT_KNEE,
                middleLandmarkType = PoseLandmark.LEFT_HIP,
                endLandmarkType = PoseLandmark.LEFT_SHOULDER,
                landmarkLine = bodyAngleLines
            ),
            LandmarkAngle(
                startLandmarkType = PoseLandmark.LEFT_HIP,
                middleLandmarkType = PoseLandmark.LEFT_SHOULDER,
                endLandmarkType = PoseLandmark.LEFT_WRIST,
                landmarkLine = armAngleLines
            )
        )
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