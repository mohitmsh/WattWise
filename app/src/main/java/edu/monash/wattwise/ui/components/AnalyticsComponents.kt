package edu.monash.wattwise.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.monash.wattwise.ui.theme.*
import kotlin.math.min

@Composable
fun WhoopStyleTopBar() {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(34.dp).background(SurfaceRaised, CircleShape), contentAlignment = Alignment.Center) {
            Text("A", style = MaterialTheme.typography.labelLarge)
        }
        Spacer(Modifier.weight(1f))
        Row(
            Modifier.background(Surface, RoundedCornerShape(50)).padding(horizontal = 13.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("‹", color = TextSecondary, fontSize = 20.sp)
            Spacer(Modifier.width(12.dp))
            Text("TODAY", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.width(12.dp))
            Text("›", color = TextSecondary, fontSize = 20.sp)
        }
        Spacer(Modifier.weight(1f))
        Box(Modifier.size(34.dp).background(SurfaceRaised, CircleShape), contentAlignment = Alignment.Center) {
            Text("⌁", color = TextPrimary, fontSize = 19.sp)
            Box(Modifier.size(8.dp).align(Alignment.TopEnd).background(StatusGood, CircleShape))
        }
    }
}

@Composable
fun CompactMetricRing(
    value: Float,
    valueText: String,
    unit: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    hero: Boolean = false,
) {
    val animated by animateFloatAsState(value.coerceIn(0f, 1f), tween(700), label = "metricRing")
    Column(
        modifier.then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .semantics { contentDescription = "$label, $valueText $unit" },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(1f), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val stroke = min(size.width, size.height) * if (hero) .043f else .052f
                val inset = stroke / 2
                drawArc(
                    TextTertiary.copy(alpha = .32f), -90f, 360f, false,
                    Offset(inset, inset), Size(size.width - stroke, size.height - stroke),
                    style = Stroke(stroke, cap = StrokeCap.Round),
                )
                drawArc(
                    color, -90f, animated * 360f, false,
                    Offset(inset, inset), Size(size.width - stroke, size.height - stroke),
                    style = Stroke(stroke, cap = StrokeCap.Round),
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (hero) {
                    Text("WATTWISE", style = MaterialTheme.typography.labelMedium, color = TextTertiary)
                    Spacer(Modifier.height(7.dp))
                }
                Text(valueText, fontFamily = DinPro, fontWeight = FontWeight.Bold, fontSize = if (hero) 58.sp else 24.sp, letterSpacing = (-.5).sp)
                if (unit.isNotEmpty()) Text(unit, style = if (hero) MaterialTheme.typography.labelLarge else MaterialTheme.typography.labelMedium, color = TextSecondary)
                if (hero) {
                    Spacer(Modifier.height(7.dp))
                    Text(label.uppercase(), style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                }
            }
        }
        if (!hero) {
            Spacer(Modifier.height(6.dp))
            Text("${label.uppercase()} ›", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
        }
    }
}

@Composable
fun MonitorCard(title: String, status: String, detail: String, statusColor: Color, modifier: Modifier = Modifier) {
    Column(modifier.background(Surface, RoundedCornerShape(12.dp)).padding(horizontal = 13.dp, vertical = 11.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title.uppercase(), style = MaterialTheme.typography.labelMedium, color = TextSecondary, modifier = Modifier.weight(1f))
            Text("›", color = TextTertiary, fontSize = 22.sp)
        }
        Spacer(Modifier.height(7.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).background(statusColor, CircleShape))
            Spacer(Modifier.width(8.dp))
            Text(status, style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(2.dp))
        Text(detail, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}

@Composable
fun EnergyTimelineRow(value: String, label: String, time: String, detail: String, color: Color) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 7.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.width(78.dp).height(46.dp).background(color.copy(alpha = .22f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(value, fontFamily = DinPro, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
        }
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(label.uppercase(), style = MaterialTheme.typography.labelLarge)
            Text(detail, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
        Text(time, fontFamily = DinPro, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextSecondary)
    }
}

@Composable
fun MetricRow(label: String, value: String, comparison: String, color: Color = TextPrimary) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(28.dp).background(SurfaceRaised, CircleShape), contentAlignment = Alignment.Center) {
            Box(Modifier.size(7.dp).background(color, CircleShape))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(label.uppercase(), style = MaterialTheme.typography.labelLarge)
            Text(comparison, style = MaterialTheme.typography.bodyMedium, color = TextTertiary)
        }
        Text(value, fontFamily = DinPro, fontWeight = FontWeight.Bold, fontSize = 21.sp)
    }
}

@Composable
fun EnergyLineChart(values: List<Float>, color: Color, modifier: Modifier = Modifier) {
    Canvas(
        modifier.height(180.dp).fillMaxWidth().semantics {
            contentDescription = "Live power line chart ranging from ${values.minOrNull()} to ${values.maxOrNull()} kilowatts"
        },
    ) {
        if (values.size < 2) return@Canvas
        repeat(4) { index ->
            val y = size.height * index / 3f
            drawLine(Border, Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
        }
        val minValue = values.minOrNull() ?: 0f
        val range = ((values.maxOrNull() ?: 1f) - minValue).coerceAtLeast(.1f)
        val path = Path()
        values.forEachIndexed { index, value ->
            val x = size.width * index / values.lastIndex
            val y = size.height - ((value - minValue) / range) * size.height
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color, style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round))
        val lastY = size.height - ((values.last() - minValue) / range) * size.height
        drawCircle(color, 5.dp.toPx(), Offset(size.width, lastY))
        drawCircle(Ink, 2.dp.toPx(), Offset(size.width, lastY))
    }
}

@Composable
fun RangeBar(label: String, value: String, progress: Float, color: Color = Cyan) {
    Column(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Row {
            Text(label.uppercase(), style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
            Text(value, fontFamily = DinPro, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        }
        Spacer(Modifier.height(9.dp))
        Box(Modifier.fillMaxWidth().height(7.dp).background(TextTertiary.copy(alpha = .22f), RoundedCornerShape(50))) {
            Box(Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).fillMaxHeight().background(color, RoundedCornerShape(50)))
        }
    }
}
