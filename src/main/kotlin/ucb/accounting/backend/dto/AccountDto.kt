package ucb.accounting.backend.dto

data class AccountDto(
    val accountId: Long?,
    val accountCode: Int?,
    val accountName: String?,
    val subaccounts: List<SubaccountDto>? // Lista de subcuentas
)
