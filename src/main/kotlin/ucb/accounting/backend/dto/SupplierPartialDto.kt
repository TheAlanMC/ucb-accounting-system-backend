package ucb.accounting.backend.dto

import java.sql.Date

data class SupplierPartialDto (
    val supplierId: Long?,
    val displayName: String?,
    val companyName: String?,
    val companyPhoneNumber: String?,
    val creationDate: Date?,
)
