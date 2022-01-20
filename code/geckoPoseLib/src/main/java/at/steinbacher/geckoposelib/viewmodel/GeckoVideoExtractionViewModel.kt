package at.steinbacher.geckoposelib.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.steinbacher.geckoposelib.ChoosePoseLogic
import at.steinbacher.geckoposelib.data.GeckoPose
import at.steinbacher.geckoposelib.data.PoseFrame
import at.steinbacher.geckoposelib.data.PoseVideo
import at.steinbacher.geckoposelib.repository.IGeckoPoseDetectionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

open class GeckoVideoExtractionViewModel(private val repository: IGeckoPoseDetectionRepository): ViewModel() {
    //config params
    var seekStepsMs = 1000L
    var choosePoseLogic: ChoosePoseLogic = { null }

    //info from media player
    var videoDuration: Long = 0

    val currentSeek: LiveData<Long>
        get() = _currentSeek
    private val _currentSeek: MutableLiveData<Long> = MutableLiveData()

    val canSeekForward: LiveData<Boolean>
        get() = _canSeekForward
    private val _canSeekForward: MutableLiveData<Boolean> = MutableLiveData(false)

    val canSeekBackward: LiveData<Boolean>
        get() = _canSeekBackward
    private val _canSeekBackward: MutableLiveData<Boolean> = MutableLiveData(false)

    val currentFrame: LiveData<Bitmap?>
        get() = _currentFrame
    private var _currentFrame: MutableLiveData<Bitmap?> = MutableLiveData(null)

    val currentPose: LiveData<GeckoPose?>
        get() = _currentPose
    private var _currentPose: MutableLiveData<GeckoPose?> = MutableLiveData(null)

    val reachedEnd: LiveData<Boolean>
        get() = _reachedEnd
    private val _reachedEnd: MutableLiveData<Boolean> = MutableLiveData(false)

    val progress: LiveData<Int>
        get() = _progress
    private val _progress: MutableLiveData<Int> = MutableLiveData(0)

    private var poseFrames: ArrayList<PoseFrame> = ArrayList()

    fun seekForward() {
        _currentSeek.value = _currentSeek.value!! + seekStepsMs

        _canSeekForward.value = _currentSeek.value!! <= videoDuration
        _canSeekBackward.value = _currentSeek.value!! > 0
    }

    fun seekBackward() {
        _currentSeek.value = _currentSeek.value!! - seekStepsMs

        _canSeekForward.value = _currentSeek.value!! <= videoDuration
        _canSeekBackward.value = _currentSeek.value!! > 0
    }

    fun onVideoSet() {
        if(_currentSeek.value == null) {
            _currentSeek.value = 0

            _canSeekForward.value = _currentSeek.value!! <= videoDuration
            _canSeekBackward.value = _currentSeek.value!! > 0
        }
    }

    fun onSeekCompleted(timestamp: Long, frame: Bitmap) {
        _currentFrame.value = frame

        val progress = ((_currentSeek.value!! / videoDuration.toFloat()) * 100)
        _progress.value = if(progress.toInt() < 100) progress.toInt() else 100

        viewModelScope.launch {
            val poseFrame = poseFrames.find { it.timestamp == timestamp }

            if (poseFrame == null) {
                //first time we seek to this frame
                val poses = repository.processImage(frame)

                if (poses != null) {
                    val pose = choosePoseLogic.invoke(poses)

                    withContext(Dispatchers.Main) {
                        _currentPose.value = pose

                        _reachedEnd.value = !_canSeekForward.value!!
                    }

                    poseFrames.add(PoseFrame(pose = pose, timestamp = timestamp, poseMark = null))
                }
            } else {
                //we moved to this frame by backward/forward
                withContext(Dispatchers.Main) {
                    _currentPose.value = poseFrame.pose

                    _reachedEnd.value = !_canSeekForward.value!!
                }
            }
        }
    }

    fun markFrame(poseMark: String?) {
        val poseFrame = poseFrames.find { it.timestamp == _currentSeek.value }

        if(poseFrame != null) {
            poseFrame.poseMark = poseMark
        }
    }
}