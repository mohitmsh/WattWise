package edu.monash.wattwise.ui.components

import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import edu.monash.wattwise.R
import edu.monash.wattwise.ui.theme.Dimens
import edu.monash.wattwise.ui.theme.TextPrimary
import kotlinx.coroutines.launch

private const val TAG = "AUTH"

/**
 * Button that opens the Google account chooser. When the user picks an
 * account, Google returns an ID token, which is passed to [onIdToken] so the
 * app can sign in to Firebase with it.
 */
@Composable
fun GoogleSignInButton(enabled: Boolean, onIdToken: (String) -> Unit, onError: (String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    OutlinedButton(
        onClick = {
            // The web client id comes from google-services.json and tells Google which project is asking.
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .build()
            val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

            // getCredential waits for the user to choose an account, so it runs in a coroutine.
            scope.launch {
                try {
                    val result = CredentialManager.create(context).getCredential(context, request)
                    val googleCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
                    onIdToken(googleCredential.idToken)
                } catch (e: GetCredentialException) {
                    Log.e(TAG, "Google sign-in did not finish", e)
                    onError("Google sign-in was cancelled or no Google account is set up on this device.")
                }
            }
        },
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(Dimens.PrimaryButtonHeight),
        shape = RoundedCornerShape(Dimens.PILL_CORNER_PERCENT),
    ) {
        Text("CONTINUE WITH GOOGLE", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
    }
}
