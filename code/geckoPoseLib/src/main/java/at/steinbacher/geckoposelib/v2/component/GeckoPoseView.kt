package at.steinbacher.geckoposelib.v2.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import at.steinbacher.geckoposelib.data.GeckoPose


@Composable
fun GeckoPoseView(
    imageBitmap: ImageBitmap,
    geckoPose: GeckoPose,
    modifier: Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    alignment: Alignment = Alignment.Center,
) {
    Box(modifier) {
        Image(
            bitmap = imageBitmap,
            contentDescription = "pose image",
            contentScale = contentScale,
            alignment = alignment,
            modifier = Modifier.fillMaxSize()
        )

        SkeletonView(
            geckoPose = geckoPose,
            srcImageBitmap = imageBitmap,
            lineColor = Color.Red,
            pointColor = Color.Red,
            contentScale = contentScale,
            alignment = alignment,
            selectedPointColor = Color.Red,
            modifier = Modifier.fillMaxSize()
        )
    }
}