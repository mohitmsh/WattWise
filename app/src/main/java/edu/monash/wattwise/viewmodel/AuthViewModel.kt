package edu.monash.wattwise.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import edu.monash.wattwise.constants.ValidationRules
import edu.monash.wattwise.data.repository.AuthRepository

/**
 * Handles sign-in, sign-up, Google sign-in, password reset and sign-out, and
 * exposes the result to the screens as Compose state.
 */
class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    /** The signed-in user, or null when nobody is signed in. */
    var currentUser by mutableStateOf<FirebaseUser?>(repository.currentUser)
        private set

    /** True while a request to Firebase is in progress, so buttons can be disabled. */
    var isLoading by mutableStateOf(false)
        private set

    /** A message to show under the form when a request fails; null when there is no error. */
    var errorMessage by mutableStateOf<String?>(null)
        private set

    /** A confirmation to show under the form, for example after a reset email is sent. */
    var infoMessage by mutableStateOf<String?>(null)
        private set

    /** Signs in with an email address and a password, then calls [onSuccess]. */
    fun signIn(email: String, password: String, onSuccess: () -> Unit) {
        startRequest()
        repository.signIn(email.trim(), password)
            .addOnSuccessListener { finishSignedIn(onSuccess) }
            .addOnFailureListener { error -> fail(error) }
    }

    /** Creates the account, then saves the name on it. */
    fun register(name: String, email: String, password: String, onSuccess: () -> Unit) {
        startRequest()
        repository.register(email.trim(), password)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user == null) {
                    finishSignedIn(onSuccess)
                } else {
                    // The account exists even if saving the name fails, so continue either way.
                    repository.setDisplayName(user, name.trim()).addOnCompleteListener { finishSignedIn(onSuccess) }
                }
            }
            .addOnFailureListener { error -> fail(error, signingUp = true) }
    }

    /** Signs in to Firebase with the ID token that Google returned, then calls [onSuccess]. */
    fun signInWithGoogle(idToken: String, onSuccess: () -> Unit) {
        startRequest()
        repository.signInWithGoogle(idToken)
            .addOnSuccessListener { finishSignedIn(onSuccess) }
            .addOnFailureListener { error -> fail(error) }
    }

    /** Asks Firebase to send a reset link and shows a confirmation under the form. */
    fun sendPasswordReset(email: String) {
        startRequest()
        repository.sendPasswordReset(email.trim())
            .addOnSuccessListener {
                isLoading = false
                infoMessage = "If an account exists for $email, a reset link has been sent to it."
                Log.d(TAG, "Password reset email requested")
            }
            .addOnFailureListener { error -> fail(error) }
    }

    /** Signs the user out and clears any message on the screen. */
    fun signOut() {
        repository.signOut()
        currentUser = null
        clearMessages()
        Log.d(TAG, "Signed out")
    }

    /** Shows a message that did not come from Firebase, such as a cancelled Google sign-in. */
    fun showError(message: String) {
        isLoading = false
        infoMessage = null
        errorMessage = message
    }

    /** Removes the error and confirmation messages, for example when the user opens a different form. */
    fun clearMessages() {
        errorMessage = null
        infoMessage = null
    }

    /** Marks a request as started: shows the loading state and clears old messages. */
    private fun startRequest() {
        isLoading = true
        clearMessages()
    }

    /** Marks a request as finished, stores the signed-in user and continues to the next screen. */
    private fun finishSignedIn(onSuccess: () -> Unit) {
        isLoading = false
        currentUser = repository.currentUser
        Log.d(TAG, "Signed in as ${currentUser?.email}")
        onSuccess()
    }

    /** Turns a Firebase error into a message the user can act on. */
    private fun fail(error: Exception, signingUp: Boolean = false) {
        isLoading = false
        Log.e(TAG, "Authentication failed", error)
        errorMessage = when (error) {
            is FirebaseAuthWeakPasswordException -> "Choose a stronger password. ${ValidationRules.PASSWORD_HINT}."
            // When signing up there is no password to get wrong, so Firebase is rejecting the address.
            is FirebaseAuthInvalidCredentialsException ->
                if (signingUp) "This email address is not valid. Check it and try again."
                else "The email or password is not correct."
            is FirebaseAuthInvalidUserException -> "No account was found for this email."
            is FirebaseAuthUserCollisionException -> "An account already exists for this email. Sign in instead, or use Continue with Google " +
                    "if you signed up with Google."
            else -> "Could not reach the sign-in service. Check your internet connection and try again."
        }
    }

    private companion object {
        const val TAG = "AUTH"
    }
}
