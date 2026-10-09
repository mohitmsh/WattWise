package edu.monash.wattwise.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.monash.wattwise.ui.theme.Amber
import edu.monash.wattwise.ui.theme.Border
import edu.monash.wattwise.ui.theme.Cyan
import edu.monash.wattwise.ui.theme.Dimens
import edu.monash.wattwise.ui.theme.DinPro
import edu.monash.wattwise.ui.theme.Electric
import edu.monash.wattwise.ui.theme.Ink
import edu.monash.wattwise.ui.theme.Surface
import edu.monash.wattwise.ui.theme.SurfaceRaised
import edu.monash.wattwise.ui.theme.TextPrimary
import edu.monash.wattwise.ui.theme.TextSecondary
import edu.monash.wattwise.ui.theme.TextTertiary
import kotlin.math.min

private const val FULL_CIRCLE_DEGREES = 360f
private const val RING_START_ANGLE = -90f
private const val RING_ANIMATION_MILLIS = 700

/** Largest impact percentage an [ImpactBar] can show; larger values fill the bar. */
private const val MAX_IMPACT_PERCENT = 30

/**
 * Circular progress ring with a value in the centre.
 *
 * @param value progress from 0 to 1.
 * @param hero true for the large single ring at the top of a screen; false for
 * the small rings shown side by side on the home screen.
 */
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
    val animatedValue by animateFloatAsState(
        targetValue = value.coerceIn(0f, 1f),
        animationSpec = tween(RING_ANIMATION_MILLIS),
        label = "metricRing",
    )
    val clickModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Column(
        modifier.then(clickModifier).semantics { contentDescription = "$label, $valueText $unit" },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(1f), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val strokeWidth = min(size.width, size.height) * if (hero) .043f else .052f
                val inset = strokeWidth / 2
                val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                val arcStyle = Stroke(strokeWidth, cap = StrokeCap.Round)
                // Grey track first, then the coloured progress arc on top.
                drawArc(
                    color = TextTertiary.copy(alpha = .32f),
                    startAngle = RING_START_ANGLE,
                    sweepAngle = FULL_CIRCLE_DEGREES,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = arcStyle,
                )
                drawArc(
                    color = color,
                    startAngle = RING_START_ANGLE,
                    sweepAngle = animatedValue * FULL_CIRCLE_DEGREES,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = arcStyle,
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (hero) {
                    Text("WATTWISE", style = MaterialTheme.typography.labelMedium, color = TextTertiary)
                    Spacer(Modifier.height(7.dp))
                }
                Text(
                    valueText,
                    fontFamily = DinPro,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (hero) 58.sp else 24.sp,
                    letterSpacing = (-.5).sp,
                )
                if (unit.isNotEmpty()) {
                    Text(
                        unit,
                        style = if (hero) MaterialTheme.typography.labelLarge else MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                    )
                }
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

/** Line chart of [values] from oldest to newest, with a marker on the latest point. */
@Composable
fun EnergyLineChart(values: List<Float>, color: Color, modifier: Modifier = Modifier) {
    val description = "Line chart ranging from ${values.minOrNull()} to ${values.maxOrNull()}"
    Canvas(modifier.height(180.dp).fillMaxWidth().semantics { contentDescription = description }) {
        if (values.size < 2) return@Canvas

        val gridLineCount = 4
        repeat(gridLineCount) { index ->
            val y = size.height * index / (gridLineCount - 1)
            drawLine(Border, Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
        }

        val minValue = values.min()
        val range = (values.max() - minValue).coerceAtLeast(.1f)
        // Higher readings are drawn nearer the top, so the y position is inverted.
        fun yFor(value: Float) = size.height - ((value - minValue) / range) * size.height

        val path = Path()
        values.forEachIndexed { index, value ->
            val x = size.width * index / values.lastIndex
            if (index == 0) path.moveTo(x, yFor(value)) else path.lineTo(x, yFor(value))
        }
        drawPath(path, color, style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round))

        val latestPoint = Offset(size.width, yFor(values.last()))
        drawCircle(color, 5.dp.toPx(), latestPoint)
        drawCircle(Ink, 2.dp.toPx(), latestPoint)
    }
}

/** Horizontal bar showing [progress] from 0 to 1 against a typical range. */
@Composable
fun RangeBar(label: String, value: String, progress: Float, color: Color = Cyan) {
    val pill = RoundedCornerShape(Dimens.PILL_CORNER_PERCENT)
    Column(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Row {
            Text(label.uppercase(), style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
            Text(value, fontFamily = DinPro, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        }
        Spacer(Modifier.height(9.dp))
        Box(Modifier.fillMaxWidth().height(7.dp).background(TextTertiary.copy(alpha = .22f), pill)) {
            Box(Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).fillMaxHeight().background(color, pill))
        }
    }
}

/**
 * Bar that grows right from the centre for behaviours that help the Energy
 * Score and left for behaviours that hurt it.
 */
@Composable
fun ImpactBar(label: String, impactPercent: Int, isPositive: Boolean = true) {
    val color = if (isPositive) Electric else Amber
    // The sign shows direction without relying on colour alone.
    val sign = if (isPositive) "+" else "−"
    Column(
        Modifier
            .fillMaxWidth()
            .background(SurfaceRaised, RoundedCornerShape(11.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Row {
            Text(label.uppercase(), style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
            Text("$sign$impactPercent%", style = MaterialTheme.typography.labelLarge, color = color)
        }
        Spacer(Modifier.height(7.dp))
        Canvas(Modifier.fillMaxWidth().height(13.dp)) {
            val centreX = size.width / 2f
            val centreY = size.height / 2f
            val trackWidth = 5.dp.toPx()
            drawLine(
                color = TextTertiary.copy(alpha = .38f),
                start = Offset(0f, centreY),
                end = Offset(size.width, centreY),
                strokeWidth = trackWidth,
                cap = StrokeCap.Round,
            )
            val fraction = impactPercent.coerceIn(0, MAX_IMPACT_PERCENT) / MAX_IMPACT_PERCENT.toFloat()
            val barLength = centreX * fraction
            val barStart = if (isPositive) centreX else centreX - barLength
            drawLine(
                color = color,
                start = Offset(barStart, centreY),
                end = Offset(barStart + barLength, centreY),
                strokeWidth = trackWidth,
                cap = StrokeCap.Round,
            )
            // Marker for the zero point in the middle of the bar.
            drawCircle(Surface, 5.dp.toPx(), Offset(centreX, centreY))
            drawCircle(TextPrimary, 2.dp.toPx(), Offset(centreX, centreY))
        }
    }
}
