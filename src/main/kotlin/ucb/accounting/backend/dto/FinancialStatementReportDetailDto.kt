package ucb.accounting.backend.dto

import java.math.BigDecimal

data class FinancialStatementReportDetailDto (
    val accountCategory: AccountCategory,
    val description: String,
    var totalAmountBs: BigDecimal,
)

data class AccountCategory (
    val accountCategoryId: Long,
    val accountCategoryCode: Int,
    val accountCategoryName: String,
    var accountGroups: List<AccountGroup>,
    var totalAmountBs: BigDecimal,
)

data class AccountGroup(
    val accountGroupId: Long,
    val accountGroupCode: Int,
    val accountGroupName: String,
    var accountSubgroups: List<AccountSubgroup>,
    var totalAmountBs: BigDecimal,
)

data class AccountSubgroup(
    val accountSubgroupId: Long,
    val accountSubgroupCode: Int,
    val accountSubgroupName: String,
    val accounts: List<Account>,
    var totalAmountBs: BigDecimal,
)

data class Account(
    val accountId: Long,
    val accountCode: Int,
    val accountName: String,
    val subaccounts: List<Subaccount>,
    var totalAmountBs: BigDecimal,
)

data class Subaccount(
    val subaccountId: Long,
    val subaccountCode: Int,
    val subaccountName: String,
    var totalAmountBs: BigDecimal,
)

