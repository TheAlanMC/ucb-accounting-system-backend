package ucb.accounting.backend.dto

import java.sql.Date


data class ListUsersDto (
    val kcGroupName: String?,
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val creationDate: Date?
)