package edu.monash.wattwise.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import edu.monash.wattwise.ui.components.BrandMark
import edu.monash.wattwise.ui.components.CompactMetricRing
import edu.monash.wattwise.ui.components.PrimaryAction
import edu.monash.wattwise.ui.theme.Dimens
import edu.monash.wattwise.ui.theme.Electric
import edu.monash.wattwise.ui.theme.Ink
import edu.monash.wattwise.ui.theme.TextPrimary
import edu.monash.wattwise.ui.theme.TextSecondary

/** First screen for signed-out users: introduces the app and offers sign-up or login. */
@Composable
fun OnboardingScreen(onGetStarted: () -> Unit, onLogin: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(Ink).padding(Dimens.ScreenPaddingWide),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(30.dp))
        BrandMark(Modifier.align(Alignment.Start))
        Spacer(Modifier.weight(.75f))
        CompactMetricRing(
            value = .82f,
            valueText = "82",
            unit = "%",
            label = "Energy score",
            color = Electric,
            modifier = Modifier.size(Dimens.HeroRingSize),
            hero = true,
        )
        Spacer(Modifier.height(38.dp))
        Text(
            "USE ENERGY\nWITH INTENTION",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Understand your household, reduce avoidable costs, and act at the right time.",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.weight(1f))
        PrimaryAction("Create my energy plan", onClick = onGetStarted)
        TextButton(onClick = onLogin) { Text("I ALREADY HAVE AN ACCOUNT", color = TextPrimary) }
    }
}
