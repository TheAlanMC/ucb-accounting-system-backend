package ucb.accounting.backend.dto

import java.math.BigDecimal

data class InvoiceDetailDto (
    val subaccountId: Long,
    val quantity: Int,
    val unitPriceBs: BigDecimal
)