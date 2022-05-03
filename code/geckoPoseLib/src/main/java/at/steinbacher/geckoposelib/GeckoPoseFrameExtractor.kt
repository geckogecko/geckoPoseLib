package at.steinbacher.geckoposelib

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import at.steinbacher.geckoposelib.data.GeckoPose
import at.steinbacher.geckoposelib.data.GeckoPoseConfiguration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.bytedeco.javacv.AndroidFrameConverter
import org.bytedeco.javacv.FFmpegFrameGrabber

//https://medium.com/@misaeljonathan17/frame-extraction-from-video-file-on-java-android-48aa9c71b97d
class GeckoPoseFrameExtractor(
    val uri: Uri,
    configuration: List<GeckoPoseConfiguration>,
    val context: Context,
) {
    private val singleImagePoseDetection = SingleImagePoseDetection(configuration)

    val extractedFrames = flow {
        val inputStream = context.contentResolver.openInputStream(uri)
        val grabber = FFmpegFrameGrabber(inputStream)
        val converter = AndroidFrameConverter()

        grabber.format = "mp4"
        grabber.start()

        var frameNr = 0
        for(i in 0..grabber.lengthInVideoFrames) {
            val frame = grabber.grabImage()
            val image = converter.convert(frame)

            if(image != null) {
                val poses = singleImagePoseDetection.processImage(image).filterNotNull()

                emit(
                    GeckoPoseFrameExtraction(
                        image = image,
                        poses = poses,
                        frameNr = frameNr,
                        totalFrames = grabber.lengthInVideoFrames
                    )
                )
                frameNr++
            }
        }
    }.flowOn(Dispatchers.IO)
}

data class GeckoPoseFrameExtraction(
    val image: Bitmap,
    val poses: List<GeckoPose>,
    val frameNr: Int,
    val totalFrames: Int
)