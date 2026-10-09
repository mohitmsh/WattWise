package edu.monash.wattwise.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.monash.wattwise.ui.theme.Dimens
import edu.monash.wattwise.ui.theme.DinPro
import edu.monash.wattwise.ui.theme.SurfaceRaised
import edu.monash.wattwise.ui.theme.TextPrimary
import edu.monash.wattwise.ui.theme.TextSecondary
import edu.monash.wattwise.ui.theme.TextTertiary

/** One usage event in the home screen timeline. */
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

/** One electrical reading with a comparison line, used on the live screen. */
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

/** One factor behind a score: its name, an explanation and a coloured result. */
@Composable
fun ScoreFactorRow(label: String, value: String, explanation: String, color: Color) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(7.dp).background(color, RoundedCornerShape(Dimens.PILL_CORNER_PERCENT)))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.titleMedium)
            Text(explanation, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
        Spacer(Modifier.width(10.dp))
        Text(value.uppercase(), style = MaterialTheme.typography.labelLarge, color = color)
    }
}
