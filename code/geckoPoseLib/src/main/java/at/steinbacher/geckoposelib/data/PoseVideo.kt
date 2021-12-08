package at.steinbacher.geckoposelib.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class PoseVideo(val uri: String, val poseFrames: List<PoseFrame>) {
    fun toNormalizedPoseVideo() = NormalizedPoseVideo(uri = uri, normalizedPoseFrames = poseFrames.map { it.toNormalizedPoseFrame() })
}

@Serializable
data class PoseFrame(val timestamp: Long, val pose: GeckoPose?, var poseMark: String?) {
    fun toNormalizedPoseFrame() = NormalizedPoseFrame(
        timestamp = timestamp,
        normalizedPose = pose?.getNormalizedPose(),
        poseMark = poseMark
    )
}



@Serializable
data class NormalizedPoseVideo(val uri: String, val normalizedPoseFrames: List<NormalizedPoseFrame>) {
    fun getAngles(angleTag: String): List<Angle> = normalizedPoseFrames.mapNotNull { it.normalizedPose?.getAngle(angleTag) }
}

@Serializable
data class NormalizedPoseFrame(val timestamp: Long, val normalizedPose: NormalizedGeckoPose?, var poseMark: String?)

