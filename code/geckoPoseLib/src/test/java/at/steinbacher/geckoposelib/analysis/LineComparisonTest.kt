package at.steinbacher.geckoposelib.analysis

import junit.framework.Assert.assertEquals
import org.junit.Assert
import org.junit.Test

class LineComparisonTest {

    @Test
    fun compareToTest1() {
        val lineComparison = LineComparison(10)
        val line = ComparableLine(points = listOf(ComparablePoint(0, 5f)))
        val line2 = ComparableLine(points = listOf(ComparablePoint(0, 7f)))

        val score = lineComparison.compare(line, line2)
        assertEquals(9f, score)
    }

    @Test
    fun compareToTest2() {
        val lineComparison = LineComparison(10)
        val line = ComparableLine(points = listOf(ComparablePoint(0, 1f), ComparablePoint(1, 2f)))
        val line2 = ComparableLine(points = listOf(ComparablePoint(0, 2f), ComparablePoint(1, 1f)))

        val score = lineComparison.compare(line, line2)
        assertEquals(9f, score)
    }

    @Test
    fun compareToTest3() {
        val lineComparison = LineComparison(10)
        val line = ComparableLine(points = listOf(
            ComparablePoint(0, 1f),
            ComparablePoint(1, 2f),
            ComparablePoint(2, 3f),
            ComparablePoint(3, 4f),
            ComparablePoint(4, 5f),
        ))
        val line2 = ComparableLine(points = listOf(
            ComparablePoint(0, 3f),
            ComparablePoint(1, 3f),
            ComparablePoint(2, 3f),
            ComparablePoint(3, 3f),
            ComparablePoint(4, 3f),
        ))

        val score = lineComparison.compare(line, line2)
        assertEquals(2.8f, score)
    }

    @Test
    fun compareToTest4() {
        val lineComparison = LineComparison(10)
        val line = ComparableLine(points = listOf(
            ComparablePoint(0, 0f),
            ComparablePoint(1, 1f),
            ComparablePoint(2, 2f),
            ComparablePoint(3, 3f),
            ComparablePoint(4, 4f),
        ))
        val line2 = ComparableLine(points = listOf(
            ComparablePoint(0, 4f),
            ComparablePoint(1, 3f),
            ComparablePoint(2, 2f),
            ComparablePoint(3, 1f),
            ComparablePoint(4, 0f),
        ))

        val score = lineComparison.compare(line, line2)
        assertEquals(5.6f, score)
    }
}