package ucb.accounting.backend.dto

import java.math.BigDecimal
import java.sql.Date

data class ExpenseTransactionPartialDto (
    val expenseTransactionId: Int,
    val expenseTransactionNumber: Int,
    val expenseTransactionDate: Date,
    val supplier: SupplierPartialDto,
    val gloss: String,
    val totalAmountBs: BigDecimal,
    val expenseTransactionAccepted: Boolean
)
