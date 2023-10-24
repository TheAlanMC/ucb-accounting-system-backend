package ucb.accounting.backend.dto

import java.math.BigDecimal

data class JournalBookTransactionDetailDto(
    val subaccount: SubaccountDto?,
    val debitAmountBs: BigDecimal,
    val creditAmountBs: BigDecimal
)