package ucb.accounting.backend.dto

import java.math.BigDecimal
import java.util.*

data class GeneralLedgerTransactionDetailDto (
    val transactionDate: Date,
    val gloss: String,
    val description: String,
    val debitAmount: BigDecimal,
    val creditAmount: BigDecimal,
    val balanceAmount: BigDecimal
)
