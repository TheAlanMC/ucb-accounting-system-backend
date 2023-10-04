package ucb.accounting.backend.dto

import java.math.BigDecimal
import java.sql.Date

data class SaleTransactionDto (
    val saleTransactionId: Int,
    val transactionType: TransactionTypeDto,
    val saleTransactionNumber: Int,
    val saleTransactionDate: Date,
    val customer: CustomerPartialDto,
    val gloss: String,
    val totalAmountBs: BigDecimal,
    val saleTransactionAccepted: Boolean
)
