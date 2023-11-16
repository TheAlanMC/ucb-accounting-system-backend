package ucb.accounting.backend.dto

import java.math.BigDecimal
import java.sql.Date

data class ExpenseTransactionDto (
    val expenseTransactionId: Int,
    val transactionType: TransactionTypeDto,
    val expenseTransactionNumber: Int,
    val expenseTransactionDate: Date,
    val supplier: SupplierPartialDto,
    val gloss: String,
    val totalAmountBs: BigDecimal,
    val expenseTransactionAccepted: Boolean
)
