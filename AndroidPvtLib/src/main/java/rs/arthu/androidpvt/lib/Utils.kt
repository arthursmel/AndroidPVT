package rs.arthu.androidpvt.lib

import com.google.gson.Gson

internal class Utils {
    companion object {
        internal fun <T> MutableList<T>.addSafe(item: T?) {
            item?.let {
                this.add(it)
            }
        }

        internal fun <T> MutableList<T>.toJson(): String = Gson().toJson(this)
    }
}
