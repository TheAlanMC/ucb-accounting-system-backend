package ucb.accounting.backend.dto

import java.math.BigDecimal

data class FinancialStatementReportDetailDto (
    val accountCategory: AccountCategory,
    val description: String,
    val totalAmountBs: BigDecimal,
)

data class AccountCategory (
    val accountCategoryId: Long,
    val accountCategoryCode: Int,
    val accountCategoryName: String,
    val accountGroups: List<AccountGroup>,
    val totalAmountBs: BigDecimal,
)

data class AccountGroup(
    val accountGroupId: Long,
    val accountGroupCode: Int,
    val accountGroupName: String,
    val accountSubgroups: List<AccountSubgroup>,
    val totalAmountBs: BigDecimal,
)

data class AccountSubgroup(
    val accountSubgroupId: Long,
    val accountSubgroupCode: Int,
    val accountSubgroupName: String,
    val accounts: List<Account>,
    val totalAmountBs: BigDecimal,
)

data class Account(
    val accountId: Long,
    val accountCode: Int,
    val accountName: String,
    val subaccounts: List<Subaccount>,
    val totalAmountBs: BigDecimal,
)

data class Subaccount(
    val subaccountId: Long,
    val subaccountCode: Int,
    val subaccountName: String,
    val totalAmountBs: BigDecimal,
)
