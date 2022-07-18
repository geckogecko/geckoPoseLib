package at.steinbacher.geckoposelib

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import at.steinbacher.geckoposelib.data.GeckoPose
import at.steinbacher.geckoposelib.data.GeckoPoseConfiguration
import at.steinbacher.geckoposelib.util.BitmapUtil.rotate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.bytedeco.javacv.AndroidFrameConverter
import org.bytedeco.javacv.FFmpegFrameGrabber

class FrameExtractor(
    uri: Uri,
    context: Context,
) {
    private val grabber: FFmpegFrameGrabber
    private val converter: AndroidFrameConverter

    val videoFrameLength: Int
    val rotation: Float

    init {
        val inputStream = context.contentResolver.openInputStream(uri)
        grabber = FFmpegFrameGrabber(inputStream)
        converter = AndroidFrameConverter()

        grabber.format = "mp4"
        grabber.start()

        rotation = grabber.getVideoMetadata("rotate").toFloat()

        videoFrameLength = grabber.lengthInVideoFrames
    }

    fun getFrame(nr: Int): Bitmap {
        grabber.setVideoFrameNumber(nr)
        val frame = grabber.grabImage()
        return converter.convert(frame)
            .rotate(rotation)
    }
}