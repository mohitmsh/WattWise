package edu.monash.wattwise.ui.screens.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.monash.wattwise.constants.ScenarioRules
import edu.monash.wattwise.ui.components.LabelledSlider
import edu.monash.wattwise.ui.components.MetricTile
import edu.monash.wattwise.ui.components.PrimaryAction
import edu.monash.wattwise.ui.components.ScreenHeader
import edu.monash.wattwise.ui.components.SurfaceCard
import edu.monash.wattwise.ui.theme.Cyan
import edu.monash.wattwise.ui.theme.Dimens
import edu.monash.wattwise.ui.theme.Electric
import edu.monash.wattwise.ui.theme.StatusGood
import edu.monash.wattwise.ui.theme.TextSecondary
import edu.monash.wattwise.utils.Formatters
import edu.monash.wattwise.utils.SavingsCalculator
import edu.monash.wattwise.utils.sliderSteps
import kotlin.math.roundToInt

/**
 * Scenario planner: the user adjusts when and how often laundry runs and sees
 * the estimated saving before saving the plan and adding a reminder.
 */
@Composable
fun GoalsScreen(onBack: () -> Unit) {
    var startHour by remember { mutableFloatStateOf(ScenarioRules.EARLIEST_START_HOUR) }
    var dryerDays by remember { mutableFloatStateOf(ScenarioRules.DEFAULT_DRYER_DAYS) }
    var scenarioSaved by remember { mutableStateOf(false) }
    var reminderScheduled by remember { mutableStateOf(false) }

    val monthlySaving = SavingsCalculator.estimateMonthlySaving(startHour, dryerDays)
    val startTimeText = Formatters.formatHour(startHour.roundToInt())

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(Dimens.ScreenPadding)) {
        Spacer(Modifier.height(14.dp))
        ScreenHeader(
            eyebrow = "Goals & planning",
            title = "Test a better routine",
            subtitle = "Explore estimates before committing to a change.",
            onBack = onBack,
        )
        Spacer(Modifier.height(22.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricTile("Energy goal", "342 kWh", "72% on track", Electric, Modifier.weight(1f))
            MetricTile("Carbon goal", "−18%", "5 kg to go", Cyan, Modifier.weight(1f))
        }
        Spacer(Modifier.height(26.dp))
        Text("LAUNDRY SCENARIO", style = MaterialTheme.typography.labelMedium, color = Electric)
        Text("Shift flexible appliance use", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(20.dp))
        LabelledSlider(
            label = "Start time",
            valueText = startTimeText,
            value = startHour,
            onValueChange = { startHour = it },
            valueRange = ScenarioRules.EARLIEST_START_HOUR..ScenarioRules.LATEST_START_HOUR,
            steps = sliderSteps(ScenarioRules.EARLIEST_START_HOUR, ScenarioRules.LATEST_START_HOUR, step = 1f),
            hint = "Later use may avoid peak pricing.",
        )
        Spacer(Modifier.height(18.dp))
        LabelledSlider(
            label = "Dryer use",
            valueText = "${dryerDays.roundToInt()} days per week",
            value = dryerDays,
            onValueChange = { dryerDays = it },
            valueRange = ScenarioRules.MIN_DRYER_DAYS..ScenarioRules.MAX_DRYER_DAYS,
            steps = sliderSteps(ScenarioRules.MIN_DRYER_DAYS, ScenarioRules.MAX_DRYER_DAYS, step = 1f),
        )
        Spacer(Modifier.height(22.dp))
        SurfaceCard {
            Text(
                "ESTIMATED MONTHLY DIFFERENCE",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "${Formatters.formatCurrency(monthlySaving)} saved",
                style = MaterialTheme.typography.headlineLarge,
                color = Electric,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Estimate based on your selected tariff and current household baseline. " +
                    "Actual results may vary.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        }
        Spacer(Modifier.height(24.dp))
        PrimaryAction(if (scenarioSaved) "Scenario saved" else "Save this scenario") {
            scenarioSaved = true
        }
        if (scenarioSaved) {
            Spacer(Modifier.height(12.dp))
            SavedPlanCard(
                planSummary = "Laundry after $startTimeText · ${dryerDays.roundToInt()} days each week",
                reminderScheduled = reminderScheduled,
                onToggleReminder = { reminderScheduled = !reminderScheduled },
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}

/** Confirms the saved plan and lets the user add or remove its reminder. */
@Composable
private fun SavedPlanCard(planSummary: String, reminderScheduled: Boolean, onToggleReminder: () -> Unit) {
    val reminderText = if (reminderScheduled) {
        "Reminder scheduled for ${ScenarioRules.REMINDER_LEAD_MINUTES} minutes before the planned start."
    } else {
        "Add a reminder so the recommendation becomes an actionable routine."
    }
    SurfaceCard {
        Text("PLAN READY", style = MaterialTheme.typography.labelMedium, color = StatusGood)
        Spacer(Modifier.height(5.dp))
        Text(planSummary, style = MaterialTheme.typography.titleMedium)
        Text(reminderText, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onToggleReminder) {
            Text(if (reminderScheduled) "REMOVE REMINDER" else "SCHEDULE REMINDER", color = Electric)
        }
    }
}
