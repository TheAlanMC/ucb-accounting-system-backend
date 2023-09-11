package ucb.accounting.backend.dto

import java.math.BigDecimal

data class TransactionDetailDto(
    val transactionDetailId: Long,
    val transactionId: Int,
    val accountId: Int,
    val currencyTypeId: Int,
    val amount: BigDecimal,
    val description: String,
    val status: Boolean,
    val transaction: TransactionDto?,
    val account: AccountDto?,
    val currencyType: CurrencyTypeDto?
)