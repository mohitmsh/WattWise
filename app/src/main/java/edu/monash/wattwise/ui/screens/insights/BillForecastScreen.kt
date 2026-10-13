package edu.monash.wattwise.ui.screens.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.monash.wattwise.constants.BillRules
import edu.monash.wattwise.data.model.Appliance
import edu.monash.wattwise.data.model.HourlyUsage
import edu.monash.wattwise.ui.components.BreakdownBar
import edu.monash.wattwise.ui.components.BreakdownSegment
import edu.monash.wattwise.ui.components.ChartLegendItem
import edu.monash.wattwise.ui.components.ComparisonLineChart
import edu.monash.wattwise.ui.components.LabelledSlider
import edu.monash.wattwise.ui.components.MetricTile
import edu.monash.wattwise.ui.components.PrimaryAction
import edu.monash.wattwise.ui.components.ScreenHeader
import edu.monash.wattwise.ui.components.SurfaceCard
import edu.monash.wattwise.ui.theme.Amber
import edu.monash.wattwise.ui.theme.Cyan
import edu.monash.wattwise.ui.theme.Dimens
import edu.monash.wattwise.ui.theme.Electric
import edu.monash.wattwise.ui.theme.StatusGood
import edu.monash.wattwise.ui.theme.SurfaceRaised
import edu.monash.wattwise.ui.theme.TextSecondary
import edu.monash.wattwise.utils.BillCalculator
import edu.monash.wattwise.utils.DateUtils
import edu.monash.wattwise.utils.EnergyCalculator
import edu.monash.wattwise.utils.Formatters
import edu.monash.wattwise.utils.TariffCalculator
import edu.monash.wattwise.utils.sliderSteps
import kotlin.math.roundToInt

/**
 * Bill forecast: estimates the bill for the current billing period from the
 * usage saved in Room, compares it with the budget the user sets, and shows
 * what the household can do to stay inside the budget.
 *
 * @param usage every hour saved in Room.
 * @param tariff the household's tariff, used to price each hour.
 * @param billingStartMillis the billing date from the household form.
 * @param savedBudget the monthly budget saved in Room, in dollars.
 * @param appliances the user's appliances, used to show which ones cost the most.
 * @param onSaveBudget called with the new budget when the user saves it.
 */
