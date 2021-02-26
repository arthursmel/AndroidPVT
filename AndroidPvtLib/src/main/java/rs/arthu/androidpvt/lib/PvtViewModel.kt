package rs.arthu.androidpvt.lib;

import androidx.lifecycle.ViewModel

class PvtViewModel : ViewModel() {

    private lateinit var pvt: Pvt

    init {


    }

    override fun onCleared() {
        super.onCleared()

        pvt.cancel()
    }

}
