package ucb.accounting.backend.dto

import java.math.BigDecimal

data class TransactionDetailDto(
    val subaccountId: Long,
    val debitAmountBs: BigDecimal,
    val creditAmountBs: BigDecimal
)