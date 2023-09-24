package ucb.accounting.backend.dto

data class SupplierDto (
    val supplierId: Long?,
    val subaccountId: Long?,
    val prefix: String?,
    val displayName: String?,
    val firstName: String?,
    val lastName: String?,
    val companyName: String?,
    val companyEmail: String?,
    val companyPhoneNumber: String?,
    val companyAddress: String?
)
