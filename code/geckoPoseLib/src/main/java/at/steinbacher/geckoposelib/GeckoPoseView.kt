package at.steinbacher.geckoposelib

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import at.steinbacher.geckoposelib.util.AngleUtil
import at.steinbacher.geckoposelib.util.BitmapUtil
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt


class GeckoPoseView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    var bitmap: Bitmap? = null
        set(value) {
            field = value
            imageView.setImageBitmap(field)
            skeletonView.bitmap = field

            field?.let {
                skeletonView.updateLayoutParams {
                    width = it.width
                    height = it.height
                }
            }
        }

    var pose: GeckoPose? = null
        set(value) {
            val poseWasNotNull = pose != null

            field = value
            skeletonView.pose = field

            if(poseWasNotNull) {
                field?.let { onPoseChangedListener?.onPoseChanged(it) }
            }
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

    private val skeletonView: SkeletonView
    private val imageView: ImageView

    interface OnPoseChangedListener {
        fun onPoseChanged(pose: GeckoPose)
    }
    private var onPoseChangedListener: OnPoseChangedListener? = null

    interface OnPointEditListener {
        fun onPointEditStarted()
    }
    private var onPointEditListener: OnPointEditListener? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_pose, this, true)

        imageView = findViewById(R.id.image_view)

        skeletonView = findViewById(R.id.skeleton_view)
        skeletonView.setSkeletonViewListener(object : SkeletonView.SkeletonViewListener {
            override fun onPointSelected() {
                onPointEditListener?.onPointEditStarted()
            }

            override fun onPointChanged(type: Int) {
                pose?.let { onPoseChangedListener?.onPoseChanged(it) }
            }
        })
    }

    fun setOnPoseChangedListener(listener: OnPoseChangedListener) {
        onPoseChangedListener = listener
    }

    fun saveEdit() {
        skeletonView.saveSelectedPoint()
    }
}