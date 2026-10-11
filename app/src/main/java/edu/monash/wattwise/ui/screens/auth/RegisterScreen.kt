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
import edu.monash.wattwise.ui.components.GoogleSignInButton
import edu.monash.wattwise.ui.components.PasswordField
import edu.monash.wattwise.ui.components.PrimaryAction
import edu.monash.wattwise.ui.components.ScreenHeader
import edu.monash.wattwise.ui.theme.Coral
import edu.monash.wattwise.ui.theme.Dimens
import edu.monash.wattwise.ui.theme.TextSecondary
import edu.monash.wattwise.utils.Validators

/**
 * Sign-up form for new users, with email and password or a Google account.
 * After the account is created the app continues to the household form.
 *
 * @param isLoading true while Firebase is creating the account.
 * @param errorMessage the reason sign-up failed, or null.
 */
@Composable
fun RegisterScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onRegister: (name: String, email: String, password: String) -> Unit,
    onGoogleIdToken: (String) -> Unit,
    onGoogleError: (String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var consentGiven by remember { mutableStateOf(false) }
    var attempted by remember { mutableStateOf(false) }

    val formValid = Validators.isValidName(name) &&
        Validators.isValidEmail(email) &&
        Validators.isValidPassword(password) &&
        consentGiven

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(Dimens.ScreenPaddingWide),
    ) {
        ScreenHeader("Step 1 of 2", "Create your account", "Next you will tell us about your household.", onBack = onBack)
        Spacer(Modifier.height(20.dp))
        Text("* Required", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full name *") },
            placeholder = { Text("e.g. Alex Chen") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = attempted && !Validators.isValidName(name),
        )
        Spacer(Modifier.height(Dimens.FieldSpacing))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email address *") },
            placeholder = { Text("e.g. name@example.com") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = attempted && !Validators.isValidEmail(email),
        )
        Spacer(Modifier.height(Dimens.FieldSpacing))
        PasswordField(
            value = password,
            onValueChange = { password = it },
            label = "Create password *",
            hint = "${ValidationRules.MIN_PASSWORD_LENGTH}+ characters; avoid personal details and reused passwords",
            isError = attempted && !Validators.isValidPassword(password),
        )
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = consentGiven, onCheckedChange = { consentGiven = it })
            Text(
                "I agree to my household and energy data being stored on this device *",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Spacer(Modifier.height(20.dp))
        PrimaryAction(if (isLoading) "Creating account…" else "Create account") {
            attempted = true
            if (formValid && !isLoading) onRegister(name, email, password)
        }
        if (attempted && !formValid) {
            Spacer(Modifier.height(10.dp))
            Text(
                "Complete all fields with a valid email and password, and tick the box to agree.",
                color = Coral,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        AuthMessages(errorMessage, infoMessage = null)
        Spacer(Modifier.height(12.dp))
        // Google sign-up needs the same consent, so the button is enabled only once the box is ticked.
        GoogleSignInButton(enabled = consentGiven && !isLoading, onIdToken = onGoogleIdToken, onError = onGoogleError)
        Spacer(Modifier.height(16.dp))
    }
}
