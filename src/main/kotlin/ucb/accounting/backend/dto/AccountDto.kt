package ucb.accounting.backend.dto

data class AccountDto(
    val accountId: Long?,
    val accountCode: Int?,
    val accountName: String?,
    val subaccounts: List<SubAccountDto>? // Lista de subcuentas
)