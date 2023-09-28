package ucb.accounting.backend.dto

data class NewUserDto (
    val email: String?,
    val firstName: String?,
    val lastName: String?,
    val password: String?,
    val confirmPassword: String?
)