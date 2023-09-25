package ucb.accounting.backend.dto

import java.sql.Date

data class SaleTransactionDto (
    val saleTransactionNumber: Int,
    val customerId: Long,
    val subaccountId: Long,
    val gloss: String,
    val description: String,
    val saleTransactionDate: Date,
    val attachments: List<AttachmentDto>? = null,
    val saleTransactionDetails: List<SaleTransactionDetailDto>
)