package at.steinbacher.geckoposelib.data

import at.steinbacher.geckoposelib.util.AngleUtil
import kotlinx.serialization.Serializable

@Serializable
data class VideoPoseAnalysis(
    val normalizedPoseFrames: List<NormalizedPoseFrame>
) {
    val frameDifferences: List<FrameDifference>
    val frameData: List<FrameData>

    val firstNotNullPose: GeckoPose = normalizedPoseFrames.find { it.geckoPose != null }?.geckoPose ?: error("All poses are null")

    init {
        val allPointTypes = firstNotNullPose.points.map { it.pointConfiguration.type }
        val allAnglesTags = firstNotNullPose.configuration.angleConfigurations.map { it.tag }
        val allLineTags = firstNotNullPose.configuration.lineConfigurations.map { it.tag }

        val frameDifferences: ArrayList<FrameDifference> = ArrayList()

        //differences
        for (i in 0..normalizedPoseFrames.size - 2) {
            val firstFrame = normalizedPoseFrames[i]
            val secondFrame = normalizedPoseFrames[i + 1]

            val pointDifferences: ArrayList<PointDifference> = ArrayList()
            val angleDifferences: ArrayList<AngleDifference> = ArrayList()

            if (firstFrame.geckoPose != null && secondFrame.geckoPose != null) {
                //points
                allPointTypes.forEach {
                    val distance = firstFrame.geckoPose.getPoint(it).distanceTo(secondFrame.geckoPose.getPoint(it))
                    pointDifferences.add(PointDifference(pointType = it, difference = distance))
                }

                //angles
                allAnglesTags.forEach {
                    val difference = firstFrame.geckoPose.getAngle(it).value - secondFrame.geckoPose.getAngle(it).value
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
        normalizedPoseFrames.forEach { poseFrame ->
            val angleAnalysis: ArrayList<AngleAnalysis> = ArrayList()
            allAnglesTags.forEach { angleTag ->
                val angle = poseFrame.geckoPose?.getAngle(angleTag)
                if (angle != null) {
                    angleAnalysis.add(AngleAnalysis(angleTag = angleTag, angle = angle.value))
                }
            }

            val pointingAngleAnalyses: ArrayList<PointingAngleAnalysis> = ArrayList()
            allLineTags.forEach { lineTag ->
                val line = poseFrame.geckoPose?.configuration?.lineConfigurations?.first { it.tag == lineTag }
                val startPoint = line?.let { poseFrame.geckoPose.getPoint(it.start).position }
                val endPoint = line?.end?.let { poseFrame.geckoPose.getPoint(it).position }

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

    fun getPoseFrame(poseMark: String): NormalizedPoseFrame? = this.frameData.find { it.frame.poseMark == poseMark }?.frame

    fun getFrameDifferenceFirstIndex(poseMark: String): Int = this.frameDifferences.indexOfFirst { it.firstFrame.poseMark == poseMark }
    fun getFrameDifferenceSecondIndex(poseMark: String): Int = this.frameDifferences.indexOfFirst { it.secondFrame.poseMark == poseMark }

    fun getFrameDifferenceFirst(poseMark: String): FrameDifference? = this.frameDifferences.find { it.firstFrame.poseMark == poseMark }
    fun getFrameDifferenceSecond(poseMark: String): FrameDifference? = this.frameDifferences.find { it.secondFrame.poseMark == poseMark }

    fun getPointingAngles(lineTag: String): List<PointingAngleAnalysis> = this.frameData.map { it.getPointingAngleAnalysis(lineTag) }
    fun getPointingAngles(lineTag: String, startFrameTag: String, endFrameTag: String): List<PointingAngleAnalysis> {
        val result: ArrayList<PointingAngleAnalysis> = ArrayList()

        forEachFrameIndexed(startFrameTag, endFrameTag) { _, frameData ->
            result.add(frameData.getPointingAngleAnalysis(lineTag))
        }

        return result
    }

    fun forEachFrameIndexed(startFrameTag: String, endFrameTag: String, onFrame: (index: Int, frameData: FrameData) -> Unit) {
        val startFrameIndex = getPoseFrameIndex(startFrameTag)
        val endFrameIndex =getPoseFrameIndex(endFrameTag)

        if(startFrameIndex != -1 && endFrameIndex != -1) {
            for(i in startFrameIndex..endFrameIndex) {
                onFrame.invoke(i, frameData[i])
            }
        } else {
            throw Exception("Unable to find Start or End Frame")
        }
    }

    fun forEachFrameDifferenceIndexed(startFrameTag: String, endFrameTag: String, onFrameDifference: (index: Int, frameDifference: FrameDifference) -> Unit) {
        val startFrameIndex = getFrameDifferenceFirstIndex(startFrameTag)
        val endFrameIndex = getFrameDifferenceFirstIndex(endFrameTag)

        if(startFrameIndex != -1 && endFrameIndex != -1) {
            for(i in startFrameIndex..endFrameIndex) {
                onFrameDifference.invoke(i, frameDifferences[i])
            }
        } else {
            throw Exception("Unable to find Start or End Frame")
        }
    }
}

@Serializable
data class FrameDifference(
    val firstFrame: NormalizedPoseFrame,
    val secondFrame: NormalizedPoseFrame,
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
    val frame: NormalizedPoseFrame,
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