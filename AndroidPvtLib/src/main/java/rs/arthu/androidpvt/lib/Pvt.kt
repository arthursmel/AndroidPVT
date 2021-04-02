package rs.arthu.androidpvt.lib

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import java.lang.IllegalStateException
import kotlin.properties.Delegates

internal const val ONE_SECOND: Long = 1000
internal const val DEFAULT_STIMULUS_COUNT = 3
internal const val DEFAULT_MIN_INTERVAL = 2 * ONE_SECOND
internal const val DEFAULT_MAX_INTERVAL = 4 * ONE_SECOND
internal const val DEFAULT_COUNTDOWN_TIME = 3 * ONE_SECOND
internal const val DEFAULT_STIMULUS_TIMEOUT = 10 * ONE_SECOND
internal const val DEFAULT_POST_RESPONSE_DELAY = 2 * ONE_SECOND

internal class Pvt(private val args: Args = Args.default()) {

    private var listener: Listener? = null
    private var stimulusListener: StimulusListener? = null
    // Stimulus listener is used to shortcut the view model
    // to improve performance showing the stimulus to the user

    private var remainingTestCount = args.stimulusCount
    private var curJob: Job? = null
    private val results: MutableList<Result> = mutableListOf()

    private var curState by Delegates.observable<State>(INIT_STATE, {
            _, oldState, newState -> if (LOG_STATE_TRANSITIONS) {
            Log.d(TAG, "transition ($oldState -> $newState)")
        }
        notifyStateChange(newState)
    })

    fun handleActionDownTouchEvent() {
        val reactionTimestamp = System.currentTimeMillis()

        when (curState) {
            is Instructions -> {
                // Checking if job is null, otherwise if user quickly presses instructions
                // multiple jobs may be run
                if (curJob == null) {
                    runTest()
                }
            }
            is Interval -> {
                curState = curState.consumeAction(Action.InvalidReaction)
            }
            is StimulusShowing -> {
                curState = curState.consumeAction(Action.ValidReaction(reactionTimestamp))
            }
            is Countdown, is InvalidReaction, is ValidReaction, is Complete -> {}
            else -> throw IllegalStateException()
        }
    }

    internal fun cancel() {
        curJob?.cancel()
    }

    private fun runTest() {
        curJob = CoroutineScope(Default).launch {

            runCountdown()

            while (testsRemain() && !jobCancelled()) {
                val intervalDelay = getRandomIntervalDelay()

                runInterval(intervalDelay)

                if (curState is InvalidReaction) {
                    continue
                }

                withContext(Main) {
                    stimulusListener?.onStimulus()
                }

                val startTimestamp = System.currentTimeMillis()
                val result = runStimulus(
                        startTimestamp,
                        intervalDelay
                )

                delay(args.postResponseDelay)

                if (result == null) {
                    continue
                } else {
                    decrementRemainingTestCount()
                    results.addSafe(result)
                }

            }

            if (!testsRemain()) {
                handleCompletePvt()
            }
        }
    }

    private fun testsRemain() : Boolean {
        return remainingTestCount > 0
    }

    private fun decrementRemainingTestCount() {
        remainingTestCount -= 1
    }

    private suspend fun runInterval(delay: Long) {
        curState = curState.consumeAction(Action.StartInterval)
        delay(delay)
    }

    private suspend fun runStimulus(startTimestamp: Long, interval: Long): Result? {
        curState = curState.consumeAction(Action.ShowStimulus)

        while (testHasNotTimedOut(startTimestamp) &&
                !validReactionHasOccurred() &&
                !jobCancelled()) {

            withContext(Main) {
                listener?.onReactionDelayUpdate(timeSinceCalled(startTimestamp))
            }
        }

        return if (curState is ValidReaction) {
            handleValidReaction(startTimestamp, interval)
        } else {
            curState = curState.consumeAction(Action.InvalidReaction)
            null // returning null as test timed out, no result created
        }
    }

    private fun validReactionHasOccurred() = curState is ValidReaction

    private fun testHasNotTimedOut(startTimestamp: Long) = timeSinceCalled(startTimestamp) < args.stimulusTimeout

    private fun jobCancelled() = curJob?.isCancelled ?: true

    private fun timeSinceCalled(startTimestamp: Long) = System.currentTimeMillis() - startTimestamp

