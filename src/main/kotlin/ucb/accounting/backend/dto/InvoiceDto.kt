package ucb.accounting.backend.dto

import java.sql.Date

data class InvoiceDto (
    val invoiceNumber: Int?,
    val reference: String?,
    val clientId: Int?,
    val paymentTypeId: Int?,
    val gloss: String?,
    val description: String?,
    val invoiceDate: Date?,
    val attachments: List<AttachmentDto>?,
    val invoiceDetails: List<InvoiceDetailDto>?,
    val taxTypeName: String?,
)