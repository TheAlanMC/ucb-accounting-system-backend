package ucb.accounting.backend.dto

import java.sql.Date

data class SaleTransactionPartialDto (
    val saleTransactionId: Int,
    val saleTransactionNumber: Int,
    val saleTransactionDate: Date,
    val customerPartial: CustomerPartialDto,
    val gloss: String,
    val saleTransactionAccepted: Boolean
)
