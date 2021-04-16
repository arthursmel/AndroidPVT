package rs.arthu.androidpvt.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import rs.arthu.androidpvt.app.databinding.ActivityMainBinding
import rs.arthu.androidpvt.lib.PVT_RESULTS_KEY
import rs.arthu.androidpvt.lib.PvtActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.buttonPvt.setOnClickListener {
            startPvtActivity()
        }
    }
    
    private fun startPvtActivity() {
        val pvtActivityIntent = PvtActivity.Builder()
            .withStimulusCount(3)
            .withCountdownTime(3 * 1000)
            .withInterval(2 * 1000, 4 * 1000)
            .withPostResponseDelay(2 * 1000)
            .withStimulusTimeout(10 * 1000)
            .build(this)

        startActivityForResult(pvtActivityIntent, PVT_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != RESULT_OK) return

        val jsonResults: String? = when (requestCode) {
            PVT_REQUEST -> {
                data?.getStringExtra(PVT_RESULTS_KEY)
            }
            else -> "No results"
        }

        Toast.makeText(this, jsonResults, Toast.LENGTH_LONG).show()
    }

    private companion object {
        private const val PVT_REQUEST = 1
    }
}
