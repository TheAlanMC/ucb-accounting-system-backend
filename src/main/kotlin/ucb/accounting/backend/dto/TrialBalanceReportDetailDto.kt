package ucb.accounting.backend.dto

import java.math.BigDecimal

data class TrialBalanceReportDetailDto (
    val subaccount: SubaccountDto,
    val debitAmount: BigDecimal,
    val creditAmount: BigDecimal,
    val balanceDebtor: BigDecimal,
    val balanceCreditor: BigDecimal,
)