package at.steinbacher.geckoposelib.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import at.steinbacher.geckoposelib.data.GeckoPose


@Composable
fun GeckoPoseComparisonView(
    pose: GeckoPose,
    poseShowAngles: Boolean = true,
    comparison: GeckoPose,
    comparisonShowAngles: Boolean = false,
    comparisonAlpha: Float = 0.5f,
    modifier: Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    alignment: Alignment = Alignment.Center,
) {
    Box(modifier) {
        SkeletonView(
            geckoPose = comparison,
            contentScale = contentScale,
            alignment = alignment,
            drawAngles = comparisonShowAngles,
            modifier = Modifier.fillMaxSize()
                .alpha(comparisonAlpha)
        )

        SkeletonView(
            geckoPose = pose,
            contentScale = contentScale,
            alignment = alignment,
            drawAngles = poseShowAngles,
            modifier = Modifier.fillMaxSize()
        )
    }
}