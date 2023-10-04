package ucb.accounting.backend.dto

data class CustomerDto (
    val customerId: Long?,
    val prefix: String?,
    val displayName: String?,
    val firstName: String?,
    val lastName: String?,
    val companyName: String?,
    val companyEmail: String?,
    val companyPhoneNumber: String?,
    val companyAddress: String?
)
