package at.steinbacher.geckoposelib.util

import android.graphics.Bitmap
import at.steinbacher.geckoposelib.data.GeckoPose

object BitmapOnImagePoseUtil {
    /*
    fun cropToPose(bitmap: Bitmap, onImagePose: OnImagePose, marginPercentage: Float): Pair<Bitmap, OnImagePose> {
        val (croppedToPoseBitmap, cropX, cropY) = cropPose(bitmap, onImagePose, marginPercentage)
       onImagePose.pose.move(-cropX, -cropY)

        return Pair(croppedToPoseBitmap, onImagePose)
    }

    fun Pair<Bitmap, GeckoPose>.scale(maxWidth: Int, maxHeight: Int): Pair<Bitmap, GeckoPose> {
        val (scaledBitmap, scaleX, scaleY) = BitmapUtil.resize(
            image = this.first,
            maxWidth = maxWidth,
            maxHeight = maxHeight
        )

        val scaledPose = this.second.copyScale(scaleX, scaleY)

        return Pair(scaledBitmap, scaledPose)
    }


    private fun cropPose(bitmap: Bitmap, onImagePose: OnImagePose, marginPercentage: Float): Triple<Bitmap, Int, Int> {
        var topMost: Float = Float.MAX_VALUE
        var leftMost: Float = Float.MAX_VALUE
        var rightMost: Float = 0.0f
        var bottomMost: Float = 0.0f

        onImagePose.pose.landmarkPoints.forEach {
            if(it.position.y < topMost) {
                topMost = it.position.y
            }

            if(it.position.y > bottomMost) {
                bottomMost = it.position.y
            }

            if(it.position.x < leftMost) {
                leftMost = it.position.x
            }

            if(it.position.x > rightMost) {
                rightMost = it.position.x
            }
        }

        val width = rightMost - leftMost
        val height = bottomMost - topMost

        val borderWidth = width * marginPercentage
        val borderHeight = height * marginPercentage

        val croppedBitmap = Bitmap.createBitmap(
            bitmap,
            leftMost.toInt() - borderWidth.toInt(),
            topMost.toInt() - borderHeight.toInt(),
            width.toInt() + 2 * borderWidth.toInt(),
            height.toInt() + 2 * borderHeight.toInt()
        )

        return Triple(croppedBitmap, leftMost.toInt() - borderWidth.toInt(), topMost.toInt() - borderHeight.toInt())
    }
     */
}