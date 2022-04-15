package at.steinbacher.geckoposelib.v2

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import at.steinbacher.geckoposelib.data.GeckoPose
import at.steinbacher.geckoposelib.data.GeckoPoseConfiguration
import at.steinbacher.geckoposelib.view.GeckoPoseComparisonView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.bytedeco.javacv.AndroidFrameConverter
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.OpenCVFrameConverter
import java.io.FileInputStream

//https://medium.com/@misaeljonathan17/frame-extraction-from-video-file-on-java-android-48aa9c71b97d
class FrameExtractor(
    val uri: Uri,
    configuration: List<GeckoPoseConfiguration>,
    val context: Context,
) {
    private val singleImagePoseDetection = SingleImagePoseDetection(configuration)

    val extractedFrames = flow<ExtractedFrame> {
        val inputStream = context.contentResolver.openInputStream(uri)
        val grabber = FFmpegFrameGrabber(inputStream)
        val converter = AndroidFrameConverter()

        grabber.format = "mp4"
        grabber.start()

        for(i in 0..grabber.lengthInVideoFrames) {
            val frame = grabber.grabImage()
            val image = converter.convert(frame)

            if(image != null) {
                val poses = singleImagePoseDetection.processImage(image)

                emit(ExtractedFrame(image, poses))
            }
        }
    }.flowOn(Dispatchers.IO)
}

data class ExtractedFrame(
    val image: Bitmap,
    val poses: List<GeckoPose?>
)