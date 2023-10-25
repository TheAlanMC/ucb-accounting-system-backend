package ucb.accounting.backend.dto

import java.math.BigDecimal

data class GeneralLedgerReportDto (
    val subaccount: SubaccountDto,
    val transactionDetails: List<GeneralLedgerTransactionDetailDto>,
    val totalDebitAmount: BigDecimal,
    val totalCreditAmount: BigDecimal,
    val totalBalanceAmount: BigDecimal
)
