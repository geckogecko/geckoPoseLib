package at.steinbacher.geckoposelib.util

import at.steinbacher.geckoposelib.PoseFrame
import at.steinbacher.geckoposelib.PoseVideo
import kotlinx.serialization.Serializable

@Serializable
data class VideoPoseAnalysis(
    val frameDifferences: List<FrameDifference>,
    val frameData: List<FrameData>
)

@Serializable
data class FrameDifference(
    val firstFrame: PoseFrame,
    val secondFrame: PoseFrame,
    val pointDifferences: List<PointDifference>,
    val angleDifferences: List<AngleDifference>
)

@Serializable
data class PointDifference(
    val pointType: Int,
    val difference: Double
)

@Serializable
data class AngleDifference(
    val angleTag: String,
    val difference: Double
)

@Serializable
data class FrameData(
    val frame: PoseFrame,
    val angleAnalysis: List<AngleAnalysis>
)

@Serializable
data class AngleAnalysis(
    val angleTag: String,
    val angle: Double
)

object PoseVideoUtil {
    fun getVideoPoseAnalysis(reverenceDistance: Double, poseVideo: PoseVideo): VideoPoseAnalysis {
        val allPointTypes = poseVideo.getFirstNotNullPose()!!.landmarkPoints.map { it.point.type }
        val allAnglesTags = poseVideo.getFirstNotNullPose()!!.configuration.angles.map { it.tag }

        val frameDifferences: ArrayList<FrameDifference> = ArrayList()

        //differences
        for(i in 0..poseVideo.poseFrames.size-2) {
            val firstFrame = poseVideo.poseFrames[i]
            val secondFrame = poseVideo.poseFrames[i+1]

            val pointDifferences: ArrayList<PointDifference> = ArrayList()
            val angleDifferences: ArrayList<AngleDifference> = ArrayList()

            if(firstFrame.geckoPose != null && secondFrame.geckoPose != null) {
                //points
                allPointTypes.forEach {
                    val distance = firstFrame.geckoPose.getLandmarkPoint(it).distanceTo(secondFrame.geckoPose.getLandmarkPoint(it))
                        .toRelative(reverenceDistance)

                    pointDifferences.add(PointDifference(pointType = it, difference = distance))
                }

                //angles
                allAnglesTags.forEach {
                    val difference = firstFrame.geckoPose.getAngle(it) - secondFrame.geckoPose.getAngle(it)
                    angleDifferences.add(AngleDifference(angleTag = it, difference = difference))
                }
            }

            frameDifferences.add(FrameDifference(
                firstFrame = firstFrame,
                secondFrame = secondFrame,
                pointDifferences = pointDifferences,
                angleDifferences = angleDifferences
            ))
        }

        //data
        val frameData: ArrayList<FrameData> = ArrayList()
        poseVideo.poseFrames.forEach { poseFrame ->
            val angleAnalysis: ArrayList<AngleAnalysis> = ArrayList()
            allAnglesTags.forEach {  angleTag ->
                val angle = poseFrame.geckoPose?.getAngle(angleTag)
                if(angle != null) {
                    angleAnalysis.add(AngleAnalysis(angleTag = angleTag, angle = angle))
                }
            }
            frameData.add(FrameData(frame = poseFrame, angleAnalysis = angleAnalysis))
        }

        return VideoPoseAnalysis(frameDifferences = frameDifferences, frameData = frameData)
    }

    private fun Double.toRelative(reverenceDistance: Double) = (this*100) / reverenceDistance
}