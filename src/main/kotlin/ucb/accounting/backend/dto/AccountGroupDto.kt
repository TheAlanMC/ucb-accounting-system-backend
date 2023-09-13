package ucb.accounting.backend.dto

data class AccountGroupDto(
    val accountGroupId: Long?,
    val accountGroupCode: Int?,
    val accountGroupName: String?,
    val accountSubgroups: List<AccountSubgroupDto>? // Lista de subgrupos de cuenta
)