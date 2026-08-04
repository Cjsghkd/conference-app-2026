package io.github.droidkaigi.confsched.feature.eventmap

sealed interface EventMapScreenAction {
    data class SelectFloor(val floor: EventMapFloor) : EventMapScreenAction
}
