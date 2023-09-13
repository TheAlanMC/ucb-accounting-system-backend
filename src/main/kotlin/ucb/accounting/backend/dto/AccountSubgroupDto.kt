package ucb.accounting.backend.dto

data class AccountSubgroupDto(
    val accountSubgroupId: Long?,
    val accountSubgroupCode: Int?,
    val accountSubgroupName: String?,
    val accounts: List<AccountDto>? // Lista de cuentas
)