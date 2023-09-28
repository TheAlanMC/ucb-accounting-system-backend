package ucb.accounting.backend.dto

data class AccountGroupPartialDto (
    val accountGroupId: Long?,
    val accountCategoryId: Long?,
    val accountGroupCode: Int?,
    val accountGroupName: String?
)