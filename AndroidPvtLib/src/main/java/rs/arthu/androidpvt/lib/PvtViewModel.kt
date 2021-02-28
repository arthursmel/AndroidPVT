package rs.arthu.androidpvt.lib;

import android.view.MotionEvent
import androidx.lifecycle.ViewModel

internal class PvtViewModel(args: PvtArgs) : ViewModel(){

    private var pvt: Pvt = Pvt(args)

    internal fun handleOnTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            pvt.handleActionDownTouchEvent()
        }

        return true
    }

    override fun onCleared() {
        super.onCleared()
        pvt.cancel()
    }
}
