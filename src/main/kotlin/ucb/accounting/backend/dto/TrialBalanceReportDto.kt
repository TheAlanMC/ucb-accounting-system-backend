package ucb.accounting.backend.dto

import java.math.BigDecimal

data class TrialBalanceReportDto (
    val trialBalanceDetails: List<TrialBalanceReportDetailDto>,
    val totalDebitAmount: BigDecimal,
    val totalCreditAmount: BigDecimal,
    val totalBalanceDebtor: BigDecimal,
    val totalBalanceCreditor: BigDecimal,
)

