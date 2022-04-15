package at.steinbacher.geckopose

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import at.steinbacher.geckoposelib.v2.ExtractedFrame
import at.steinbacher.geckoposelib.v2.FrameExtractor
import at.steinbacher.geckoposelib.v2.component.GeckoPoseView
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    modifier: Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val vm: MainViewModel = viewModel()

    val state = vm.state.collectAsState()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
        if(activityResult.resultCode == Activity.RESULT_OK) {
           vm.onUriReceived(activityResult.data?.data!!)
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


    if(state.value != null) {
        GeckoPoseView(
            imageBitmap = state.value!!.image.asImageBitmap(),
            modifier = modifier
        )
    }
}

class MainViewModel(application: Application): AndroidViewModel(application) {

    private val _state: MutableStateFlow<ExtractedFrame?> = MutableStateFlow(null)
    val state: StateFlow<ExtractedFrame?>
        get() = _state

    @OptIn(InternalCoroutinesApi::class)
    fun onUriReceived(uri: Uri) {
        viewModelScope.launch {
            val frameExtractor = FrameExtractor(
                uri = uri,
                configuration = tennisConfiguration,
                context = getApplication()
            )
            frameExtractor.extractedFrames.collect {
                _state.value = it
            }
        }
    }

}