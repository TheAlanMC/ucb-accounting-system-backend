package ucb.accounting.backend.dto

data class SubaccountPartialDto (
    val subaccountId: Long?,
    val accountId: Long?,
    val subaccountCode: Int?,
    val subaccountName: String?
)