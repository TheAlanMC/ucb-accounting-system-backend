package ucb.accounting.backend.dto

data class PasswordUpdateDto (
    val oldPassword: String,
    val newPassword: String,
    val confirmPassword: String
)