package edu.monash.wattwise.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.monash.wattwise.ui.theme.Dimens
import edu.monash.wattwise.ui.theme.Electric
import edu.monash.wattwise.ui.theme.Ink
import edu.monash.wattwise.ui.theme.StatusGood
import edu.monash.wattwise.ui.theme.Surface
import edu.monash.wattwise.ui.theme.SurfaceRaised
import edu.monash.wattwise.ui.theme.TextPrimary
import edu.monash.wattwise.ui.theme.TextSecondary

/** App logo and name shown on the onboarding screen. */
@Composable
fun BrandMark(modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(28.dp).background(Electric, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text("W", color = Ink, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.width(9.dp))
        Text("WATTWISE", style = MaterialTheme.typography.labelLarge, letterSpacing = 2.sp)
    }
}

/**
 * Title block at the top of a screen. A back button is shown only when
 * [onBack] is provided.
 */
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
            if (subtitle != null) {
                Spacer(Modifier.height(7.dp))
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
        }
    }
}

/** Heading for a section of a screen, with an optional text action on the right. */
@Composable
fun SectionTitle(title: String, action: String? = null, onAction: () -> Unit = {}) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        if (action != null) {
            TextButton(onClick = onAction) {
                Text(action.uppercase(), style = MaterialTheme.typography.labelMedium, color = Electric)
            }
        }
    }
}

/** Top bar of the dashboard tabs: profile initial, day selector and meter status. */
@Composable
fun DashboardTopBar() {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(34.dp).background(SurfaceRaised, CircleShape), contentAlignment = Alignment.Center) {
            Text("A", style = MaterialTheme.typography.labelLarge)
        }
        Spacer(Modifier.weight(1f))
        Row(
            Modifier
                .background(Surface, RoundedCornerShape(Dimens.PILL_CORNER_PERCENT))
                .padding(horizontal = 13.dp, vertical = 9.dp),
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
            Box(Modifier.size(Dimens.StatusDotSize).align(Alignment.TopEnd).background(StatusGood, CircleShape))
        }
    }
}
