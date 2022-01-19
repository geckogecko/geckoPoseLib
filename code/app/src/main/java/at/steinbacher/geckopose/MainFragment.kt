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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.fragment.app.viewModels
import at.steinbacher.geckoposelib.*
import at.steinbacher.geckoposelib.data.*
import at.steinbacher.geckoposelib.fragment.ImageVideoSelectionFragment
import at.steinbacher.geckoposelib.repository.GeckoPoseDetectionRepository
import at.steinbacher.geckoposelib.repository.IGeckoPoseDetectionRepository
import at.steinbacher.geckoposelib.util.BitmapUtil
import at.steinbacher.geckoposelib.view.GeckoLineChart
import at.steinbacher.geckoposelib.view.GeckoPoseView
import at.steinbacher.geckoposelib.view.GeckoVideoExtractionView
import at.steinbacher.geckoposelib.viewmodel.GeckoVideoExtractionViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.*
import java.io.File

class MainFragment : ImageVideoSelectionFragment() {

    private lateinit var fabImageChooser: FloatingActionButton
    private lateinit var fabVideoChooser: FloatingActionButton
    private lateinit var fabSeekTo: FloatingActionButton
    private lateinit var fabSeekBack: FloatingActionButton
    private lateinit var txtAngleA: TextView
    private lateinit var txtAngleB: TextView
    private lateinit var geckoPoseView: GeckoPoseView
    private lateinit var geckoLineChart: GeckoLineChart
    private lateinit var videoExtractionView: GeckoVideoExtractionView

    private lateinit var viewModel: VideoExtractionViewModel

    private lateinit var singleImagePoseDetection: SingleImagePoseDetection

    private val inFrameLikelihoodThreshold: Float = 0.8f

    private val choosePoseLogic: ChoosePoseLogic = { onImagePoses ->
        val preferred = onImagePoses.getByTag(preferredPose)
        val best = onImagePoses.getBest(inFrameLikelihoodThreshold)

        if(preferred != null) {
            preferred
        }  else if(best != null){
            best
        } else {
            null
        }
    }

    private val manipulatePoseLogic: ManipulatePoseLogic = { bitmap: Bitmap, onImagePose: GeckoPose ->
        /*
        BitmapPoseUtil.cropToPose(bitmap, onImagePose.pose, 0.2f)
            .scale(geckoPoseView.width, geckoPoseView.height)

         */

        Pair(bitmap, onImagePose)
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

        videoExtractionView = view.findViewById(R.id.video_extraction_view)

        val singleImagePoseDetection = SingleImagePoseDetection(
            configurations = tennisConfiguration,
        )

        val repository = GeckoPoseDetectionRepository(singleImagePoseDetection)
        val vm: VideoExtractionViewModel by viewModels {
            VideoExtractionViewModelFactory(repository)
        }
        viewModel = vm

        fabSeekTo = view.findViewById(R.id.fab_seek_to)
        fabSeekBack = view.findViewById(R.id.fab_seek_back)

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
        fabSeekBack.visibility = View.VISIBLE

        viewModel.choosePoseLogic = choosePoseLogic

        videoExtractionView.setVideoExtractionListener(object : GeckoVideoExtractionView.VideoExtractionListener {
            override fun onVideoDurationReceived(duration: Long) {
                viewModel.videoDuration = duration
            }

            override fun onVideoSet() {
                viewModel.onVideoSet()
            }

            override fun onSeekCompleted(timestamp: Long, frame: Bitmap) {
                viewModel.onSeekCompleted(timestamp, frame)
            }
        })

        viewModel.currentSeek.observe(this, { newSeek ->
            videoExtractionView.seekTo(newSeek)
        })

        viewModel.currentFrame.observe(this, { newFrame ->
            videoExtractionView.currentFrame = newFrame
        })

        viewModel.currentPose.observe(this, { newPose ->
            videoExtractionView.currentPose = newPose
        })

        viewModel.canSeekBackward.observe(this, { canSeekBackward ->
            fabSeekBack.isEnabled = canSeekBackward
        })

        viewModel.canSeekForward.observe(this, { canSeekForward ->
            fabSeekTo.isEnabled = canSeekForward
        })

        videoExtractionView.video = uri

        fabSeekTo.setOnClickListener {
            fabSeekTo.isEnabled = false
            viewModel.seekForward()
        }

        fabSeekBack.setOnClickListener {
            fabSeekBack.isEnabled = false
            viewModel.seekBackward()
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
        pose.configuration.angleConfigurations.forEach {
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

class VideoExtractionViewModel(private val repository: IGeckoPoseDetectionRepository): GeckoVideoExtractionViewModel(repository) {

}

class VideoExtractionViewModelFactory(private val repository: IGeckoPoseDetectionRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VideoExtractionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VideoExtractionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}