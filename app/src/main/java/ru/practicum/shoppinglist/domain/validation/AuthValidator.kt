package ru.practicum.shoppinglist.domain.validation

internal object AuthValidator {

    fun isEmailValid(email: String): Boolean {
        return EMAIL_REGEX.matches(email.trim())
    }

    fun isPasswordValid(password: String): Boolean {
        return password.length >= MIN_PASSWORD_LENGTH
    }

    fun doPasswordsMatch(
        password: String,
        repeatedPassword: String,
    ): Boolean {
        return password == repeatedPassword
    }

    private const val MIN_PASSWORD_LENGTH = 7

    private val EMAIL_REGEX = Regex(
        pattern = "^[A-Za-z0-9._%+-]+@" +
                "[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
    )
}
