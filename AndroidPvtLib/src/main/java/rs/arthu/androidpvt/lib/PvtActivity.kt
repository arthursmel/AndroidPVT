package rs.arthu.androidpvt.lib

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import rs.arthu.androidpvt.lib.databinding.ActivityPvtBinding
import java.io.Serializable

const val PVT_RESULTS_KEY = "pvtResultsKey"
internal const val TEST_COUNT = "testCount"
internal const val MIN_INTERVAL = "minInterval"
internal const val MAX_INTERVAL = "maxInterval"
internal const val COUNTDOWN_TIME = "countdownTime"
internal const val STIMULUS_TIMEOUT = "stimulusTimeout"
internal const val POST_RESPONSE_DELAY = "postResponseDelay"

class PvtActivity : AppCompatActivity(), Pvt.StimulusListener {

    private lateinit var binding: ActivityPvtBinding
    private lateinit var viewModel: PvtViewModel
    private lateinit var viewModelFactory: PvtViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPvtBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        initViewModel()
    }

    private fun initViewModel() {
        val intentExtras = intent.extras
        val pvtArgs = Args.fromBundle(intentExtras)

        viewModelFactory = PvtViewModelFactory(pvtArgs)
        viewModel = ViewModelProvider(this, viewModelFactory).get(PvtViewModel::class.java)
        viewModel.pvt.setStimulusListener(this)

        viewModel.countdown.observe(this, {
            updateCountdown(it)
        })

        viewModel.reactionDelay.observe(this, {
            updateReactionDelay(it)
        })

        viewModel.results.observe(this, {
            returnResults(it)
        })

        viewModel.pvtState.observe(this, stateObserver)
    }

    private val stateObserver = Observer<PvtState.State> {
        when (it) {
            is PvtState.Instructions -> displayInstructions()
            is PvtState.Countdown -> displayCountdown()
            is PvtState.Interval -> displayInterval()
            is PvtState.StimulusShowing -> {} // Stimulus listener handles
            // state change here, otherwise time taken to show stimulus
            // affects result significantly
            is PvtState.InvalidReaction -> displayInvalidReaction()
            is PvtState.ValidReaction -> {}
            is PvtState.Complete -> displayComplete()
            else -> throw IllegalStateException()
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)

        if (event?.action == MotionEvent.ACTION_DOWN) {
            // Shortcutting the view model, directly sending touch event to pvt
            viewModel.pvt.handleActionDownTouchEvent()
        }

        return true
    }

    private fun updateCountdown(millisElapsed: String) {
        binding.textViewSub.text = millisElapsed
    }

    private fun updateReactionDelay(millisElapsed: String) {
        binding.textViewMain.text = getString(R.string.reaction_delay, millisElapsed)
    }

    private fun displayInstructions() {
        binding.viewStimulus.visibility = View.GONE

        binding.textViewMain.visibility = View.VISIBLE
        binding.textViewMain.text = getString(R.string.instructions)

        binding.textViewSub.visibility = View.GONE
        binding.textViewSub.text = ""
    }

    private fun displayCountdown() {
        binding.viewStimulus.visibility = View.GONE

        binding.textViewMain.text = getString(R.string.ready_message)
        binding.textViewMain.visibility = View.VISIBLE

        binding.textViewSub.visibility = View.VISIBLE
    }

    private fun displayInterval() {
        binding.textViewMain.text = ""
        binding.textViewMain.visibility = View.GONE

        binding.textViewSub.text = ""
        binding.textViewSub.visibility = View.GONE

        binding.viewStimulus.visibility = View.GONE
    }

    override fun onStimulus() {
        binding.viewStimulus.visibility = View.VISIBLE
        binding.textViewMain.visibility = View.VISIBLE
    }

    private fun displayInvalidReaction() {
        binding.viewStimulus.visibility = View.GONE

        binding.textViewMain.text = getString(R.string.invalid_reaction)
        binding.textViewMain.visibility = View.VISIBLE
    }

    private fun displayComplete() {
        binding.viewStimulus.visibility = View.GONE

        binding.textViewMain.visibility = View.VISIBLE
        binding.textViewMain.text = getString(R.string.test_complete)
    }

    private fun returnResults(results: List<Result>) {
        val returnIntent = Intent()

        returnIntent.putExtra(PVT_RESULTS_KEY, results as Serializable)

        setResult(RESULT_OK, returnIntent)
        finish()
    }

    class Builder {
        private var testCount: Int? = null
        private var minInterval: Long? = null
        private var maxInterval: Long? = null
        private var countDownTime: Long? = null
        private var stimulusTimeout: Long? = null
        private var postResponseDelay: Long? = null

        fun withTestCount(count: Int): Builder {
            this.testCount = count
            return this
        }

        fun withInterval(min: Long, max: Long): Builder {
            this.minInterval = min
            this.maxInterval = max
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

            testCount?.let {
                intent.putExtra(TEST_COUNT, it)
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
}
