package ucb.accounting.backend.dto

data class UserDto (
    val companyId: String?,
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val profilePicture: String?
)