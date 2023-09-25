package ucb.accounting.backend.dto

import java.sql.Date

data class ExpenseTransactionDto (
    val expenseTransactionNumber: Int,
    val supplierId: Long,
    val subaccountId: Long,
    val gloss: String,
    val description: String,
    val expenseTransactionDate: Date,
    val attachments: List<AttachmentDto>? = null,
    val expenseTransactionDetails: List<ExpenseTransactionDetailDto>
)