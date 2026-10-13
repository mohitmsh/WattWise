package edu.monash.wattwise.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest

/**
 * Single access point for Firebase Authentication, so the ViewModel never
 * touches FirebaseAuth directly. Each call returns a Task that finishes later.
 */
class AuthRepository {

    private val auth = FirebaseAuth.getInstance()

    /** The signed-in user, or null. Firebase keeps the user signed in between app launches. */
    val currentUser: FirebaseUser? get() = auth.currentUser

    /** Signs in an existing user with an email address and a password. */
    fun signIn(email: String, password: String): Task<AuthResult> =
        auth.signInWithEmailAndPassword(email, password)

    /** Creates a new account with an email address and a password. */
    fun register(email: String, password: String): Task<AuthResult> =
        auth.createUserWithEmailAndPassword(email, password)

    /** Saves the user's name on their Firebase account after registering. */
    fun setDisplayName(user: FirebaseUser, name: String): Task<Void> =
        user.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name).build())

    /** Signs in to Firebase with the ID token Google returned for the chosen account. */
    fun signInWithGoogle(idToken: String): Task<AuthResult> =
        auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))

    /** Asks Firebase to send a password reset link to [email]. */
    fun sendPasswordReset(email: String): Task<Void> = auth.sendPasswordResetEmail(email)

    /** Signs the user out on this device. */
    fun signOut() = auth.signOut()
}
