package ucb.accounting.backend.dto

data class AccountCategoryDto(
    val accountCategoryId: Long?,
    val accountCategoryCode: Int?,
    val accountCategoryName: String?,
    val accountGroups: List<AccountGroupDto>? // Lista de grupos de cuenta
)