    private suspend fun runCountdown() {
        curState = curState.consumeAction(Action.StartCountdown)

        (args.countDownTime downTo ONE_SECOND step ONE_SECOND).forEach { i ->
            withContext(Main) {
                listener?.onCountdownUpdate(i)
            }
            delay(ONE_SECOND)
        }
    }

    private suspend fun handleCompletePvt() {
        curState = curState.consumeAction(Action.Complete)

        delay(args.postResponseDelay)

        withContext(Main) {
            listener?.onCompleteTest(results.toJson())
        }
    }

    private suspend fun handleValidReaction(startTimestamp: Long, interval: Long): Result {
        val reactionTimestamp = (curState as ValidReaction).reactionDelay
        val reactionDelay = reactionTimestamp - startTimestamp

        // Posting the most accurate reaction delay to the main thread
        // otherwise there will be a few ms difference between reactionDelay
        // and value displaced on screen for the post response delay
        withContext(Main) { listener?.onReactionDelayUpdate(reactionDelay) }

        return Result(
                remainingTestCount,
                startTimestamp,
                interval,
                reactionDelay
        )
    }

    private fun getRandomIntervalDelay(): Long = (args.minInterval..args.maxInterval).random()

    private fun <T> MutableList<T>.addSafe(item: T?) {
        item?.let {
            this.add(it)
        }
    }

    private fun <T> MutableList<T>.toJson(): String = Gson().toJson(this)

    internal class Instructions : State {
        override fun consumeAction(action: Action): State {
            return when (action) {
                is Action.StartCountdown -> Countdown()
                is Action.Restart -> INIT_STATE
                else -> throw IllegalStateException()
            }
        }
    }

    internal class Countdown : State {
        override fun consumeAction(action: Action): State {
            return when (action) {
                is Action.StartInterval -> Interval()
                is Action.Restart -> INIT_STATE
                else -> throw IllegalStateException()
            }
        }
    }

    internal class Interval : State {
        override fun consumeAction(action: Action): State {
            return when (action) {
                is Action.ShowStimulus -> StimulusShowing()
                is Action.InvalidReaction -> InvalidReaction()
                is Action.Restart -> INIT_STATE
                else -> throw IllegalStateException()
            }
        }
    }

    internal class StimulusShowing : State {
        override fun consumeAction(action: Action): State {
            return when (action) {
                is Action.ValidReaction -> ValidReaction(action.reactionDelay)
                is Action.InvalidReaction -> InvalidReaction()
                is Action.Restart -> INIT_STATE
                else -> throw IllegalStateException()
            }
        }
    }

    internal class ValidReaction(val reactionDelay: Long) : State {
        override fun consumeAction(action: Action): State {
            return when (action) {
                is Action.StartInterval -> Interval()
                is Action.Complete -> Complete()
                is Action.Restart -> INIT_STATE
                else -> throw IllegalStateException()
            }
        }
    }

    internal class InvalidReaction : State {
        override fun consumeAction(action: Action): State {
            return when (action) {
                is Action.StartInterval -> Interval()
                is Action.Restart -> INIT_STATE
                else -> throw IllegalStateException()
            }
        }
    }

    internal class Complete : State {
        override fun consumeAction(action: Action): State {
            return when (action) {
                is Action.Restart -> INIT_STATE
                else -> this
            }
        }
    }

    internal sealed class Action {
        object Restart : Action()
        object StartCountdown : Action()
        object StartInterval : Action()
        object ShowStimulus : Action()
        class ValidReaction(val reactionDelay: Long) : Action()
        object InvalidReaction : Action()
        object Complete : Action()
    }

    internal interface State {
        fun consumeAction(action: Action): State
    }

    internal interface Listener {
        fun onStateUpdate(newState: State) = Unit
        fun onCountdownUpdate(millisElapsed: Long) = Unit
        fun onReactionDelayUpdate(millisElapsed: Long) = Unit
        fun onCompleteTest(jsonResults: String) = Unit
    }

    internal interface StimulusListener {
        fun onStimulus() = Unit
    }

    internal fun setListener(listener: Listener) {
        this.listener = listener
    }

    internal fun setStimulusListener(listener: StimulusListener) {
        stimulusListener = listener
    }

    private fun notifyStateChange(newState: State) {
        CoroutineScope(Main).launch {
            listener?.onStateUpdate(newState)
        }
    }

    private companion object {
        private const val TAG = "PVT"
        private const val LOG_STATE_TRANSITIONS: Boolean = false
        private val INIT_STATE = Instructions()
    }

}
