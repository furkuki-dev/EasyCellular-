package com.werty.easycellular.ui

import android.content.Intent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.werty.easycellular.TestPhase
import com.werty.easycellular.SpeedUi
import com.werty.easycellular.i18n.LocalL

@Composable
fun SpeedScreen(state: SpeedUi, onStart: () -> Unit, onStop: () -> Unit) {
    val t = LocalL.current
    val ctx = LocalContext.current
    val running = state.phase == TestPhase.PING || state.phase == TestPhase.DOWN || state.phase == TestPhase.UP

    val transition = rememberInfiniteTransition(label = "pulse")
    val pulse by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label = "pulse"
    )

    val phaseText = when (state.phase) {
        TestPhase.IDLE -> t.t("phase_idle")
        TestPhase.PING -> t.t("phase_ping")
        TestPhase.DOWN -> t.t("phase_down")
        TestPhase.UP -> t.t("phase_up")
        TestPhase.DONE -> t.t("phase_done")
        TestPhase.FAILED -> t.t("phase_failed")
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Box(Modifier.size(260.dp).scale(if (running) pulse else 1f), contentAlignment = Alignment.Center) {
            SpeedGauge(state.live, Modifier.fillMaxSize())
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = String.format("%.0f", state.live),
                    fontSize = 60.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(t.t("mbps"), color = Color.White.copy(alpha = 0.55f))
            }
        }

        Text(phaseText, style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.8f))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricTile(
                title = t.t("ping"),
                value = state.ping?.let { String.format("%.0f", it.avg) } ?: "—",
                unit = t.t("ms"),
                tint = EcWarn,
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                title = t.t("download"),
                value = state.down?.let { String.format("%.0f", it) } ?: "—",
                unit = t.t("mbps"),
                tint = EcAccent,
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                title = t.t("upload"),
                value = state.up?.let { String.format("%.0f", it) } ?: "—",
                unit = t.t("mbps"),
                tint = EcAccent2,
                modifier = Modifier.weight(1f)
            )
        }

        if (running) {
            OutlinedButton(
                onClick = onStop,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp)
            ) { Text(t.t("test_stop"), fontSize = 18.sp) }
        } else {
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EcAccent, contentColor = Color(0xFF00121C))
            ) { Text(t.t("test_start"), fontSize = 18.sp, fontWeight = FontWeight.SemiBold) }
        }

        if (state.phase == TestPhase.DONE) {
            GlassCard {
                SectionTitle(t.t("details"))
                state.ping?.let {
                    InfoRow(t.t("min"), t.f("fmt_ms", it.min))
                    InfoRow(t.t("max"), t.f("fmt_ms", it.max))
                    InfoRow(t.t("jitter"), t.f("fmt_ms", it.jitter))
                    InfoRow(t.t("loss"), t.f("fmt_pct", it.loss))
                }
                InfoRow(t.t("gen_result"), state.gen.title(t), valueColor = EcAccent)
                InfoRow(t.t("data_used"), t.f("fmt_mb", state.bytes / 1_000_000.0))
                OutlinedButton(
                    onClick = {
                        val text = t.f("share_text", state.down ?: 0.0, state.up ?: 0.0, state.ping?.avg ?: 0.0) +
                            " · " + state.gen.title(t)
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, text)
                        }
                        ctx.startActivity(Intent.createChooser(intent, null))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(t.t("share")) }
            }
        }
    }
}

@Composable
private fun MetricTile(
    title: String,
    value: String,
    unit: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(title, color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.bodySmall)
        Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = tint)
        Text(unit, color = Color.White.copy(alpha = 0.4f), style = MaterialTheme.typography.labelSmall)
    }
}
