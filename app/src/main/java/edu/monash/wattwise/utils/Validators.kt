package edu.monash.wattwise.utils

import edu.monash.wattwise.constants.ValidationRules

/** Input checks shared by the forms, kept out of the composables so they can be reused. */
object Validators {

    fun isValidEmail(email: String): Boolean =
        email.contains(ValidationRules.EMAIL_REQUIRED_SYMBOL)

    fun isValidPassword(password: String): Boolean =
        password.length >= ValidationRules.MIN_PASSWORD_LENGTH

    fun isValidName(name: String): Boolean = name.isNotBlank()
}
