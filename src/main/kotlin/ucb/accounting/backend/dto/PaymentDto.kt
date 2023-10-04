package ucb.accounting.backend.dto

import java.sql.Date

data class PaymentDto (
    val expenseTransactionNumber: Int?,
    val clientId: Int?,
    val transactionTypeId: Int?,
    val paymentTypeId: Int?,
    val subaccountId: Int?,
    val gloss: String?,
    val description: String?,
    val expenseTransactionDate: Date?,
    val attachments: List<AttachmentDto>?,
    val expenseTransactionDetails: List<PaymentDetailDto>?
)