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
            .addOnFailureListener { error -> fail(error) }
    }

    fun signInWithGoogle(idToken: String, onSuccess: () -> Unit) {
        startRequest()
        repository.signInWithGoogle(idToken)
            .addOnSuccessListener { finishSignedIn(onSuccess) }
            .addOnFailureListener { error -> fail(error) }
    }

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

    fun signOut() {
        repository.signOut()
        currentUser = null
        clearMessages()
        Log.d(TAG, "Signed out")
    }

    /** Shows a message that did not come from Firebase, such as a cancelled Google sign-in. */
    fun showError(message: String) {
        isLoading = false
        errorMessage = message
    }

    fun clearMessages() {
        errorMessage = null
        infoMessage = null
    }

    private fun startRequest() {
        isLoading = true
        clearMessages()
    }

    private fun finishSignedIn(onSuccess: () -> Unit) {
        isLoading = false
        currentUser = repository.currentUser
        Log.d(TAG, "Signed in as ${currentUser?.email}")
        onSuccess()
    }

    /** Turns a Firebase error into a message the user can act on. */
    private fun fail(error: Exception) {
        isLoading = false
        Log.e(TAG, "Authentication failed", error)
        errorMessage = when (error) {
            is FirebaseAuthWeakPasswordException -> "Choose a stronger password with at least 8 characters."
            is FirebaseAuthInvalidCredentialsException -> "The email or password is not correct."
            is FirebaseAuthInvalidUserException -> "No account was found for this email."
            is FirebaseAuthUserCollisionException -> "An account already exists for this email. Sign in instead."
            else -> "Could not reach the sign-in service. Check your internet connection and try again."
        }
    }

    private companion object {
        const val TAG = "AUTH"
    }
}
