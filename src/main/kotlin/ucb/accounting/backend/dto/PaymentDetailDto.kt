package ucb.accounting.backend.dto

import java.math.BigDecimal

data class PaymentDetailDto (
    val subaccountId: Long,
    val amountBs: BigDecimal
)