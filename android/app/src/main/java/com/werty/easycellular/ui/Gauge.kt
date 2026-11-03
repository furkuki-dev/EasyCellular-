package com.werty.easycellular.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.werty.easycellular.core.SpeedConfig
import kotlin.math.ln

/** Дуговой спидометр с логарифмической шкалой и плавной анимацией. */
@Composable
fun SpeedGauge(value: Double, modifier: Modifier = Modifier) {
    val target = if (value <= 0) 0f
    else (ln(1 + value) / ln(1 + SpeedConfig.GAUGE_MAX)).toFloat().coerceIn(0f, 1f)
    val fractionState = animateFloatAsState(
        targetValue = target,
        animationSpec = tween(350),
        label = "gauge"
    )
    // Read State.value directly to avoid delegated-property resolution issues.
    val fraction = fractionState.value

    Canvas(modifier) {
        val stroke = 18.dp.toPx()
        val inset = stroke / 2
        val arcSize = Size(size.width - stroke, size.height - stroke)
        val topLeft = Offset(inset, inset)
        drawArc(
            color = Color.White.copy(alpha = 0.08f),
            startAngle = 135f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        drawArc(
            brush = Brush.sweepGradient(listOf(EcAccent, EcAccent2, EcAccent), center = center),
            startAngle = 135f,
            sweepAngle = 270f * fraction,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
    }
}
