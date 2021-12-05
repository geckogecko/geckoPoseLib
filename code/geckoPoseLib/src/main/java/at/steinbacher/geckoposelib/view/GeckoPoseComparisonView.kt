package at.steinbacher.geckoposelib.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import at.steinbacher.geckoposelib.R
import at.steinbacher.geckoposelib.data.GeckoPose
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

    private var secondScaledPose: GeckoPose? = null
        set(value) {
            field = value
            secondSkeletonView.pose = value
        }

    private val firstSkeletonView: SkeletonView
    private val secondSkeletonView: SkeletonView

    private val viewCenter: PointF
        get() = PointF((width / 2).toFloat(), (height / 2).toFloat())

    init {
        LayoutInflater.from(context).inflate(R.layout.view_pose_comparison, this, true)

        firstSkeletonView = findViewById(R.id.first_skeleton_view)
        secondSkeletonView = findViewById(R.id.second_skeleton_view)

        context.theme.obtainStyledAttributes(attrs, R.styleable.GeckoPoseComparisonView, 0, 0).apply {
            try {
                firstSkeletonView.defaultPointColorLight = getColor(R.styleable.GeckoPoseComparisonView_mainDefaultPointColor,
                    ContextCompat.getColor(context, R.color.black))
                secondSkeletonView.defaultPointColorLight = getColor(R.styleable.GeckoPoseComparisonView_secondDefaultPointColor,
                    ContextCompat.getColor(context, R.color.black))

                firstSkeletonView.defaultLineColor= getColor(R.styleable.GeckoPoseComparisonView_mainDefaultLineColor,
                    ContextCompat.getColor(context, R.color.blue))
                secondSkeletonView.defaultPointColorDark = getColor(R.styleable.GeckoPoseComparisonView_secondDefaultLineColor,
                    ContextCompat.getColor(context, R.color.blue))

                firstSkeletonView.defaultAngleColor= getColor(R.styleable.GeckoPoseComparisonView_mainDefaultAngleColor,
                    ContextCompat.getColor(context, R.color.green))
                secondSkeletonView.defaultAngleColor = getColor(R.styleable.GeckoPoseComparisonView_secondDefaultAngleColor,
                    ContextCompat.getColor(context, R.color.green))
            } finally {
                recycle()
            }
        }
    }

    fun setNormalizedPoses(first: GeckoPose, second: GeckoPose) {
        firstScaledPose = first.scaleToView().moveToViewCenter()
        secondScaledPose = second.scaleToView().moveToViewCenter()
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