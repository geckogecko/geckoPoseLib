package at.steinbacher.geckoposelib.v2.component

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter


@Composable
fun GeckoPoseView(
    imageBitmap: ImageBitmap,
    modifier: Modifier
) {
    Image(
        bitmap = imageBitmap,
        contentDescription = "pose image",
        modifier = modifier
    )
}