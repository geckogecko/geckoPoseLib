package at.steinbacher.geckopose

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import at.steinbacher.geckoposelib.*
import at.steinbacher.geckoposelib.GeckoPose.Companion.copy
import at.steinbacher.geckoposelib.util.BitmapUtil
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import java.io.File
import java.lang.Exception

class MainFragment : ImageCaptureFragment() {
    private lateinit var fabImageChooser: FloatingActionButton
    private lateinit var txtAngleA: TextView
    private lateinit var txtAngleB: TextView
    private lateinit var geckoPoseView: GeckoPoseView

    private lateinit var geckoPoseDetection: GeckoPoseDetection

    private val inFrameLikelihoodThreshold: Float = 0.8f

    private val preferredPose: String = "left_pose"
    private val geckoPoseConfigurations = listOf(
        GeckoPoseConfiguration(
            tag = "left_pose",
            points = listOf(
                Point(PoseLandmark.LEFT_HIP),
                Point(PoseLandmark.LEFT_KNEE),
                Point(PoseLandmark.LEFT_ANKLE),
                Point(PoseLandmark.LEFT_SHOULDER),
                Point(PoseLandmark.LEFT_ELBOW),
                Point(PoseLandmark.LEFT_WRIST),
            ),
            lines = listOf(
                Line(start = PoseLandmark.LEFT_KNEE, end = PoseLandmark.LEFT_HIP, tag = "knee_hip"),
                Line(start = PoseLandmark.LEFT_ANKLE, end = PoseLandmark.LEFT_KNEE, tag = "knee_ankle"),
                Line(start = PoseLandmark.LEFT_HIP, end = PoseLandmark.LEFT_SHOULDER, tag = "hip_shoulder"),
                Line(start = PoseLandmark.LEFT_SHOULDER, end = PoseLandmark.LEFT_ELBOW, tag = "shoulder_elbow"),
                Line(start = PoseLandmark.LEFT_ELBOW, end = PoseLandmark.LEFT_WRIST, tag = "elbow_wrist"),
            ),
            angles = listOf(
                MinMaxAngle(startPointType = PoseLandmark.LEFT_HIP, middlePointType = PoseLandmark.LEFT_KNEE,
                    endPointType = PoseLandmark.LEFT_ANKLE, tag = "a", minAngle = 0f, maxAngle = 40f),
                Angle(startPointType = PoseLandmark.LEFT_KNEE, middlePointType = PoseLandmark.LEFT_HIP,
                    endPointType = PoseLandmark.LEFT_SHOULDER, tag = "b"),
                Angle(startPointType = PoseLandmark.LEFT_HIP, middlePointType = PoseLandmark.LEFT_SHOULDER,
                    endPointType = PoseLandmark.LEFT_ELBOW, tag = "c"),
                Angle(startPointType = PoseLandmark.LEFT_SHOULDER, middlePointType = PoseLandmark.LEFT_ELBOW,
                    endPointType = PoseLandmark.LEFT_WRIST, tag = "d"),
            ),
            defaultPointColorLight = R.color.white,
            defaultPointColorDark = R.color.black,
            defaultSelectedPointColor = R.color.red,
            defaultLineColor = R.color.blue,
            defaultAngleColor = R.color.color_angle_ok,
            defaultNOKAngleColor = R.color.color_angle_nok,
        ),
        GeckoPoseConfiguration(
            tag = "right_pose",
            points = listOf(
                Point(PoseLandmark.RIGHT_HIP),
                Point(PoseLandmark.RIGHT_KNEE),
                Point(PoseLandmark.RIGHT_ANKLE),
                Point(PoseLandmark.RIGHT_SHOULDER),
                Point(PoseLandmark.RIGHT_ELBOW),
                Point(PoseLandmark.RIGHT_WRIST),
            ),
            lines = listOf(
                Line(start = PoseLandmark.RIGHT_KNEE, end = PoseLandmark.RIGHT_HIP, tag = "knee_hip"),
                Line(start = PoseLandmark.RIGHT_ANKLE, end = PoseLandmark.RIGHT_KNEE, tag = "knee_ankle"),
                Line(start = PoseLandmark.RIGHT_HIP, end = PoseLandmark.RIGHT_SHOULDER, tag = "hip_shoulder"),
                Line(start = PoseLandmark.RIGHT_SHOULDER, end = PoseLandmark.RIGHT_ELBOW, tag = "shoulder_elbow"),
                Line(start = PoseLandmark.RIGHT_ELBOW, end = PoseLandmark.RIGHT_WRIST, tag = "elbow_wrist"),
            ),
            angles = listOf(
                MinMaxAngle(startPointType = PoseLandmark.RIGHT_HIP, middlePointType = PoseLandmark.RIGHT_KNEE,
                    endPointType = PoseLandmark.RIGHT_ANKLE, tag = "a", minAngle = 0f, maxAngle = 40f),
                Angle(startPointType = PoseLandmark.RIGHT_KNEE, middlePointType = PoseLandmark.RIGHT_HIP,
                    endPointType = PoseLandmark.RIGHT_SHOULDER, tag = "b"),
                Angle(startPointType = PoseLandmark.RIGHT_HIP, middlePointType = PoseLandmark.RIGHT_SHOULDER,
                    endPointType = PoseLandmark.RIGHT_ELBOW, tag = "c"),
                Angle(startPointType = PoseLandmark.RIGHT_SHOULDER, middlePointType = PoseLandmark.RIGHT_ELBOW,
                    endPointType = PoseLandmark.RIGHT_WRIST, tag = "d"),
            ),
            defaultPointColorLight = R.color.white,
            defaultPointColorDark = R.color.black,
            defaultSelectedPointColor = R.color.red,
            defaultLineColor = R.color.blue,
            defaultAngleColor = R.color.color_angle_ok,
            defaultNOKAngleColor = R.color.color_angle_nok,
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
        geckoPoseView.setOnPoseChangedListener(object : GeckoPoseView.OnPoseChangedListener {
            override fun onPoseChanged(pose: GeckoPose) {
                geckoPoseView.pose?.let { updateAngleTexts(it) }
            }
        })

        fabImageChooser = view.findViewById(R.id.fab_image_chooser)

        fabImageChooser.setOnClickListener {

            val photoFile = File.createTempFile(
                "IMG_",
                ".jpg",
                requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            )

            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                photoFile
            )

            openImagePicker(uri)
        }

        geckoPoseDetection = GeckoPoseDetection(
            detectorMode = AccuratePoseDetectorOptions.SINGLE_IMAGE_MODE,
            configurations = geckoPoseConfigurations,
            listener = object : GeckoPoseDetectionListener {
                override fun onSuccess(geckoPoses: List<GeckoPose?>) {
                    val preferred = geckoPoses.getByTag(preferredPose)
                    val best = geckoPoses.getBest(inFrameLikelihoodThreshold)

                    val pose: GeckoPose? = if(preferred != null) {
                        preferred
                    }  else if(best != null){
                        best
                    } else {
                        Toast.makeText(requireContext(), "onSuccess but none found", Toast.LENGTH_SHORT).show()
                        null
                    }

                    if(pose != null) {
                        geckoPoseView.pose = pose
                        onPoseSet(pose)
                    }
                }

                override fun onCompletedWithoutSuccess() {
                    Toast.makeText(requireContext(), "onCompletedWithoutSuccess", Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(exception: Exception) {
                    Toast.makeText(requireContext(), "onFailure", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    override fun onPictureReceived(uri: Uri) {
        setPoseViewPicture(BitmapUtil.getBitmap(uri, requireContext()))
    }

    override fun onTakePictureFailed() {
        TODO("Not yet implemented")
    }

    override fun onChoosePictureFailed() {
        TODO("Not yet implemented")
    }

    private fun setPoseViewPicture(bitmap: Bitmap) {
        geckoPoseView.post {
            val scaledBitmap = BitmapUtil.resize(
                image = bitmap,
                maxWidth = geckoPoseView.width,
                maxHeight = geckoPoseView.height
            )

            geckoPoseView.bitmap = scaledBitmap
            geckoPoseDetection.processImage(scaledBitmap)

            onPictureSet()
        }
    }

    private fun onPictureSet() {
        fabImageChooser.visibility = View.GONE

        txtAngleA.visibility = View.VISIBLE
        txtAngleB.visibility = View.VISIBLE
    }

    private fun onPoseSet(pose: GeckoPose) {
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