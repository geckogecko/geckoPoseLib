package at.steinbacher.geckoposelib.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class PoseVideo(val uri: String, val poseFrames: List<PoseFrame>) {
    fun getFirstNotNullPose(): OnImagePose = poseFrames.first { it.onImagePose != null }.onImagePose ?: throw Exception("All Poses are null")
}

@Serializable
data class PoseFrame(val timestamp: Long, val onImagePose: OnImagePose?, var poseMark: String?)