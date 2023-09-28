package ucb.accounting.backend.dto

data class CompanyPartialDto (
    val industryId: Long?,
    val businessEntityId: Long?,
    val companyName: String?,
    val companyNit: String?,
    val companyAddress: String?,
    val phoneNumber: String?,
    val s3CompanyLogoId: Long?
)