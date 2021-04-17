# AndroidPvt

A psychomotor vigilance task (PVT) for Android.

[![arthursmel](https://circleci.com/gh/arthursmel/AndroidPvt.svg?style=svg)](https://app.circleci.com/pipelines/github/arthursmel/AndroidPvt) [![Maven Central](https://img.shields.io/maven-central/v/rs.arthu/androidpvt.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22rs.arthu%22%20AND%20a:%22androidpvt%22)

## Installation
```swift
implementation("rs.arthu:androidpvt:1.2.0")
```

## Usage
Create an Intent using the Builder:
```swift
val pvtActivityIntent = PvtActivity.Builder()
    .withTestCount(3) //
    .withCountdownTime(3 * 1000)
    .withInterval(2 * 1000, 4 * 1000)
    .withPostResponseDelay(2 * 1000)
    .withStimulusTimeout(10 * 1000)
    .build(this)
```

Start the PvtActivity with a request code:
```swift
startActivityForResult(pvtActivityIntent, PVT_REQUEST)
```

Builder methods:

method | description | Default Value
--- | --- | ---
`.withTestCount(count: Int)` | Number of tests a user will be asked to complete | 3
`.withCountdownTime(time: Long)` | The countdown timer duration before the test starts | 3000ms
`.withInterval(min: Long, max: Long)` | The interval used to general a random waiting duration before the stimulus is shown | 2000ms, 4000ms
`.withStimulusTimeout(timeout: Long)` | The maximum duration a user can take to respond | 10000ms
`.withPostResponseDelay(delay: Long)` | The time the user's response will be held on the screen for | 2000ms


Results will then be returned as a `List<HashMap<String, Number>>`
```swift
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    if (resultCode != RESULT_OK) return

    val results: List<HashMap<String, Number>>? = when (requestCode) {
        PVT_REQUEST -> {
            val list = data?.getSerializableExtra(PVT_RESULTS_KEY) as List<*>
            list.filterIsInstance<HashMap<String, Number>>().takeIf { it.size == list.size }
        }
        else -> null
    }
}
```
Result format:
```json
[
  {
    "interval": "<the random wait time before the stimulus is shown>",
    "reactionDelay": "<the time it took for the user to response to the stimulus>",
    "testNumber": "<the index of the test the user has completed>",
    "timestamp": "<timestamp of reaction>"
  }
]
```

## References
The behaviour of the PVT is inspired by [Android cognitive test battery](https://github.com/movisens/AndroidCognitiveTestBattery)
