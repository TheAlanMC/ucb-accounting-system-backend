package ucb.accounting.backend.dto

import java.math.BigDecimal

data class TransactionDetailPartialDto(
    val subaccount: SubaccountDto?,
    val debitAmountBs: BigDecimal,
    val creditAmountBs: BigDecimal
)