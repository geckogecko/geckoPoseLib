package at.steinbacher.geckoposelib

import com.google.mlkit.vision.pose.PoseLandmark


data class LandmarkLineInput(
    val landmarkLines: List<LandmarkLine>,
    val alternativeLandmarkLines: List<LandmarkLine>?
)

data class LandmarkLine(
    val tag: String,
    val poseLandmarkTypes: List<Int>
)

data class LandmarkLineResult(
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