package rs.arthu.mel.androidpvt.lib

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import rs.arthu.mel.androidpvt.lib.databinding.ActivityPvtBinding

class PvtActivity : AppCompatActivity() {

    private var pvt: Pvt? = null
    private lateinit var binding: ActivityPvtBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPvtBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val numberOfStimulus =
            intent.getIntExtra(NUMBER_OF_STIMULUS_KEY, DEFAULT_NUMBER_OF_STIMULUS)

        pvt = Pvt(numberOfStimulus)
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

    override fun onDestroy() {
        super.onDestroy()
        pvt?.cancel()
    }

    companion object {
        private const val TAG = "PvtNew"
        const val PVT_RESULTS_KEY = "pvtResultsKey"
        const val NUMBER_OF_STIMULUS_KEY = "numberOfStimulus"
        const val DEFAULT_NUMBER_OF_STIMULUS = 3
    }
}
