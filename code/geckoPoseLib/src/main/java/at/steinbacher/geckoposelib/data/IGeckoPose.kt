package at.steinbacher.geckoposelib.data

interface IGeckoPose {
    val configuration: GeckoPoseConfiguration
    val points: List<Point>
    val width: Int
    val height: Int
    val tag: String?

    fun copyScale(scaleX: Float, scaleY: Float): IGeckoPose
    fun copyMove(moveX: Int, moveY: Int): IGeckoPose
    fun getAnglePositions(angleTag: String): Triple<PointF, PointF, PointF>
    fun getPoint(type: Int): Point
}