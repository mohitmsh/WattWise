package edu.monash.wattwise.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import edu.monash.wattwise.ui.components.BrandMark
import edu.monash.wattwise.ui.components.PrimaryAction
import edu.monash.wattwise.ui.components.ScreenHeader
import edu.monash.wattwise.ui.theme.*

@Composable
fun OnboardingScreen(onGetStarted: () -> Unit, onLogin: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(Ink).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(30.dp))
        BrandMark(Modifier.align(Alignment.Start))
        Spacer(Modifier.weight(.75f))
        edu.monash.wattwise.ui.components.CompactMetricRing(
            value = .82f,
            valueText = "82",
            unit = "%",
            label = "Energy score",
            color = Electric,
            modifier = Modifier.size(218.dp),
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

@Composable
fun LoginScreen(onBack: () -> Unit, onLogin: () -> Unit, onRegister: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var attempted by remember { mutableStateOf(false) }
    val valid = email.contains("@") && password.length >= 8

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
    ) {
        ScreenHeader("Welcome back", "Know where your energy goes.", onBack = onBack)
        Spacer(Modifier.height(34.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email address") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = attempted && !email.contains("@"),
            supportingText = if (attempted && !email.contains("@")) ({ Text("Enter a valid email address") }) else null,
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    PasswordVisibilityIcon(passwordVisible)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            isError = attempted && password.length < 8,
            supportingText = { Text("Use at least 8 characters") },
        )
        TextButton(
            onClick = { /* Password recovery will be connected to authentication in Assessment 4. */ },
            modifier = Modifier.align(Alignment.End),
        ) {
            Text("FORGOT PASSWORD?", color = Electric)
        }
        Spacer(Modifier.height(10.dp))
        PrimaryAction("Sign in") {
            attempted = true
            if (valid) onLogin()
        }
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onRegister, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("NEW HERE? CREATE AN ACCOUNT", color = Electric)
        }
        Spacer(Modifier.height(22.dp))
        Text(
            "Prototype tip: use hello@wattwise.app and any 8-character password.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )
    }
}

@Composable
fun RegisterScreen(onBack: () -> Unit, onContinue: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var updates by remember { mutableStateOf(true) }
    var attempted by remember { mutableStateOf(false) }
    val valid = name.isNotBlank() && email.contains("@") && password.length >= 8

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
        ScreenHeader("Create account", "Start with a baseline, then improve one action at a time.", onBack = onBack)
        Spacer(Modifier.height(26.dp))
        OutlinedTextField(name, { name = it }, label = { Text("Full name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(email, { email = it }, label = { Text("Email address") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            password,
            { password = it },
            label = { Text("Create password") },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    PasswordVisibilityIcon(passwordVisible)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            isError = attempted && password.length < 8,
            supportingText = { Text("8+ characters; avoid personal details and reused passwords") },
        )
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = updates, onCheckedChange = { updates = it })
            Text("Send a weekly energy summary", style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(20.dp))
        PrimaryAction("Continue") {
            attempted = true
            if (valid) onContinue()
        }
        if (attempted && !valid) {
            Spacer(Modifier.height(10.dp))
            Text("Complete all fields using a valid email and password.", color = Coral, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/** Accessible eye/eye-off control without adding a separate icon dependency. */
@Composable
private fun PasswordVisibilityIcon(passwordVisible: Boolean) {
    val description = if (passwordVisible) "Hide password" else "Show password"
    Canvas(
        Modifier.size(22.dp).semantics { contentDescription = description },
    ) {
        val strokeWidth = 1.8.dp.toPx()
        drawOval(
            color = TextSecondary,
            topLeft = Offset(size.width * .08f, size.height * .24f),
            size = androidx.compose.ui.geometry.Size(size.width * .84f, size.height * .52f),
            style = Stroke(width = strokeWidth),
        )
        drawCircle(
            color = TextSecondary,
            radius = size.minDimension * .12f,
            center = center,
        )
        if (passwordVisible) {
            drawLine(
                color = TextSecondary,
                start = Offset(size.width * .12f, size.height * .1f),
                end = Offset(size.width * .88f, size.height * .9f),
                strokeWidth = strokeWidth,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HouseholdSetupScreen(onComplete: () -> Unit) {
    val homeTypes = listOf("Apartment", "Townhouse", "Detached house", "Shared accommodation")
    val tariffs = listOf("Flat rate", "Time of use", "Solar feed-in")
    var homeExpanded by remember { mutableStateOf(false) }
    var tariffExpanded by remember { mutableStateOf(false) }
    var home by remember { mutableStateOf(homeTypes.first()) }
    var tariff by remember { mutableStateOf(tariffs[1]) }
    var occupants by remember { mutableFloatStateOf(2f) }
    var target by remember { mutableFloatStateOf(15f) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
        Text("STEP 1 OF 1", style = MaterialTheme.typography.labelMedium, color = Electric)
        Spacer(Modifier.height(8.dp))
        Text("Build your household baseline", style = MaterialTheme.typography.headlineLarge)
        Text("We use these details to make recommendations relevant.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Spacer(Modifier.height(28.dp))

        ExposedDropdownMenuBox(expanded = homeExpanded, onExpandedChange = { homeExpanded = it }) {
            OutlinedTextField(
                value = home,
                onValueChange = {},
                readOnly = true,
                label = { Text("Home type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(homeExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
            )
            ExposedDropdownMenu(expanded = homeExpanded, onDismissRequest = { homeExpanded = false }) {
                homeTypes.forEach { option ->
                    DropdownMenuItem(text = { Text(option) }, onClick = { home = option; homeExpanded = false })
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        ExposedDropdownMenuBox(expanded = tariffExpanded, onExpandedChange = { tariffExpanded = it }) {
            OutlinedTextField(
                value = tariff,
                onValueChange = {},
                readOnly = true,
                label = { Text("Electricity tariff") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(tariffExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
            )
            ExposedDropdownMenu(expanded = tariffExpanded, onDismissRequest = { tariffExpanded = false }) {
                tariffs.forEach { option ->
                    DropdownMenuItem(text = { Text(option) }, onClick = { tariff = option; tariffExpanded = false })
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        Text("OCCUPANTS", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        Text("${occupants.toInt()} people", style = MaterialTheme.typography.titleLarge)
        Slider(value = occupants, onValueChange = { occupants = it }, valueRange = 1f..6f, steps = 4)
        Spacer(Modifier.height(16.dp))
        Text("DAILY ENERGY TARGET", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        Text("${target.toInt()} kWh", style = MaterialTheme.typography.titleLarge)
        Slider(value = target, onValueChange = { target = it }, valueRange = 8f..30f, steps = 10)
        Text("You can change this target later.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Spacer(Modifier.height(28.dp))
        PrimaryAction("Create my dashboard", onClick = onComplete)
    }
}
