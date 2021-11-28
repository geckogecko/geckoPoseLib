package at.steinbacher.geckoposelib.analysis

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