package ucb.accounting.backend.dto

import java.math.BigDecimal

data class ExpenseTransactionDetailDto (
    val subaccountId: Long,
    val amountBs: BigDecimal
)