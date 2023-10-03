package ucb.accounting.backend.dto

import java.sql.Date

data class CustomerPartialDto (
    val customerId: Long?,
    val displayName: String?,
    val companyName: String?,
    val companyPhoneNumber: String?,
    val creationDate: Date?,
)
