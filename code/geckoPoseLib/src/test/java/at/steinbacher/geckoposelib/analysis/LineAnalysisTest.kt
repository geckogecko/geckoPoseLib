package at.steinbacher.geckoposelib.analysis

import junit.framework.Assert.assertEquals
import org.junit.Test

class LineAnalysisTest {

    @Test
    fun compareToTest1() {
        val lineComparison = LineAnalysis(10, 10)
        val line = ComparableLine(points = listOf(5f, 5f, 5f, 5f, 5f, 5f, 5f, 5f, 5f, 5f, 5f, 5f, 5f, 5f, 5f, 5f, 5f, 5f, 5f, 5f))
        val line2 = ComparableLine(points = listOf(5f, 5f, 5f, 5f, 5f, 5f, 5f, 5f, 5f, 5f))

        val score = lineComparison.compare(line, line2)
        assertEquals(0f, score)
    }

    @Test
    fun compareToTest2() {
        val lineComparison = LineAnalysis(10, 10)
        val line = ComparableLine(points = listOf(0f, 10f, 0f, 10f, 0f, 10f, 0f, 10f, 0f, 10f, 0f, 10f, 0f, 10f, 0f, 10f, 0f, 10f, 0f, 10f))
        val line2 = ComparableLine(points = listOf(5f, 5f, 5f, 5f, 5f, 5f, 5f, 5f, 5f, 5f))

        val score = lineComparison.compare(line, line2)
        assertEquals(0f, score)
    }

    @Test
    fun compareToTest3() {
        val lineComparison = LineAnalysis(10, 10)
        val line = ComparableLine(points = listOf(0f, 10f, 0f, 10f, 0f, 10f, 0f, 10f, 0f, 10f, 0f, 10f, 0f, 10f, 0f, 10f))
        val line2 = ComparableLine(points = listOf(5f, 5f, 5f, 5f, 5f, 5f, 5f, 5f, 5f, 5f))

        val score = lineComparison.compare(line, line2)
        assertEquals(1.125f, score)
    }
}