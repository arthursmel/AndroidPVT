package rs.arthu.androidpvt.lib

import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import rs.arthu.androidpvt.lib.Utils.Companion.addSafe
import rs.arthu.androidpvt.lib.Utils.Companion.toJson
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

    private var curState by Delegates.observable<PvtState.State>(PvtState.INIT_STATE, {
            _, _, newState ->
        notifyStateChange(newState)
    })

    fun handleActionDownTouchEvent() {
        val reactionTimestamp = System.currentTimeMillis()

        when (curState) {
            is PvtState.Instructions -> {
                // Checking if job is null, otherwise if user quickly presses instructions
                // multiple jobs may be run
                if (curJob == null) {
                    runTest()
                }
            }
            is PvtState.Interval -> {
                curState = curState.consumeAction(PvtState.Action.InvalidReaction)
            }
            is PvtState.StimulusShowing -> {
                curState = curState.consumeAction(PvtState.Action.ValidReaction(reactionTimestamp))
            }
            is PvtState.Countdown,
            is PvtState.InvalidReaction,
            is PvtState.ValidReaction,
            is PvtState.Complete -> {}
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

                if (curState is PvtState.InvalidReaction) {
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
        curState = curState.consumeAction(PvtState.Action.StartInterval)
        delay(delay)
    }

    private suspend fun runStimulus(startTimestamp: Long, interval: Long): Result? {
        curState = curState.consumeAction(PvtState.Action.ShowStimulus)

        while (testHasNotTimedOut(startTimestamp) &&
                !validReactionHasOccurred() &&
                !jobCancelled()) {

            withContext(Main) {
                listener?.onReactionDelayUpdate(timeSinceCalled(startTimestamp))
            }
        }

        return when (curState) {
            is PvtState.ValidReaction -> handleValidReaction(startTimestamp, interval)
            else -> {
                curState = curState.consumeAction(PvtState.Action.InvalidReaction)
                null // returning null as test timed out, no result created
            }
        }
    }

    private fun validReactionHasOccurred() = curState is PvtState.ValidReaction

    private fun testHasNotTimedOut(startTimestamp: Long) = timeSinceCalled(startTimestamp) < args.stimulusTimeout

    private fun jobCancelled() = curJob?.isCancelled ?: true

    private fun timeSinceCalled(startTimestamp: Long) = System.currentTimeMillis() - startTimestamp

    private suspend fun runCountdown() {
        curState = curState.consumeAction(PvtState.Action.StartCountdown)

        (args.countDownTime downTo ONE_SECOND step ONE_SECOND).forEach { i ->
            withContext(Main) {
                listener?.onCountdownUpdate(i)
            }
            delay(ONE_SECOND)
        }
    }

    private suspend fun handleCompletePvt() {
        curState = curState.consumeAction(PvtState.Action.Complete)

        delay(args.postResponseDelay)

        withContext(Main) {
            listener?.onCompleteTest(results.toJson())
        }
    }

    private suspend fun handleValidReaction(startTimestamp: Long, interval: Long): Result {
        val reactionTimestamp = (curState as PvtState.ValidReaction).reactionDelay
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

    internal interface Listener {
        fun onStateUpdate(newState: PvtState.State) = Unit
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

    private fun notifyStateChange(newState: PvtState.State) {
        CoroutineScope(Main).launch {
            listener?.onStateUpdate(newState)
        }
    }
}
