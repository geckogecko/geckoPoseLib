package at.steinbacher.geckopose

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import at.steinbacher.geckoposelib.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.mlkit.vision.pose.PoseLandmark

class MainFragment : GeckoPoseFragment() {

    private lateinit var fabImageChooser: FloatingActionButton
    private lateinit var fabTakeImage: FloatingActionButton
    private lateinit var fabGalleryChooser: FloatingActionButton
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
                Line(start = PoseLandmark.LEFT_ANKLE, end = PoseLandmark.LEFT_KNEE, tag = "knee_ankle", color = Color.BLUE),
                Line(start = PoseLandmark.LEFT_HIP, end = PoseLandmark.LEFT_SHOULDER, tag = "hip_shoulder", color = Color.BLUE),
                Line(start = PoseLandmark.LEFT_SHOULDER, end = PoseLandmark.LEFT_ELBOW, tag = "shoulder_elbow", color = Color.BLUE),
                Line(start = PoseLandmark.LEFT_ELBOW, end = PoseLandmark.LEFT_WRIST, tag = "elbow_wrist", color = Color.BLUE),
            ),
            angles = listOf(
                Angle(startPointType = PoseLandmark.LEFT_ANKLE, middlePointType = PoseLandmark.LEFT_KNEE,
                    endPointType = PoseLandmark.LEFT_HIP, target = AngleTarget.FIRST, tag = "a", color = Color.GREEN),
                Angle(startPointType = PoseLandmark.LEFT_KNEE, middlePointType = PoseLandmark.LEFT_HIP,
                    endPointType = PoseLandmark.LEFT_SHOULDER, target = AngleTarget.SECOND, tag = "b", color = Color.GREEN),
                Angle(startPointType = PoseLandmark.LEFT_HIP, middlePointType = PoseLandmark.LEFT_SHOULDER,
                    endPointType = PoseLandmark.LEFT_ELBOW, target = AngleTarget.FIRST, tag = "c", color = Color.GREEN),
                Angle(startPointType = PoseLandmark.LEFT_SHOULDER, middlePointType = PoseLandmark.LEFT_ELBOW,
                    endPointType = PoseLandmark.LEFT_WRIST, target = AngleTarget.FIRST, tag = "d", color = Color.GREEN),
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
                Line(start = PoseLandmark.RIGHT_ANKLE, end = PoseLandmark.RIGHT_KNEE, tag = "knee_ankle", color = Color.BLUE),
                Line(start = PoseLandmark.RIGHT_HIP, end = PoseLandmark.RIGHT_SHOULDER, tag = "hip_shoulder", color = Color.BLUE),
                Line(start = PoseLandmark.RIGHT_SHOULDER, end = PoseLandmark.RIGHT_ELBOW, tag = "shoulder_elbow", color = Color.BLUE),
                Line(start = PoseLandmark.RIGHT_ELBOW, end = PoseLandmark.RIGHT_WRIST, tag = "elbow_wrist", color = Color.BLUE),
            ),
            angles = listOf(
                Angle(startPointType = PoseLandmark.RIGHT_HIP, middlePointType = PoseLandmark.RIGHT_KNEE,
                    endPointType = PoseLandmark.RIGHT_ANKLE, target = AngleTarget.FIRST, tag = "a", color = Color.GREEN),
                Angle(startPointType = PoseLandmark.RIGHT_SHOULDER, middlePointType = PoseLandmark.RIGHT_HIP,
                    endPointType = PoseLandmark.RIGHT_KNEE, target = AngleTarget.SECOND, tag = "b", color = Color.GREEN),
                Angle(startPointType = PoseLandmark.RIGHT_HIP, middlePointType = PoseLandmark.RIGHT_SHOULDER,
                    endPointType = PoseLandmark.RIGHT_ELBOW, target = AngleTarget.FIRST, tag = "c", color = Color.GREEN),
                Angle(startPointType = PoseLandmark.RIGHT_SHOULDER, middlePointType = PoseLandmark.RIGHT_ELBOW,
                    endPointType = PoseLandmark.RIGHT_WRIST, target = AngleTarget.FIRST, tag = "d", color = Color.GREEN),
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

        fabImageChooser = view.findViewById(R.id.fab_image_chooser)
        fabTakeImage = view.findViewById(R.id.fab_take_image)
        fabGalleryChooser = view.findViewById(R.id.fab_gallery_chooser)

        fabImageChooser.setOnClickListener {
            openImagePicker()
        }

        fabTakeImage.setOnClickListener {
            openTakeImage()
        }

        fabGalleryChooser.setOnClickListener {
            openChooseFromGallery()
        }
    }

    override fun onPictureSet() {
        fabImageChooser.visibility = View.GONE
        fabTakeImage.visibility = View.GONE
        fabGalleryChooser.visibility = View.GONE

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