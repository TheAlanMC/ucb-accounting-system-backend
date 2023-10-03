package ucb.accounting.backend.dto

data class PartnerDto (
    val customers: List<CustomerPartialDto>,
    val suppliers: List<SupplierPartialDto>
)