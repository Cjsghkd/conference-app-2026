package io.github.droidkaigi.confsched.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

private val bands: List<Pair<Color, String>> = listOf(
    Color(0xFFFF1744) to "RED",
    Color(0xFFFF9100) to "ORANGE",
    Color(0xFFFFEA00) to "YELLOW",
    Color(0xFF00E676) to "GREEN",
    Color(0xFF00B0FF) to "CYAN",
    Color(0xFF2979FF) to "BLUE",
    Color(0xFFD500F9) to "MAGENTA",
    Color(0xFF000000) to "BLACK",
    Color(0xFFFFFFFF) to "WHITE",
)

@Composable
private fun ColorfulScrollContent(initialScrollIndex: Int) {
    val rows = (0 until 6).flatMap { cycle -> bands.map { cycle to it } }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialScrollIndex)
    Box(Modifier.fillMaxSize().background(Color(0xFF101010))) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(rows) { (cycle, band) ->
                val (color, name) = band
                val textColor = if (name == "WHITE" || name == "YELLOW") Color.Black else Color.White
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp)
                        .background(color, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        text = "$name #$cycle",
                        color = textColor,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Black,
                    )
                }
            }
        }
    }
}

fun GlassSpikeViewController(initialScrollIndex: Int): UIViewController = ComposeUIViewController {
    ColorfulScrollContent(initialScrollIndex = initialScrollIndex)
}
