package at.steinbacher.geckoposelib.analysis

import java.lang.Exception
import kotlin.math.abs

data class ComparableAngle(val originalAngle: Float, val sampleSizeVertical: Int) {
    private val comparisonGrid = ComparisonGrid(0f, 360f, sampleSizeVertical)

    val sampledAngle: Int = comparisonGrid.getVerticalGridNumber(originalAngle)

    //https://math.stackexchange.com/a/110236
    fun distance(to: ComparableAngle): Float {
        val a = abs(to.originalAngle - originalAngle)
        val b = abs(to.originalAngle - originalAngle + 360)
        val c = abs(to.originalAngle - originalAngle - 360)

        return listOf(a, b, c).minOrNull()!!
    }

    fun sampledDistance(to: ComparableAngle): Int {
        return comparisonGrid.getVerticalGridNumber(distance(to))
    }
}

data class ComparableAngles(
    val originalAngles: List<Float>,
    val sampleSizeVertical: Int,
    val sampleSizeHorizontal: Int,
) {
    val sampledAngles: List<ComparableAngle> = createSamplesAngles()

    private fun createSamplesAngles(): List<ComparableAngle> {
        if(originalAngles.size < sampleSizeVertical) {
            throw Exception("Sample size > line size! $sampleSizeVertical - ${originalAngles.size}")
        }

        val sampleSteps = originalAngles.size / sampleSizeVertical.toFloat()

        //sample vertical
        var currentScaledPointsIndex = 0
        val scaledPointsVertical = ArrayList<Float>()
        for(i in 0 until sampleSizeVertical) {
            scaledPointsVertical.add(0f)
        }

        val currentSampleTargets = ArrayList<Float>()
        var currentStepLimit = sampleSteps
        for(i in originalAngles.indices) {
            if(i < currentStepLimit) {
                currentSampleTargets.add(originalAngles[i])
            } else {
                scaledPointsVertical[currentScaledPointsIndex] = currentSampleTargets.average().toFloat()
                currentScaledPointsIndex++
                currentStepLimit += sampleSteps
                currentSampleTargets.clear()
                currentSampleTargets.add(originalAngles[i])
            }
        }
        scaledPointsVertical[sampleSizeVertical-1] = currentSampleTargets.average().toFloat()


        return scaledPointsVertical.map { ComparableAngle(it, sampleSizeVertical) }
    }
}

class AngleComparison(angles1: ComparableAngles, angles2: ComparableAngles) {
    val sampledDifferences: List<Int> = calculateDifferences(angles1, angles2)

    private fun calculateDifferences(angles1: ComparableAngles, angles2: ComparableAngles): List<Int> {
        val differences = ArrayList<Int>()

        for(i in angles1.sampledAngles.indices) {
            differences.add(angles1.sampledAngles[i].sampledDistance(angles2.sampledAngles[i]))
        }

        return differences
    }
}