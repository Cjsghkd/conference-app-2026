package io.github.droidkaigi.confsched.feature.debug

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.droidkaigi.confsched.core.common.NoopSoilErrorMonitor
import io.github.droidkaigi.confsched.core.common.SoilErrorMonitor
import io.github.droidkaigi.confsched.core.preview.wrapper.KaigiPreviewWrapper
import io.github.droidkaigi.confsched.core.ui.safeClick
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import soil.query.core.ErrorRecord
import soil.query.core.ErrorRelay

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, replaces = [NoopSoilErrorMonitor::class])
class DebugSoilErrorMonitor(
    private val debugPreferencesStore: DebugPreferencesStore,
    private val errorRelay: ErrorRelay,
) : SoilErrorMonitor {
    private val monitorScope = CoroutineScope(SupervisorJob())

    // In-memory history of every relayed error, oldest first. Collected in the monitor's own
    // scope so errors accumulate even while the overlay is disabled or not composed.
    val errors: StateFlow<List<SoilError>>
        field = MutableStateFlow<List<SoilError>>(emptyList())

    init {
        monitorScope.launch {
            errorRelay.receiveAsFlow().collect { error ->
                errors.update { it + error.toSoilError() }
            }
        }
    }

    @Composable
    override fun Overlay() {
        val errors by errors.collectAsState()
        // The sheet auto-opens whenever an error arrives past the last dismissal.
        var dismissedCount by remember { mutableIntStateOf(0) }
        // When disabled, behaves like production: errors are swallowed silently.
        val enabled by debugPreferencesStore.soilErrorOverlayEnabled.collectAsState(initial = true)
        if (enabled && errors.size > dismissedCount) {
            SoilErrorBottomSheet(
                errors = errors,
                onDismiss = { dismissedCount = errors.size },
            )
        }
    }
}

private fun ErrorRecord.toSoilError() = SoilError(keyId = "$keyId", exception = exception)

// ModalBottomSheet renders into a popup window, which a preview captures as an empty tree, so the
// sheet itself has no preview and SoilErrorSheetContent carries one instead.
@Suppress("UI_COMPONENT_WITHOUT_PREVIEW")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SoilErrorBottomSheet(
    errors: List<SoilError>,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        SoilErrorSheetContent(errors = errors)
    }
}

// Split from SoilErrorBottomSheet: ModalBottomSheet renders into a popup window, which a preview
// captures as an empty tree, so the content is previewed on its own.
@Composable
internal fun SoilErrorSheetContent(errors: List<SoilError>) {
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    var selectedIndex by remember { mutableIntStateOf(errors.lastIndex) }
    // Jump to the newest error when a new one arrives while the sheet is open.
    LaunchedEffect(errors.size) { selectedIndex = errors.lastIndex }

    val error = errors[selectedIndex.coerceIn(errors.indices)]
    val stackTrace = remember(error, error.exception::stackTraceToString)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Soil error", fontWeight = FontWeight.Bold)
            Text("${selectedIndex + 1} / ${errors.size}")
            Button(
                onClick = safeClick { selectedIndex-- },
                enabled = selectedIndex > 0,
            ) { Text("◀") }
            Button(
                onClick = safeClick { selectedIndex++ },
                enabled = selectedIndex < errors.lastIndex,
            ) { Text("▶") }
        }
        Text("Key: ${error.keyId}")
        Text(error.exception.message ?: "(no message)")
        Text(
            text = stackTrace,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
                .heightIn(max = 240.dp)
                .verticalScroll(rememberScrollState())
                .horizontalScroll(rememberScrollState()),
        )
        Button(
            onClick = safeClick {
                coroutineScope.launch { clipboard.setClipEntry(clipEntryOfPlainText(stackTrace)) }
            },
        ) { Text("Copy stack trace") }
    }
}

@PreviewWrapper(KaigiPreviewWrapper::class)
@Preview
@Composable
fun SoilErrorSheetContentPreview() {
    SoilErrorSheetContent(errors = listOf(SoilError.fake()))
}
