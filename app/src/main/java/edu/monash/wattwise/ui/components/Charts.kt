package edu.monash.wattwise.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.monash.wattwise.ui.theme.DinPro
import edu.monash.wattwise.ui.theme.TextPrimary
import edu.monash.wattwise.ui.theme.TextSecondary
import edu.monash.wattwise.ui.theme.TextTertiary
import kotlin.math.min

private const val FULL_CIRCLE_DEGREES = 360f
private const val RING_START_ANGLE = -90f
private const val RING_ANIMATION_MILLIS = 700

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
