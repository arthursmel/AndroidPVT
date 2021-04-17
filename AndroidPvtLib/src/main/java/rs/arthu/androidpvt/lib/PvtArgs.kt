package rs.arthu.androidpvt.lib

import android.os.Bundle

internal class Args(
    var testCount: Int = DEFAULT_TEST_COUNT,
    var minInterval: Long = DEFAULT_MIN_INTERVAL,
    var maxInterval: Long = DEFAULT_MAX_INTERVAL,
    var countDownTime: Long = DEFAULT_COUNTDOWN_TIME,
    var stimulusTimeout: Long = DEFAULT_STIMULUS_TIMEOUT,
    var postResponseDelay: Long = DEFAULT_POST_RESPONSE_DELAY,
    ) {

    constructor(
            testCount: Int? = null,
            minInterval: Long? = null,
            maxInterval: Long? = null,
            countDownTime: Long? = null,
            stimulusTimeout: Long? = null,
            postResponseDelay: Long? = null
    ) : this(
        testCount ?: DEFAULT_TEST_COUNT,
            minInterval ?: DEFAULT_MIN_INTERVAL,
            maxInterval ?: DEFAULT_MAX_INTERVAL,
            countDownTime ?: DEFAULT_COUNTDOWN_TIME,
            stimulusTimeout ?: DEFAULT_STIMULUS_TIMEOUT,
            postResponseDelay ?: DEFAULT_POST_RESPONSE_DELAY
    )

    companion object {
        fun default(): Args {
            return Args(
                    DEFAULT_TEST_COUNT,
                    DEFAULT_MIN_INTERVAL,
                    DEFAULT_MAX_INTERVAL,
                    DEFAULT_COUNTDOWN_TIME,
                    DEFAULT_STIMULUS_TIMEOUT,
                    DEFAULT_POST_RESPONSE_DELAY
            )
        }

        fun fromBundle(bundle: Bundle?): Args {
            if (bundle == null) return default()

            val keySet = bundle.keySet()

            var testCount: Int? = null
            var minInterval: Long? = null
            var maxInterval: Long? = null
            var countDownTime: Long? = null
            var stimulusTimeout: Long? = null
            var postResponseDelay: Long? = null

            if (keySet.contains(TEST_COUNT)) {
                testCount = bundle.getInt(TEST_COUNT)
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

            return Args(
                testCount,
                    minInterval,
                    maxInterval,
                    countDownTime,
                    stimulusTimeout,
                    postResponseDelay
            )
        }
    }
}

