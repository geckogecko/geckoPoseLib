package at.steinbacher.geckoposelib

import android.content.Context
import android.net.Uri
import at.steinbacher.geckoposelib.data.GeckoPoseConfiguration
import org.bytedeco.javacv.FFmpegFrameGrabber

class VideoFrameRateExtractor(
    private val uri: Uri,
    private val context: Context,
) {
    suspend fun getFrameRate(): Double {
        val inputStream = context.contentResolver.openInputStream(uri)
        val grabber = FFmpegFrameGrabber(inputStream)
        grabber.start()

        val frameRate = grabber.frameRate

        grabber.release()
        return frameRate
    }
}