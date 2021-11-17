package at.steinbacher.geckoposelib.view

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.session.PlaybackState
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.TextureView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import at.steinbacher.geckoposelib.*
import at.steinbacher.geckoposelib.data.GeckoPose
import at.steinbacher.geckoposelib.data.PoseFrame
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GeckoVideoExtractionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    var video: Uri? = null
        set(value) {
            field = value

            if(value != null) {
                initVideo(value)
            }
        }

    var poseFrames: ArrayList<PoseFrame> = ArrayList()

    var currentFramePose: GeckoPose? = null
        set(value) {
            field = value
            skeletonView.pose = field
        }

    var seekForwardStepsMs = 1000

    lateinit var poseDetection: SingleImagePoseDetection

    var choosePoseLogic: ChoosePoseLogic = { null }

    var drawLines
        set(value) { skeletonView.drawLines = value }
        get() = skeletonView.drawLines

    var drawAngles
        set(value) { skeletonView.drawAngles = value }
        get() = skeletonView.drawAngles

    var isEditable: Boolean
        get() = skeletonView.isClickable
        set(value) {
            skeletonView.isClickable = value
        }

    private var previousFramePose: GeckoPose? = null

    private var currentPoseMark: String? = null

    private val playerView: PlayerView
    private val skeletonView: SkeletonView

    private var player: ExoPlayer? = null

    private var currentSeek = 0L
    private var currentFrame: Bitmap? = null
        set(value) {
            field = value
            skeletonView.bitmap = currentFrame
        }

    private var totalVideoLength: Long = 0

    interface VideoExtractionListener {
        fun onFrameSet(frame: Bitmap, pose: GeckoPose)
        fun onPoseNotRecognized(frame: Bitmap, previousPose: GeckoPose?)
        fun onProgress(percentage: Int)
        fun onFinishedEnd(poseFrames: List<PoseFrame>)
    }
    private var videoExtractionListener: VideoExtractionListener? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_video_extraction, this, true)

        playerView = findViewById(R.id.player_view)
        skeletonView = findViewById(R.id.skeleton_view)

        isEditable = false
    }

    fun setVideoExtractionListener(listener: VideoExtractionListener) {
        videoExtractionListener = listener
    }

    fun seekForward() {
        skeletonView.pose = null

        poseFrames.add(PoseFrame(geckoPose = currentFramePose, timestamp = currentSeek, poseMark = currentPoseMark))

        currentSeek += seekForwardStepsMs
        player?.seekTo(currentSeek)
    }

    fun canSeekForward() = currentSeek <= player?.duration ?: 0

    fun markCurrentPoseFrame(poseMark: String) {
        currentPoseMark = poseMark
    }

    private fun initVideo(uri: Uri) {
        val mediaMetadataReceiver = MediaMetadataRetriever()
        mediaMetadataReceiver.setDataSource(context, uri)
        totalVideoLength = mediaMetadataReceiver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0

        player = ExoPlayer.Builder(context).build()
        val mediaItem = MediaItem.fromUri(uri)

        playerView.player = player
        player?.let {
            it.addMediaItem(mediaItem)
            it.prepare()

            it.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)

                    if(playbackState == PlaybackState.STATE_PLAYING) {
                        playerView.getCurrentFrame()?.let { frame ->
                            skeletonView.updateLayoutParams {
                                this.width = frame.width
                                this.height = frame.height
                            }

                            currentFrame = frame

                            val progress = ((currentSeek / totalVideoLength.toFloat()) * 100)
                            videoExtractionListener?.onProgress(if(progress.toInt() < 100) progress.toInt() else 100)

                            onSeekCompleted(frame)
                        }
                    }
                }
            })
        }
    }

    private fun onSeekCompleted(frame: Bitmap) {
        CoroutineScope(Dispatchers.IO).launch {
            val poses = poseDetection.processImage(frame)

            if(poses != null) {
                val pose = choosePoseLogic.invoke(poses)

                withContext(Dispatchers.Main) {
                    if(currentFramePose != null) {
                        previousFramePose = currentFramePose
                    }

                    currentFramePose = pose

                    when {
                        pose == null -> {
                            videoExtractionListener?.onPoseNotRecognized(frame, previousFramePose)
                        }
                        canSeekForward() -> {
                            videoExtractionListener?.onFrameSet(frame, pose)
                        }
                        else -> {
                            videoExtractionListener?.onFinishedEnd(poseFrames)
                        }
                    }
                }
            }
        }
    }

    private fun PlayerView.getCurrentFrame(): Bitmap? = (this.videoSurfaceView as TextureView).bitmap
}