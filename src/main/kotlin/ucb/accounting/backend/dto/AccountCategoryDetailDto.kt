package ucb.accounting.backend.dto

data class AccountCategoryDetailDto (
    val accountCategoryId: Long?,
    val accountCategoryCode: Int?,
    val accountCategoryName: String?,
    val accountGroup: AccountGroupDetailDto?
)

data class AccountGroupDetailDto (
    val accountGroupId: Long?,
    val accountGroupCode: Int?,
    val accountGroupName: String?,
    val accountSubgroup: AccountSubgroupDetailDto?
)

data class AccountSubgroupDetailDto (
    val accountSubgroupId: Long?,
    val accountSubgroupCode: Int?,
    val accountSubgroupName: String?,
    val account: AccountDetailDto?
)

data class AccountDetailDto (
    val accountId: Long?,
    val accountCode: Int?,
    val accountName: String?,
    val subaccount: SubaccountDetailDto?
)

data class SubaccountDetailDto (
    val subaccountId: Long?,
    val subaccountCode: Int?,
    val subaccountName: String?
)


