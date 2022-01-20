package at.steinbacher.geckoposelib.view

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.session.PlaybackState
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.TextureView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import at.steinbacher.geckoposelib.*
import at.steinbacher.geckoposelib.data.GeckoPose
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView

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

    var currentPose: GeckoPose? = null
        set(value) {
            field = value
            skeletonView.pose = field
        }

    var currentFrame: Bitmap? = null
        set(value) {
            field = value
            skeletonView.bitmap = currentFrame
        }

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

    private val playerView: PlayerView
    private val skeletonView: SkeletonView

    private var currentTimestamp: Long = 0

    private var player: ExoPlayer? = null

    interface VideoExtractionListener {
        fun onVideoDurationReceived(duration: Long)
        fun onVideoSet()
        fun onSeekCompleted(timestamp: Long, frame: Bitmap)
    }
    private var videoExtractionListener: VideoExtractionListener? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_video_extraction, this, true)

        playerView = findViewById(R.id.player_view)
        skeletonView = findViewById(R.id.skeleton_view)

        context.theme.obtainStyledAttributes(attrs, R.styleable.GeckoPoseView, 0, 0).apply {
            try {
                skeletonView.defaultPointColorLight = getColor(R.styleable.GeckoPoseView_defaultPointColorLight, ContextCompat.getColor(context, R.color.white))
                skeletonView.defaultPointColorDark = getColor(R.styleable.GeckoPoseView_defaultPointColorDark, ContextCompat.getColor(context, R.color.black))
                skeletonView.defaultSelectedPointColor = getColor(R.styleable.GeckoPoseView_defaultSelectedPointColor, ContextCompat.getColor(context, R.color.red))
                skeletonView.defaultLineColor = getColor(R.styleable.GeckoPoseView_defaultLineColor, ContextCompat.getColor(context, R.color.blue))
                skeletonView.defaultAngleColor = getColor(R.styleable.GeckoPoseView_defaultAngleColor, ContextCompat.getColor(context, R.color.green))

                drawLines = getBoolean(R.styleable.GeckoPoseView_drawLines, true)
                drawAngles = getBoolean(R.styleable.GeckoPoseView_drawAngles, true)
                isEditable = getBoolean(R.styleable.GeckoPoseView_editable, false)
            } finally {
                recycle()
            }
        }
    }

    fun setVideoExtractionListener(listener: VideoExtractionListener) {
        videoExtractionListener = listener
    }

    fun seekTo(timestamp: Long) {
        currentTimestamp = timestamp
        player?.seekTo(timestamp)
    }

    private fun initVideo(uri: Uri) {
        val mediaMetadataReceiver = MediaMetadataRetriever()
        mediaMetadataReceiver.setDataSource(context, uri)

        val totalVideoLength = mediaMetadataReceiver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0
        videoExtractionListener?.onVideoDurationReceived(totalVideoLength)

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

                            videoExtractionListener?.onSeekCompleted(currentTimestamp, frame)
                        }
                    }
                }
            })

            videoExtractionListener?.onVideoSet()
        }
    }

    private fun PlayerView.getCurrentFrame(): Bitmap? = (this.videoSurfaceView as TextureView).bitmap
}