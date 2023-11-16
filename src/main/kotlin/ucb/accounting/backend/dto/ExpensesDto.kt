package ucb.accounting.backend.dto

import java.math.BigDecimal

data class ExpensesDto (
    val name: String,
    val total: BigDecimal
)