package edu.monash.wattwise.ui.screens.insights

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.monash.wattwise.constants.ScenarioRules
import edu.monash.wattwise.data.model.EnergyContext
import edu.monash.wattwise.data.model.ScenarioPlan
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
 * the estimated saving. The plan is saved to Room, and a daily reminder can be
 * scheduled for it with AlarmManager.
 *
 * @param plan the plan saved in Room, or null if none has been saved yet.
 * @param energyContext used to show today's progress; null until the first meter reading.
 * @param tariff the household's tariff; it decides whether a later start is cheaper.
 */
@Composable
fun GoalsScreen(
    plan: ScenarioPlan?,
    energyContext: EnergyContext?,
    tariff: String,
    onSavePlan: (startHour: Int, dryerDays: Int, monthlySaving: Double) -> Unit,
    onSetReminder: (Boolean) -> Unit,
    onDeletePlan: () -> Unit,
    onBack: () -> Unit,
) {
    // The sliders start from the saved plan, and reset when the saved plan changes.
    var startHour by remember(plan) {
        mutableFloatStateOf(plan?.startHour?.toFloat() ?: ScenarioRules.EARLIEST_START_HOUR)
    }
    var dryerDays by remember(plan) {
        mutableFloatStateOf(plan?.dryerDaysPerWeek?.toFloat() ?: ScenarioRules.DEFAULT_DRYER_DAYS)
    }

    val monthlySaving = SavingsCalculator.estimateMonthlySaving(startHour.roundToInt(), dryerDays.roundToInt(), tariff)
    val startTimeText = Formatters.formatHour(startHour.roundToInt())
    val hasUnsavedChanges = plan == null ||
        plan.startHour != startHour.roundToInt() || plan.dryerDaysPerWeek != dryerDays.roundToInt()

    // From Android 13 the app must ask before it can show the reminder notification.
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        // The alarm is scheduled either way; the notification appears only if allowed.
        onSetReminder(true)
    }

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
            MetricTile(
                label = "Daily target",
                value = energyContext?.let { "${it.dailyTargetKwh} kWh" } ?: "Not set",
                trend = energyContext?.let { "${(it.targetProgress * 100).toInt()}% used today" } ?: "Waiting for meter",
                color = Electric,
                modifier = Modifier.weight(1f),
            )
            MetricTile(
                label = "Saved plan",
                value = plan?.let { Formatters.formatCurrency(it.monthlySaving) } ?: "None",
                trend = if (plan != null) "Estimated per month" else "Save a plan below",
                color = Cyan,
                modifier = Modifier.weight(1f),
            )
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
            hint = "The hour you start the washing. Peak pricing ends at 9 PM on a time-of-use tariff.",
        )
        Spacer(Modifier.height(18.dp))
        LabelledSlider(
            label = "Dryer use",
            valueText = "${dryerDays.roundToInt()} days per week",
            value = dryerDays,
            onValueChange = { dryerDays = it },
            valueRange = ScenarioRules.MIN_DRYER_DAYS..ScenarioRules.MAX_DRYER_DAYS,
            steps = sliderSteps(ScenarioRules.MIN_DRYER_DAYS, ScenarioRules.MAX_DRYER_DAYS, step = 1f),
            hint = "Days each week you use the tumble dryer. Line drying on the other days saves energy.",
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
"Compared with washing at ${Formatters.formatHour(ScenarioRules.EARLIEST_START_HOUR.toInt())} and " +
                    "using the dryer every day, on your ${tariff.lowercase()} tariff. A wash is counted as " +
                    "${ScenarioRules.WASH_LOAD_KWH} kWh and a dryer load as ${ScenarioRules.DRYER_LOAD_KWH} kWh.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        }
        Spacer(Modifier.height(24.dp))
        PrimaryAction(if (hasUnsavedChanges) "Save this scenario" else "Scenario saved") {
            onSavePlan(startHour.roundToInt(), dryerDays.roundToInt(), monthlySaving)
        }
        if (plan != null) {
            Spacer(Modifier.height(12.dp))
            SavedPlanCard(
                plan = plan,
                onToggleReminder = {
                    val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    if (!plan.reminderEnabled && needsPermission) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        onSetReminder(!plan.reminderEnabled)
                    }
                },
                onDelete = onDeletePlan,
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}

/** Shows the plan saved in Room and lets the user add or remove its reminder, or delete the plan. */
@Composable
private fun SavedPlanCard(plan: ScenarioPlan, onToggleReminder: () -> Unit, onDelete: () -> Unit) {
    val reminderText = if (plan.reminderEnabled) {
        "Daily reminder set for ${ScenarioRules.REMINDER_LEAD_MINUTES} minutes before the planned start."
    } else {
        "Add a reminder so the plan becomes part of your routine."
    }
    SurfaceCard {
        Text("SAVED PLAN", style = MaterialTheme.typography.labelMedium, color = StatusGood)
        Spacer(Modifier.height(5.dp))
        Text(
            "Laundry after ${Formatters.formatHour(plan.startHour)} · dryer ${plan.dryerDaysPerWeek} days each week",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(reminderText, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Spacer(Modifier.height(8.dp))
        Row {
            TextButton(onClick = onToggleReminder) {
                Text(if (plan.reminderEnabled) "REMOVE REMINDER" else "SCHEDULE REMINDER", color = Electric)
            }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onDelete) { Text("DELETE PLAN", color = TextSecondary) }
        }
    }
}
