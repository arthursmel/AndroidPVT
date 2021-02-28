package rs.arthu.androidpvt.lib;

import android.view.MotionEvent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

internal class PvtViewModel(args: Args) : ViewModel(), Pvt.Listener {

    private var pvt: Pvt = Pvt(args)

    private val _pvtState = MutableLiveData<Pvt.State>()
    val pvtState: LiveData<Pvt.State>
        get() = _pvtState

    private val _countdown = MutableLiveData<String>()
    val countdown: LiveData<String>
        get() = _countdown

    private val _reaction = MutableLiveData<String>()
    val reaction: LiveData<String>
        get() = _reaction

    private val _results = MutableLiveData<String>()
    val results: LiveData<String>
        get() = _results

    init {
        pvt.setListener(this)
    }

    internal fun handleOnTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            pvt.handleActionDownTouchEvent()
        }
        return true
    }

    override fun onStateUpdate(newState: Pvt.State) {
        _pvtState.value = newState
    }

    override fun onCountdownUpdate(millisElapsed: Long) {
        _countdown.value = (millisElapsed / 1000).toString()
    }

    override fun onReactionDelayUpdate(millisElapsed: Long) {
        _reaction.value = millisElapsed.toString()
    }

    override fun onCompleteTest(jsonResults: String) {
        _results.value = jsonResults
    }

    override fun onCleared() {
        super.onCleared()
        pvt.cancel()
    }
}
