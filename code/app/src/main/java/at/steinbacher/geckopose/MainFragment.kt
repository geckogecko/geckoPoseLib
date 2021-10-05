package at.steinbacher.geckopose

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
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
            points = listOf(
                Point(PoseLandmark.LEFT_HIP, R.color.grey, R.color.red),
                Point(PoseLandmark.LEFT_KNEE, R.color.grey, R.color.red),
                Point(PoseLandmark.LEFT_ANKLE, R.color.grey, R.color.red),
                Point(PoseLandmark.LEFT_SHOULDER, R.color.grey, R.color.red),
                Point(PoseLandmark.LEFT_ELBOW, R.color.grey, R.color.red),
                Point(PoseLandmark.LEFT_WRIST, R.color.grey, R.color.red),
            ),
            lines = listOf(
                Line(start = PoseLandmark.LEFT_KNEE, end = PoseLandmark.LEFT_HIP, tag = "knee_hip", color = R.color.blue),
                Line(start = PoseLandmark.LEFT_ANKLE, end = PoseLandmark.LEFT_KNEE, tag = "knee_ankle", color = R.color.blue),
                Line(start = PoseLandmark.LEFT_HIP, end = PoseLandmark.LEFT_SHOULDER, tag = "hip_shoulder", color = R.color.blue),
                Line(start = PoseLandmark.LEFT_SHOULDER, end = PoseLandmark.LEFT_ELBOW, tag = "shoulder_elbow", color = R.color.blue),
                Line(start = PoseLandmark.LEFT_ELBOW, end = PoseLandmark.LEFT_WRIST, tag = "elbow_wrist", color = R.color.blue),
            ),
            angles = listOf(
                MinMaxAngle(startPointType = PoseLandmark.LEFT_HIP, middlePointType = PoseLandmark.LEFT_KNEE,
                    endPointType = PoseLandmark.LEFT_ANKLE, tag = "a", color = R.color.color_angle_ok, 0f, 40f, R.color.color_angle_nok),
                Angle(startPointType = PoseLandmark.LEFT_KNEE, middlePointType = PoseLandmark.LEFT_HIP,
                    endPointType = PoseLandmark.LEFT_SHOULDER, tag = "b", color = R.color.color_angle_ok),
                Angle(startPointType = PoseLandmark.LEFT_HIP, middlePointType = PoseLandmark.LEFT_SHOULDER,
                    endPointType = PoseLandmark.LEFT_ELBOW, tag = "c", color = R.color.color_angle_ok),
                Angle(startPointType = PoseLandmark.LEFT_SHOULDER, middlePointType = PoseLandmark.LEFT_ELBOW,
                    endPointType = PoseLandmark.LEFT_WRIST, tag = "d", color = R.color.color_angle_ok),
            )
        ),
        GeckoPoseConfiguration(
            tag = "right_pose",
            points = listOf(
                Point(PoseLandmark.RIGHT_HIP, R.color.grey, R.color.red),
                Point(PoseLandmark.RIGHT_KNEE, R.color.grey, R.color.red),
                Point(PoseLandmark.RIGHT_ANKLE, R.color.grey, R.color.red),
                Point(PoseLandmark.RIGHT_SHOULDER, R.color.grey, R.color.red),
                Point(PoseLandmark.RIGHT_ELBOW, R.color.grey, R.color.red),
                Point(PoseLandmark.RIGHT_WRIST, R.color.grey, R.color.red),
            ),
            lines = listOf(
                Line(start = PoseLandmark.RIGHT_KNEE, end = PoseLandmark.RIGHT_HIP, tag = "knee_hip", color = R.color.blue),
                Line(start = PoseLandmark.RIGHT_ANKLE, end = PoseLandmark.RIGHT_KNEE, tag = "knee_ankle", color = R.color.blue),
                Line(start = PoseLandmark.RIGHT_HIP, end = PoseLandmark.RIGHT_SHOULDER, tag = "hip_shoulder", color = R.color.blue),
                Line(start = PoseLandmark.RIGHT_SHOULDER, end = PoseLandmark.RIGHT_ELBOW, tag = "shoulder_elbow", color = R.color.blue),
                Line(start = PoseLandmark.RIGHT_ELBOW, end = PoseLandmark.RIGHT_WRIST, tag = "elbow_wrist", color = R.color.blue),
            ),
            angles = listOf(
                MinMaxAngle(startPointType = PoseLandmark.RIGHT_HIP, middlePointType = PoseLandmark.RIGHT_KNEE,
                    endPointType = PoseLandmark.RIGHT_ANKLE, tag = "a", color = R.color.color_angle_ok, 0f, 40f, R.color.color_angle_nok),
                Angle(startPointType = PoseLandmark.RIGHT_KNEE, middlePointType = PoseLandmark.RIGHT_HIP,
                    endPointType = PoseLandmark.RIGHT_SHOULDER, tag = "b", color = R.color.color_angle_ok),
                Angle(startPointType = PoseLandmark.RIGHT_HIP, middlePointType = PoseLandmark.RIGHT_SHOULDER,
                    endPointType = PoseLandmark.RIGHT_ELBOW, tag = "c", color = R.color.color_angle_ok),
                Angle(startPointType = PoseLandmark.RIGHT_SHOULDER, middlePointType = PoseLandmark.RIGHT_ELBOW,
                    endPointType = PoseLandmark.RIGHT_WRIST, tag = "d", color = R.color.color_angle_ok),
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