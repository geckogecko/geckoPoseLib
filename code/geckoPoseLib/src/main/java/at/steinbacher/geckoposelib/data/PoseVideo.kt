package at.steinbacher.geckoposelib.data

import at.steinbacher.geckoposelib.util.AngleUtil
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.lang.Exception
import kotlin.math.abs

@Serializable
data class PoseVideo(
    val uri: String,
    val poses: List<PoseFrame>,
    val normalizedPoses: List<NormalizedPoseFrame>
) {
    suspend fun getSmoothPoses(
        periods: Int,
    ): List<NormalizedPoseFrame> {
        return normalizedPoses.windowed(size = periods, step = 1, partialWindows = true)
            .map {
                //smoothPoints
                val smoothPoints = it.first().normalizedPose.configuration.pointConfigurations.map { pointConfiguration ->
                    val averagePosition = it.map { normalizedPoseFrame ->
                        normalizedPoseFrame.normalizedPose.getPoint(pointConfiguration.type).position
                    }.average()

                    Point(
                        position = averagePosition,
                        pointConfiguration = pointConfiguration,
                        inFrameLikelihood = it.first().normalizedPose.getPoint(pointConfiguration.type).inFrameLikelihood
                    )
                }

                //newAngles
                val angles = it.first().normalizedPose.configuration.angleConfigurations.map { angleConfiguration ->
                    Angle(
                        tag = angleConfiguration.tag,
                        startPointType = angleConfiguration.startPointType,
                        middlePointType = angleConfiguration.middlePointType,
                        endPointType = angleConfiguration.endPointType,
                        value = AngleUtil.getAngle(
                            smoothPoints.find { sp -> sp.pointConfiguration.type == angleConfiguration.startPointType }!!,
                            smoothPoints.find { sp -> sp.pointConfiguration.type == angleConfiguration.middlePointType }!!,
                            smoothPoints.find { sp -> sp.pointConfiguration.type == angleConfiguration.endPointType }!!
                        )
                    )
                }
                NormalizedPoseFrame(
                    frameNr = it.first().frameNr,
                    normalizedPose = NormalizedGeckoPose(
                        points = smoothPoints,
                        angles = angles,
                        configuration = it.first().normalizedPose.configuration
                    )
                )
            }
    }

    private fun List<PointF>.average(): PointF {
        return PointF(
            x = (this.sumOf { it.x.toDouble() } / this.size).toFloat(),
            y = (this.sumOf { it.y.toDouble() } / this.size).toFloat()
        )
    }
}

@Serializable
data class PoseFrame(
    val frameNr: Int,
    val pose: GeckoPose,
    val tag: String? = null,
) {
    fun toNormalizedPoseFrame() = NormalizedPoseFrame(
        frameNr = this.frameNr,
        normalizedPose = pose.getNormalizedPose(),
        tag = this.tag
    )
}

@Serializable
data class NormalizedPoseFrame(
    val frameNr: Int,
    val normalizedPose: NormalizedGeckoPose,
    val tag: String? = null,
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

