package rs.arthu.androidpvt.lib

import java.lang.IllegalStateException

class PvtState {
    internal class Instructions : State {
        override fun consumeAction(action: Action): State {
            return when (action) {
                is Action.StartCountdown -> Countdown()
                is Action.Restart -> INIT_STATE
                else -> throw IllegalStateException()
            }
        }
    }

    internal class Countdown : State {
        override fun consumeAction(action: Action): State {
            return when (action) {
                is Action.StartInterval -> Interval()
                is Action.Restart -> INIT_STATE
                else -> throw IllegalStateException()
            }
        }
    }

    internal class Interval : State {
        override fun consumeAction(action: Action): State {
            return when (action) {
                is Action.ShowStimulus -> StimulusShowing()
                is Action.InvalidReaction -> InvalidReaction()
                is Action.Restart -> INIT_STATE
                else -> throw IllegalStateException()
            }
        }
    }

    internal class StimulusShowing : State {
        override fun consumeAction(action: Action): State {
            return when (action) {
                is Action.ValidReaction -> ValidReaction(action.reactionDelay)
                is Action.InvalidReaction -> InvalidReaction()
                is Action.Restart -> INIT_STATE
                else -> throw IllegalStateException()
            }
        }
    }

    internal class ValidReaction(val reactionDelay: Long) : State {
        override fun consumeAction(action: Action): State {
            return when (action) {
                is Action.StartInterval -> Interval()
                is Action.Complete -> Complete()
                is Action.Restart -> INIT_STATE
                else -> throw IllegalStateException()
            }
        }
    }

    internal class InvalidReaction : State {
        override fun consumeAction(action: Action): State {
            return when (action) {
                is Action.StartInterval -> Interval()
                is Action.Restart -> INIT_STATE
                else -> throw IllegalStateException()
            }
        }
    }

    internal class Complete : State {
        override fun consumeAction(action: Action): State {
            return when (action) {
                is Action.Restart -> INIT_STATE
                else -> this
            }
        }
    }

    internal sealed class Action {
        object Restart : Action()
        object StartCountdown : Action()
        object StartInterval : Action()
        object ShowStimulus : Action()
        class ValidReaction(val reactionDelay: Long) : Action()
        object InvalidReaction : Action()
        object Complete : Action()
    }

    internal interface State {
        fun consumeAction(action: Action): State
    }

    internal companion object {
        val INIT_STATE = Instructions()
    }
}
