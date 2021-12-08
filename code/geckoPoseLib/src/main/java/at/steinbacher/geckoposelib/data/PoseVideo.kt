package at.steinbacher.geckoposelib.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class PoseVideo(val uri: String, val poseFrames: List<PoseFrame>)

@Serializable
data class PoseFrame(val timestamp: Long, val pose: GeckoPose?, var poseMark: String?)



@Serializable
data class NormalizedPoseVideo(val uri: String, val normalizedPoseFrames: List<NormalizedPoseFrame>) {
    fun getAngles(angleTag: String): List<Angle> = normalizedPoseFrames.mapNotNull { it.normalizedPose?.getAngle(angleTag) }
}

@Serializable
data class NormalizedPoseFrame(val timestamp: Long, val normalizedPose: NormalizedGeckoPose?, var poseMark: String?)

