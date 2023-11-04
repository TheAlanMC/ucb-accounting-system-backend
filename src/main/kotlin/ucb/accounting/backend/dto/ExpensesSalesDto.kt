package ucb.accounting.backend.dto

import java.math.BigDecimal

data class ExpensesSalesDto (
    val year: Int,
    val month: Int,
    val expenses: Int,
    val sales: Int
)