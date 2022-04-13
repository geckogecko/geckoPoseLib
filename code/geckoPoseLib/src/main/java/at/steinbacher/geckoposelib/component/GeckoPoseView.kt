package at.steinbacher.geckoposelib.component

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter


@Composable
fun GeckoPoseView(
    painter: Painter,
    modifier: Modifier
) {
    Image(
        painter = painter,
        contentDescription = "pose image",
        modifier = modifier
    )
}