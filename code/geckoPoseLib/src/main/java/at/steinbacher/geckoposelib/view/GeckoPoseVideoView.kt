package at.steinbacher.geckoposelib.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import at.steinbacher.geckoposelib.R
import at.steinbacher.geckoposelib.data.GeckoPose
import at.steinbacher.geckoposelib.data.NormalizedPoseVideo
import at.steinbacher.geckoposelib.data.PointF
import kotlin.math.roundToInt

class GeckoPoseVideoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    var normalizedPoseVideo: NormalizedPoseVideo? = null
        set(value) {
            field = value

            if(value != null) {
                poseVideoSet(value)
            }
        }

    private val skeletonView: SkeletonView
    private val seekbar: SeekBar

    private val viewCenter: PointF
        get() = PointF((width / 2).toFloat(), (height / 2).toFloat())

    init {
        LayoutInflater.from(context).inflate(R.layout.view_pose_video, this, true)

        skeletonView = findViewById(R.id.skeleton_view)
        seekbar = findViewById(R.id.seek_bar)

        context.theme.obtainStyledAttributes(attrs, R.styleable.GeckoPoseView, 0, 0).apply {
            try {
                skeletonView.defaultPointColorLight = getColor(R.styleable.GeckoPoseView_defaultPointColorLight, ContextCompat.getColor(context, R.color.white))
                skeletonView.defaultPointColorDark = getColor(R.styleable.GeckoPoseView_defaultPointColorDark, ContextCompat.getColor(context, R.color.black))
                skeletonView.defaultSelectedPointColor = getColor(R.styleable.GeckoPoseView_defaultSelectedPointColor, ContextCompat.getColor(context, R.color.red))
                skeletonView.defaultLineColor = getColor(R.styleable.GeckoPoseView_defaultLineColor, ContextCompat.getColor(context, R.color.blue))
                skeletonView.defaultAngleColor = getColor(R.styleable.GeckoPoseView_defaultAngleColor, ContextCompat.getColor(context, R.color.green))
            } finally {
                recycle()
            }
        }

        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                normalizedPoseVideo?.let {
                    skeletonView.pose = it.getClosestFrame(p1.toLong()).normalizedPose?.toGeckoPose()?.scaleToView()?.moveToViewCenter()
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
    }

    private fun poseVideoSet(poseVideo: NormalizedPoseVideo) {
        seekbar.max = poseVideo.normalizedPoseFrames.last().timestamp.toInt()
        seekbar.incrementProgressBy(poseVideo.timestampSteps)
    }

    private fun GeckoPose.scaleToView(): GeckoPose {
        val scaleWidthFactor = width / 100
        val scaleHeightFactor = height / 100
        val minScaleFactor = listOf(scaleWidthFactor, scaleHeightFactor).minOrNull()!!.toFloat()

        return this.copyScale(minScaleFactor, minScaleFactor)
    }

    private fun GeckoPose.moveToViewCenter(): GeckoPose {
        val middleDistanceX = (viewCenter.x - this.poseCenterPoint.x).toInt()
        val middleDistanceY = (viewCenter.y - this.poseCenterPoint.y).toInt()

        return this.copyMove(middleDistanceX, middleDistanceY)
    }
}