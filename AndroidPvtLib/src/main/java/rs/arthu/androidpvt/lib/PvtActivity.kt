package rs.arthu.androidpvt.lib

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import rs.arthu.androidpvt.lib.databinding.ActivityPvtBinding

class PvtActivity : AppCompatActivity() {

    private var pvt: Pvt? = null
    private lateinit var binding: ActivityPvtBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPvtBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val intentExtras = intent.extras
        val pvtArgs = getPvtArgsFromIntentExtras(intentExtras)

        Log.d(TAG, "args: $pvtArgs")

        pvt = Pvt(pvtArgs)

        pvt?.setListener(
            onStartCountdown = {
                binding.textViewCountdown.visibility = View.VISIBLE
                binding.textViewMessage.text = getString(R.string.ready_message)
                binding.textViewMessage.visibility = View.VISIBLE
                binding.viewStimulus.visibility = View.GONE
            },
            onCountdownUpdate = { updateCountdown(it) },
            onFinishCountdown = {
                binding.textViewCountdown.visibility = View.GONE
                binding.textViewMessage.visibility = View.GONE
                binding.textViewMessage.text = ""
            },
            onIntervalShowing = {
                binding.textViewMessage.text = ""
                binding.textViewMessage.visibility = View.GONE
            },
            onStimulusShowing = {
                binding.viewStimulus.visibility = View.VISIBLE
                binding.textViewMessage.visibility = View.VISIBLE
            },
            onStimulusHidden = {
                binding.viewStimulus.visibility = View.GONE
                binding.textViewMessage.visibility = View.GONE
            },
            onReactionDelayUpdate = {
                updateReactionDelay(it)
            },
            onInvalidReaction = {
                binding.textViewMessage.text = getString(R.string.invalid_reaction)
                binding.textViewMessage.visibility = View.VISIBLE
            },
            onCompleteTest = {
                binding.textViewMessage.visibility = View.VISIBLE
                binding.textViewMessage.text = getString(R.string.test_complete)
            },
            onResults = {
                val returnIntent = Intent()
                returnIntent.putExtra(PVT_RESULTS_KEY, it)
                setResult(RESULT_OK, returnIntent)
                finish()
            },
        )
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)

        if (event?.action == MotionEvent.ACTION_DOWN) {
            pvt?.handleActionDownTouchEvent()
        }

        return true
    }

    private fun updateCountdown(millisElapsed: Long) {
        binding.textViewCountdown.text = (millisElapsed / 1000).toString()
    }

    private fun updateReactionDelay(millisElapsed: Long) {
        binding.textViewMessage.text = getString(R.string.reaction_delay, millisElapsed)
    }

    private fun getPvtArgsFromIntentExtras(bundle: Bundle?): Pvt.Args {
        if (bundle == null) return Pvt.Args.default()

        val keySet = bundle.keySet()

        var stimulusCount: Int? = null
        var minInterval: Long? = null
        var maxInterval: Long? = null
        var countDownTime: Long? = null
        var stimulusTimeout: Long? = null
        var postResponseDelay: Long? = null

        if (keySet.contains(STIMULUS_COUNT)) {
            stimulusCount = bundle.getInt(STIMULUS_COUNT)
        }

        if (keySet.contains(MIN_INTERVAL)) {
            minInterval = bundle.getLong(MIN_INTERVAL)
        }

        if (keySet.contains(MAX_INTERVAL)) {
            maxInterval = bundle.getLong(MAX_INTERVAL)
        }

        if (keySet.contains(COUNTDOWN_TIME)) {
            countDownTime = bundle.getLong(COUNTDOWN_TIME)
        }

        if (keySet.contains(STIMULUS_TIMEOUT)) {
            stimulusTimeout = bundle.getLong(STIMULUS_TIMEOUT)
        }

        if (keySet.contains(POST_RESPONSE_DELAY)) {
            postResponseDelay = bundle.getLong(POST_RESPONSE_DELAY)
        }

        return Pvt.Args(
            stimulusCount, minInterval, maxInterval, countDownTime, stimulusTimeout, postResponseDelay
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        pvt?.cancel()
    }

    class Builder {
        private var stimulusCount: Int? = null
        private var minInterval: Long? = null
        private var maxInterval: Long? = null
        private var countDownTime: Long? = null
        private var stimulusTimeout: Long? = null
        private var postResponseDelay: Long? = null

        fun withStimulusCount(count: Int): Builder {
            this.stimulusCount = count
            return this
        }

        fun withInterval(minInterval: Long, maxInterval: Long): Builder {
            this.minInterval = minInterval
            this.maxInterval = maxInterval
            return this
        }

        fun withCountdownTime(time: Long): Builder {
            this.countDownTime = time
            return this
        }

        fun withStimulusTimeout(timeout: Long): Builder {
            this.stimulusTimeout = timeout
            return this
        }

        fun withPostResponseDelay(delay: Long): Builder {
            this.postResponseDelay = delay
            return this
        }

        fun build(context: Context): Intent {
            val intent = Intent(context, PvtActivity::class.java)

            stimulusCount?.let {
                intent.putExtra(STIMULUS_COUNT, it)
            }

            minInterval?.let {
                intent.putExtra(MIN_INTERVAL, it)
            }

            maxInterval?.let {
                intent.putExtra(MAX_INTERVAL, it)
            }

            countDownTime?.let {
                intent.putExtra(COUNTDOWN_TIME, it)
            }

            stimulusTimeout?.let {
                intent.putExtra(STIMULUS_TIMEOUT, it)
            }

            postResponseDelay?.let {
                intent.putExtra(POST_RESPONSE_DELAY, it)
            }

            return intent
        }
    }

    companion object {
        private const val TAG = "PvtNew"
        const val PVT_RESULTS_KEY = "pvtResultsKey"
        const val STIMULUS_COUNT = "stimulusCount"
        const val MIN_INTERVAL = "minInterval"
        const val MAX_INTERVAL = "maxInterval"
        const val COUNTDOWN_TIME = "countdownTime"
        const val STIMULUS_TIMEOUT = "stimulusTimeout"
        const val POST_RESPONSE_DELAY = "postResponseDelay"
    }
}
