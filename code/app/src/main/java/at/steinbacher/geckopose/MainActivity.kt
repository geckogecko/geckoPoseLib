package at.steinbacher.geckopose

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
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
                tag = "armBodyAngle",
                poseLandmarkTypes = listOf(PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_ELBOW)
            ),
            LandmarkLine(
                tag = "armAngle",
                poseLandmarkTypes = listOf(PoseLandmark.LEFT_WRIST, PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_SHOULDER)
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
        val originalBitmap = BitmapFactory.decodeStream(file)

        bitmap = resize(
            originalBitmap,
            Resources.getSystem().displayMetrics.widthPixels,
            Resources.getSystem().displayMetrics.heightPixels
        )

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
        val armBodyAngleLines = landmarkLineResults.getPoseLandmarksByTag("armBodyAngle")!!
        val armAngleLines = landmarkLineResults.getPoseLandmarksByTag("armAngle")!!
        poseView.landmarkAngles = listOf(
            LandmarkAngle(
                startLandmarkType = PoseLandmark.LEFT_HIP,
                middleLandmarkType = PoseLandmark.LEFT_KNEE,
                endLandmarkType = PoseLandmark.LEFT_ANKLE,
                landmarkLine = kneeAngleLines,
                displayTag = "a",
                color = Color.GREEN
            ),
            LandmarkAngle(
                startLandmarkType = PoseLandmark.LEFT_KNEE,
                middleLandmarkType = PoseLandmark.LEFT_HIP,
                endLandmarkType = PoseLandmark.LEFT_SHOULDER,
                landmarkLine = bodyAngleLines,
                displayTag = "b",
                color = Color.CYAN
            ),
            LandmarkAngle(
                startLandmarkType = PoseLandmark.LEFT_HIP,
                middleLandmarkType = PoseLandmark.LEFT_SHOULDER,
                endLandmarkType = PoseLandmark.LEFT_ELBOW,
                landmarkLine = armBodyAngleLines,
                displayTag = "c",
                color = Color.LTGRAY
            ),
            LandmarkAngle(
                startLandmarkType = PoseLandmark.LEFT_WRIST,
                middleLandmarkType = PoseLandmark.LEFT_ELBOW,
                endLandmarkType = PoseLandmark.LEFT_SHOULDER,
                landmarkLine = armAngleLines,
                displayTag = "d",
                color = Color.MAGENTA
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

    private fun resize(image: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        return if (maxHeight > 0 && maxWidth > 0) {
            val width = image.width
            val height = image.height
            val ratioBitmap = width.toFloat() / height.toFloat()
            val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()
            var finalWidth = maxWidth
            var finalHeight = maxHeight
            if (ratioMax > ratioBitmap) {
                finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
            } else {
                finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
            }
            Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true)
        } else {
            image
        }
    }
}