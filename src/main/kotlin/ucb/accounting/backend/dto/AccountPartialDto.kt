package ucb.accounting.backend.dto

data class AccountPartialDto (
    val accountId: Long?,
    val accountSubgroupId: Long?,
    val accountCode: Int?,
    val accountName: String?,
)