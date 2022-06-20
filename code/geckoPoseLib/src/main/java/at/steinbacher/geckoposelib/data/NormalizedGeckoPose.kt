package at.steinbacher.geckoposelib.data

import kotlinx.serialization.Serializable

@Serializable
class NormalizedGeckoPose(
    override val points: List<Point>,
    val angles: List<Angle>,
    override val configuration: GeckoPoseConfiguration,
    override val width: Int = 100,
    override val height: Int = 100,
    override val tag: String? = null,
): IGeckoPose{
    val poseCenterPoint = PointF(50f, 50f)

    override fun copyScale(scaleX: Float, scaleY: Float): IGeckoPose {
        return NormalizedGeckoPose(
            configuration = this.configuration.copy(),
            angles = this.angles,
            points = this.points.map { lp -> lp.copyScale(scaleX, scaleY) },
            width = this.width,
            height = this.height,
            tag = this.tag
        )
    }

    override fun copyMove(moveX: Int, moveY: Int): IGeckoPose {
        return NormalizedGeckoPose(
            configuration = this.configuration.copy(),
            angles = this.angles,
            points = this.points.map { lp -> lp.copyMove(moveX, moveY) },
            width = this.width,
            height = this.height,
            tag = this.tag
        )
    }

    override fun getAnglePositions(angleTag: String): Triple<PointF, PointF, PointF> {
        val angle = getAngle(angleTag)
        val startPosition = getPoint(angle.startPointType)
        val middlePosition = getPoint(angle.middlePointType)
        val endPosition = getPoint(angle.endPointType)
        return Triple(startPosition.position, middlePosition.position, endPosition.position)
    }

    override fun getPoint(type: Int) = points.find { it.pointConfiguration.type == type } ?: error("Point $type not found!")

    fun getAngle(tag: String) = angles.find { it.tag == tag } ?: error("Angle with $tag not found!")
}