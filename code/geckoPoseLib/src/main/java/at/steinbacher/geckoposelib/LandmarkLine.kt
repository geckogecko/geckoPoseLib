package at.steinbacher.geckoposelib

import com.google.mlkit.vision.pose.PoseLandmark

class LandmarkLine(
    val tag: String,
    val poseLandmarkTypes: List<Int>
)

class LandmarkLineResult(
    val tag: String,
    val poseLandmarks: List<PoseLandmark>
) {
    fun getPoseLandmark(landmarkType: Int): PoseLandmark? {
        return poseLandmarks.find { it.landmarkType == landmarkType }
    }
}

fun List<LandmarkLineResult>.getPoseLandmarksByTag(tag: String): LandmarkLineResult? {
    return this.find { it.tag == tag }
}