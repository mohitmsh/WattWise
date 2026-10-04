package edu.monash.wattwise.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.monash.wattwise.ui.theme.*
import kotlin.math.min

@Composable
fun BrandMark(modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(28.dp)
                .background(Electric, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text("W", color = Ink, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.width(9.dp))
        Text("WATTWISE", style = MaterialTheme.typography.labelLarge, letterSpacing = 2.sp)
    }
}

@Composable
fun ScreenHeader(
    eyebrow: String,
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
) {
    Row(verticalAlignment = Alignment.Top) {
        if (onBack != null) {
            TextButton(onClick = onBack, contentPadding = PaddingValues(end = 12.dp)) {
                Text("‹", fontSize = 38.sp, color = TextPrimary)
            }
        }
        Column(Modifier.weight(1f)) {
            Text(eyebrow.uppercase(), style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Spacer(Modifier.height(7.dp))
            Text(title, style = MaterialTheme.typography.headlineLarge)
            subtitle?.let {
                Spacer(Modifier.height(7.dp))
                Text(it, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
        }
    }
}

@Composable
fun MetricRing(
    value: Int,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    suffix: String = "%",
) {
    Box(modifier.aspectRatio(1f), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = min(size.width, size.height) * 0.055f
            val inset = stroke / 2
            drawArc(
                color = Border,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(size.width - stroke, size.height - stroke),
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * value.coerceIn(0, 100) / 100f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(size.width - stroke, size.height - stroke),
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$value$suffix", style = MaterialTheme.typography.displayMedium)
            Text(label.uppercase(), style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        }
    }
}

@Composable
fun MetricTile(label: String, value: String, trend: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier
            .background(Surface, RoundedCornerShape(18.dp))
            .padding(15.dp),
    ) {
        Box(Modifier.size(8.dp).background(color, CircleShape))
        Spacer(Modifier.height(12.dp))
        Text(value, style = MaterialTheme.typography.titleLarge)
        Text(label.uppercase(), style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        Spacer(Modifier.height(7.dp))
        Text(trend, style = MaterialTheme.typography.bodyMedium, color = color)
    }
}

@Composable
fun SectionTitle(title: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        if (action != null) {
            TextButton(onClick = { onAction?.invoke() }) {
                Text(action.uppercase(), style = MaterialTheme.typography.labelMedium, color = Electric)
            }
        }
    }
}

@Composable
fun SmartActionCard(
    title: String,
    reason: String,
    saving: String,
    action: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .background(Surface, RoundedCornerShape(20.dp))
            .padding(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("SMART ACTION", style = MaterialTheme.typography.labelMedium, color = Electric)
            Spacer(Modifier.weight(1f))
            Text(saving, style = MaterialTheme.typography.labelMedium, color = Electric)
        }
        Spacer(Modifier.height(12.dp))
        Text(title, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(7.dp))
        Text(reason, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Spacer(Modifier.height(16.dp))
        OutlinedButton(
            onClick = onAction,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
            border = androidx.compose.foundation.BorderStroke(1.dp, Electric),
            shape = RoundedCornerShape(50),
        ) {
            Text(action, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun StatusPill(text: String, color: Color, modifier: Modifier = Modifier) {
    Row(
        modifier.background(color.copy(alpha = .14f), RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(7.dp).background(color, CircleShape))
        Spacer(Modifier.width(7.dp))
        Text(text.uppercase(), style = MaterialTheme.typography.labelMedium, color = color)
    }
}

@Composable
fun ImpactBar(label: String, value: Int, positive: Boolean = true) {
    val color = if (positive) Electric else Amber
    Column(
        Modifier
            .fillMaxWidth()
            .background(SurfaceRaised, RoundedCornerShape(11.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Row {
            Text(label.uppercase(), style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
            // A sign makes direction understandable without relying on colour or bar geometry.
            Text("${if (positive) "+" else "−"}$value%", style = MaterialTheme.typography.labelLarge, color = color)
        }
        Spacer(Modifier.height(7.dp))
        Canvas(Modifier.fillMaxWidth().height(13.dp)) {
            val center = size.width / 2f
            val y = size.height / 2f
            drawLine(TextTertiary.copy(alpha = .38f), Offset(0f, y), Offset(size.width, y), 5.dp.toPx(), StrokeCap.Round)
            var stripeX = 3.dp.toPx()
            while (stripeX < size.width) {
                drawLine(Border, Offset(stripeX, y - 2.dp.toPx()), Offset(stripeX + 4.dp.toPx(), y + 2.dp.toPx()), 1.dp.toPx())
                stripeX += 9.dp.toPx()
            }
            val length = (size.width / 2f) * value.coerceIn(0, 30) / 30f
            val start = if (positive) center else center - length
            val end = if (positive) center + length else center
            drawLine(color, Offset(start, y), Offset(end, y), 5.dp.toPx(), StrokeCap.Round)
            drawCircle(Surface, 5.dp.toPx(), Offset(center, y))
            drawCircle(TextPrimary, 2.dp.toPx(), Offset(center, y))
        }
    }
}

@Composable
fun MiniBarChart(values: List<Float>, modifier: Modifier = Modifier, highlight: Int = values.lastIndex) {
    Canvas(modifier.height(150.dp).fillMaxWidth()) {
        if (values.isEmpty()) return@Canvas
        val gap = 7.dp.toPx()
        val barWidth = (size.width - gap * (values.size - 1)) / values.size
        val max = values.maxOrNull()?.coerceAtLeast(1f) ?: 1f
        values.forEachIndexed { index, value ->
            val height = (value / max) * size.height
            drawRoundRect(
                color = if (index == highlight) Electric else Border,
                topLeft = Offset(index * (barWidth + gap), size.height - height),
                size = Size(barWidth, height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()),
            )
        }
    }
}

@Composable
fun PrimaryAction(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(52.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
        border = androidx.compose.foundation.BorderStroke(1.dp, Electric),
        shape = RoundedCornerShape(50),
    ) {
        Text(text.uppercase(), style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun EmptyState(title: String, body: String, action: String, onAction: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("＋", fontSize = 44.sp, color = Electric)
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(body, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(18.dp))
        TextButton(onClick = onAction) { Text(action.uppercase(), color = Electric) }
    }
}
