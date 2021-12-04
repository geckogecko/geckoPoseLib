package at.steinbacher.geckoposelib.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import at.steinbacher.geckoposelib.R
import at.steinbacher.geckoposelib.data.GeckoPose
import at.steinbacher.geckoposelib.data.GeckoPoseDrawConfiguration
import at.steinbacher.geckoposelib.data.PointF

class GeckoPoseComparisonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var firstScaledPose: GeckoPose? = null
        set(value) {
            field = value
            firstSkeletonView.pose = value
        }

    private var firstPoseDrawConfiguration: GeckoPoseDrawConfiguration? = null
        set(value) {
            field = value
            firstSkeletonView.poseDrawConfiguration = field
        }

    private var secondScaledPose: GeckoPose? = null
        set(value) {
            field = value
            secondSkeletonView.pose = value
        }

    private var secondPoseDrawConfiguration: GeckoPoseDrawConfiguration? = null
        set(value) {
            field = value
            secondSkeletonView.poseDrawConfiguration = field
        }



    private val firstSkeletonView: SkeletonView
    private val secondSkeletonView: SkeletonView

    private val viewCenter: PointF
        get() = PointF((width / 2).toFloat(), (height / 2).toFloat())

    init {
        LayoutInflater.from(context).inflate(R.layout.view_pose_comparison, this, true)

        firstSkeletonView = findViewById(R.id.first_skeleton_view)
        secondSkeletonView = findViewById(R.id.second_skeleton_view)

        firstSkeletonView.apply {
            this.drawLines = true
            this.drawAngles = false
            this.isClickable = false
        }

        secondSkeletonView.apply {
            this.drawLines = true
            this.drawAngles = false
            this.isClickable = false
        }
    }

    fun setNormalizedPoses(first: GeckoPose, firstConfiguration: GeckoPoseDrawConfiguration,
                           second: GeckoPose, secondConfiguration: GeckoPoseDrawConfiguration
    ) {
        firstScaledPose = first.scaleToView().moveToViewCenter()
        firstPoseDrawConfiguration = firstConfiguration

        secondScaledPose = second.scaleToView().moveToViewCenter()
        secondPoseDrawConfiguration = secondConfiguration
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