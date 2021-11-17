package at.steinbacher.geckoposelib

import kotlinx.serialization.Serializable

@Serializable
data class PoseVideo(val uri: String, val poseFrames: List<PoseFrame>) {
    fun getFirstNotNullPose(): GeckoPose? = poseFrames.first { it.geckoPose != null }.geckoPose
}

@Serializable
data class PoseFrame(val timestamp: Long, val geckoPose: GeckoPose?, var poseMark: String?)