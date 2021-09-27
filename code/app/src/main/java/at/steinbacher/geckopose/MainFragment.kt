package at.steinbacher.geckopose

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import at.steinbacher.geckoposelib.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

class MainFragment : GeckoPoseFragment() {

    private lateinit var fabTakePicture: FloatingActionButton
    private lateinit var txtAngleA: TextView
    private lateinit var txtAngleB: TextView

    override val preferredPose: String = "left_pose"
    override val geckoPoseConfigurations = listOf(
        GeckoPoseConfiguration(
            tag = "left_pose",
            pointTypes = listOf(
                PoseLandmark.LEFT_HIP,
                PoseLandmark.LEFT_KNEE,
                PoseLandmark.LEFT_ANKLE,
                PoseLandmark.LEFT_SHOULDER,
                PoseLandmark.LEFT_ELBOW,
                PoseLandmark.LEFT_WRIST
            ),
            lines = listOf(
                Line(start = PoseLandmark.LEFT_KNEE, end = PoseLandmark.LEFT_HIP, tag = "knee_hip", color = Color.BLUE),
                Line(start = PoseLandmark.LEFT_KNEE, end = PoseLandmark.LEFT_ANKLE, tag = "knee_ankle", color = Color.BLUE),
                Line(start = PoseLandmark.LEFT_HIP, end = PoseLandmark.LEFT_SHOULDER, tag = "hip_shoulder", color = Color.BLUE),
                Line(start = PoseLandmark.LEFT_SHOULDER, end = PoseLandmark.LEFT_ELBOW, tag = "shoulder_elbow", color = Color.BLUE),
                Line(start = PoseLandmark.LEFT_ELBOW, end = PoseLandmark.LEFT_WRIST, tag = "elbow_wrist", color = Color.BLUE),
            ),
            angles = listOf(
                Angle(line1Tag = "knee_hip", line2Tag = "knee_ankle", tag = "a", color = Color.GREEN),
                Angle(line1Tag = "knee_hip", line2Tag = "hip_shoulder", tag = "b", color = Color.GREEN),
                Angle(line1Tag = "hip_shoulder", line2Tag = "shoulder_elbow", tag = "c", color = Color.GREEN),
                Angle(line1Tag = "shoulder_elbow", line2Tag = "elbow_wrist", tag = "d", color = Color.GREEN),
            )
        ),
        GeckoPoseConfiguration(
            tag = "right_pose",
            pointTypes = listOf(
                PoseLandmark.RIGHT_HIP,
                PoseLandmark.RIGHT_KNEE,
                PoseLandmark.RIGHT_ANKLE,
                PoseLandmark.RIGHT_SHOULDER,
                PoseLandmark.RIGHT_ELBOW,
                PoseLandmark.RIGHT_WRIST
            ),
            lines = listOf(
                Line(start = PoseLandmark.RIGHT_KNEE, end = PoseLandmark.RIGHT_HIP, tag = "knee_hip", color = Color.BLUE),
                Line(start = PoseLandmark.RIGHT_KNEE, end = PoseLandmark.RIGHT_ANKLE, tag = "knee_ankle", color = Color.BLUE),
                Line(start = PoseLandmark.RIGHT_HIP, end = PoseLandmark.RIGHT_SHOULDER, tag = "hip_shoulder", color = Color.BLUE),
                Line(start = PoseLandmark.RIGHT_SHOULDER, end = PoseLandmark.RIGHT_ELBOW, tag = "shoulder_elbow", color = Color.BLUE),
                Line(start = PoseLandmark.RIGHT_ELBOW, end = PoseLandmark.RIGHT_WRIST, tag = "elbow_wrist", color = Color.BLUE),
            ),
            angles = listOf(
                Angle(line1Tag = "knee_hip", line2Tag = "knee_ankle", tag = "a", color = Color.GREEN),
                Angle(line1Tag = "knee_hip", line2Tag = "hip_shoulder", tag = "b", color = Color.GREEN),
                Angle(line1Tag = "hip_shoulder", line2Tag = "shoulder_elbow", tag = "c", color = Color.GREEN),
                Angle(line1Tag = "shoulder_elbow", line2Tag = "elbow_wrist", tag = "d", color = Color.GREEN),
            )
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        txtAngleA = view.findViewById(R.id.txt_angle_a)
        txtAngleB = view.findViewById(R.id.txt_angle_b)

        geckoPoseView = view.findViewById(R.id.pose_view)
        geckoPoseView.setOnPointChangedListener(object : GeckoPoseView.OnPointChangedListener {
            override fun onPointChanged(type: Int) {
                geckoPoseView.pose?.let { updateAngleTexts(it) }
            }
        })

        fabTakePicture = view.findViewById(R.id.fab_take_picture)

        fabTakePicture.setOnClickListener {
            openTakePicture()
        }
    }

    override fun onPictureSet() {
        fabTakePicture.visibility = View.GONE

        txtAngleA.visibility = View.VISIBLE
        txtAngleB.visibility = View.VISIBLE
    }

    override fun onPoseSet(pose: GeckoPose) {
        super.onPoseSet(pose)

        updateAngleTexts(pose)
    }

    private fun updateAngleTexts(pose: GeckoPose) {
        pose.configuration.angles.forEach {
            when(it.tag) {
                "a" -> txtAngleA.text = pose.getAngle(it.tag).toString()
                "b" -> txtAngleB.text = pose.getAngle(it.tag).toString()
            }
        }
    }
}