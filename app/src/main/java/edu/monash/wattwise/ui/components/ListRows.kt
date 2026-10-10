package edu.monash.wattwise.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.monash.wattwise.ui.theme.DinPro
import edu.monash.wattwise.ui.theme.OnAccent
import edu.monash.wattwise.ui.theme.SurfaceRaised
import edu.monash.wattwise.ui.theme.TextSecondary

/**
 * One activity on the home screen: a coloured badge with an icon and the
 * energy used, the activity name, and its start and end times on the right.
 * Tapping the row opens the screen with more detail.
 */
@Composable
fun ActivityRow(
    icon: Int,
    value: String,
    label: String,
    startTime: String,
    endTime: String,
    color: Color,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(SurfaceRaised, RoundedCornerShape(8.dp))
            .padding(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            Modifier.width(96.dp).height(40.dp).background(color, RoundedCornerShape(6.dp)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(painterResource(icon), contentDescription = null, tint = OnAccent, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(7.dp))
            Text(value, fontFamily = DinPro, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = OnAccent)
        }
        Spacer(Modifier.width(13.dp))
        Text(label.uppercase(), style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End) {
            Text(endTime, fontFamily = DinPro, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextSecondary)
            Spacer(Modifier.height(5.dp))
            Text(startTime, fontFamily = DinPro, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextSecondary)
        }
        Spacer(Modifier.width(7.dp))
        // The line joins the two times, showing they are the end and the start of one period.
        Box(Modifier.width(2.dp).height(30.dp).background(color))
        Spacer(Modifier.width(4.dp))
    }
}
