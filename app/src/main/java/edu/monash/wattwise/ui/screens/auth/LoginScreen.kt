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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.monash.wattwise.ui.components.GoogleSignInButton
import edu.monash.wattwise.ui.theme.Coral
import edu.monash.wattwise.ui.components.PasswordField
import edu.monash.wattwise.ui.components.PrimaryAction
import edu.monash.wattwise.ui.components.ScreenHeader
import edu.monash.wattwise.ui.theme.Dimens
import edu.monash.wattwise.ui.theme.Electric
import edu.monash.wattwise.ui.theme.TextSecondary
import edu.monash.wattwise.utils.Validators

/**
 * Sign-in form for existing users, with email and password or a Google account.
 * Field errors appear only after the first sign-in attempt.
 *
 * @param isLoading true while Firebase is checking the details.
 * @param errorMessage the reason sign-in failed, or null.
 * @param infoMessage a confirmation such as "reset link sent", or null.
 */
@Composable
fun LoginScreen(
    isLoading: Boolean,
    errorMessage: String?,
    infoMessage: String?,
    onBack: () -> Unit,
    onLogin: (email: String, password: String) -> Unit,
    onGoogleIdToken: (String) -> Unit,
    onGoogleError: (String) -> Unit,
    onForgotPassword: (email: String) -> Unit,
    onRegister: () -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    // The password is not kept across a rotation, so it is never written to saved state.
    var password by remember { mutableStateOf("") }
    var attempted by rememberSaveable { mutableStateOf(false) }
    // Set when "Forgot password?" is tapped without a valid email in the field.
    var emailNeededForReset by rememberSaveable { mutableStateOf(false) }

    val emailError = (attempted || emailNeededForReset) && !Validators.isValidEmail(email)
    // Signing in only needs a password to be typed; the strength rule applies when creating one.
    val passwordError = attempted && password.isBlank()

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(Dimens.ScreenPaddingWide),
    ) {
        ScreenHeader("Welcome back", "Know where your energy goes.", onBack = onBack)
        Spacer(Modifier.height(34.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email address *") },
            placeholder = { Text("e.g. name@example.com") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = emailError,
            supportingText = {
                if (emailError) {
                    Text(if (emailNeededForReset) "Enter your email first, then tap Forgot password" else "Enter a valid email address")
                }
            },
        )
        Spacer(Modifier.height(Dimens.FieldSpacing))
        PasswordField(
            value = password,
            onValueChange = { password = it },
            label = "Password *",
            hint = if (passwordError) "Enter your password" else "",
            isError = passwordError,
        )
        TextButton(
            onClick = {
                if (Validators.isValidEmail(email)) {
                    emailNeededForReset = false
                    onForgotPassword(email)
                } else {
                    emailNeededForReset = true
                }
            },
            modifier = Modifier.align(Alignment.End),
        ) {
            Text("FORGOT PASSWORD?", color = Electric)
        }
        AuthMessages(errorMessage, infoMessage)
        Spacer(Modifier.height(10.dp))
        PrimaryAction(if (isLoading) "Signing in…" else "Sign in", isLoading = isLoading) {
            attempted = true
            if (!isLoading && Validators.isValidEmail(email) && password.isNotBlank()) {
                onLogin(email, password)
            }
        }
        Spacer(Modifier.height(12.dp))
        GoogleSignInButton(enabled = !isLoading, onIdToken = onGoogleIdToken, onError = onGoogleError)
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onRegister, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("NEW HERE? CREATE AN ACCOUNT", color = Electric)
        }
    }
}

/** The error or confirmation from the last sign-in request, shown under the form. */
@Composable
fun AuthMessages(errorMessage: String?, infoMessage: String?) {
    if (errorMessage != null) {
        Spacer(Modifier.height(6.dp))
        Text(errorMessage, color = Coral, style = MaterialTheme.typography.bodyMedium)
    }
    if (infoMessage != null) {
        Spacer(Modifier.height(6.dp))
        Text(infoMessage, color = Electric, style = MaterialTheme.typography.bodyMedium)
    }
}
