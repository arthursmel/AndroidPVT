# AndroidPvt

A psychomotor vigilance task (PVT) for Android.

[![arthursmel](https://circleci.com/gh/arthursmel/AndroidPvt.svg?style=svg)](https://app.circleci.com/pipelines/github/arthursmel/AndroidPvt) [![Maven Central](https://img.shields.io/maven-central/v/rs.arthu/androidpvt.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22rs.arthu%22%20AND%20a:%22androidpvt%22)

# Installation
`implementation("rs.arthu:androidpvt:1.1.0")`

# Usage
Create an Intent using the Builder.
```
val pvtActivityIntent = PvtActivity.Builder()
    .withStimulusCount(3) // Number of stimuli a user will be shown
    .withCountdownTime(3 * 1000) // 3 second countdown
    .withInterval(2 * 1000, 4 * 1000) // random interval between 2 and 4 seconds duration
    .withPostResponseDelay(2 * 1000) // The time the response will be shown to the user for
    .withStimulusTimeout(10 * 1000) // The maximum duration a user can take to respond
    .build(this)
```
  Start the PvtActivity with a request code
`startActivityForResult(pvtActivityIntent, PVT_REQUEST)`

Results will then be returned in JSON format (for now)
```
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (resultCode != RESULT_OK) return
    val jsonResults: String? = when (requestCode) {
        PVT_REQUEST -> {
            data?.getStringExtra(PVT_RESULTS_KEY)
        }
        else -> "No results"
  }
}
```

# References
The behaviour of the PVT is inspired by [Android cognitive test battery](https://github.com/movisens/AndroidCognitiveTestBattery)
