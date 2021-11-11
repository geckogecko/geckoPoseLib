package at.steinbacher.geckoposelib.util

import at.steinbacher.geckoposelib.PoseFrame
import at.steinbacher.geckoposelib.PoseVideo
import kotlinx.serialization.Serializable

@Serializable
data class VideoPoseAnalysis(
    val frameDifferences: List<FrameDifference>
)

@Serializable
data class FrameDifference(
    val firstFrame: PoseFrame,
    val secondFrame: PoseFrame,
    val pointDifferences: List<PointDifference>
)

@Serializable
data class PointDifference(
    val pointType: Int,
    val difference: Double
)

object PoseVideoUtil {
    fun getRelativeVideoPoseAnalysis(reverenceDistance: Double, poseVideo: PoseVideo): VideoPoseAnalysis {
        val allPointTypes = poseVideo.poseFrames.first { it.geckoPose != null }.geckoPose!!.landmarkPoints.map { it.point.type }

        val frameDifferences: ArrayList<FrameDifference> = ArrayList()

        for(i in 0..poseVideo.poseFrames.size-2) {
            val firstFrame = poseVideo.poseFrames[i]
            val secondFrame = poseVideo.poseFrames[i+1]
            val pointDifferences: ArrayList<PointDifference> = ArrayList()

            if(firstFrame.geckoPose != null && secondFrame.geckoPose != null) {
                allPointTypes.forEach {
                    val distance = firstFrame.geckoPose.getLandmarkPoint(it).distanceTo(secondFrame.geckoPose.getLandmarkPoint(it))
                        .toRelative(reverenceDistance)

                    pointDifferences.add(PointDifference(pointType = it, difference = distance))
                }
            }

            frameDifferences.add(FrameDifference(
                firstFrame = firstFrame,
                secondFrame = secondFrame,
                pointDifferences = pointDifferences
            ))
        }

        return VideoPoseAnalysis(frameDifferences = frameDifferences)
    }

    private fun Double.toRelative(reverenceDistance: Double) = (this*100) / reverenceDistance
}