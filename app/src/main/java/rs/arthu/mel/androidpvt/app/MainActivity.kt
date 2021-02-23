package rs.arthu.mel.androidpvt.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import rs.arthu.mel.androidpvt.lib.PvtActivity.Companion.NUMBER_OF_STIMULUS_KEY
import rs.arthu.mel.androidpvt.app.databinding.ActivityMainBinding
import rs.arthu.mel.androidpvt.lib.PvtActivity

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
        val intent = Intent(this, PvtActivity::class.java)
        intent.putExtra(NUMBER_OF_STIMULUS_KEY, 3)
        startActivityForResult(intent, PVT_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != RESULT_OK) return

        val jsonResults: String? = when (requestCode) {
            PVT_REQUEST -> {
                data?.getStringExtra(PvtActivity.PVT_RESULTS_KEY)
            }
            else -> {
                "No results"
            }
        }

        Toast.makeText(this, jsonResults, Toast.LENGTH_LONG).show()
        Log.d(TAG, jsonResults!!)
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val PVT_REQUEST = 1
    }
}
