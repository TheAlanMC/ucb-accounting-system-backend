package ucb.accounting.backend.dto

import java.math.BigDecimal
import java.sql.Date

data class SaleTransactionPartialDto (
    val saleTransactionId: Int,
    val saleTransactionNumber: Int,
    val saleTransactionDate: Date,
    val customer: CustomerPartialDto,
    val gloss: String,
    val totalAmountBs: BigDecimal,
    val saleTransactionAccepted: Boolean
)
