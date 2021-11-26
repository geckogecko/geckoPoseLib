package at.steinbacher.geckoposelib.data

import at.steinbacher.geckoposelib.util.AngleUtil
import kotlinx.serialization.Serializable

@Serializable
data class VideoPoseAnalysis(
    val poseVideo: PoseVideo
) {
    val frameDifferences: List<FrameDifference>
    val frameData: List<FrameData>

    init {
        val allPointTypes = poseVideo.getFirstNotNullPose().landmarkPoints.map { it.point.type }
        val allAnglesTags = poseVideo.getFirstNotNullPose().configuration.angles.map { it.tag }
        val allLineTags = poseVideo.getFirstNotNullPose().configuration.lines.map { it.tag }

        val frameDifferences: ArrayList<FrameDifference> = ArrayList()

        //differences
        for (i in 0..poseVideo.poseFrames.size - 2) {
            val firstFrame = poseVideo.poseFrames[i]
            val secondFrame = poseVideo.poseFrames[i + 1]

            val pointDifferences: ArrayList<PointDifference> = ArrayList()
            val angleDifferences: ArrayList<AngleDifference> = ArrayList()

            if (firstFrame.geckoPose != null && secondFrame.geckoPose != null) {
                //points
                allPointTypes.forEach {
                    val distance = firstFrame.geckoPose.getLandmarkPoint(it).distanceTo(secondFrame.geckoPose.getLandmarkPoint(it))
                    pointDifferences.add(PointDifference(pointType = it, difference = distance))
                }

                //angles
                allAnglesTags.forEach {
                    val difference = firstFrame.geckoPose.getAngle(it) - secondFrame.geckoPose.getAngle(it)
                    angleDifferences.add(AngleDifference(angleTag = it, difference = difference))
                }
            }

            frameDifferences.add(
                FrameDifference(
                    firstFrame = firstFrame,
                    secondFrame = secondFrame,
                    pointDifferences = pointDifferences,
                    angleDifferences = angleDifferences
                )
            )
        }

        //data
        val frameData: ArrayList<FrameData> = ArrayList()
        poseVideo.poseFrames.forEach { poseFrame ->
            val angleAnalysis: ArrayList<AngleAnalysis> = ArrayList()
            allAnglesTags.forEach { angleTag ->
                val angle = poseFrame.geckoPose?.getAngle(angleTag)
                if (angle != null) {
                    angleAnalysis.add(AngleAnalysis(angleTag = angleTag, angle = angle))
                }
            }

            val pointingAngleAnalyses: ArrayList<PointingAngleAnalysis> = ArrayList()
            allLineTags.forEach { lineTag ->
                val line = poseFrame.geckoPose?.configuration?.lines?.first { it.tag == lineTag }
                val startPoint = line?.let { poseFrame.geckoPose.getLandmarkPoint(it.start).position }
                val endPoint = line?.end?.let { poseFrame.geckoPose.getLandmarkPoint(it).position }

                if(startPoint != null && endPoint != null) {
                    val pointingAngle = AngleUtil.getClockWiseAngle(startPoint, endPoint)
                    pointingAngleAnalyses.add(PointingAngleAnalysis(lineTag, pointingAngle.toFloat()))
                }
            }

            frameData.add(FrameData(frame = poseFrame, angleAnalysis = angleAnalysis, pointingAngleAnalyses = pointingAngleAnalyses))
        }

        this.frameDifferences = frameDifferences
        this.frameData = frameData
    }

    fun getAngleFrameData(angleTag: String): List<DataEntry> = frameData.map { frameData ->
        DataEntry(frameData.frame.timestamp, frameData.getAngleAnalysis(angleTag).angle)
    }

    fun getPointDifferenceData(pointType: Int): List<DataEntry> = frameDifferences.map { frameDifference ->
        DataEntry(frameDifference.secondFrame.timestamp, frameDifference.getPointDifference(pointType).difference)
    }

    fun getAngleDifferenceData(angleTag: String): List<DataEntry> = frameDifferences.map { frameDifference ->
        DataEntry(frameDifference.secondFrame.timestamp, frameDifference.getAngleDifference(angleTag).difference)
    }

    fun getPoseFrameIndex(poseMark: String): Int = this.frameData.indexOfFirst { it.frame.poseMark == poseMark }

    fun getPoseFrame(poseMark: String): PoseFrame? = this.frameData.find { it.frame.poseMark == poseMark }?.frame

    fun getFrameDifferenceFirstIndex(poseMark: String): Int = this.frameDifferences.indexOfFirst { it.firstFrame.poseMark == poseMark }
    fun getFrameDifferenceSecondIndex(poseMark: String): Int = this.frameDifferences.indexOfFirst { it.secondFrame.poseMark == poseMark }

    fun getFrameDifferenceFirst(poseMark: String): FrameDifference? = this.frameDifferences.find { it.firstFrame.poseMark == poseMark }
    fun getFrameDifferenceSecond(poseMark: String): FrameDifference? = this.frameDifferences.find { it.secondFrame.poseMark == poseMark }

    fun getPointingAngles(lineTag: String): List<PointingAngleAnalysis> = this.frameData.map { it.getPointingAngleAnalysis(lineTag) }
}

@Serializable
data class FrameDifference(
    val firstFrame: PoseFrame,
    val secondFrame: PoseFrame,
    val pointDifferences: List<PointDifference>,
    val angleDifferences: List<AngleDifference>
) {
    fun getPointDifference(pointType: Int): PointDifference = pointDifferences.first { it.pointType == pointType }
    fun getAngleDifference(angleTag: String): AngleDifference = angleDifferences.first { it.angleTag == angleTag }
}

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
    val angleAnalysis: List<AngleAnalysis>,
    val pointingAngleAnalyses: List<PointingAngleAnalysis>
) {
    fun getAngleAnalysis(angleTag: String): AngleAnalysis = angleAnalysis.first { it.angleTag == angleTag }
    fun getPointingAngleAnalysis(lineTag: String): PointingAngleAnalysis = pointingAngleAnalyses.first { it.lineTag == lineTag }
}

@Serializable
data class AngleAnalysis(
    val angleTag: String,
    val angle: Double
)

@Serializable
data class PointingAngleAnalysis(
    val lineTag: String,
    val clockwiseAngle: Float,
)

data class DataEntry(val timestamp: Long, val value: Double)