package at.steinbacher.geckoposelib.analysis

import java.lang.Exception
import kotlin.math.abs


data class ComparablePoint(val time: Long, val value: Float)
data class ComparableLine(val points: List<ComparablePoint>) {
    fun getPoint(time: Long) = points.find { it.time == time }
}

class LineComparison(private val verticalGridCount: Int) {

    /**
     * @return the higher the value the different the lines are
     */
    fun compare(first: ComparableLine, second: ComparableLine): Float {
        val minVerticalValue = listOf(first.points.minOf { it.value }, second.points.minOf { it.value }).minOrNull()
        val maxVerticalValue = listOf(first.points.maxOf { it.value }, second.points.maxOf { it.value }).maxOrNull()

        if(minVerticalValue != null && maxVerticalValue != null) {
            val comparisonGrid = ComparisonGrid(verticalGridCount, minVerticalValue, maxVerticalValue)

            var score = 0
            first.points.forEach {
                val gridNumberThis = comparisonGrid.getVerticalGridNumber(it.value)

                val pointTo = second.getPoint(it.time)
                if(pointTo != null) {
                    val gridNumberTo = comparisonGrid.getVerticalGridNumber(pointTo.value)
                    val tempScore = abs(gridNumberThis - gridNumberTo)
                    score += tempScore
                } else {
                    throw Exception("Comparison failed! comparteTo Line misses point at ${it.time}")
                }
            }

            return score / first.points.size.toFloat()
        } else {
            throw Exception("Unable to process line! min or max not found")
        }
    }

    data class ComparisonGrid(val verticalGridCount: Int, val minVerticalValue: Float, val maxVerticalValue: Float) {
        private val valueRange = maxVerticalValue - minVerticalValue
        private val gridValueStep = valueRange / verticalGridCount

        fun getVerticalGridNumber(value: Float): Int {
            if(value == maxVerticalValue) {
                return verticalGridCount
            }

            var gridStep = 1
            var currentGridLimit = minVerticalValue + gridValueStep
            while(gridStep <= verticalGridCount) {
                if(value < currentGridLimit) {
                    return gridStep
                }

                currentGridLimit += gridValueStep
                gridStep++
            }

            return -1
        }
    }
}