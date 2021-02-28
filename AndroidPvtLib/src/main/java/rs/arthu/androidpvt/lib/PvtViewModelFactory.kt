package rs.arthu.androidpvt.lib

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

internal class PvtViewModelFactory(private val args: PvtArgs) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PvtViewModel::class.java)) {
            return PvtViewModel(args) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
