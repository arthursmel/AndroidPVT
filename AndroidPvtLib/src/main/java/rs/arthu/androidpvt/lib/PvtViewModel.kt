package rs.arthu.androidpvt.lib;

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import androidx.lifecycle.ViewModel

internal class PvtViewModel(private val args: PvtArgs) : ViewModel(){

    private var pvt: Pvt

    init {
        Log.d("PvtViewModel", "viewmodel created")

        pvt = Pvt()
    }

    internal fun handleOnTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            pvt.handleActionDownTouchEvent()
        }

        return true
    }

    override fun onCleared() {
        super.onCleared()
        pvt.cancel()
        Log.d("PvtViewModel", "oncleared")
    }

}
