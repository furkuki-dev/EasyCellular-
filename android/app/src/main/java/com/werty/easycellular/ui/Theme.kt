package com.werty.easycellular.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.werty.easycellular.core.Gen
import com.werty.easycellular.i18n.L
import com.werty.easycellular.i18n.LocalL

val EcBgTop = Color(0xFF070C1F)
val EcBgBottom = Color(0xFF10183A)
val EcAccent = Color(0xFF59D8FF)
val EcAccent2 = Color(0xFF9973FF)
val EcGood = Color(0xFF4DE699)
val EcWarn = Color(0xFFFFBF4D)
val EcBad = Color(0xFFFF6673)

val EcColors = darkColorScheme(
    primary = EcAccent,
    onPrimary = Color(0xFF00222E),
    secondary = EcAccent2,
    onSecondary = Color.White,
    background = EcBgTop,
    onBackground = Color.White,
    surface = Color(0xFF121A36),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1B2446),
    onSurfaceVariant = Color(0xFFB8C2E0)
)

fun Gen.title(t: L): String = when (this) {
    Gen.NONE -> t.t("gen_none")
    Gen.WIFI -> t.t("gen_wifi")
    Gen.G2 -> "2G"
    Gen.G3 -> "3G"
    Gen.G4 -> "4G"
    Gen.G5 -> "5G"
    Gen.G5_PLUS -> "5G+"
    Gen.UNKNOWN -> t.t("gen_unknown")
}

@Composable
fun GlassCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.07f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
}

@Composable
fun InfoRow(title: String, value: String, valueColor: Color = Color.Unspecified) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = title,
            color = Color.White.copy(alpha = 0.6f),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = value,
            fontWeight = FontWeight.SemiBold,
            color = if (valueColor == Color.Unspecified) Color.White else valueColor,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

fun fmtDb(value: Int?, unit: String): String = value?.let { "$it $unit" } ?: "—"
