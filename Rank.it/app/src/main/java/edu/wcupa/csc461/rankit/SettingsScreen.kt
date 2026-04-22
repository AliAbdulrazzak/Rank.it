package edu.wcupa.csc461.rankit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.wcupa.csc461.rankit.ui.theme.RankitTheme
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    fontScale: Float,
    onFontScaleChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Font Size Setting
        Text(
            text = "Font Size",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "Adjust the application font scale",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Slider(
            value = fontScale,
            onValueChange = onFontScaleChange,
            valueRange = 0.8f..1.5f,
            steps = 6,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = "Scale: ${(fontScale * 100).roundToInt()}%",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.align(Alignment.End)
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    RankitTheme {
        SettingsScreen(fontScale = 1.0f, onFontScaleChange = {})
    }
}
