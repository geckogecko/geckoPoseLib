package at.steinbacher.geckoposelib.analysis

import at.steinbacher.geckoposelib.analysis.algo.DTW
import at.steinbacher.geckoposelib.data.NormalizedGeckoPose
import at.steinbacher.geckoposelib.data.NormalizedPoseVideo

class NormalizedPoseVideoComparison(
    val sample: NormalizedPoseVideo,
    val template: NormalizedPoseVideo,
    val angles: List<String>
) {
    private val dtw: DTW = DTW()

    val dtwKeypointDistances: List<DTWAngleDistance> = angles.map {
        val sampleAngles = sample.getAngles(it).map { it.value.toFloat() }.toFloatArray()
        val templateAngles = template.getAngles(it).map { it.value.toFloat() }.toFloatArray()

        DTWAngleDistance(it, dtw.compute(sampleAngles, templateAngles).distance)
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