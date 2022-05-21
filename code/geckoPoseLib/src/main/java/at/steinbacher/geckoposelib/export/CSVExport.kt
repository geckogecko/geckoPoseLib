package at.steinbacher.geckoposelib.export

import at.steinbacher.geckoposelib.data.NormalizedPoseFrame
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import java.io.File
import java.io.OutputStream

object CSVExport {

    suspend fun writeAnglesToCSV(normalizedPoses: List<NormalizedPoseFrame>, outputStream: OutputStream) {
        val header: ArrayList<String?> = arrayListOf("frameNr", "tag")
        normalizedPoses.first().normalizedPose.angles.forEach { header.add(it.tag) }

        val rows: ArrayList<ArrayList<String?>> = arrayListOf(header)
        normalizedPoses.forEach { normalizedPoseFrame ->
            val row: ArrayList<String?> = arrayListOf(normalizedPoseFrame.frameNr.toString(), normalizedPoseFrame.tag)
            normalizedPoseFrame.normalizedPose.angles.forEach { row.add(it.value.toString()) }
            rows.add(row)
        }

        csvWriter().writeAll(
            rows = rows,
            ops = outputStream
        )
    }
}