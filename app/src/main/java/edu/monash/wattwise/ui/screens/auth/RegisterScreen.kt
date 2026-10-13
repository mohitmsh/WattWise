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
import androidx.compose.runtime.saveable.rememberSaveable
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
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    // The password is not kept across a rotation, so it is never written to saved state.
    var password by remember { mutableStateOf("") }
    var consentGiven by rememberSaveable { mutableStateOf(false) }
    var attempted by rememberSaveable { mutableStateOf(false) }

    // Each field is checked on its own so the message can appear beside the field that needs fixing.
    val nameError = attempted && !Validators.isValidName(name)
    val emailError = attempted && !Validators.isValidEmail(email)
    val passwordError = attempted && !Validators.isValidPassword(password)
    val consentError = attempted && !consentGiven
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
            isError = nameError,
            supportingText = {
                if (nameError) Text("Enter your name, up to ${ValidationRules.MAX_NAME_LENGTH} characters")
            },
        )
        Spacer(Modifier.height(Dimens.FieldSpacing))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email address *") },
            placeholder = { Text("e.g. name@example.com") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = emailError,
            supportingText = { if (emailError) Text("Enter a valid email address, such as name@example.com") },
        )
        Spacer(Modifier.height(Dimens.FieldSpacing))
        PasswordField(
            value = password,
            onValueChange = { password = it },
            label = "Create password *",
            hint = ValidationRules.PASSWORD_HINT,
            isError = passwordError,
        )
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = consentGiven, onCheckedChange = { consentGiven = it })
            Text(
                "I agree to my household and energy data being stored on this device and backed up to my account *",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Spacer(Modifier.height(20.dp))
        PrimaryAction(if (isLoading) "Creating account…" else "Create account", isLoading = isLoading) {
            attempted = true
            if (formValid && !isLoading) onRegister(name, email, password)
        }
        if (consentError) {
            Spacer(Modifier.height(10.dp))
            Text(
                "Tick the box to agree before creating your account.",
                color = Coral,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        AuthMessages(errorMessage, infoMessage = null)
        Spacer(Modifier.height(12.dp))
        // The Google button is always available. The tick box belongs to the email form above,
        // so a Google sign-up is told about the data storage in the line under the button instead.
        GoogleSignInButton(enabled = !isLoading, onIdToken = onGoogleIdToken, onError = onGoogleError)
        Spacer(Modifier.height(8.dp))
        Text(
            "By continuing with Google you agree to your household and energy data being stored on this device and backed up to your account.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )
        Spacer(Modifier.height(16.dp))
    }
}
