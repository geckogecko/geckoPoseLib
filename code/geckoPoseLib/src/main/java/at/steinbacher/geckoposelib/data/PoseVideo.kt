package at.steinbacher.geckoposelib.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.lang.Exception
import kotlin.math.abs

@Serializable
data class PoseVideo(
    val uri: String,
    val poses: List<PoseFrame>,
    val normalizedPoses: List<NormalizedPoseFrame>
)

@Serializable
data class PoseFrame(
    val frameNr: Int,
    val pose: GeckoPose
) {
    fun toNormalizedPoseFrame() = NormalizedPoseFrame(
        frameNr = this.frameNr,
        normalizedPose = pose.getNormalizedPose(),
    )
}

@Serializable
data class NormalizedPoseFrame(
    val frameNr: Int,
    val normalizedPose: NormalizedGeckoPose,
)


/*
@Serializable
data class NormalizedPoseVideo(val uri: String, val timestampSteps: Int, val normalizedPoseFrames: List<NormalizedPoseFrame>) {
    fun getAngles(angleTag: String): List<Angle> = normalizedPoseFrames.mapNotNull { it.normalizedPose?.getAngle(angleTag) }

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

 */

