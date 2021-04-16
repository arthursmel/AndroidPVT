package rs.arthu.androidpvt.lib;

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

internal class PvtViewModel(args: Args) : ViewModel(), Pvt.Listener {

    internal var pvt: Pvt = Pvt(args)

    private val _pvtState = MutableLiveData<PvtState.State>()
    val pvtState: LiveData<PvtState.State>
        get() = _pvtState

    private val _countdown = MutableLiveData<String>()
    val countdown: LiveData<String>
        get() = _countdown

    private val _reactionDelay = MutableLiveData<String>()
    val reactionDelay: LiveData<String>
        get() = _reactionDelay

    private val _results = MutableLiveData<String>()
    val results: LiveData<String>
        get() = _results

    init {
        pvt.setListener(this)
    }

    override fun onStateUpdate(newState: PvtState.State) {
        _pvtState.value = newState
    }

    override fun onCountdownUpdate(millisElapsed: Long) {
        _countdown.value = (millisElapsed / 1000).toString()
    }

    override fun onReactionDelayUpdate(millisElapsed: Long) {
        _reactionDelay.value = millisElapsed.toString()
    }

    override fun onCompleteTest(jsonResults: String) {
        _results.value = jsonResults
    }

    override fun onCleared() {
        super.onCleared()
        pvt.cancel()
    }
}
