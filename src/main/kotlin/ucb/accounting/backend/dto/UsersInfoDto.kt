package ucb.accounting.backend.dto

data class UsersInfoDto (
        val companyId: Long,
        val firstName: String,
        val lastName: String,
        val email: String,
        val profilePicture: String
)