package edu.monash.wattwise.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.monash.wattwise.data.SampleData
import edu.monash.wattwise.data.model.UsagePeriod
import edu.monash.wattwise.ui.components.DashboardTopBar
import edu.monash.wattwise.ui.components.DatePickerPopup
import edu.monash.wattwise.ui.components.EnergyLineChart
import edu.monash.wattwise.ui.components.RangeBar
import edu.monash.wattwise.ui.components.SurfaceCard
import edu.monash.wattwise.ui.theme.Amber
import edu.monash.wattwise.ui.theme.Cyan
import edu.monash.wattwise.ui.theme.Dimens
import edu.monash.wattwise.ui.theme.DinPro
import edu.monash.wattwise.ui.theme.StatusGood
import edu.monash.wattwise.ui.theme.Surface
import edu.monash.wattwise.ui.theme.SurfaceRaised
import edu.monash.wattwise.ui.theme.TextPrimary
import edu.monash.wattwise.ui.theme.TextSecondary
import edu.monash.wattwise.ui.theme.TextTertiary
import edu.monash.wattwise.utils.Formatters

/** Usage report that can be viewed by day, week or month, or for a chosen date. */
@Composable
fun TrendsScreen() {
    var period by remember { mutableStateOf(UsagePeriod.DAY) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.ScreenPaddingCompact),
    ) {
        Spacer(Modifier.height(8.dp))
        DashboardTopBar()
        Spacer(Modifier.height(18.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("ENERGY USAGE", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { showDatePicker = true }) {
                Text(
                    Formatters.formatDisplayDate(selectedDateMillis),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        PeriodSelector(selected = period, onSelected = { period = it })
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                SampleData.usageTotalKwh.getValue(period),
                fontFamily = DinPro,
                fontWeight = FontWeight.Bold,
                fontSize = 52.sp,
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "kWh",
                style = MaterialTheme.typography.titleMedium,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 9.dp),
            )
        }
        Text(
            "↓ 8% vs typical ${period.label.lowercase()}",
            style = MaterialTheme.typography.bodyMedium,
            color = StatusGood,
        )
        Spacer(Modifier.height(22.dp))
        SurfaceCard {
            EnergyLineChart(SampleData.weeklyUsageKwh, Cyan)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SampleData.weekdayLabels.forEach { day ->
                    Text(day, style = MaterialTheme.typography.labelMedium, color = TextTertiary)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        Text("USAGE VS TYPICAL RANGE", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        Spacer(Modifier.height(6.dp))
        SurfaceCard(contentPadding = PaddingValues(horizontal = Dimens.CardPadding, vertical = 5.dp)) {
            RangeBar("Morning", "4.8 kWh", .48f)
            RangeBar("Afternoon", "5.2 kWh", .56f)
            RangeBar("Evening", "8.6 kWh", .86f, Amber)
        }
        Spacer(Modifier.height(24.dp))
        SurfaceCard {
            Text("ON TRACK", style = MaterialTheme.typography.labelMedium, color = StatusGood)
            Spacer(Modifier.height(9.dp))
            Text(
                "You’re projected to spend $18 less than your typical monthly usage.",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        Spacer(Modifier.height(26.dp))
    }

    if (showDatePicker) {
        DatePickerPopup(
            initialDateMillis = selectedDateMillis,
            onDateSelected = { selectedDateMillis = it },
            onDismiss = { showDatePicker = false },
        )
    }
}

/** Row of buttons for switching the report between day, week and month. */
@Composable
private fun PeriodSelector(selected: UsagePeriod, onSelected: (UsagePeriod) -> Unit) {
    val pill = RoundedCornerShape(Dimens.PILL_CORNER_PERCENT)
    Row(Modifier.fillMaxWidth().background(Surface, pill).padding(4.dp)) {
        UsagePeriod.entries.forEach { option ->
            val isSelected = option == selected
            Box(
                Modifier
                    .weight(1f)
                    .clickable { onSelected(option) }
                    .background(if (isSelected) SurfaceRaised else Color.Transparent, pill)
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    option.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) TextPrimary else TextTertiary,
                )
            }
        }
    }
}
