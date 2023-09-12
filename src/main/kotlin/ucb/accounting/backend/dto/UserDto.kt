package ucb.accounting.backend.dto

data class UserDto (
    val companyId: Long?,
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val profilePicture: String?
)

