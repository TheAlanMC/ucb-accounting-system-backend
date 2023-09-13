package ucb.accounting.backend.dto

data class UserDto (
    val companyIds: List<Long>?,
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val s3ProfilePictureId: Long?,
    val profilePicture: String?
)
