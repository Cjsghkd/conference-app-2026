package io.github.droidkaigi.confsched.jetwhale.protocol

/**
 * Pairs the app's agent plugin with its host counterpart. The app contributes one plugin holding
 * every debug control it offers, so a new control is another message type here rather than a new id.
 *
 * It carries the year for the same reason the application id does: a host installation holds one jar
 * per id, and next year's app is a different debuggee.
 */
const val KAIGI_PLUGIN_ID: String = "io.github.droidkaigi.confsched2026"
