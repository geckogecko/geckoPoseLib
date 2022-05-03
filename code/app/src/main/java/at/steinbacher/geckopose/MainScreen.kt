package at.steinbacher.geckopose

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import at.steinbacher.geckoposelib.data.GeckoPose
import at.steinbacher.geckoposelib.GeckoPoseFrameExtractor
import at.steinbacher.geckoposelib.component.GeckoPoseView
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
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

    if(state.value.frame != null && state.value.pose != null) {
        Column {
            GeckoPoseView(
                imageBitmap = state.value.frame!!.asImageBitmap(),
                geckoPose = state.value.pose!!,
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f)
            )
        }
    }
}

data class MainScreenState(
    val frame: Bitmap? = null,
    val pose: GeckoPose? = null,
    val frameNr: Int = 0,
    val totalFrames: Int = 0
)

class MainViewModel(application: Application): AndroidViewModel(application) {

    private val poses = ArrayList<GeckoPose>()
    private var extractionCompleted = false

    private val _state: MutableStateFlow<MainScreenState> = MutableStateFlow(MainScreenState())
    val state: StateFlow<MainScreenState>
        get() = _state

    @OptIn(InternalCoroutinesApi::class)
    fun onUriReceived(uri: Uri) {
        viewModelScope.launch {
            val frameExtractor = GeckoPoseFrameExtractor(
                uri = uri,
                configuration = tennisConfiguration,
                context = getApplication()
            )
            frameExtractor.extractedFrames
                .onCompletion { extractionCompleted = true }
                .collect {
                    if(it.poses.isNotEmpty()) {
                        poses.add(it.poses.first())
                        _state.value = MainScreenState(
                            frame = it.image,
                            pose = it.poses.first(),
                            frameNr = it.frameNr,
                            totalFrames = it.totalFrames
                        )
                    }
            }
        }
    }
}