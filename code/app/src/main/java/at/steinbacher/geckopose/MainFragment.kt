package at.steinbacher.geckopose

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.FileProvider
import at.steinbacher.geckoposelib.*
import at.steinbacher.geckoposelib.fragment.ImageVideoSelectionFragment
import at.steinbacher.geckoposelib.util.BitmapPoseUtil
import at.steinbacher.geckoposelib.util.BitmapPoseUtil.scale
import at.steinbacher.geckoposelib.util.BitmapUtil
import at.steinbacher.geckoposelib.view.GeckoPoseView
import at.steinbacher.geckoposelib.view.GeckoVideoExtractionView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainFragment : ImageVideoSelectionFragment() {

    private lateinit var fabImageChooser: FloatingActionButton
    private lateinit var fabVideoChooser: FloatingActionButton
    private lateinit var fabSeekTo: FloatingActionButton
    private lateinit var txtAngleA: TextView
    private lateinit var txtAngleB: TextView
    private lateinit var geckoPoseView: GeckoPoseView
    private lateinit var videoExtractionView: GeckoVideoExtractionView

    private lateinit var singleImagePoseDetection: SingleImagePoseDetection

    private val inFrameLikelihoodThreshold: Float = 0.8f

    private val choosePoseLogic: ChoosePoseLogic = { geckoPoses ->
        val preferred = geckoPoses.getByTag(preferredPose)
        val best = geckoPoses.getBest(inFrameLikelihoodThreshold)

        if(preferred != null) {
            preferred
        }  else if(best != null){
            best
        } else {
            null
        }
    }

    private val manipulatePoseLogic: ManipulatePoseLogic = { bitmap: Bitmap, pose: GeckoPose ->
        BitmapPoseUtil.cropToPose(bitmap, pose, 0.2f)
            .scale(geckoPoseView.width, geckoPoseView.height)

        Pair(bitmap, pose)
    }

    private val preferredPose: String = "right_pose"

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

        fabSeekTo = view.findViewById(R.id.fab_seek_to)

        fabImageChooser = view.findViewById(R.id.fab_image_chooser)
        fabImageChooser.setOnClickListener {
            val photoFile = File.createTempFile(
                "IMAGE_",
                ".jpeg",
                requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            )

            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                photoFile
            )

            openImagePicker(uri)
        }

        fabVideoChooser = view.findViewById(R.id.fab_video_chooser)
        fabVideoChooser.setOnClickListener {
            val photoFile = File.createTempFile(
                "VIDEO_",
                ".mp4",
                requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            )

            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                photoFile
            )

            openVideoPicker(uri)
        }

        singleImagePoseDetection = SingleImagePoseDetection(
            configurations = tennisConfiguration,
        )

        videoExtractionView = view.findViewById(R.id.video_extraction_view)
        videoExtractionView.choosePoseLogic = choosePoseLogic
        videoExtractionView.poseDetection = singleImagePoseDetection
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

    override fun onVideoReceived(uri: Uri) {
        fabImageChooser.visibility = View.GONE
        fabVideoChooser.visibility = View.GONE

        fabSeekTo.visibility = View.VISIBLE

        videoExtractionView.video = uri
        videoExtractionView.setVideoExtractionListener(object : GeckoVideoExtractionView.VideoExtractionListener {
            override fun onFrameSet(frame: Bitmap, pose: GeckoPose) {
                fabSeekTo.isEnabled = true
            }

            override fun onPoseNotRecognized(frame: Bitmap, previousPose: GeckoPose?) {

            }

            override fun onProgress(percentage: Int) {

            }

            override fun onFinishedEnd(poseFrames: List<PoseFrame>) {
            }
        })

        fabSeekTo.setOnClickListener {
            fabSeekTo.isEnabled = false
            videoExtractionView.seekForward()
        }
    }

    override fun onTakeVideoFailed() {
        TODO("Not yet implemented")
    }

    override fun onChooseVideoFailed() {
        TODO("Not yet implemented")
    }

    private fun setPoseViewPicture(bitmap: Bitmap) {
        CoroutineScope(Dispatchers.IO).launch {
            val poses = singleImagePoseDetection.processImage(bitmap)
            val pose = poses?.let { choosePoseLogic.invoke(it) }
            if(pose != null) {
                val (manipulatedBitmap, manipulatedPose) = manipulatePoseLogic.invoke(bitmap, pose)

                withContext(Dispatchers.Main) {
                    geckoPoseView.bitmap = manipulatedBitmap
                    onPictureSet()


                    geckoPoseView.pose = manipulatedPose
                    onPoseSet(manipulatedPose)
                }
            }
        }
    }

    private fun onPictureSet() {
        fabImageChooser.visibility = View.GONE
        fabVideoChooser.visibility = View.GONE

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

    companion object {
        private const val TAG = "MainFragment"
    }
}