@Composable
fun BillForecastScreen(
    usage: List<HourlyUsage>,
    tariff: String,
    billingStartMillis: Long,
    savedBudget: Int,
    appliances: List<Appliance>,
    onSaveBudget: (Int) -> Unit,
    onBack: () -> Unit,
) {
    // The slider starts from the saved budget and resets when the saved budget changes.
    var budget by rememberSaveable(savedBudget) { mutableFloatStateOf(savedBudget.toFloat()) }
    val budgetDollars = budget.roundToInt()

    val forecast = BillCalculator.forecast(usage, tariff, billingStartMillis)
    val difference = forecast.projectedBill - budgetDollars
    val isOverBudget = difference > 0
    val budgetShare = if (budgetDollars > 0) (forecast.projectedBill / budgetDollars).toFloat() else 0f
    val allowanceKwh = BillCalculator.dailyAllowanceKwh(forecast, budgetDollars, tariff)
    val periodEnd = DateUtils.addDays(forecast.periodStartMillis, BillRules.PERIOD_DAYS - 1)

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(Dimens.ScreenPadding)) {
        Spacer(Modifier.height(14.dp))
        ScreenHeader(
            eyebrow = "Bill forecast",
            title = "Will this bill fit your budget?",
            subtitle = "Billing period ${Formatters.formatShortDate(forecast.periodStartMillis)} to " +
                "${Formatters.formatShortDate(periodEnd)}, from your household details.",
            onBack = onBack,
        )
        Spacer(Modifier.height(22.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricTile(
                label = "Estimated bill",
                value = Formatters.formatCurrency(forecast.projectedBill),
                trend = "${Formatters.formatCurrency(forecast.lowEstimate)} to ${Formatters.formatCurrency(forecast.highEstimate)}",
                color = if (isOverBudget) Amber else StatusGood,
                modifier = Modifier.weight(1f),
            )
            MetricTile(
                label = "Your budget",
                value = "$$budgetDollars",
                trend = if (isOverBudget) {
                    "${Formatters.formatCurrency(difference)} over"
                } else {
                    "${Formatters.formatCurrency(-difference)} to spare"
                },
                color = Cyan,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(12.dp))
        // Progress indicator: the estimated bill as a share of the budget. It is full at 100%.
        LinearProgressIndicator(
            progress = { budgetShare.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = if (isOverBudget) Amber else Electric,
            trackColor = SurfaceRaised,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "The estimate is ${Formatters.toPercent(budgetShare)}% of your budget, with " +
                "${Formatters.formatCount(forecast.daysLeft, "day")} left in the period.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )
        Spacer(Modifier.height(18.dp))

        // What to do: the daily energy that keeps the bill inside the budget.
        SurfaceCard {
            Text("WHAT TO DO", style = MaterialTheme.typography.labelMedium, color = Electric)
            Spacer(Modifier.height(6.dp))
            Text(
                when {
                    forecast.daysLeft <= 0 -> "This billing period has ended. A new period starts tomorrow."
                    allowanceKwh <= 0.0 -> "The budget for this period is already used. Raise the budget, or use as little as you can until the period ends."
                    isOverBudget -> "Use ${Formatters.formatKwh(allowanceKwh)} kWh a day or less for the rest of the period to stay inside your budget."
                    else -> "You are on track. You can use up to ${Formatters.formatKwh(allowanceKwh)} kWh a day and stay inside your budget."
                },
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Your usual day costs ${Formatters.formatCurrency(forecast.averageDailyCost)} for energy on your " +
                    "${tariff.lowercase()} tariff.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        }
        Spacer(Modifier.height(16.dp))

        // Line chart: the running total of the bill against an even spend of the budget.
        SurfaceCard {
            Text("BILL SO FAR AND ESTIMATE ($)", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ChartLegendItem("Estimated bill", Electric)
                ChartLegendItem("Budget pace", TextSecondary)
            }
            Spacer(Modifier.height(8.dp))
            ComparisonLineChart(
                actual = forecast.cumulativeCost,
                typical = BillCalculator.budgetPace(budgetDollars),
                labels = (1..BillRules.PERIOD_DAYS).map { day -> "$day" },
                actualColor = Electric,
                animateChanges = false,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                if (forecast.completedDays == 0) {
                    "This billing period starts today, so every day uses your usual daily cost. Above the " +
                        "budget line means the bill will pass the budget."
                } else {
                    "Day ${forecast.completedDays} of ${BillRules.PERIOD_DAYS} is the last finished day. Later days use " +
                        "your usual daily cost. Above the budget line means the bill will pass the budget."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        }
        Spacer(Modifier.height(16.dp))

        // How the estimate is made, so the user can check every number.
        SurfaceCard {
            Text("HOW THE ESTIMATE IS MADE", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Spacer(Modifier.height(8.dp))
            BreakdownBar(
                listOf(
                    BreakdownSegment(
                        amountText = Formatters.formatCurrency(forecast.costSoFar),
                        label = "Energy used on ${Formatters.formatCount(forecast.completedDays, "finished day")}",
                        weight = forecast.costSoFar.toFloat(),
                        color = Electric,
                    ),
                    BreakdownSegment(
                        amountText = Formatters.formatCurrency(forecast.averageDailyCost * forecast.daysLeft),
                        label = "${Formatters.formatCount(forecast.daysLeft, "day")} left at your usual daily cost",
                        weight = (forecast.averageDailyCost * forecast.daysLeft).toFloat(),
                        color = Cyan,
                    ),
                    BreakdownSegment(
                        amountText = Formatters.formatCurrency(forecast.supplyCharge),
                        label = "Supply charge, ${Formatters.formatCurrency(BillRules.DAILY_SUPPLY_CHARGE)} a day",
                        weight = forecast.supplyCharge.toFloat(),
                        color = Amber,
                    ),
                ),
            )
        }
        Spacer(Modifier.height(16.dp))

        // The saved appliances that add the most to a bill, largest first.
        val costliest = appliances
            .map { appliance ->
                appliance to EnergyCalculator.applianceDailyKwh(appliance) * TariffCalculator.averageRate(tariff) * BillRules.PERIOD_DAYS
            }
            .sortedByDescending { it.second }
            .take(MAX_APPLIANCES_SHOWN)
        SurfaceCard {
            Text("APPLIANCES THAT ADD THE MOST", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Spacer(Modifier.height(8.dp))
            if (costliest.isEmpty()) {
                Text("Add your appliances in More to see what each one adds to a bill.", color = TextSecondary)
            }
            costliest.forEach { (appliance, periodCost) ->
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(appliance.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    Text(
                        "${Formatters.formatCurrency(periodCost)} each period",
                        style = MaterialTheme.typography.titleMedium,
                        color = Electric,
                    )
                }
            }
        }
        Spacer(Modifier.height(22.dp))

        LabelledSlider(
            label = "Budget for one billing period",
            valueText = "$$budgetDollars",
            value = budget,
            onValueChange = { budget = it },
            valueRange = BillRules.MIN_BUDGET..BillRules.MAX_BUDGET,
            steps = sliderSteps(BillRules.MIN_BUDGET, BillRules.MAX_BUDGET, BillRules.BUDGET_STEP),
            hint = "Move the slider to see how the estimate compares with a different budget. Save it to keep it.",
        )
        Spacer(Modifier.height(18.dp))
        PrimaryAction(if (budgetDollars == savedBudget) "Budget saved" else "Save this budget") {
            onSaveBudget(budgetDollars)
        }
        Spacer(Modifier.height(24.dp))
    }
}

/** Number of appliances listed under "appliances that add the most". */
private const val MAX_APPLIANCES_SHOWN = 3
