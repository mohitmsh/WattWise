package edu.monash.wattwise.utils

import android.util.Patterns
import edu.monash.wattwise.constants.ValidationRules

/** Input checks shared by the forms, kept out of the composables so they can be reused. */
object Validators {

    /** True for an address with a name, an @ and a domain, such as name@example.com. */
    fun isValidEmail(email: String): Boolean = Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()

    /** A new password must be long enough and mix letters with at least one number. */
    fun isValidPassword(password: String): Boolean =
        password.length >= ValidationRules.MIN_PASSWORD_LENGTH &&
            password.any { it.isLetter() } &&
            password.any { it.isDigit() }

    /** True when a name has been typed and it is short enough to fit on the screens. */
    fun isValidName(name: String): Boolean =
        name.isNotBlank() && name.trim().length <= ValidationRules.MAX_NAME_LENGTH
}
