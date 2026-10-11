package edu.monash.wattwise.ui.screens.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import edu.monash.wattwise.R
import edu.monash.wattwise.ui.components.BrandMark
import edu.monash.wattwise.ui.components.PrimaryAction
import edu.monash.wattwise.ui.theme.Border
import edu.monash.wattwise.ui.theme.Dimens
import edu.monash.wattwise.ui.theme.Electric
import edu.monash.wattwise.ui.theme.Ink
import edu.monash.wattwise.ui.theme.TextPrimary
import edu.monash.wattwise.ui.theme.TextSecondary
import edu.monash.wattwise.ui.theme.TextTertiary

/**
 * One page of the introduction: a real screenshot of the app with its main
 * parts highlighted, and a title and sentence explaining the screen.
 */
private data class OnboardingPage(val image: Int, val title: String, val detail: String)

private val onboardingPages = listOf(
    OnboardingPage(
        image = R.drawable.onboarding_home,
        title = "See your energy at a glance",
        detail = "Home shows your live power, today’s energy and how today compares with your usual day.",
    ),
    OnboardingPage(
        image = R.drawable.onboarding_usage,
        title = "Track it against your target",
        detail = "Usage reports each day, week or month and marks the days that went over your target.",
    ),
    OnboardingPage(
        image = R.drawable.onboarding_map,
        title = "Plan around the weather",
        detail = "The map turns today’s weather into heating, cooling and solar advice for your home.",
    ),
)

/**
 * First screens for signed-out users. Three pages introduce the app, one at a
 * time, and the last page leads to creating an account. Existing users can
 * sign in from any page.
 */
@Composable
fun OnboardingScreen(onGetStarted: () -> Unit, onLogin: () -> Unit) {
    var pageIndex by remember { mutableIntStateOf(0) }
    val page = onboardingPages[pageIndex]
    val isLastPage = pageIndex == onboardingPages.lastIndex

    // The system Back button returns to the previous page before it leaves the app.
    BackHandler(enabled = pageIndex > 0) { pageIndex-- }

    Column(
        Modifier.fillMaxSize().background(Ink).padding(Dimens.ScreenPaddingWide),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            BrandMark()
            Spacer(Modifier.weight(1f))
            // Skip jumps to the last page, where the account buttons are.
            if (!isLastPage) {
                TextButton(onClick = { pageIndex = onboardingPages.lastIndex }) {
                    Text("SKIP", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Image(
            painter = painterResource(page.image),
            contentDescription = "Screenshot of the app: ${page.title}",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(Dimens.TileRadius))
                .border(1.dp, Border, RoundedCornerShape(Dimens.TileRadius)),
        )
        Spacer(Modifier.height(18.dp))
        Text(page.title, style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            page.detail,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))

        // One dot for each page; the wider dot is the page being shown.
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            onboardingPages.indices.forEach { index ->
                val isCurrent = index == pageIndex
                Box(
                    Modifier
                        .height(8.dp)
                        .width(if (isCurrent) 22.dp else 8.dp)
                        .background(if (isCurrent) Electric else TextTertiary, CircleShape),
                )
            }
        }
        Spacer(Modifier.height(18.dp))
        PrimaryAction(if (isLastPage) "Create my account" else "Next") {
            if (isLastPage) onGetStarted() else pageIndex++
        }
        TextButton(onClick = onLogin) { Text("I ALREADY HAVE AN ACCOUNT", color = TextPrimary) }
    }
}
