package rs.arthu.mel.androidpvtlib

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import java.lang.IllegalStateException
import kotlin.properties.Delegates

/**
 * @property numberOfStimulus the number of PVTs that will be displayed to the user
 */
class Pvt(private val numberOfStimulus: Int) {

    private var remainingTestCount = numberOfStimulus
    private var listener: PvtListener? = null
    private var curJob: Job? = null
    private val results: MutableList<PvtResult> = mutableListOf()

    private var curState by Delegates.observable<PvtState>(INIT_STATE, {
            _, oldState, newState -> if (LOG_STATE_CHANGES) {
        Log.d(TAG, "transition ($oldState -> $newState)")
    }
    })

    fun handleActionDownTouchEvent() {
        when (curState) {
            is Instructions -> {
                // Checking if job is null, otherwise if user quickly presses instructions
                // multiple jobs may be run
                if (curJob == null) {
                    curJob = runTest(true, remainingTestCount)
                }
            }
            is Countdown -> {}
            is Interval -> {
                CoroutineScope(Default).launch {
                    curJob?.cancelAndJoin()
                    handleInvalidReaction()
                }
            }
            is StimulusShowing -> {
                val reactionTimestamp = System.currentTimeMillis()
                curState = curState.consumeAction(Action.ValidReaction(reactionTimestamp))
            }
            is InvalidReaction -> {}
            is ValidReaction -> {}
            is Complete -> {}
            else -> throw IllegalStateException()
        }
    }

    /**
     * Restarts the PVT, can be called during any test state
     */
    fun restart() {
        CoroutineScope(Default).launch {
            curJob?.cancelAndJoin()

            remainingTestCount = numberOfStimulus
            curState = curState.consumeAction(Action.Restart)

            curJob = runTest(true, remainingTestCount)
        }
    }

    fun cancel() {
        curJob?.cancel()
    }

    private fun runTest(runCountdown: Boolean, remainingTestCount: Int): Job? {
        return CoroutineScope(Default).launch {
            if (remainingTestCount == 0) {
                handleCompletePvt()
                cancel()
            }

            if (runCountdown) runCountdown()
            val intervalDelay = getRandomIntervalDelay()
            runInterval(intervalDelay)

            val result = runStimulus(System.currentTimeMillis(), intervalDelay)
            results.addSafe(result)

            runNextTest(result)
        }
    }

    private suspend fun runNextTest(result: PvtResult?) {
        if (result == null) {
            handleInvalidReaction()
        } else {
            remainingTestCount -= 1
            curJob = runTest(false, remainingTestCount)
        }
    }

    private suspend fun runCountdown() {
        curState = curState.consumeAction(Action.StartCountdown)

        withContext(Main) { listener?.onStartCountdown() }

        (COUNTDOWN_MILLIS downTo ONE_SECOND step ONE_SECOND).forEach { i ->
            withContext(Main) { listener?.onCountdownUpdate(i) }
            delay(ONE_SECOND)
        }

        withContext(Main) { listener?.onFinishCountdown() }
    }

    private suspend fun runInterval(delay: Long) {
        curState = curState.consumeAction(Action.StartInterval)
        withContext(Main) { listener?.onIntervalShowing() }

        delay(delay)
    }

    private suspend fun runStimulus(startTimestamp: Long, interval: Long): PvtResult? {
        curState = curState.consumeAction(Action.ShowStimulus)
        withContext(Main) { listener?.onStimulusShowing() }

        while (timeSinceCalled(startTimestamp) < STIMULUS_TIMEOUT) {
            if (curState is ValidReaction) {
                return handleValidReaction(startTimestamp, interval)
            }

            withContext(Main) {
                listener?.onReactionDelayUpdate(timeSinceCalled(startTimestamp))
            }
        }

        withContext(Main) { listener?.onStimulusHidden() }
        return null
    }

    private suspend fun handleValidReaction(startTimestamp: Long, interval: Long): PvtResult {
        val reactionTimestamp = (curState as ValidReaction).reactionDelay
        val reactionDelay = reactionTimestamp - startTimestamp

        // Posting the most accurate reaction delay to the main thread
        // otherwise there will be a few ms difference between reactionDelay
        // and value displaced on screen for the post response delay
        withContext(Main) { listener?.onReactionDelayUpdate(reactionDelay) }

        delay(STIMULUS_POST_RESPONSE_DELAY)
        withContext(Main) { listener?.onStimulusHidden() }
        return PvtResult(remainingTestCount, startTimestamp, interval, reactionDelay)
    }

    private suspend fun handleInvalidReaction() {
        curState = curState.consumeAction(Action.InvalidReaction)
        withContext(Main) { listener?.onInvalidReaction() }

        delay(POST_INVALID_REACTION_DELAY)

        curJob = runTest(false, remainingTestCount)
    }

    private suspend fun handleCompletePvt() {
        curState = curState.consumeAction(Action.Complete)
        withContext(Main) { listener?.onCompleteTest() }

        delay(POST_COMPLETE_DELAY)
        withContext(Main) {
            listener?.onResults(results.toJson())
        }
    }

    private fun timeSinceCalled(startTimestamp: Long) = System.currentTimeMillis() - startTimestamp

    private fun getRandomIntervalDelay(): Long = (MIN_INTERVAL_DELAY..MAX_INTERVAL_DELAY).random()

