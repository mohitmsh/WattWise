package edu.monash.wattwise.ui.screens.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import edu.monash.wattwise.ui.theme.Dimens
import edu.monash.wattwise.ui.theme.Electric
import edu.monash.wattwise.ui.theme.TextSecondary
import edu.monash.wattwise.utils.Validators

/** Sign-in form for existing users. Errors appear only after the first sign-in attempt. */
@Composable
fun LoginScreen(onBack: () -> Unit, onLogin: () -> Unit, onRegister: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var attempted by remember { mutableStateOf(false) }

    val emailError = attempted && !Validators.isValidEmail(email)
    val passwordError = attempted && !Validators.isValidPassword(password)

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(Dimens.ScreenPaddingWide),
    ) {
        ScreenHeader("Welcome back", "Know where your energy goes.", onBack = onBack)
        Spacer(Modifier.height(34.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email address") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = emailError,
            supportingText = { if (emailError) Text("Enter a valid email address") },
        )
        Spacer(Modifier.height(Dimens.FieldSpacing))
        PasswordField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            hint = "Use at least ${ValidationRules.MIN_PASSWORD_LENGTH} characters",
            isError = passwordError,
        )
        TextButton(
            onClick = { /* Password recovery is connected once authentication is implemented. */ },
            modifier = Modifier.align(Alignment.End),
        ) {
            Text("FORGOT PASSWORD?", color = Electric)
        }
        Spacer(Modifier.height(10.dp))
        PrimaryAction("Sign in") {
            attempted = true
            if (Validators.isValidEmail(email) && Validators.isValidPassword(password)) onLogin()
        }
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onRegister, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("NEW HERE? CREATE AN ACCOUNT", color = Electric)
        }
        Spacer(Modifier.height(22.dp))
        Text(
            "Prototype tip: use hello@wattwise.app and any " +
                "${ValidationRules.MIN_PASSWORD_LENGTH}-character password.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )
    }
}
