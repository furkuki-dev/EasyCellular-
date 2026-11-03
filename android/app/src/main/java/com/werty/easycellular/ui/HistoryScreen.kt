package com.werty.easycellular.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.item
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.werty.easycellular.core.SpeedRecord
import com.werty.easycellular.i18n.LocalL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(records: List<SpeedRecord>, onClear: () -> Unit) {
    val t = LocalL.current
    var confirm by remember { mutableStateOf(false) }

    if (records.isEmpty()) {
        Column(
            Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(t.t("hist_empty"), style = MaterialTheme.typography.titleLarge)
            Text(t.t("hist_empty_hint"), color = Color.White.copy(alpha = 0.6f))
        }
    } else {
        LazyColumn(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                GlassCard {
                    SectionTitle(t.t("hist_chart"))
                    DownloadChart(records.take(20).reversed().map { it.down })
                }
            }
            item {
                Button(onClick = { confirm = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(t.t("hist_clear"))
                }
            }
            items(records, key = { it.id }) { r ->
                RecordRow(r)
            }
        }
    }

    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text(t.t("hist_clear_ask")) },
            confirmButton = {
                TextButton(onClick = {
                    onClear()
                    confirm = false
                }) { Text(t.t("hist_clear")) }
            },
            dismissButton = {
                TextButton(onClick = { confirm = false }) { Text(t.t("cancel")) }
            }
        )
    }
}

@Composable
private fun DownloadChart(values: List<Double>) {
    Canvas(Modifier.fillMaxWidth().height(150.dp)) {
        if (values.size < 2) return@Canvas
        val max = (values.maxOrNull() ?: 1.0).coerceAtLeast(1.0)
        val stepX = size.width / (values.size - 1)
        val path = Path()
        values.forEachIndexed { i, v ->
            val x = i * stepX
            val y = size.height - (v / max * size.height).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(
            path = path,
            brush = Brush.horizontalGradient(listOf(EcAccent, EcAccent2)),
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun RecordRow(r: SpeedRecord) {
    val t = LocalL.current
    val date = SimpleDateFormat("dd.MM HH:mm", Locale.getDefault()).format(Date(r.time))
    GlassCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(date, fontWeight = FontWeight.SemiBold)
            Text(r.gen.title(t), color = EcAccent, fontWeight = FontWeight.Bold)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("↓ ${t.f("fmt_mbps", r.down)}", color = Color.White.copy(alpha = 0.8f))
            Text("↑ ${t.f("fmt_mbps", r.up)}", color = Color.White.copy(alpha = 0.8f))
            r.ping?.let { Text(t.f("fmt_ms", it), color = Color.White.copy(alpha = 0.8f)) }
        }
    }
}
