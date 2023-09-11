package ucb.accounting.backend.dto

data class PasswordUpdateDto (
    val currentPassword: String,
    val newPassword: String,
    val confirmNewPassword: String
)