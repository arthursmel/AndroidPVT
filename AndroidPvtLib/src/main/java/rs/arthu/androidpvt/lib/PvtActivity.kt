package rs.arthu.androidpvt.lib

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.lifecycle.ViewModelProvider
import rs.arthu.androidpvt.lib.databinding.ActivityPvtBinding

const val PVT_RESULTS_KEY = "pvtResultsKey"
internal const val STIMULUS_COUNT = "stimulusCount"
internal const val MIN_INTERVAL = "minInterval"
internal const val MAX_INTERVAL = "maxInterval"
internal const val COUNTDOWN_TIME = "countdownTime"
internal const val STIMULUS_TIMEOUT = "stimulusTimeout"
internal const val POST_RESPONSE_DELAY = "postResponseDelay"

class PvtActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPvtBinding
    private lateinit var viewModel: PvtViewModel
    private lateinit var viewModelFactory: PvtViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPvtBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val intentExtras = intent.extras
        val pvtArgs = PvtArgs.fromBundle(intentExtras)

        viewModelFactory = PvtViewModelFactory(pvtArgs)
        viewModel = ViewModelProvider(this, viewModelFactory).get(PvtViewModel::class.java)

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)

        return viewModel.handleOnTouchEvent(event)
    }

    private fun updateCountdown(millisElapsed: Long) {
        binding.textViewCountdown.text = (millisElapsed / 1000).toString()
    }

    private fun updateReactionDelay(millisElapsed: Long) {
        binding.textViewMessage.text = getString(R.string.reaction_delay, millisElapsed)
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
    }
}
