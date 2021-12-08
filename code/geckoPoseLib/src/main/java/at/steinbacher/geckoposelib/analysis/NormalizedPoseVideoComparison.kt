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
        val sampleAngles = sample.getAngles(tag)
            .map { it.value.toFloat() }
            .scale(verticalScale)
            .toFloatArray()
        val templateAngles = template.getAngles(tag)
            .map { it.value.toFloat() }
            .scale(verticalScale)
            .toFloatArray()

        DTWAngleDistance(tag, dtw.compute(sampleAngles, templateAngles).distance)
    }

    val dtwDistances = dtwKeypointDistances.map { it.distance }
    val averageDtwDistance = dtwDistances.average()
    val minDtwDistance = dtwDistances.minOrNull() ?: error("dtwDistances is empty")
    val maxDtwDistance = dtwDistances.maxOrNull() ?: error("dtwDistances is empty")

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

data class DTWAngleDistance(
    val angleTag: String,
    val distance: Double
)