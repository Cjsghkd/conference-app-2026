package io.github.droidkaigi.confsched.core.testing

/**
 * A screen behaviour test driven by a [Robot]. The Android host tests render through Robolectric,
 * which engages only for a class carrying a JUnit runner, and that annotation cannot be written in
 * common code — so it rides here, where `@RunWith` being `@Inherited` carries it to every subclass.
 */
expect abstract class RobotTest()
