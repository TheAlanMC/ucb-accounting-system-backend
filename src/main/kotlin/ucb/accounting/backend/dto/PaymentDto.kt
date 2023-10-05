package ucb.accounting.backend.dto

import java.sql.Date

data class PaymentDto (
    val paymentNumber: Int?,
    val reference: String?,
    val clientId: Int?,
    val paymentTypeId: Int?,
    val gloss: String?,
    val description: String?,
    val paymentDate: Date?,
    val attachments: List<AttachmentDto>?,
    val paymentDetail: PaymentDetailDto?
)