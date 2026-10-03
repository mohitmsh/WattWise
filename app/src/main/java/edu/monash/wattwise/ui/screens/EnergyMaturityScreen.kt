package edu.monash.wattwise.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.monash.wattwise.ui.components.ScreenHeader
import edu.monash.wattwise.ui.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Long-term behavioural profile; deliberately not described as a literal building age. */
@Composable
fun EnergyMaturityScreen(onBack: () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(14.dp))
        ScreenHeader("Last 90 days", "Energy maturity", "A long-term view of how consistently your household uses energy.", onBack)
        Spacer(Modifier.height(16.dp))
        MaturityOrb(Modifier.size(240.dp).align(Alignment.CenterHorizontally))
        Text("ADVANCED", style = MaterialTheme.typography.labelLarge, color = Electric, modifier = Modifier.align(Alignment.CenterHorizontally))
        Text("Top 18% of similar households", style = MaterialTheme.typography.bodyMedium, color = TextSecondary, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(22.dp))
        Column(Modifier.fillMaxWidth().background(Surface, RoundedCornerShape(13.dp)).padding(horizontal = 14.dp)) {
            MaturityFactor("Base-load health", "78", "Improving", StatusGood)
            HorizontalDivider(color = Border)
            MaturityFactor("Peak-demand behaviour", "86", "Strong", Electric)
            HorizontalDivider(color = Border)
            MaturityFactor("Appliance efficiency", "74", "Opportunity", Amber)
            HorizontalDivider(color = Border)
            MaturityFactor("Usage consistency", "88", "Strong", SoftBlue)
        }
        Spacer(Modifier.height(16.dp))
        Column(Modifier.fillMaxWidth().background(Surface, RoundedCornerShape(13.dp)).padding(14.dp)) {
            Text("YOUR PROGRESS", style = MaterialTheme.typography.labelMedium, color = Electric)
            Spacer(Modifier.height(6.dp))
            Text("Improved 12% this month", style = MaterialTheme.typography.titleLarge)
            Text("Shifting laundry outside peak hours created the largest improvement. Reducing overnight standby load is your next best action.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Spacer(Modifier.height(12.dp))
            Text("HOW IT IS CALCULATED", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Text("A prototype score derived from 90 days of simulated usage patterns and household settings. It is comparative guidance, not an energy-rating certificate.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun MaturityOrb(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "maturityOrb")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "orbPhase",
    )
    Box(modifier.semantics { contentDescription = "Energy maturity score 82, advanced" }, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val centre = Offset(size.width / 2, size.height / 2)
            val baseRadius = size.minDimension * (.39f + phase * .012f)
            val points = 64
            val path = Path()
            repeat(points + 1) { index ->
                val angle = index.toDouble() / points * PI * 2.0
                val wave = sin(angle * 3 + phase * PI) * .035 + cos(angle * 5 - phase * PI) * .018
                val radius = baseRadius * (1f + wave.toFloat())
                val point = Offset(
                    centre.x + cos(angle).toFloat() * radius,
                    centre.y + sin(angle).toFloat() * radius,
                )
                if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
            }
            path.close()
            drawPath(
                path,
                brush = Brush.radialGradient(
                    listOf(Electric.copy(alpha = .42f), Color(0xFF13705A).copy(alpha = .55f), Surface.copy(alpha = .12f)),
                    center = centre,
                    radius = size.minDimension * .48f,
                ),
            )
            drawPath(path, Electric.copy(alpha = .45f), style = Stroke(1.2.dp.toPx()))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("82", fontFamily = DinPro, fontWeight = FontWeight.Bold, fontSize = 62.sp)
            Text("MATURITY SCORE", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        }
    }
}

@Composable
private fun MaturityFactor(label: String, score: String, status: String, color: Color) {
    Row(Modifier.fillMaxWidth().padding(vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.titleMedium)
            Text(status, style = MaterialTheme.typography.bodyMedium, color = color)
        }
        Text(score, fontFamily = DinPro, fontWeight = FontWeight.Bold, fontSize = 26.sp)
    }
}
