package at.steinbacher.geckoposelib.repository

import android.graphics.Bitmap
import at.steinbacher.geckoposelib.data.GeckoPose
import at.steinbacher.geckoposelib.v2.SingleImagePoseDetection


interface IGeckoPoseDetectionRepository {
    suspend fun processImage(frame: Bitmap): List<GeckoPose?>?
}

class GeckoPoseDetectionRepository(private val singleImagePoseDetection: SingleImagePoseDetection): IGeckoPoseDetectionRepository {
    override suspend fun processImage(frame: Bitmap): List<GeckoPose?>? = singleImagePoseDetection.processImage(frame)
}