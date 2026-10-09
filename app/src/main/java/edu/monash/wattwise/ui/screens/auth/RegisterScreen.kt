package edu.monash.wattwise.ui.screens.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.monash.wattwise.constants.ValidationRules
import edu.monash.wattwise.ui.components.PasswordField
import edu.monash.wattwise.ui.components.PrimaryAction
import edu.monash.wattwise.ui.components.ScreenHeader
import edu.monash.wattwise.ui.theme.Coral
import edu.monash.wattwise.ui.theme.Dimens
import edu.monash.wattwise.utils.Validators

/** Sign-up form for new users; continues to household setup once the inputs are valid. */
@Composable
fun RegisterScreen(onBack: () -> Unit, onContinue: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var weeklySummary by remember { mutableStateOf(true) }
    var attempted by remember { mutableStateOf(false) }

    val formValid = Validators.isValidName(name) &&
        Validators.isValidEmail(email) &&
        Validators.isValidPassword(password)

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(Dimens.ScreenPaddingWide),
    ) {
        ScreenHeader("Create account", "Start with a baseline, then improve one action at a time.", onBack = onBack)
        Spacer(Modifier.height(26.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = attempted && !Validators.isValidName(name),
        )
        Spacer(Modifier.height(Dimens.FieldSpacing))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email address") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = attempted && !Validators.isValidEmail(email),
        )
        Spacer(Modifier.height(Dimens.FieldSpacing))
        PasswordField(
            value = password,
            onValueChange = { password = it },
            label = "Create password",
            hint = "${ValidationRules.MIN_PASSWORD_LENGTH}+ characters; avoid personal details and reused passwords",
            isError = attempted && !Validators.isValidPassword(password),
        )
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = weeklySummary, onCheckedChange = { weeklySummary = it })
            Text("Send a weekly energy summary", style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(20.dp))
        PrimaryAction("Continue") {
            attempted = true
            if (formValid) onContinue()
        }
        if (attempted && !formValid) {
            Spacer(Modifier.height(10.dp))
            Text(
                "Complete all fields using a valid email and password.",
                color = Coral,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
