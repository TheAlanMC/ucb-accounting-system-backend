package ucb.accounting.backend.dto

data class CompanyDto (
    val industry: IndustryDto,
    val businessEntity: BusinessEntityDto,
    val companyName: String,
    val companyNit: String,
    val companyAddress: String,
    val phoneNumber: String,
    val companyLogo: String
)