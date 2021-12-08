package at.steinbacher.geckoposelib.analysis

import at.steinbacher.geckoposelib.analysis.algo.DTW
import at.steinbacher.geckoposelib.data.NormalizedGeckoPose
import at.steinbacher.geckoposelib.data.NormalizedPoseVideo
import java.lang.Exception

class NormalizedPoseVideoComparison(
    val sample: NormalizedPoseVideo,
    val template: NormalizedPoseVideo,
    val angles: List<String>,
    val verticalScale: Int = 15
) {
    private val dtw: DTW = DTW()

    val dtwKeypointDistances: List<DTWAngleDistance> = angles.map { tag ->
        val sampleAngles = sample.getScaledAngles(tag, verticalScale).toFloatArray()
        val templateAngles = template.getScaledAngles(tag, verticalScale).toFloatArray()

        DTWAngleDistance(tag, dtw.compute(sampleAngles, templateAngles).distance)
    }

    val dtwDistances = dtwKeypointDistances.map { it.distance }
    val averageDtwDistance = dtwDistances.average()
    val minDtwDistance = dtwDistances.minOrNull() ?: error("dtwDistances is empty")
    val maxDtwDistance = dtwDistances.maxOrNull() ?: error("dtwDistances is empty")
}

data class DTWAngleDistance(
    val angleTag: String,
    val distance: Double
)