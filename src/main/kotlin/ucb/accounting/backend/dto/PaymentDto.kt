package ucb.accounting.backend.dto

import java.sql.Date

data class PaymentDto (
    val paymentNumber: Int?,
    val referenceNumber: String?,
    val clientId: Int?,
    val paymentTypeId: Int?,
    val subaccountId: Int?,
    val gloss: String?,
    val description: String?,
    val expenseTransactionDate: Date?,
    val attachments: List<AttachmentDto>?,
    val expenseTransactionDetails: List<PaymentDetailDto>?
)