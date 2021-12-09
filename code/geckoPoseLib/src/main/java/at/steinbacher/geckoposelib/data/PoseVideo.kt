package at.steinbacher.geckoposelib.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.lang.Exception
import kotlin.math.abs

@Serializable
data class PoseVideo(val uri: String, val timestampSteps: Int, val poseFrames: List<PoseFrame>) {
    fun toNormalizedPoseVideo() = NormalizedPoseVideo(
        uri = uri,
        timestampSteps = timestampSteps,
        normalizedPoseFrames = poseFrames.map { it.toNormalizedPoseFrame() }
    )
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
data class NormalizedPoseVideo(val uri: String, val timestampSteps: Int, val normalizedPoseFrames: List<NormalizedPoseFrame>) {
    fun getAngles(angleTag: String): List<Angle> = normalizedPoseFrames.mapNotNull { it.normalizedPose?.getAngle(angleTag) }

    fun getScaledAngles(angleTag: String, verticalScale: Int) = getAngles(angleTag)
        .map { it.value.toFloat() }
        .scale(verticalScale)

    fun getByTimestamp(timestamp: Long): NormalizedPoseFrame
        = normalizedPoseFrames.find { it.timestamp == timestamp } ?: error("No frame with $timestamp found!")

    fun getClosestFrame(timestamp: Long): NormalizedPoseFrame = normalizedPoseFrames.minByOrNull {
        abs(timestamp - it.timestamp)
    } ?: error("PoseVideo has no Frames!")

    private fun List<Float>.scale(sampleSize: Int): List<Float> {
        if(this.size < sampleSize) {
            throw Exception("Sample size > line size! $sampleSize - ${this.size}")
        }

        val sampleSteps = this.size / sampleSize.toFloat()

        var currentScaledPointsIndex = 0
        val scaledPoints = ArrayList<Float>()
        for(i in 0 until sampleSize) {
            scaledPoints.add(0f)
        }

        val currentSampleTargets = ArrayList<Float>()
        var currentStepLimit = sampleSteps
        for(i in this.indices) {
            if(i < currentStepLimit) {
                currentSampleTargets.add(this[i])
            } else {
                scaledPoints[currentScaledPointsIndex] = currentSampleTargets.average().toFloat()
                currentScaledPointsIndex++
                currentStepLimit += sampleSteps
                currentSampleTargets.clear()
                currentSampleTargets.add(this[i])
            }
        }
        scaledPoints[sampleSize-1] = currentSampleTargets.average().toFloat()

        return scaledPoints.toList()
    }
}

@Serializable
data class NormalizedPoseFrame(val timestamp: Long, val normalizedPose: NormalizedGeckoPose?, var poseMark: String?)

