package at.steinbacher.geckoposelib.analysis

import at.steinbacher.geckoposelib.data.PoseVideo


data class SequenceAngleMark(
    val angleTag: String,
    val targetValue: Double
)

class PoseSequenceDetection(
    val poseVideo: PoseVideo,
) {

    data class SequenceScore(val timeStamp: Long, val distance: Double)

    /*
    fun find(sequence: List<SequenceAngleMark>): Long {
        val sequenceScores = poseVideo.normalizedPoseFrames.map { normalizedPoseFrame ->
            SequenceScore(normalizedPoseFrame.timestamp, if(normalizedPoseFrame.normalizedPose != null) {
                sequence.fold(0.0) { acc, sequenceAngleMark ->
                    acc + abs(normalizedPoseFrame.normalizedPose.getAngle(sequenceAngleMark.angleTag).value - sequenceAngleMark.targetValue)
                }
            } else {
               Double.MAX_VALUE
            })
        }

        return sequenceScores.minByOrNull { it.distance }?.timeStamp ?: error("Unable to find smallest distance in list! Is it empty?")
    }
     */
}