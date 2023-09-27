package ucb.accounting.backend.dto

data class KcUsersDto (
    val email: String,
    val firstName: String,
    val lastName: String,
    val password: String?,
    val confirmPassword: String?
)