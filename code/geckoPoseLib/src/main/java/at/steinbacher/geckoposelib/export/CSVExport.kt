package at.steinbacher.geckoposelib.export

import at.steinbacher.geckoposelib.data.NormalizedPoseFrame
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import java.io.File

object CSVExport {

    suspend fun writeAnglesToCSV(normalizedPoses: List<NormalizedPoseFrame>, targetFile: File) {
        val header = arrayListOf("frameNr", "tag")
        normalizedPoses.first().normalizedPose.angles.forEach { header.add(it.tag) }

        val data = normalizedPoses.map { normalizedPoseFrame ->
            val row = arrayListOf(normalizedPoseFrame.frameNr, normalizedPoseFrame.tag)
            normalizedPoseFrame.normalizedPose.angles.forEach { row.add(it.value) }
        }

        csvWriter().writeAll(
            rows = listOf(header, data),
            targetFile = targetFile
        )
    }
}