package at.steinbacher.geckoposelib.data

import kotlinx.serialization.Serializable

@Serializable
class NormalizedGeckoPose(
    val points: List<Point>,
    val angles: List<Angle>,
    val configuration: GeckoPoseConfiguration
) {
    val poseCenterPoint = PointF(50f, 50f)

    fun getPoint(type: Int) = points.find { it.pointConfiguration.type == type } ?: error("Point $type not found!")
    fun getAngle(tag: String) = angles.find { it.tag == tag } ?: error("Angle with $tag not found!")

    fun toGeckoPose(): GeckoPose = GeckoPose(
        configuration = this.configuration.copy(),
        points = this.points.map { it.copy() }
    )
}