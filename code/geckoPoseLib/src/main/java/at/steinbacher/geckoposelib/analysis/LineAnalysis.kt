package at.steinbacher.geckoposelib.analysis

import java.lang.Exception
import kotlin.math.abs


data class ComparableLine(val points: List<Float>) {
    fun scale(sampleSize: Int): ComparableLine {
        if(this.points.size < sampleSize) {
            throw Exception("Sample size > line size! $sampleSize - ${this.points.size}")
        }

        val sampleSteps = this.points.size / sampleSize.toFloat()

        var currentScaledPointsIndex = 0
        val scaledPoints = ArrayList<Float>()
        for(i in 0 until sampleSize) {
            scaledPoints.add(0f)
        }

        val currentSampleTargets = ArrayList<Float>()
        var currentStepLimit = sampleSteps
        for(i in this.points.indices) {
            if(i < currentStepLimit) {
                currentSampleTargets.add(this.points[i])
            } else {
                scaledPoints[currentScaledPointsIndex] = currentSampleTargets.average().toFloat()
                currentScaledPointsIndex++
                currentStepLimit += sampleSteps
                currentSampleTargets.clear()
                currentSampleTargets.add(this.points[i])
            }
        }
        scaledPoints[sampleSize-1] = currentSampleTargets.average().toFloat()

        return ComparableLine(scaledPoints.toList())
    }
}

class LineAnalysis(
    private val sampleSizeVertical: Int,
    private val sampleSizeHorizontal: Int
) {

    /**
     * @return the higher the value the different the lines are
     */
    fun compare(first: ComparableLine, second: ComparableLine): Float {
        val scaledFirst = first.scale(sampleSizeHorizontal)
        val scaledSecond= second.scale(sampleSizeHorizontal)

        val minVerticalValue = listOf(scaledFirst.points.minOrNull()!!, scaledSecond.points.minOrNull()!!).minOrNull()!!.toFloat()
        val maxVerticalValue = listOf(scaledFirst.points.maxOrNull()!!, scaledSecond.points.maxOrNull()!!).maxOrNull()!!.toFloat()

        val comparisonGrid = ComparisonGrid(minVerticalValue, maxVerticalValue, sampleSizeVertical)

        var score = 0
        scaledFirst.points.forEachIndexed { index, pointFirst ->
            val pointSecond = scaledSecond.points[index]

            val gridNumberFirst = comparisonGrid.getVerticalGridNumber(pointFirst)
            val gridNumberSecond = comparisonGrid.getVerticalGridNumber(pointSecond)

            score += abs(gridNumberFirst - gridNumberSecond)
        }

        return score / first.points.size.toFloat()
    }

    fun getSampled(
        line: ComparableLine,
        minVerticalValue: Float = line.points.minOrNull()!!,
        maxVerticalValue: Float = line.points.maxOrNull()!!,
    ): List<Int> {
        val comparisonGrid = ComparisonGrid(minVerticalValue, maxVerticalValue, sampleSizeVertical)

        return line.points.map { comparisonGrid.getVerticalGridNumber(it) }
    }

    data class ComparisonGrid(
        val minVerticalValue: Float,
        val maxVerticalValue: Float,
        val verticalGridCount: Int

    ) {

        private val valueRange = maxVerticalValue - minVerticalValue
        private val gridValueStep = valueRange / verticalGridCount

        fun getVerticalGridNumber(value: Float): Int {
            if (value == maxVerticalValue) {
                return verticalGridCount
            }

            var gridStep = 1
            var currentGridLimit = minVerticalValue + gridValueStep
            while (gridStep <= verticalGridCount) {
                if (value < currentGridLimit) {
                    return gridStep
                }

                currentGridLimit += gridValueStep
                gridStep++
            }

            return -1
        }
    }
}