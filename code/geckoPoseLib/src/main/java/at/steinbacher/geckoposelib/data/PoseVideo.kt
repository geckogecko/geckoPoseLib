package at.steinbacher.geckoposelib.data

import at.steinbacher.geckoposelib.util.AngleUtil
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.lang.Exception
import kotlin.math.abs

@Serializable
abstract class PoseVideo {
    abstract val uri: String
    abstract val poses: List<PoseFrame>
    abstract val normalizedPoses: List<NormalizedPoseFrame>
    abstract val frameRate: Double

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
                    tag = it.first().tag,
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

