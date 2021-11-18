package at.steinbacher.geckoposelib.data

import at.steinbacher.geckoposelib.room.PoseVideoEntity
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class PoseVideo(val uri: String, val poseFrames: List<PoseFrame>) {
    fun getFirstNotNullPose(): GeckoPose? = poseFrames.first { it.geckoPose != null }.geckoPose

    fun toEntity(timestamp: Long, isUserData: Boolean, tag: String? = null): PoseVideoEntity = PoseVideoEntity(
        poseVideo = this,
        timestamp = timestamp,
        isUserData = isUserData,
        tag = tag
    )
}

@Serializable
data class PoseFrame(val timestamp: Long, val geckoPose: GeckoPose?, var poseMark: String?)