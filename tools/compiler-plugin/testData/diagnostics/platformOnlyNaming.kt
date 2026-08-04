// The reverse rule (a platform prefix requires @PlatformOnly) only applies under /commonMain/, and
// the test framework gives every source file the path "/<file name>", so it cannot be covered here.
import io.github.droidkaigi.confsched.core.common.PlatformOnly
import io.github.droidkaigi.confsched.core.common.TargetPlatform

<!PLATFORM_ONLY_NAME_MISMATCH!>@PlatformOnly(TargetPlatform.Ios)
fun HapticsSyncEffect() {
}<!>

@PlatformOnly(TargetPlatform.Ios)
fun IosHapticsSyncEffect() {
}

fun plainEffect() {
}
