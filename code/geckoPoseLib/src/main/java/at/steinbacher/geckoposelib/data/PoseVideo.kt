package at.steinbacher.geckoposelib.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class PoseVideo(val uri: String, val poseFrames: List<PoseFrame>) {
    val normalizedPoses: List<NormalizedPoseFrame> = poseFrames.map {
        NormalizedPoseFrame(timestamp = it.timestamp, geckoPose = it.onImagePose?.normalizedPose, poseMark = it.poseMark)
    }

    val firstNotNullNormalizedPose: GeckoPose = normalizedPoses.find { it.geckoPose != null }?.geckoPose ?: error("All poses are null")
}

@Serializable
data class PoseFrame(val timestamp: Long, val onImagePose: OnImagePose?, var poseMark: String?)

@Serializable
data class NormalizedPoseFrame(val timestamp: Long, val geckoPose: GeckoPose?, var poseMark: String?)