    private fun <T> MutableList<T>.addSafe(item: T?) {
        item?.let {
            this.add(it)
        }
    }

    private fun <T> MutableList<T>.toJson(): String = Gson().toJson(this)

    private class Instructions : PvtState {
        override fun consumeAction(action: Action): PvtState {
            return when (action) {
                is Action.StartCountdown -> Countdown()
                is Action.Restart -> INIT_STATE
                else -> throw IllegalStateException()
            }
        }
    }

    private class Countdown : PvtState {
        override fun consumeAction(action: Action): PvtState {
            return when (action) {
                is Action.StartInterval -> Interval()
                is Action.Restart -> INIT_STATE
                else -> throw IllegalStateException()
            }
        }
    }

    private class Interval : PvtState {
        override fun consumeAction(action: Action): PvtState {
            return when (action) {
                is Action.ShowStimulus -> StimulusShowing()
                is Action.InvalidReaction -> InvalidReaction()
                is Action.Restart -> INIT_STATE
                else -> throw IllegalStateException()
            }
        }
    }

    private class StimulusShowing : PvtState {
        override fun consumeAction(action: Action): PvtState {
            return when (action) {
                is Action.ValidReaction -> ValidReaction(action.reactionDelay)
                is Action.InvalidReaction -> InvalidReaction()
                is Action.Restart -> INIT_STATE
                else -> throw IllegalStateException()
            }
        }
    }

    private class ValidReaction(val reactionDelay: Long) : PvtState {
        override fun consumeAction(action: Action): PvtState {
            return when (action) {
                is Action.StartInterval -> Interval()
                is Action.Complete -> Complete()
                is Action.Restart -> INIT_STATE
                else -> throw IllegalStateException()
            }
        }
    }

    private class InvalidReaction : PvtState {
        override fun consumeAction(action: Action): PvtState {
            return when (action) {
                is Action.StartInterval -> Interval()
                is Action.Restart -> INIT_STATE
                else -> throw IllegalStateException()
            }
        }
    }

    private class Complete : PvtState {
        override fun consumeAction(action: Action): PvtState {
            return when (action) {
                is Action.Restart -> INIT_STATE
                else -> this
            }
        }
    }

    sealed class Action {
        object Restart : Action()
        object StartCountdown : Action()
        object StartInterval : Action()
        object ShowStimulus : Action()
        class ValidReaction(val reactionDelay: Long) : Action()
        object InvalidReaction : Action()
        object Complete : Action()
    }

    interface PvtState {
        fun consumeAction(action: Action): PvtState
    }

    interface PvtListener {
        fun onStartCountdown() = Unit
        fun onCountdownUpdate(millisElapsed: Long) = Unit
        fun onFinishCountdown() = Unit
        fun onIntervalShowing() = Unit
        fun onStimulusShowing() = Unit
        fun onInvalidReaction() = Unit
        fun onReactionDelayUpdate(millisElapsed: Long) = Unit
        fun onStimulusHidden() = Unit
        fun onResults(resultsJson: String)
        fun onCompleteTest() = Unit
    }

    fun setListener(
        onStartCountdown: () -> Unit = {},
        onCountdownUpdate: (millisElapsed: Long) -> Unit = {},
        onFinishCountdown: () -> Unit = {},
        onIntervalShowing: () -> Unit = {},
        onStimulusShowing: () -> Unit = {},
        onInvalidReaction: () -> Unit = {},
        onReactionDelayUpdate: (millisElapsed: Long) -> Unit = {},
        onStimulusHidden: () -> Unit = {},
        onCompleteTest: () -> Unit = {},
        onResults: (resultsJson: String) -> Unit = {}
    ) {
        listener = object : PvtListener {
            override fun onStartCountdown() = onStartCountdown()
            override fun onCountdownUpdate(millisElapsed: Long) =
                onCountdownUpdate(millisElapsed)
            override fun onFinishCountdown() = onFinishCountdown()
            override fun onIntervalShowing() = onIntervalShowing()
            override fun onStimulusShowing() = onStimulusShowing()
            override fun onInvalidReaction() = onInvalidReaction()
            override fun onReactionDelayUpdate(millisElapsed: Long) =
                onReactionDelayUpdate(millisElapsed)
            override fun onStimulusHidden() = onStimulusHidden()
            override fun onCompleteTest() = onCompleteTest()
            override fun onResults(resultsJson: String) = onResults(resultsJson)
        }
    }

    companion object {
        private const val TAG = "PVT"
        private const val ONE_SECOND: Long = 1000
        private const val LOG_STATE_CHANGES = false
        private val INIT_STATE = Instructions()

        private const val COUNTDOWN_MILLIS = 3 * ONE_SECOND
        private const val STIMULUS_TIMEOUT = 10 * ONE_SECOND
        private const val STIMULUS_POST_RESPONSE_DELAY = 2 * ONE_SECOND
        private const val POST_INVALID_REACTION_DELAY = 2 * ONE_SECOND
        private const val POST_COMPLETE_DELAY = 2 * ONE_SECOND
        private const val MIN_INTERVAL_DELAY = 2 * ONE_SECOND
        private const val MAX_INTERVAL_DELAY = 4 * ONE_SECOND
    }

    data class PvtResult(val testNumber: Int, val timestamp: Long, val interval: Long, val reactionDelay: Long)
}
