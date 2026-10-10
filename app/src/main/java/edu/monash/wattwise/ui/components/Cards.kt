package edu.monash.wattwise.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import edu.monash.wattwise.ui.theme.Amber
import edu.monash.wattwise.ui.theme.DinPro
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.monash.wattwise.ui.theme.Dimens
import edu.monash.wattwise.ui.theme.Electric
import edu.monash.wattwise.ui.theme.Surface
import edu.monash.wattwise.ui.theme.TextPrimary
import edu.monash.wattwise.ui.theme.TextSecondary
import edu.monash.wattwise.ui.theme.TextTertiary

/** Rounded container used for every grouped block of content. */
@Composable
fun SurfaceCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(Dimens.CardPadding),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(Dimens.CardRadius))
            .padding(contentPadding),
        content = content,
    )
}

/** Small tile showing one figure with its label and a trend line. */
@Composable
fun MetricTile(label: String, value: String, trend: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier
            .background(Surface, RoundedCornerShape(Dimens.TileRadius))
            .padding(15.dp),
    ) {
        Box(Modifier.size(Dimens.StatusDotSize).background(color, CircleShape))
        Spacer(Modifier.height(12.dp))
        Text(value, style = MaterialTheme.typography.titleLarge)
        Text(label.uppercase(), style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        Spacer(Modifier.height(7.dp))
        Text(trend, style = MaterialTheme.typography.bodyMedium, color = color)
    }
}

/** Recommendation card with the reason behind it and one action button. */
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
            border = BorderStroke(1.dp, Electric),
            shape = RoundedCornerShape(Dimens.PILL_CORNER_PERCENT),
        ) {
            Text(action, style = MaterialTheme.typography.labelLarge)
        }
    }
}

/** Compact status card used in pairs on the home screen. */
@Composable
fun MonitorCard(title: String, status: String, detail: String, statusColor: Color, modifier: Modifier = Modifier) {
    Column(
        modifier
            .background(Surface, RoundedCornerShape(12.dp))
            .padding(horizontal = 13.dp, vertical = 11.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                title.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                modifier = Modifier.weight(1f),
            )
            Text("›", color = TextTertiary, fontSize = 22.sp)
        }
        Spacer(Modifier.height(7.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(Dimens.StatusDotSize).background(statusColor, CircleShape))
            Spacer(Modifier.width(8.dp))
            Text(status, style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(2.dp))
        Text(detail, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}

/** Coloured label with a dot, used for appliance status. */
@Composable
fun StatusPill(text: String, color: Color, modifier: Modifier = Modifier) {
    Row(
        modifier
            .background(color.copy(alpha = .14f), RoundedCornerShape(Dimens.PILL_CORNER_PERCENT))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(7.dp).background(color, CircleShape))
        Spacer(Modifier.width(7.dp))
        Text(text.uppercase(), style = MaterialTheme.typography.labelMedium, color = color)
    }
}

/**
 * Tile showing one reading with an icon, its value and a status chip, for
 * example an appliance's power with "RUNNING NOW".
 *
 * @param isGood colours the chip green when true and amber when false. The
 * chip text always says the status in words as well.
 */
@Composable
fun MonitorTile(
    icon: Int,
    label: String,
    value: String,
    unit: String,
    chipText: String,
    isGood: Boolean,
    modifier: Modifier = Modifier,
) {
    val chipColor = if (isGood) Electric else Amber
    Column(
        modifier
            .background(Surface, RoundedCornerShape(Dimens.CardRadius))
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(icon), contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(label.uppercase(), style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, fontFamily = DinPro, fontWeight = FontWeight.Bold, fontSize = 28.sp)
            Spacer(Modifier.width(3.dp))
            Text(unit, style = MaterialTheme.typography.labelLarge, color = TextSecondary, modifier = Modifier.padding(bottom = 5.dp))
        }
        Spacer(Modifier.height(10.dp))
        Text(
            chipText.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = chipColor,
            modifier = Modifier
                .background(chipColor.copy(alpha = .14f), RoundedCornerShape(4.dp))
                .padding(horizontal = 7.dp, vertical = 4.dp),
        )
    }
}
