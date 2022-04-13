package at.steinbacher.geckopose

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import at.steinbacher.geckoposelib.component.GeckoPoseView
import at.steinbacher.geckoposelib.data.GeckoPose
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    modifier: Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    val result = remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
        if(activityResult.resultCode == Activity.RESULT_OK) {
            result.value = activityResult.data?.data!!
        }
    }
    LaunchedEffect(launcher) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI).apply {
            type = "video/*"
        }
        coroutineScope.launch {
            launcher.launch(intent)
        }
    }


    GeckoPoseView(
        painter = painterResource(id = R.drawable.demo_image),
        modifier = modifier
    )
}