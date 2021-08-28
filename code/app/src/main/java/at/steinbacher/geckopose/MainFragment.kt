package at.steinbacher.geckopose

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import at.steinbacher.geckoposelib.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import java.lang.Exception

class MainFragment : PoseFragment() {

    private lateinit var fabTakePicture: FloatingActionButton

    override val poseDetection = PoseDetection(
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        poseView = view.findViewById(R.id.pose_view)
        fabTakePicture = view.findViewById(R.id.fab_take_picture)

        fabTakePicture.setOnClickListener {
            openTakePicture()
        }
    }

    override fun onPictureSet() {
        fabTakePicture.visibility = View.GONE
    }

    override fun onSuccess(landmarkLineResults: List<LandmarkLineResult>) {
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
}